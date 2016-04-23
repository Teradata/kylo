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

