define(['angular','ops-mgr/overview/module-name'], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "E",
            scope: true,
            bindToController: {
                panelTitle: "@",
                feedName:'@',
                refreshIntervalTime:'=?'
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

        /**
         * Handle on the feed alerts refresh interval
         * @type {null}
         */
        this.feedRefresh = null;

        this.refreshIntervalTime = angular.isUndefined(self.refreshIntervalTime) ? 5000 : self.refreshIntervalTime;



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
            AlertsService.startRefreshingAlerts(self.refreshIntervalTime);
        }
        else {
            self.alerts = [];
            stopFeedRefresh();
            fetchFeedAlerts();
           self.feedRefresh = $interval(fetchFeedAlerts,5000);
        }

        function fetchFeedAlerts(){
            AlertsService.fetchFeedAlerts(self.feedName).then(function(alerts) {
                self.alerts =alerts;
            });
        }

        function stopFeedRefresh(){
            if(self.feedRefresh != null){
                $interval.cancel(self.feedRefresh);
                self.feedRefresh = null;
            }
        }


        this.navigateToAlerts = function(alertsSummary) {

            //generate Query
            var query = "UNHANDLED,"+ alertsSummary.type;
            if(alertsSummary.groupDisplayName != null && alertsSummary.groupDisplayName != null) {
                query += ","+alertsSummary.groupDisplayName;
            }
            else if(alertsSummary.subtype != null && alertsSummary.subtype != '') {
                query += ","+alertsSummary.subtype;
            }
            StateService.OpsManager().Alert().navigateToAlerts(query);

        }

        $scope.$on('$destroy', function () {
            stopFeedRefresh();
            AlertsService.stopRefreshingAlerts();
        });

    };

    angular.module(moduleName).controller('AlertsOverviewController', ["$scope","$element","$interval","AlertsService","StateService",controller]);


    angular.module(moduleName)
        .directive('tbaAlerts', directive);

});

