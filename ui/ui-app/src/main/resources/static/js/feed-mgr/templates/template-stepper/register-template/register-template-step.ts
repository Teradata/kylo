import * as angular from 'angular';
import * as _ from "underscore";
import { moduleName } from "../../module-name";
import LabelValue = Common.LabelValue;
import { RegisterTemplateServiceFactory } from '../../../services/RegisterTemplateServiceFactory';
import {StateService} from '../../../../services/StateService';
import { EntityAccessControlService } from '../../../shared/entity-access-control/EntityAccessControlService';
import '../../module-require';
import {Common} from '../../../../../lib/common/CommonTypes';


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
    stepIndex: string;
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


    static $inject = ["$scope", "$http", "$mdToast", "$mdDialog", "RestUrlService", "StateService", "RegisterTemplateService", "EntityAccessControlService"];

    constructor(private $scope: IScope, private $http: angular.IHttpService, private $mdToast: angular.material.IToastService, private $mdDialog: angular.material.IDialogService, private RestUrlService: any
        , private stateService: StateService, private registerTemplateService: RegisterTemplateServiceFactory, private entityAccessControlService: EntityAccessControlService) {


        /**
         * The Template Model
         */
        this.model = this.registerTemplateService.model;

        //set the step number
        this.stepNumber = parseInt(this.stepIndex) + 1
    }

    ngOnInit() {
        /**
         * Initialize the connections and flow data
         */
        this.initTemplateFlowData();
        /**
         * if this template has a RemotePRocessGroup then validate to make sure its connection to the remote input port exists
         */
        this.validateRemoteInputPort();

        this.$scope.$watch(() => {
            return this.model.nifiTemplateId;
        }, (newVal: any) => {
            if (newVal != null) {
                this.registrationSuccess = false;
            }
        });
    }

    $onInit() {
        this.ngOnInit();
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

                // Check for invalid connections
                angular.forEach(this.model.reusableTemplateConnections, (connection) => {
                    //initially mark as valid
                    this.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", true);
                    if (!angular.isDefined(this.connectionMap[connection.inputPortDisplayName])) {
                        connection.inputPortDisplayName = null;
                        //mark as invalid
                        this.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", false);
                    }
                });

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
        this.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", true);
        this.buildTemplateFlowData();
    };

    /**
     * Called when the user clicks to change the icon
     */
    showIconPicker(): void {
        var iconModel: any = { icon: this.model.icon.title, iconColor: this.model.icon.color };
        iconModel.name = this.model.templateName;

        this.$mdDialog.show({
            controller: 'IconPickerDialog',
            templateUrl: '../../../../common/icon-picker-dialog/icon-picker-dialog.html',
            parent: angular.element(document.body),
            clickOutsideToClose: false,
            fullscreen: true,
            locals: {
                iconModel: iconModel
            }
        }).then((msg: any) => {
            if (msg) {
                this.model.icon.title = msg.icon;
                this.model.icon.color = msg.color;
            }

        }, function () {

        });
    };

    /**
     * Called when the user clicks Register
     * @returns {*}
     */
    registerTemplate() {

        console.log('register', this.registerTemplateService.model.changeComment);
        this.showRegistrationInProgressDialog();
        let successFn = (response: any) => {
            this.$mdDialog.hide();
            var message = 'Template Registered with ' + response.data.properties.length + ' properties';
            this.registrationSuccess = true;

            this.$mdToast.show(
                this.$mdToast.simple()
                    .textContent(message)
                    .hideDelay(3000)
            );
            this.stateService.FeedManager().Template().navigateToRegisterTemplateComplete(message, this.model, null);
        }
        let errorFn = (response: any) => {
            this.$mdDialog.hide();
            var message = 'Error Registering Template ' + response.data.message;
            this.registrationSuccess = false;
            this.$mdToast.show(
                this.$mdToast.simple()
                    .textContent(message)
                    .hideDelay(3000)
            );
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

        var promise = this.$http({
            url: this.RestUrlService.REGISTER_TEMPLATE_URL(),
            method: "POST",
            data: angular.toJson(savedTemplate),
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }
        }).then(successFn, errorFn);
        return promise;
    }


    /**
     * Shows a dialog with a progress when the registration is in progress
     */
    showRegistrationInProgressDialog() {
        //hide any dialogs
        this.$mdDialog.hide();
        this.$mdDialog.show({
            controller: ["$scope", "templateName", ($scope, templateName) => {
                $scope.templateName = templateName;
            }],
            templateUrl: './register-template-inprogress-dialog.html',
            parent: angular.element(document.body),
            clickOutsideToClose: false,
            fullscreen: true,
            locals: {
                templateName: this.model.templateName
            }
        })

    }

    /**
     * Shows an Error dialog with a message
     * @param message
     */
    showErrorDialog(message: any) {

        this.$mdDialog.show({
            controller: ["$scope", "$mdDialog", "nifiTemplateId", "templateName", "message",
                ($scope: any, $mdDialog: any, nifiTemplateId: any, templateName: any, message: any) => {
                    $scope.nifiTemplateId = nifiTemplateId;
                    $scope.templateName = templateName;
                    $scope.message = message;

                    $scope.gotIt = function () {
                        $mdDialog.cancel();
                    };
                }],
            templateUrl: './register-template-error-dialog.html',
            parent: angular.element(document.body),
            clickOutsideToClose: true,
            fullscreen: true,
            locals: {
                nifiTemplateId: this.model.nifiTemplateId,
                templateName: this.model.templateName,
                message: message
            }
        });
    };


}

angular.module(moduleName).component("thinkbigRegisterCompleteRegistration", {
    bindings: {
        stepIndex: '@'
    },
    controllerAs: 'vm',
    templateUrl: './register-template-step.html',
    controller: RegisterCompleteRegistrationController,

});

export class RegisterTemplateCompleteController {

    static readonly $inject = ["StateService"];
    constructor(private StateService: any) {

    }
    gotIt() {
        this.StateService.FeedManager().Template().navigateToRegisteredTemplates();
    }
}

const module = angular.module(moduleName).component("registerTemplateCompleteController", {
    templateUrl: './register-template-complete.html',
    controller: RegisterTemplateCompleteController,
    controllerAs: 'vm'
});
export default module;
