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
(function () {

    var directive = function () {
        return {
            restrict: "E",
            scope: true,
            bindToController: {
                panelTitle: "@",
                refreshIntervalTime: "@",
                feedName:'@'
            },
            controllerAs: 'vm',
            templateUrl: 'js/overview/alerts/alerts-template.html',
            controller: "AlertsController",
            link: function ($scope, element, attrs) {
                $scope.$on('$destroy', function () {

                });
            } //DOM manipulation\}
        }

    };

    var controller = function ($scope, $element, $interval, AlertsService, StateService) {
        var self = this;
        this.dataLoaded = false;
        this.refreshIntervalTime = 1000;
        this.alertsService = AlertsService;
        this.alerts = [];



        if(this.feedName == undefined || this.feedName == ''){
            this.alerts = AlertsService.alerts;
            $scope.$watchCollection(
                function () {
                    return AlertsService.alerts;
                },
                function (newVal) {
                    self.alerts = newVal;
                }
            );
        }
        else {
            this.alerts = [AlertsService.feedFailureAlerts[self.feedName]];
            $scope.$watch(
                function () {
                    return AlertsService.feedFailureAlerts;
                },
                function (newVal) {
                    if(newVal && newVal[self.feedName]){
                        self.alerts = [newVal[self.feedName]]
                    }
                    else {
                        self.alerts = [];
                    }
                },true
            );
        }


/*
 function refresh(){
 if(self.feedName == undefined || self.feedName == ''){
 self.alerts = AlertsService.alerts;
 }
 else {
 self.alerts = [AlertsService.feedFailureAlerts[self.feedName]];
 }
 }

        this.clearRefreshInterval = function () {
            if (self.refreshInterval != null) {
                $interval.cancel(self.refreshInterval);
                self.refreshInterval = null;
            }
        }

        this.setRefreshInterval = function () {
            self.clearRefreshInterval();
            if (self.refreshIntervalTime) {
                self.refreshInterval = $interval(refresh, self.refreshIntervalTime);

            }
        }

        this.init = function () {
            self.setRefreshInterval();
        }

        this.init();
*/


        this.navigateToAlert = function(alert) {
            if(alert.type == 'Feed' && self.feedName == undefined){
                StateService.navigateToFeedDetails(alert.name);
            }
            else if(alert.type == 'Service' ) {
                StateService.navigateToServiceDetails(alert.name);
            }

        }

        $scope.$on('$destroy', function () {
       //     self.clearRefreshInterval();
        });
    };

    angular.module(MODULE_OPERATIONS).controller('AlertsController', controller);


    angular.module(MODULE_OPERATIONS)
        .directive('tbaAlerts', directive);

}());

