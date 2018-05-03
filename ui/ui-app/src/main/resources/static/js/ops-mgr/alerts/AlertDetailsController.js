define(["require", "exports", "angular", "../module-name", "../../constants/AccessConstants"], function (require, exports, angular, module_name_1, AccessConstants_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /** Manages the Alert Details page.
      * @constructor
      * @param $scope the Angular scope
      * @param $http the HTTP service
      * @param $mdDialog the dialog server
      * @param AccessControlService the access control service
      * @param OpsManagerRestUrlService the REST URL service
      */
    var AlertDetailsDirectiveController = /** @class */ (function () {
        function AlertDetailsDirectiveController($scope, $http, $mdDialog, accessControlService, OpsManagerRestUrlService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$mdDialog = $mdDialog;
            this.accessControlService = accessControlService;
            this.OpsManagerRestUrlService = OpsManagerRestUrlService;
            this.allowAdmin = false; //Indicates that admin operations are allowed. {boolean}
            /**
            * Gets the class for the specified state.
            * @param {string} state the name of the state
            * @returns {string} class name
            */
            this.getStateClass = function (state) {
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
            this.getStateIcon = function (state) {
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
            this.getStateText = function (state) {
                if (state === "IN_PROGRESS") {
                    return "IN PROGRESS";
                }
                else {
                    return state;
                }
            };
            //Hides this alert on the list page.
            this.hideAlert = function () {
                _this.alertData.cleared = true;
                _this.$http.post(_this.OpsManagerRestUrlService.ALERT_DETAILS_URL(_this.alertData.id), { state: _this.alertData.state, clear: true });
            };
            //Shows the alert removing the 'cleared' flag
            this.showAlert = function () {
                _this.alertData.cleared = false;
                _this.$http.post(_this.OpsManagerRestUrlService.ALERT_DETAILS_URL(_this.alertData.id), { state: _this.alertData.state, clear: false, unclear: true });
            };
            /** Loads the data for the specified alert.
             * @param {string} alertId the id of the alert
             */
            this.loadAlert = function (alertId) {
                if (alertId) {
                    _this.$http.get(_this.OpsManagerRestUrlService.ALERT_DETAILS_URL(alertId))
                        .then(function (response) {
                        _this.alertData = response.data;
                        // Set time since created
                        if (angular.isNumber(_this.alertData.createdTime)) {
                            _this.alertData.createdTimeSince = Date.now() - _this.alertData.createdTime;
                        }
                        // Set state information
                        if (angular.isString(_this.alertData.state)) {
                            _this.alertData.stateClass = _this.getStateClass(_this.alertData.state);
                            _this.alertData.stateIcon = _this.getStateIcon(_this.alertData.state);
                            _this.alertData.stateText = _this.getStateText(_this.alertData.state);
                        }
                        var isStream = false;
                        if (angular.isArray(_this.alertData.events)) {
                            angular.forEach(_this.alertData.events, function (event) {
                                event.stateClass = _this.getStateClass(event.state);
                                event.stateIcon = _this.getStateIcon(event.state);
                                event.stateText = _this.getStateText(event.state);
                                event.contentSummary = null;
                                if (angular.isDefined(event.content)) {
                                    try {
                                        var alertEventContent = angular.fromJson(event.content);
                                        if (alertEventContent && alertEventContent.content) {
                                            event.contentSummary = angular.isDefined(alertEventContent.content.failedCount) ? alertEventContent.content.failedCount + " failures" : null;
                                            if (!isStream && angular.isDefined(alertEventContent.content.stream)) {
                                                isStream = alertEventContent.content.stream;
                                            }
                                        }
                                    }
                                    catch (err) {
                                    }
                                }
                            });
                        }
                        _this.alertData.links = [];
                        //add in the detail URLs
                        if (_this.alertData.type == 'http://kylo.io/alert/job/failure') {
                            if (angular.isDefined(_this.alertData.content) && !isStream) {
                                var jobExecutionId = _this.alertData.content;
                                _this.alertData.links.push({ label: "Job Execution", value: "job-details({executionId:'" + jobExecutionId + "'})" });
                            }
                            _this.alertData.links.push({ label: "Feed Details", value: "ops-feed-details({feedName:'" + _this.alertData.entityId + "'})" });
                        }
                        else if (_this.alertData.type == 'http://kylo.io/alert/alert/sla/violation') {
                            if (angular.isDefined(_this.alertData.content)) {
                                _this.alertData.links.push({ label: "Service Level Assessment", value: "service-level-assessment({assessmentId:'" + _this.alertData.content + "'})" });
                            }
                            _this.alertData.links.push({ label: "Service Level Agreement", value: "service-level-agreements({slaId:'" + _this.alertData.entityId + "'})" });
                        }
                        else if (_this.alertData.type == 'http://kylo.io/alert/service') {
                            _this.alertData.links.push({ label: "Service Details", value: "service-details({serviceName:'" + _this.alertData.subtype + "'})" });
                        }
                    });
                }
            };
            /**
             * Shows a dialog for adding a new event.
             * @param $event the event that triggered this dialog
             */
            this.showEventDialog = function ($event) {
                _this.$mdDialog.show({
                    controller: 'EventDialogController',
                    locals: {
                        alert: _this.alertData
                    },
                    parent: angular.element(document.body),
                    targetEvent: $event,
                    templateUrl: "js/ops-mgr/alerts/event-dialog.html"
                }).then(function (result) {
                    if (result) {
                        _this.loadAlert(_this.alertData.id);
                    }
                });
            };
            this.loadAlert(this.alertId); // Fetch alert details
            accessControlService.getUserAllowedActions() // Fetch allowed permissions
                .then(function (actionSet) {
                _this.allowAdmin = accessControlService.hasAction(AccessConstants_1.default.OPERATIONS_ADMIN, actionSet.actions);
            });
        } // end of constructor
        AlertDetailsDirectiveController.$inject = ["$scope", "$http", "$mdDialog", "AccessControlService", "OpsManagerRestUrlService"];
        return AlertDetailsDirectiveController;
    }());
    exports.AlertDetailsDirectiveController = AlertDetailsDirectiveController;
    /**
        * Manages the Update Alert dialog.
        * @constructor
        * @param $scope the Angular scope
        * @param $http
        * @param $mdDialog the dialog service
        * @param OpsManagerRestUrlService the REST URL service
        * @param alert the alert to update
        */
    var EventDialogController = /** @class */ (function () {
        function EventDialogController($scope, //the Angular scope
            $http, //the HTTP service
            $mdDialog, //the dialog service
            OpsManagerRestUrlService, //the REST URL service
            alert //the alert to update
        ) {
            this.$scope = $scope;
            this.$http = $http;
            this.$mdDialog = $mdDialog;
            this.OpsManagerRestUrlService = OpsManagerRestUrlService;
            this.alert = alert; //the alert to update
            $scope.saving = false; //Indicates that this update is currently being saved  {boolean}
            $scope.state = (alert.state === "HANDLED") ? "HANDLED" : "IN_PROGRESS"; //The new state for the alert{string}
            /**
             * Closes this dialog and discards any changes.
             */
            $scope.closeDialog = function () {
                $mdDialog.hide(false);
            };
            /**
             * Saves this update and closes this dialog.
             */
            $scope.saveDialog = function () {
                $scope.saving = true;
                var event = { state: $scope.state, description: $scope.description, clear: false };
                $http.post(OpsManagerRestUrlService.ALERT_DETAILS_URL(alert.id), event)
                    .then(function () {
                    $mdDialog.hide(true);
                }, function () {
                    $scope.saving = false;
                });
            };
        }
        EventDialogController.$inject = ["$scope", "$http", "$mdDialog", "OpsManagerRestUrlService", "alert"];
        return EventDialogController;
    }());
    exports.EventDialogController = EventDialogController;
    var AlertDetailsController = /** @class */ (function () {
        function AlertDetailsController() {
            this.alertId = this.$transition$.params().alertId;
        }
        return AlertDetailsController;
    }());
    exports.AlertDetailsController = AlertDetailsController;
    angular.module(module_name_1.moduleName).component("alertDetailsController", {
        bindings: {
            $transition$: '<'
        },
        controller: AlertDetailsController,
        controllerAs: "vm",
        templateUrl: "js/ops-mgr/alerts/alert-details.html"
    });
    angular.module(module_name_1.moduleName).component("alertDetailsDirectiveController", {
        controller: AlertDetailsDirectiveController,
    });
    angular.module(module_name_1.moduleName).directive("tbaAlertDetails", [
        function () {
            return {
                restrict: "EA",
                bindToController: {
                    cardTitle: "@",
                    alertId: "="
                },
                controllerAs: "vm",
                scope: true,
                templateUrl: "js/ops-mgr/alerts/alert-details-template.html",
                controller: AlertDetailsDirectiveController
            };
        }
    ]);
    angular.module(module_name_1.moduleName).component("eventDialogController", {
        controller: EventDialogController,
    });
});
//# sourceMappingURL=AlertDetailsController.js.map