import * as angular from "angular";
import {moduleName} from "../module-name";
import * as _ from "underscore";
import OpsManagerRestUrlService from "../services/OpsManagerRestUrlService";
import {AccessControlService} from "../../services/AccessControlService";
import AccessConstants from '../../constants/AccessConstants';
import {Transition} from "@uirouter/core";
import "./module-require";
import {FEED_DEFINITION_SUMMARY_STATE_NAME} from "../../feed-mgr/model/feed/feed-constants";

   /** Manages the Alert Details page.
     * @constructor
     * @param $scope the Angular scope
     * @param $http the HTTP service
     * @param $mdDialog the dialog server
     * @param AccessControlService the access control service
     * @param OpsManagerRestUrlService the REST URL service
     */
export class AlertDetailsDirectiveController implements ng.IComponentController{
    allowAdmin: boolean = false; //Indicates that admin operations are allowed. {boolean}
    alertData: any; //The alert details. {Object}
    alertId: any;
    static readonly $inject=["$scope","$http","$mdDialog","AccessControlService","OpsManagerRestUrlService"];
    ngOnInit(){
            this.loadAlert(this.alertId); // Fetch alert details
            this.accessControlService.getUserAllowedActions() // Fetch allowed permissions
                        .then((actionSet: any) =>{
                            this.allowAdmin = this.accessControlService.hasAction(AccessConstants.OPERATIONS_ADMIN, actionSet.actions);
                        });
    }
    constructor(private $scope: angular.IScope,
                private $http: angular.IHttpService,
                private $mdDialog: angular.material.IDialogService ,
                private accessControlService: AccessControlService,
                private OpsManagerRestUrlService: OpsManagerRestUrlService){
                this.ngOnInit();
    }// end of constructor
         /**
         * Gets the class for the specified state.
         * @param {string} state the name of the state
         * @returns {string} class name
         */
        getStateClass =(state: string)=> {
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
        getStateIcon = (state: string)=> {
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
        getStateText = (state: string)=> {
            if (state === "IN_PROGRESS") {
                return "IN PROGRESS";
            } else {
                return state;
            }
        };

        //Hides this alert on the list page.
        hideAlert = ()=> {
            this.alertData.cleared = true;
            this.$http.post(this.OpsManagerRestUrlService.ALERT_DETAILS_URL(this.alertData.id), {state: this.alertData.state, clear: true});
        };

        //Shows the alert removing the 'cleared' flag
        showAlert = ()=> {
            this.alertData.cleared = false;
            this.$http.post(this.OpsManagerRestUrlService.ALERT_DETAILS_URL(this.alertData.id), {state: this.alertData.state, clear: false, unclear:true});
        };


        /** Loads the data for the specified alert.
         * @param {string} alertId the id of the alert
         */
        loadAlert = (alertId: string)=> {
            if(alertId) {
                this.$http.get(this.OpsManagerRestUrlService.ALERT_DETAILS_URL(alertId))
                    .then( (response: any)=> {
                        this.alertData = response.data;

                        // Set time since created
                        if (angular.isNumber(this.alertData.createdTime)) {
                            this.alertData.createdTimeSince = Date.now() - this.alertData.createdTime;
                        }

                        // Set state information
                        if (angular.isString(this.alertData.state)) {
                            this.alertData.stateClass = this.getStateClass(this.alertData.state);
                            this.alertData.stateIcon = this.getStateIcon(this.alertData.state);
                            this.alertData.stateText = this.getStateText(this.alertData.state);
                        }
                        var isStream = false;

                        if (angular.isArray(this.alertData.events)) {
                            angular.forEach(this.alertData.events,  (event: any)=> {
                                event.stateClass = this.getStateClass(event.state);
                                event.stateIcon = this.getStateIcon(event.state);
                                event.stateText = this.getStateText(event.state);
                                event.contentSummary = null;
                                if(angular.isDefined(event.content)){
                                    try {
                                        var alertEventContent = angular.fromJson(event.content);
                                        if(alertEventContent && alertEventContent.content){
                                            event.contentSummary = angular.isDefined(alertEventContent.content.failedCount) ? alertEventContent.content.failedCount +" failures"  : null;
                                            if(!isStream && angular.isDefined(alertEventContent.content.stream)){
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
                            if(angular.isDefined(this.alertData.content) && !isStream) {
                                var jobExecutionId = this.alertData.content;
                                this.alertData.links.push({label: "Job Execution", value: "job-details({executionId:'" + jobExecutionId + "'})"});
                            }



                            this.alertData.links.push({label:"Feed Details",  value:FEED_DEFINITION_SUMMARY_STATE_NAME+".feed-activity"+"({feedId:'"+this.alertData.entityId+"'})"});

                        }
                        else   if(this.alertData.type == 'http://kylo.io/alert/alert/sla/violation') {
                            if(angular.isDefined(this.alertData.content)) {
                                this.alertData.links.push({label: "Service Level Assessment", value: "service-level-assessment({assessmentId:'" + this.alertData.content + "'})"});
                            }
                            this.alertData.links.push({label:"Service Level Agreement",  value:"service-level-agreements({slaId:'"+this.alertData.entityId+"'})"});
                        }

                        else   if(this.alertData.type == 'http://kylo.io/alert/service') {
                            this.alertData.links.push({label:"Service Details",  value:"service-details({serviceName:'"+this.alertData.subtype+"'})"});
                        }

                    });
            }
        };

        /**
         * Shows a dialog for adding a new event.
         * @param $event the event that triggered this dialog
         */
        showEventDialog = ($event: any)=> {
            this.$mdDialog.show({
                controller: 'EventDialogController',
                locals: {
                    alert: this.alertData
                },
                parent: angular.element(document.body),
                targetEvent: $event,
                templateUrl: "./event-dialog.html"
            }).then((result: any)=> {
                if (result) {
                    this.loadAlert(this.alertData.id);
                }
            });
        };
      
}
export interface IMyScope extends ng.IScope {
  saving?: boolean;
  state?: string;
  closeDialog?: any;
  saveDialog?: any;
  description?: any;
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
export class EventDialogController implements ng.IComponentController{
    static readonly $inject=["$scope","$http","$mdDialog","OpsManagerRestUrlService","alert"];
    constructor(private $scope: IMyScope, //the Angular scope
                private $http: angular.IHttpService,  //the HTTP service
                private $mdDialog: angular.material.IDialogService, //the dialog service
                private OpsManagerRestUrlService: OpsManagerRestUrlService, //the REST URL service
                private alert: any //the alert to update
                ){
        this.ngOnInit();
    }
    ngOnInit(){
        this.$scope.saving = false; //Indicates that this update is currently being saved  {boolean}
        this.$scope.state = (this.alert.state === "HANDLED") ? "HANDLED" : "IN_PROGRESS"; //The new state for the alert{string}
        /**
         * Closes this dialog and discards any changes.
         */
        this.$scope.closeDialog = ()=> {
           this.$mdDialog.hide(false);
        };
        /**
         * Saves this update and closes this dialog.
         */
        this.$scope.saveDialog = ()=> {
            this.$scope.saving = true;
            var event = {state: this.$scope.state, description: this.$scope.description, clear: false};
            this.$http.post(this.OpsManagerRestUrlService.ALERT_DETAILS_URL(this.alert.id), event)
                .then(()=> {
                    this.$mdDialog.hide(true);
                }, () =>{
                    this.$scope.saving = false;
                });
        };
    }
}

export class AlertDetailsController implements ng.IComponentController{
    alertId: any;
    $transition$: Transition;
    constructor(){
        this.ngOnInit();
    }
    ngOnInit(){
        this.alertId = this.$transition$.params().alertId;
    }
}

const module = angular.module(moduleName).component("alertDetailsController", {
        bindings: {
            $transition$: '<'
        },
        controller: AlertDetailsController,
        controllerAs: "vm",
        templateUrl: "./alert-details.html"
    });
export default module;

angular.module(moduleName).controller("alertDetailsDirectiveController",
         AlertDetailsDirectiveController,
    );
angular.module(moduleName).directive("tbaAlertDetails",
        [
            ()=> {
            return {
                    restrict: "EA",
                    bindToController: {
                        cardTitle: "@",
                        alertId:"="
                    },
                    controllerAs: "vm",
                    scope: true,
                    templateUrl: "./alert-details-template.html",
                    controller: AlertDetailsDirectiveController
                };
            }
        ]);
angular.module(moduleName).controller("EventDialogController",
         EventDialogController
    );