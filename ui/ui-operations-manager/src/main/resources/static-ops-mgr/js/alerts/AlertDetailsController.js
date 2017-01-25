/*-
 * #%L
 * thinkbig-ui-operations-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
(function() {

    var directive = function() {
        return {
            restrict: "EA",
            bindToController: {
                cardTitle: "@"
            },
            controllerAs: "vm",
            scope: true,
            templateUrl: "js/alerts/alert-details-template.html",
            controller: "AlertDetailsController"
        };
    };

    /**
     * Manages the Alert Details page.
     * @constructor
     * @param $scope the Angular scope
     * @param $http the HTTP service
     * @param $mdDialog the dialog server
     * @param $stateParams the state parameters
     * @param AccessControlService the access control service
     * @param RestUrlService the REST URL service
     */
    function AlertDetailsController($scope, $http, $mdDialog, $stateParams, AccessControlService, RestUrlService) {
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
            $http.post(RestUrlService.ALERT_DETAILS_URL(self.alertData.id), {state: self.alertData.state, clear: true});
        };

        /**
         * Loads the data for the specified alert.
         * @param {string} alertId the id of the alert
         */
        self.loadAlert = function(alertId) {
            $http.get(RestUrlService.ALERT_DETAILS_URL(alertId))
                    .then(function(response) {
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

                        if (angular.isArray(self.alertData.events)) {
                            angular.forEach(self.alertData.events, function(event) {
                                event.stateClass = self.getStateClass(event.state);
                                event.stateIcon = self.getStateIcon(event.state);
                                event.stateText = self.getStateText(event.state);
                            });
                        }
                    });
        };

        /**
         * Shows a dialog for adding a new event.
         * @param $event the event that triggered this dialog
         */
        self.showEventDialog = function($event) {
            $mdDialog.show({
                controller: EventDialogController,
                locals: {
                    alert: self.alertData
                },
                parent: angular.element(document.body),
                targetEvent: $event,
                templateUrl: "js/alerts/event-dialog.html"
            }).then(function(result) {
                if (result) {
                    self.loadAlert(self.alertData.id);
                }
            });
        };

        // Fetch alert details
        self.loadAlert($stateParams.alertId);

        // Fetch allowed permissions
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    self.allowAdmin = AccessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
                });
    }

    angular.module(MODULE_OPERATIONS).controller("AlertDetailsController", AlertDetailsController);
    angular.module(MODULE_OPERATIONS).directive("tbaAlertDetails", directive);

    /**
     * Manages the Update Alert dialog.
     * @constructor
     * @param $scope the Angular scope
     * @param $http the HTTP service
     * @param $mdDialog the dialog service
     * @param RestUrlService the REST URL service
     * @param alert the alert to update
     */
    function EventDialogController($scope, $http, $mdDialog, RestUrlService, alert) {

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
            $http.post(RestUrlService.ALERT_DETAILS_URL(alert.id), event)
                    .then(function() {
                        $mdDialog.hide(true);
                    }, function() {
                        $scope.saving = false;
                    });
        };
    }
})();
