import * as angular from "angular";
import * as _ from "underscore";
import OpsManagerRestUrlService from "../services/OpsManagerRestUrlService";
import AccessControlService from "../../services/AccessControlService";
import AccessConstants from '../../constants/AccessConstants';
import { StateService} from "@uirouter/core";
import { Component, Inject, Input } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { ObjectUtils } from "../../common/utils/object-utils";

   /** Manages the Alert Details page.
     * @constructor
     * @param AccessControlService the access control service
     * @param OpsManagerRestUrlService the REST URL service
     */

@Component({
    selector: 'tba-alert-details',
    templateUrl: 'js/ops-mgr/alerts/alert-details-template.html'
})
export class AlertDetailsTemplateComponent {

    allowAdmin: boolean = false; //Indicates that admin operations are allowed. {boolean}
    alertData: any; //The alert details. {Object}
    @Input() alertId: any;
    @Input() cardTitle: string;

    loading = true;

    ngOnInit() {
        this.loadAlert(this.alertId); // Fetch alert details
        this.accessControlService.getUserAllowedActions() // Fetch allowed permissions
                    .then((actionSet: any) =>{
                        this.allowAdmin = this.accessControlService.hasAction(AccessConstants.OPERATIONS_ADMIN, actionSet.actions);
                    });
    }
    constructor(private http: HttpClient,
                private dialog: MatDialog,
                private accessControlService: AccessControlService,
                private OpsManagerRestUrlService: OpsManagerRestUrlService){
    }// end of constructor
         /**
         * Gets the class for the specified state.
         * @param {string} state the name of the state
         * @returns {string} class name
         */
        getStateClass(state: string) {
            switch (state) {
                case "UNHANDLED":
                    return "error";
                case "IN_PROGRESS":
                    return "warn";
                case "HANDLED":
                    return "success";
                default:
                    return "unknown";
            }
        };

        /**
         * Gets the icon for the specified state.
         * @param {string} state the name of the state
         * @returns {string} icon name
         */
        getStateIcon(state: string) {
            switch (state) {
                case "CREATED":
                case "UNHANDLED":
                    return "error_outline";

                case "IN_PROGRESS":
                    return "schedule";

                case "HANDLED":
                    return "check_circle";

                default:
                    return "help_outline";
            }
        };

        /**
         * Gets the display text for the specified state.
         * @param {string} state the name of the state
         * @returns {string} display text
         */
        getStateText(state: string) {
            if (state === "IN_PROGRESS") {
                return "IN PROGRESS";
            } else {
                return state;
            }
        };

        //Hides this alert on the list page.
        hideAlert() {
            this.alertData.cleared = true;
            this.http.post(this.OpsManagerRestUrlService.ALERT_DETAILS_URL(this.alertData.id), {state: this.alertData.state, clear: true}).toPromise();
        };

        //Shows the alert removing the 'cleared' flag
        showAlert() {
            this.alertData.cleared = false;
            this.http.post(this.OpsManagerRestUrlService.ALERT_DETAILS_URL(this.alertData.id), {state: this.alertData.state, clear: false, unclear:true}).toPromise();
        };


        /** Loads the data for the specified alert.
         * @param {string} alertId the id of the alert
         */
        loadAlert(alertId: string) {
            if(alertId) {
                this.http.get(this.OpsManagerRestUrlService.ALERT_DETAILS_URL(alertId))
                    .toPromise().then( (response: any)=> {
                        this.alertData = response;
                        this.loading = false;

                // Set time since created
                if (ObjectUtils.isNumber(this.alertData.createdTime)) {
                    this.alertData.createdTimeSince = Date.now() - this.alertData.createdTime;
                }

                // Set state information
                if (ObjectUtils.isString(this.alertData.state)) {
                    this.alertData.stateClass = this.getStateClass(this.alertData.state);
                    this.alertData.stateIcon = this.getStateIcon(this.alertData.state);
                    this.alertData.stateText = this.getStateText(this.alertData.state);
                }
                var isStream = false;

                if (Array.isArray(this.alertData.events)) {
                    this.alertData.events.forEach((event: any)=> {
                        event.stateClass = this.getStateClass(event.state);
                        event.stateIcon = this.getStateIcon(event.state);
                        event.stateText = this.getStateText(event.state);
                        event.contentSummary = null;
                        if(ObjectUtils.isDefined(event.content)){
                            try {
                                var alertEventContent = ObjectUtils.fromJson(event.content);
                                if(alertEventContent && alertEventContent.content){
                                    event.contentSummary = ObjectUtils.isDefined(alertEventContent.content.failedCount) ? alertEventContent.content.failedCount +" failures"  : null;
                                    if(!isStream && ObjectUtils.isDefined(alertEventContent.content.stream)){
                                        isStream = alertEventContent.content.stream;
                                    }
                                }
                            }catch(err){

                            }
                        }
                    });
                }
                this.alertData.links = [];
                //add in the detail URLs
                if(this.alertData.type == 'http://kylo.io/alert/job/failure') {
                    if(ObjectUtils.isDefined(this.alertData.content) && !isStream) {
                        var jobExecutionId = this.alertData.content;
                        this.alertData.links.push({label: "Job Execution", value: "job-details({executionId:'" + jobExecutionId + "'})"});
                    }
                    this.alertData.links.push({label:"Feed Details",  value:"ops-feed-details({feedName:'"+this.alertData.entityId+"'})"});

                }
                else if(this.alertData.type == 'http://kylo.io/alert/alert/sla/violation') {
                    if(ObjectUtils.isDefined(this.alertData.content)) {
                        this.alertData.links.push({label: "Service Level Assessment", value: "service-level-assessment({assessmentId:'" + this.alertData.content + "'})"});
                    }
                    this.alertData.links.push({label:"Service Level Agreement",  value:"service-level-agreements({slaId:'"+this.alertData.entityId+"'})"});
                }

                else if(this.alertData.type == 'http://kylo.io/alert/service') {
                    this.alertData.links.push({label:"Service Details",  value:"service-details({serviceName:'"+this.alertData.subtype+"'})"});
                }

            });
        }
    };

    /**
     * Shows a dialog for adding a new event.
     * @param $event the event that triggered this dialog
     */
    showEventDialog ($event: any) {

        let dialogRef = this.dialog.open(EventDialogComponent, {
            data: { alert: this.alertData },
            panelClass: "full-screen-dialog"
            });
        
        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.loadAlert(this.alertData.id);
            }
        });

    };
    
}
 /**
     * Manages the Update Alert dialog.
     * @constructor
     * @param $scope the Angular scope
     * @param $http 
     * @param $mdDialog the dialog service
     * @param OpsManagerRestUrlService the REST URL service
     * @param alert the alert to update
     */

@Component({
    selector: 'events-dialog',
    templateUrl: 'js/ops-mgr/alerts/event-dialog.html'
})
export class EventDialogComponent {
    saving: boolean;
    state: string;
    alert: any;
    @Input() description: string;

    constructor(private http: HttpClient,  //the HTTP service
                private OpsManagerRestUrlService: OpsManagerRestUrlService, //the REST URL service
                private dialogRef: MatDialogRef<EventDialogComponent>,
                @Inject(MAT_DIALOG_DATA) private data: any){}

    ngOnInit(){
        this.alert = this.data.alert;
        this.saving = false; //Indicates that this update is currently being saved  {boolean}
        this.state = (this.alert.state === "HANDLED") ? "HANDLED" : "IN_PROGRESS"; //The new state for the alert{string}
        /**
         * Closes this dialog and discards any changes.
         */
    }

    closeDialog() {
        this.dialogRef.close(false);
     };
     /**
      * Saves this update and closes this dialog.
      */
     saveDialog() {
         this.saving = true;
         var event = {state: this.state, description: this.description, clear: false};
         this.http.post(this.OpsManagerRestUrlService.ALERT_DETAILS_URL(this.alert.id), event)
             .toPromise().then(()=> {
                 this.dialogRef.close(true);
             }, () =>{
                 this.saving = false;
             });
     };
}

@Component({
    selector: 'alerts-details',
    templateUrl: 'js/ops-mgr/alerts/alert-details.html'
})
export class AlertDetailsComponent {
    alertId: any;
    
    constructor(private stateService: StateService){}
    ngOnInit(){
        this.alertId = this.stateService.params.alertId;
    }
}
