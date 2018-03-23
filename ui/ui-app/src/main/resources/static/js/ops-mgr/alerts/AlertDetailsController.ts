import * as angular from "angular";
import {moduleName} from "../module-name";
import * as _ from "underscore";
import OpsManagerRestUrlService from "../services/OpsManagerRestUrlService";

   /**
     * Manages the Alert Details page.
     * @constructor
     * @param $scope the Angular scope
     * @param $http the HTTP service
     * @param $mdDialog the dialog server
     * @param AccessControlService the access control service
     * @param OpsManagerRestUrlService the REST URL service
     */
export class AlertDetailsDirectiveController implements ng.IComponentController{
    allowAdmin: any;
    alertData: any;
    alertId: any;
    constructor(private $scope: any,
                private $http: any,
                private $mdDialog: any,
                private AccessControlService: any,
                private OpsManagerRestUrlService: any){// $scope, $http, $mdDialog, AccessControlService, OpsManagerRestUrlService
    /**
     * Indicates that admin operations are allowed.
     * @type {boolean}
     */
    this.allowAdmin = false;

    /**
     * The alert details.
     * @type {Object}
     */
    this.alertData = {};    
         // Fetch alert details
        this.loadAlert(this.alertId);

        // Fetch allowed permissions
        AccessControlService.getUserAllowedActions()
                .then((actionSet: any) =>{
                    this.allowAdmin = AccessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
                }); 
    
    }// end of constructor
     
         /**
         * Gets the class for the specified state.
         * @param {string} state the name of the state
         * @returns {string} class name
         */
        getStateClass =(state: any)=> {
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
        getStateIcon = (state: any)=> {
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
        getStateText = (state: any)=> {
            if (state === "IN_PROGRESS") {
                return "IN PROGRESS";
            } else {
                return state;
            }
        };

        /**
         * Hides this alert on the list page.
         */
        hideAlert = ()=> {
            this.alertData.cleared = true;
            this.$http.post(this.OpsManagerRestUrlService.ALERT_DETAILS_URL(this.alertData.id), {state: this.alertData.state, clear: true});
        };

        /**
         * Shows the alert removing the 'cleared' flag
         */
        showAlert = ()=> {
            this.alertData.cleared = false;
            this.$http.post(this.OpsManagerRestUrlService.ALERT_DETAILS_URL(this.alertData.id), {state: this.alertData.state, clear: false, unclear:true});
        };


        /**
         * Loads the data for the specified alert.
         * @param {string} alertId the id of the alert
         */
        loadAlert = (alertId: any)=> {
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
                            this.alertData.links.push({label:"Feed Details",  value:"ops-feed-details({feedName:'"+this.alertData.entityId+"'})"});

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
                templateUrl: "js/ops-mgr/alerts/event-dialog.html"
            }).then((result: any)=> {
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
     * @param $http the HTTP service
     * @param $mdDialog the dialog service
     * @param OpsManagerRestUrlService the REST URL service
     * @param alert the alert to update
     */

export class EventDialogController implements ng.IComponentController{
    constructor(private $scope: any,
                private $http: any,
                private $mdDialog: any,
                private OpsManagerRestUrlService: any,
                private alert: any){
        /**
         * Indicates that this update is currently being saved.
         * @type {boolean}
         */
        $scope.saving = false;
        /**
         * The new state for the alert.
         * @type {string}
         */
        $scope.state = (alert.state === "HANDLED") ? "HANDLED" : "IN_PROGRESS";
        /**
         * Closes this dialog and discards any changes.
         */
        $scope.closeDialog = ()=> {
            $mdDialog.hide(false);
        };
        /**
         * Saves this update and closes this dialog.
         */
        $scope.saveDialog = ()=> {
            $scope.saving = true;

            var event = {state: $scope.state, description: $scope.description, clear: false};
            $http.post(OpsManagerRestUrlService.ALERT_DETAILS_URL(alert.id), event)
                .then(()=> {
                    $mdDialog.hide(true);
                }, () =>{
                    $scope.saving = false;
                });
        };
    }
}

export class AlertDetailsController implements ng.IComponentController{
    alertId: any;
    constructor(private $transition$: any){
        this.alertId = $transition$.params().alertId;
    }
}


angular.module(moduleName).controller("AlertDetailsController",["$transition$",AlertDetailsController]);
angular.module(moduleName).controller("AlertDetailsDirectiveController", ["$scope","$http","$mdDialog","AccessControlService","OpsManagerRestUrlService",AlertDetailsDirectiveController]);
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
                                templateUrl: "js/ops-mgr/alerts/alert-details-template.html",
                                controller: "AlertDetailsDirectiveController"
                            };
                        }
                    ]);
angular.module(moduleName)
.service('OpsManagerRestUrlService',[OpsManagerRestUrlService])
.controller("EventDialogController", ["$scope","$http","$mdDialog","OpsManagerRestUrlService","alert",EventDialogController]);
