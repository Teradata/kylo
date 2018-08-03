import * as angular from 'angular';
import * as _ from "underscore";
import { Common } from "../../../../common/CommonTypes";
import LabelValue = Common.LabelValue;
import { RegisterTemplateServiceFactory } from '../../../services/RegisterTemplateServiceFactory';
import StateService from '../../../../services/StateService';
import { EntityAccessControlService } from '../../../shared/entity-access-control/EntityAccessControlService';
import { Component, Input, Inject } from '@angular/core';
import { RestUrlService } from '../../../services/RestUrlService';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import { RegisterTemplateInprogressDialog } from './register-template-inprogress.component';
import { RegisterTemplateErrorDialog } from './register-template-error.component';
import {IconPickerDialog} from '../../../../common/icon-picker-dialog/icon-picker-dialog.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { FormGroup, FormControl, Validators, ValidatorFn, AbstractControl } from '@angular/forms';

export function invalidConnection(connectionMap: any, connection: any): ValidatorFn{

    return (control: AbstractControl): {[key: string]: any} | null => {
        if (!angular.isDefined(connectionMap[connection.inputPortDisplayName])) {
            connection.inputPortDisplayName = null;
            //mark as invalid
            return {invalidConnection: true};
        }
        else {
            return null;
        }
    };
}

@Component({
    selector: 'thinkbig-register-complete-registration',
    templateUrl: 'js/feed-mgr/templates/template-stepper/register-template/register-template-step.html'
})
export class RegisterCompleteRegistrationController {


    /**
     * Order of the templates via the dndLists
     * @type {Array}
     */
    templateOrder: any[] = [];
    /**
     * the angular form
     */
    registerTemplateForm: any = {};
    /**
     * Indicates if the edit form is valid.
     * @type {boolean}
     */
    isValid: boolean = false;

    /**
     * The Source/Destination Datasources assigned to this template
     * @type {Array}
     */
    processorDatasourceDefinitions: any[] = [];
    /**
     * flag to tell when the system is loading datasources
     * @type {boolean}
     */
    loadingFlowData: boolean = true;
    /**
     * The Template model
     */
    model: any;
    /**
     * Global message to present to the user
     */
    message: string = null;
    /**
     * Flag indicating if the template succeeded registration
     * @type {boolean}
     */
    registrationSuccess: boolean = false;

    /*
    Passed in step index
     */
    @Input() stepIndex: string;
    /**
     * The Step number  (1 plus the index)
     * @type {number}
     */
    stepNumber: number;

    /**
     * A map of the port names to the port Object
     * used for the connections from the outputs to input ports
     * @type {{}}
     */
    connectionMap: any = {};
    /**
     * The available options in the list of possible inputPorts to connect to
     * @type {Array} of {label: port.name, value: port.name}
     */
    inputPortList: LabelValue[] = [];

    remoteProcessGroupValidation: any = { validatingPorts: true, valid: false, invalidMessage: null };

    @Input() formGroup: FormGroup;

    loaded: boolean = false;

    constructor(private RestUrlService: RestUrlService,
                private stateService: StateService, 
                private registerTemplateService: RegisterTemplateServiceFactory, 
                private entityAccessControlService: EntityAccessControlService,
                private dialog: MatDialog,
                private snackBar: MatSnackBar,
                private http: HttpClient) {}

    ngOnInit() {

        /**
         * The Template Model
         */
        this.model = this.registerTemplateService.model;

        //set the step number
        this.stepNumber = parseInt(this.stepIndex) + 1;
        /**
         * Initialize the connections and flow data
         */
        this.initTemplateFlowData();
        /**
         * if this template has a RemotePRocessGroup then validate to make sure its connection to the remote input port exists
         */
        this.validateRemoteInputPort();

        this.registerTemplateService.modelNifiTemplateIdObserver.subscribe((nifiTemplateId)=>{
            if (nifiTemplateId != null) {
                this.registrationSuccess = false;
            }
        });
        // this.$scope.$watch(() => {
        //     return this.model.nifiTemplateId;
        // }, (newVal: any) => {
        //     if (newVal != null) {
        //         this.registrationSuccess = false;
        //     }
        // });
    }

    /**
     * Calls the server to get all the Datasources and the Flow processors and flow types
     * Caled initially when the page loads and then any time a user changes a input port connection
     */
    private buildTemplateFlowData(): void {
        this.loadingFlowData = true;
        var assignedPortIds: any = [];

        _.each(this.model.reusableTemplateConnections, (conn: any) => {
            var inputPort = conn.inputPortDisplayName;
            var port = this.connectionMap[inputPort];
            if (port != undefined) {
                assignedPortIds.push(port.id);
            }
        });
        var selectedPortIds = '';
        if (assignedPortIds.length > 0) {
            selectedPortIds = assignedPortIds.join(",");
        }
        //only attempt to query if we have connections set
        var hasPortConnections = (this.model.reusableTemplateConnections == null || this.model.reusableTemplateConnections.length == 0) || (this.model.reusableTemplateConnections != null && this.model.reusableTemplateConnections.length > 0 && assignedPortIds.length == this.model.reusableTemplateConnections.length);
        if (hasPortConnections) {
            this.registerTemplateService.getNiFiTemplateFlowInformation(this.model.nifiTemplateId, this.model.reusableTemplateConnections).then((response: angular.IHttpResponse<any>) => {
                var map = {};

                if (response && response.data) {

                    var datasourceDefinitions = response.data.templateProcessorDatasourceDefinitions;

                    //merge in those already selected/saved on this template
                    _.each(datasourceDefinitions, (def: any) => {
                        def.selectedDatasource = false;
                        if (this.model.registeredDatasourceDefinitions.length == 0) {
                            def.selectedDatasource = true;
                        }
                        else {

                            var matchingTypes = _.filter(this.model.registeredDatasourceDefinitions, function (ds: any) {
                                return (def.processorType == ds.processorType && (ds.processorId == def.processorId || ds.processorName == def.processorName));
                            });
                            if (matchingTypes.length > 0) {
                                def.selectedDatasource = true;
                            }
                        }
                    });
                    //sort with SOURCE's first
                    this.processorDatasourceDefinitions = _.sortBy(datasourceDefinitions, function (def: any) {
                        if (def.datasourceDefinition.connectionType == 'SOURCE') {
                            return 1;
                        }
                        else {
                            return 2;
                        }
                    });

                }
                this.loadingFlowData = false;
            });
        }
        else {
            this.loadingFlowData = false;
        }

    };

    /**
     * if this template has a RemotePRocessGroup then validate to make sure its connection to the remote input port exists
     */
    private validateRemoteInputPort(): void {
        this.remoteProcessGroupValidation.validatingPorts = true;
        this.remoteProcessGroupValidation.invalidMessage = "";
        if (this.model.additionalProperties) {
            let remoteInputPortProperty = _.find(this.model.additionalProperties, function (prop: any) {
                return prop.processorType = "REMOTE_PROCESS_GROUP" && prop.key == "Remote Input Port";
            });
            if (remoteInputPortProperty != undefined) {
                //validate it against the possible remote input ports
                this.registerTemplateService.fetchRootInputPorts().then((response: angular.IHttpResponse<any>) => {
                    if (response && response.data) {
                        let matchingPort = _.find(response.data, function (port: any) {
                            return port.name == remoteInputPortProperty.value;
                        });
                        if (matchingPort != undefined) {
                            //VALID
                            this.remoteProcessGroupValidation.valid = true;
                        }
                        else {
                            //INVALID
                            this.remoteProcessGroupValidation.valid = false;
                            this.remoteProcessGroupValidation.invalidMessage = "The Remote Input Port defined for this Remote Process Group, <b>" + remoteInputPortProperty.value + "</b> does not exist.<br/>" +
                                "You need to register a reusable template with this '<b>" + remoteInputPortProperty.value + "</b>' input port and check the box 'Remote Process Group Aware' when registering to make it available to this template. ";
                        }
                        this.remoteProcessGroupValidation.validatingPorts = false;
                    }
                }, (err: any) => {
                    this.remoteProcessGroupValidation.validatingPorts = false;
                    this.remoteProcessGroupValidation.valid = false;
                    this.remoteProcessGroupValidation.invalidMessage = "Unable to verify input port connections for your remote process group";

                });
            }
            else {
                this.remoteProcessGroupValidation.validatingPorts = false;
                this.remoteProcessGroupValidation.valid = true;
            }
        }
        else {
            this.remoteProcessGroupValidation.validatingPorts = false;
            this.remoteProcessGroupValidation.valid = true;
        }
    }

    /**
     * Fetches the output/input ports and walks the flow to build the processor graph for the Data sources and the flow types
     */
    private initTemplateFlowData(): void {
        if (this.model.needsReusableTemplate) {
            this.registerTemplateService.fetchRegisteredReusableFeedInputPorts().then((response: any) => {
                // Update connectionMap and inputPortList
                this.inputPortList = [];
                if (response.data) {
                    angular.forEach(response.data, (port, i) => {
                        this.inputPortList.push({ label: port.name, value: port.name, description: port.destinationProcessGroupName });
                        this.connectionMap[port.name] = port;
                    });
                }

                // Check for invalid and required connections
                angular.forEach(this.model.reusableTemplateConnections, (connection) => {
                    //initially mark as valid
                    this.formGroup.addControl("port-" + connection.feedOutputPortName,
                                new FormControl(null,[Validators.required, invalidConnection(this.connectionMap, connection)]));
                    
                });
                this.loaded = true;

                this.buildTemplateFlowData();
            });
        }
        else {
            this.buildTemplateFlowData();
        }
    }

    

    /**
     * Called when the user changes the output port connections
     * @param connection
     */
    onReusableTemplateConnectionChange(connection: any) {
        var port = this.connectionMap[connection.inputPortDisplayName];
        connection.reusableTemplateInputPortName = port.name;
        //mark as valid
        // this.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", true);
        this.buildTemplateFlowData();
    };

    /**
     * Called when the user clicks to change the icon
     */
    showIconPicker(): void {
        var iconModel: any = { icon: this.model.icon.title, iconColor: this.model.icon.color };
        iconModel.name = this.model.templateName;

        let dialogRef = this.dialog.open(IconPickerDialog, {
            data: { iconModel: iconModel },
            panelClass: "full-screen-dialog"
          });
      
        dialogRef.afterClosed().subscribe(msg => {
            if (msg) {
                this.model.icon.title = msg.icon;
                this.model.icon.color = msg.color;
            }
        });
    };

    /**
     * Called when the user clicks Register
     * @returns {*}
     */
    registerTemplate() {

        this.showRegistrationInProgressDialog();

        let successFn = (response: any) => {
            this.dialog.closeAll();
            var message = 'Template Registered with ' + response.properties.length + ' properties';
            this.registrationSuccess = true;

            this.snackBar.open(message, "OK", {duration: 3000});
            this.stateService.FeedManager().Template().navigateToRegisterTemplateComplete(message, this.model, null);
        }
        let errorFn = (response: any) => {
            this.dialog.closeAll();
            var message = 'Error Registering Template ' + response.message;
            this.registrationSuccess = false;
            this.snackBar.open(message, "OK", {duration: 3000});
            this.showErrorDialog(message);
        }

        //get all properties that are selected
        var savedTemplate = this.registerTemplateService.getModelForSave();

        //prepare access control changes if any
        this.entityAccessControlService.updateRoleMembershipsForSave(savedTemplate.roleMemberships);

        //get template order
        var order: any = [];
        _.each(this.templateOrder, function (template: any) {
            order.push(template.id);
        });
        savedTemplate.templateOrder = order;

        var thisOrder : number = order.length - 1;
        if (this.model.id != undefined) {
            thisOrder = _.indexOf(order, this.model.id)
        }
        else {
            thisOrder = _.indexOf(order, 'NEW');
        }
        savedTemplate.order = thisOrder

        //add in the datasources
        var selectedDatasourceDefinitions = _.filter(this.processorDatasourceDefinitions, function (ds: any) {
            return ds.selectedDatasource == true;
        })

        savedTemplate.registeredDatasourceDefinitions = selectedDatasourceDefinitions;

        var promise = this.http.post(this.RestUrlService.REGISTER_TEMPLATE_URL(),
            angular.toJson(savedTemplate),
            {headers :new HttpHeaders({'Content-Type':'application/json; charset=utf-8'}) })
            .toPromise().then(successFn, errorFn);

        return promise;
    }


    /**
     * Shows a dialog with a progress when the registration is in progress
     */
    showRegistrationInProgressDialog() {
        //hide any dialogs  
        let dialogRef = this.dialog.open(RegisterTemplateInprogressDialog, {
            data: { templateName: this.model.templateName },
            panelClass: "full-screen-dialog"
          });
    }

    /**
     * Shows an Error dialog with a message
     * @param message
     */
    showErrorDialog(message: any) {

        let dialogRef = this.dialog.open(RegisterTemplateErrorDialog, {
            data: { templateName: this.model.templateName,
                    nifiTemplateId: this.model.nifiTemplateId,
                    message: message },
            panelClass: "full-screen-dialog"
          });
    }

}


@Component({
    selector: 'register-template-complete-controller',
    templateUrl: 'js/feed-mgr/templates/template-stepper/register-template/register-template-complete.html'
})
export class RegisterTemplateCompleteController {

    constructor(private StateService: StateService) {}

    gotIt() {
        this.StateService.FeedManager().Template().navigateToRegisteredTemplates();
    }
}
