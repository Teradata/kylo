define(["require", "exports", "angular", "../module-name", "../../services/AlertsServiceV2", "../../services/OpsManagerDashboardService"], function (require, exports, angular, module_name_1, AlertsServiceV2_1, OpsManagerDashboardService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $element, $interval, AlertsServiceV2, StateService, OpsManagerDashboardService, BroadcastService) {
            var _this = this;
            this.$scope = $scope;
            this.$element = $element;
            this.$interval = $interval;
            this.AlertsServiceV2 = AlertsServiceV2;
            this.StateService = StateService;
            this.OpsManagerDashboardService = OpsManagerDashboardService;
            this.BroadcastService = BroadcastService;
            this.watchDashboard = function () {
                _this.BroadcastService.subscribe(_this.$scope, _this.OpsManagerDashboardService.DASHBOARD_UPDATED, function (dashboard) {
                    var alerts = _this.OpsManagerDashboardService.dashboard.alerts;
                    _this.AlertsServiceV2.transformAlerts(alerts);
                    _this.alerts = alerts;
                });
            };
            this.fetchFeedAlerts = function () {
                _this.AlertsServiceV2.fetchFeedAlerts(_this.feedName).then(function (alerts) {
                    _this.alerts = alerts;
                });
            };
            this.stopFeedRefresh = function () {
                if (_this.feedRefresh != null) {
                    _this.$interval.cancel(_this.feedRefresh);
                    _this.feedRefresh = null;
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
                _this.StateService.OpsManager().Alert().navigateToAlerts(query);
            };
            //  this.alertsService = AlertsServiceV2;
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
        .service('AlertsServiceV2', ["$q", "$http", "$interval", "OpsManagerRestUrlService", AlertsServiceV2_1.default])
        .service('OpsManagerDashboardService', ['$q', '$http', '$interval', '$timeout', 'HttpService', 'IconService', 'AlertsService', 'OpsManagerRestUrlService', 'BroadcastService', 'OpsManagerFeedService', OpsManagerDashboardService_1.default])
        .controller('AlertsOverviewController', ["$scope", "$element", "$interval", "AlertsServiceV2", "StateService", "OpsManagerDashboardService",
        "BroadcastService", controller]);
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