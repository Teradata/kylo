define(['angular','ops-mgr/alerts/module-name'], function (angular,moduleName) {

    var directive = function() {
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
    };

    /**
     * Manages the Alert Details page.
     * @constructor
     * @param $scope the Angular scope
     * @param $http the HTTP service
     * @param $mdDialog the dialog server
     * @param AccessControlService the access control service
     * @param OpsManagerRestUrlService the REST URL service
     */
    function AlertDetailsDirectiveController($scope, $http, $mdDialog, AccessControlService, OpsManagerRestUrlService) {
        var self = this;
        /**
         * Indicates that admin operations are allowed.
         * @type {boolean}
         */
        self.allowAdmin = false;

        /**
         * The alert details.
         * @type {Object}
         */
        self.alertData = {};

         /**
         * Gets the class for the specified state.
         * @param {string} state the name of the state
         * @returns {string} class name
         */
        self.getStateClass = function(state) {
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
        self.getStateIcon = function(state) {
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
        self.getStateText = function(state) {
            if (state === "IN_PROGRESS") {
                return "IN PROGRESS";
            } else {
                return state;
            }
        };

        /**
         * Hides this alert on the list page.
         */
        self.hideAlert = function() {
            self.alertData.cleared = true;
            $http.post(OpsManagerRestUrlService.ALERT_DETAILS_URL(self.alertData.id), {state: self.alertData.state, clear: true});
        };

        /**
         * Shows the alert removing the 'cleared' flag
         */
        self.showAlert = function() {
            self.alertData.cleared = false;
            $http.post(OpsManagerRestUrlService.ALERT_DETAILS_URL(self.alertData.id), {state: self.alertData.state, clear: false, unclear:true});
        };


        /**
         * Loads the data for the specified alert.
         * @param {string} alertId the id of the alert
         */
        self.loadAlert = function(alertId) {
            if(alertId) {
                $http.get(OpsManagerRestUrlService.ALERT_DETAILS_URL(alertId))
                    .then(function (response) {
                        self.alertData = response.data;

                        // Set time since created
                        if (angular.isNumber(self.alertData.createdTime)) {
                            self.alertData.createdTimeSince = Date.now() - self.alertData.createdTime;
                        }

                        // Set state information
                        if (angular.isString(self.alertData.state)) {
                            self.alertData.stateClass = self.getStateClass(self.alertData.state);
                            self.alertData.stateIcon = self.getStateIcon(self.alertData.state);
                            self.alertData.stateText = self.getStateText(self.alertData.state);
                        }
                        var isStream = false;

                        if (angular.isArray(self.alertData.events)) {
                            angular.forEach(self.alertData.events, function (event) {
                                event.stateClass = self.getStateClass(event.state);
                                event.stateIcon = self.getStateIcon(event.state);
                                event.stateText = self.getStateText(event.state);
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
                        self.alertData.links = [];
                        //add in the detail URLs
                        if(self.alertData.type == 'http://kylo.io/alert/job/failure') {
                            if(angular.isDefined(self.alertData.content) && !isStream) {
                                var jobExecutionId = self.alertData.content;
                                self.alertData.links.push({label: "Job Execution", value: "job-details({executionId:'" + jobExecutionId + "'})"});
                            }
                            self.alertData.links.push({label:"Feed Details",  value:"ops-feed-details({feedName:'"+self.alertData.entityId+"'})"});

                        }
                        else   if(self.alertData.type == 'http://kylo.io/alert/alert/sla/violation') {
                            if(angular.isDefined(self.alertData.content)) {
                                self.alertData.links.push({label: "Service Level Assessment", value: "service-level-assessment({assessmentId:'" + self.alertData.content + "'})"});
                            }
                            self.alertData.links.push({label:"Service Level Agreement",  value:"service-level-agreements({slaId:'"+self.alertData.entityId+"'})"});
                        }

                        else   if(self.alertData.type == 'http://kylo.io/alert/service') {
                            self.alertData.links.push({label:"Service Details",  value:"service-details({serviceName:'"+self.alertData.subtype+"'})"});
                        }

                    });
            }
        };

        /**
         * Shows a dialog for adding a new event.
         * @param $event the event that triggered this dialog
         */
        self.showEventDialog = function($event) {
            $mdDialog.show({
                controller: 'EventDialogController',
                locals: {
                    alert: self.alertData
                },
                parent: angular.element(document.body),
                targetEvent: $event,
                templateUrl: "js/ops-mgr/alerts/event-dialog.html"
            }).then(function(result) {
                if (result) {
                    self.loadAlert(self.alertData.id);
                }
            });
        };

        // Fetch alert details
        self.loadAlert(this.alertId);

        // Fetch allowed permissions
        AccessControlService.getUserAllowedActions()
                .then(function(actionSet) {
                    self.allowAdmin = AccessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
                });
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
    function EventDialogController($scope, $http, $mdDialog, OpsManagerRestUrlService, alert) {

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
        $scope.closeDialog = function() {
            $mdDialog.hide(false);
        };

        /**
         * Saves this update and closes this dialog.
         */
        $scope.saveDialog = function() {
            $scope.saving = true;

            var event = {state: $scope.state, description: $scope.description, clear: false};
            $http.post(OpsManagerRestUrlService.ALERT_DETAILS_URL(alert.id), event)
                .then(function() {
                    $mdDialog.hide(true);
                }, function() {
                    $scope.saving = false;
                });
        };
    }

    function AlertDetailsController($transition$) {
        this.alertId = $transition$.params().alertId;
    }

    angular.module(moduleName).controller("AlertDetailsController",["$transition$",AlertDetailsController]);
    angular.module(moduleName).controller("AlertDetailsDirectiveController", ["$scope","$http","$mdDialog","AccessControlService","OpsManagerRestUrlService",AlertDetailsDirectiveController]);
    angular.module(moduleName).directive("tbaAlertDetails", directive);

    angular.module(moduleName).controller("EventDialogController", ["$scope","$http","$mdDialog","OpsManagerRestUrlService","alert",EventDialogController]);


});
