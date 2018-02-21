define(["require", "exports", "angular", "../module-name", "../../services/ServicesStatusService", "../../services/AlertsService", "../../services/OpsManagerDashboardService"], function (require, exports, angular, module_name_1, ServicesStatusService_1, AlertsService_1, OpsManagerDashboardService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $element, $interval, AlertsService, StateService, ServicesStatusData, OpsManagerDashboardService, BroadcastService) {
            var _this = this;
            this.$scope = $scope;
            this.$element = $element;
            this.$interval = $interval;
            this.AlertsService = AlertsService;
            this.StateService = StateService;
            this.ServicesStatusData = ServicesStatusData;
            this.OpsManagerDashboardService = OpsManagerDashboardService;
            this.BroadcastService = BroadcastService;
            this.watchDashboard = function () {
                var _this = this;
                this.BroadcastService.subscribe(this.$scope, this.OpsManagerDashboardService.DASHBOARD_UPDATED, function (dashboard) {
                    var alerts = _this.OpsManagerDashboardService.dashboard.alerts;
                    _this.AlertsService.transformAlerts(alerts);
                    _this.alerts = alerts;
                });
            };
            this.fetchFeedAlerts = function () {
                var _this = this;
                this.AlertsService.fetchFeedAlerts(this.feedName).then(function (alerts) {
                    _this.alerts = alerts;
                });
            };
            this.stopFeedRefresh = function () {
                if (this.feedRefresh != null) {
                    this.$interval.cancel(this.feedRefresh);
                    this.feedRefresh = null;
                }
            };
            this.navigateToAlerts = function (alertsSummary) {
                //generate Query
                var query = "UNHANDLED," + alertsSummary.type;
                if (alertsSummary.groupDisplayName != null && alertsSummary.groupDisplayName != null) {
                    query += "," + alertsSummary.groupDisplayName;
                }
                else if (alertsSummary.subtype != null && alertsSummary.subtype != '') {
                    query += "," + alertsSummary.subtype;
                }
                this.StateService.OpsManager().Alert().navigateToAlerts(query);
            };
            this.alertsService = AlertsService;
            this.alerts = [];
            /**
            * Handle on the feed alerts refresh interval
            * @type {null}
            */
            this.feedRefresh = null;
            this.refreshIntervalTime = angular.isUndefined(this.refreshIntervalTime) ? 5000 : this.refreshIntervalTime;
            if (this.feedName == undefined || this.feedName == '') {
                this.watchDashboard();
            }
            else {
                this.alerts = [];
                this.stopFeedRefresh();
                this.fetchFeedAlerts();
                this.feedRefresh = $interval(this.fetchFeedAlerts, 5000);
            }
            $scope.$on('$destroy', function () {
                _this.stopFeedRefresh();
            });
        }
        return controller;
    }());
    exports.default = controller;
    angular.module(module_name_1.moduleName)
        .service("AlertsService", [AlertsService_1.default])
        .service("ServicesStatusData", ["$q", '$http', '$interval', '$timeout', 'AlertsService', 'IconService', 'OpsManagerRestUrlService', ServicesStatusService_1.default])
        .service('OpsManagerDashboardService', ['$q', '$http', '$interval', '$timeout', 'HttpService', 'IconService', 'AlertsService', 'OpsManagerRestUrlService', 'BroadcastService', 'OpsManagerFeedService', OpsManagerDashboardService_1.default])
        .controller('AlertsOverviewController', ["$scope", "$element", "$interval", "AlertsService", "StateService", "OpsManagerDashboardService", "BroadcastService", controller]);
    angular.module(module_name_1.moduleName)
        .directive('tbaAlerts', [function () {
            return {
                restrict: "E",
                scope: true,
                bindToController: {
                    panelTitle: "@",
                    feedName: '@',
                    refreshIntervalTime: '=?'
                },
                controllerAs: 'vm',
                templateUrl: 'js/ops-mgr/overview/alerts/alerts-template.html',
                controller: "AlertsOverviewController",
                link: function ($scope, element, attrs) {
                    $scope.$on('$destroy', function () {
                    });
                } //DOM manipulation\}
            };
        }
    ]);
});
//# sourceMappingURL=AlertsDirective.js.map