define(['angular','ops-mgr/overview/module-name'], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "E",
            scope: true,
            bindToController: {
                panelTitle: "@",
                feedName:'@'
            },
            controllerAs: 'vm',
            templateUrl: 'js/ops-mgr/overview/alerts/alerts-template.html',
            controller: "AlertsOverviewController",
            link: function ($scope, element, attrs) {
                $scope.$on('$destroy', function () {

                });
            } //DOM manipulation\}
        }

    };

    var controller = function ($scope, $element, $interval, AlertsService, StateService) {
        var self = this;
        this.alertsService = AlertsService;
        this.alerts = [];



        if(this.feedName == undefined || this.feedName == ''){
            this.alerts = AlertsService.alertsSummary.data;
            $scope.$watchCollection(
                function () {
                    return AlertsService.alertsSummary.data;
                },
                function (newVal) {
                    self.alerts = newVal;
                }
            );
        }
        else {
            self.alerts = [];
            //TODO implement
            /*
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
            */
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


        this.navigateToAlerts = function(alertsSummary) {

            //generate Query
            var query = "UNHANDLED,"+ alertsSummary.type;
            if(alertsSummary.subtype != null && alertsSummary.subtype != null) {
                query += ","+alertsSummary.subtype;
            }
            StateService.OpsManager().Alert().navigateToAlerts(query);
            /*
            if(alert.type == 'Feed' && self.feedName == undefined){
                StateService.OpsManager().Feed().navigateToFeedDetails(alert.name);
            }
            else if(alert.type == 'Service' ) {
                StateService.OpsManager().ServiceStatus().navigateToServiceDetails(alert.name);
            }
            */

        }

        $scope.$on('$destroy', function () {
       //     self.clearRefreshInterval();
        });
    };

    angular.module(moduleName).controller('AlertsOverviewController', ["$scope","$element","$interval","AlertsService","StateService",controller]);


    angular.module(moduleName)
        .directive('tbaAlerts', directive);

});

