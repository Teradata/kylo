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

    var controller = function ($scope, $element, $interval, AlertsService, StateService,OpsManagerDashboardService,BroadcastService) {
        var self = this;
        this.alertsService = AlertsService;
        this.alerts = [];

        /**
         * Handle on the feed alerts refresh interval
         * @type {null}
         */
        this.feedRefresh = null;

        this.refreshIntervalTime = angular.isUndefined(self.refreshIntervalTime) ? 5000 : self.refreshIntervalTime;


        function watchDashboard() {
            BroadcastService.subscribe($scope,OpsManagerDashboardService.DASHBOARD_UPDATED,function(dashboard){
                var alerts = OpsManagerDashboardService.dashboard.alerts;
                AlertsService.transformAlerts(alerts);
                self.alerts = alerts;
            });
        }

        if(this.feedName == undefined || this.feedName == ''){
            watchDashboard();
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
        });

    };

    angular.module(moduleName).controller('AlertsOverviewController', ["$scope","$element","$interval","AlertsService","StateService","OpsManagerDashboardService","BroadcastService",controller]);


    angular.module(moduleName)
        .directive('tbaAlerts', directive);

});

