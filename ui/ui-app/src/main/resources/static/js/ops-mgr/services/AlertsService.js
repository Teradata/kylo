define(["require", "exports", "angular", "../module-name", "moment", "underscore", "../module"], function (require, exports, angular, module_name_1, moment, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AlertsService = /** @class */ (function () {
        function AlertsService() {
            var _this = this;
            this.feedFailureAlerts = {};
            this.serviceAlerts = {};
            this.alerts = [];
            this.alertsById = {};
            this.findFeedFailureAlert = function (feedName) {
                return _.find(_this.alerts, function (alert) {
                    return alert.type == 'Feed' && alert.name == feedName;
                });
            };
            this.findFeedFailureAlertIndex = function (feedName) {
                return _.findIndex(_this.alerts, function (alert) {
                    return alert.type == 'Feed' && alert.name == feedName;
                });
            };
            this.replaceFeedAlert = function (feedName, feedHealth) {
            };
            this.addFeedHealthFailureAlert = function (feedHealth) {
                //first remove it and add a new entry
                if (_this.feedFailureAlerts[feedHealth.feed] != undefined) {
                    _this.removeFeedFailureAlertByName(feedHealth.feed);
                }
                var alertId = _this.IDGenerator.generateId('alert');
                var alert = {
                    id: alertId,
                    type: 'Feed',
                    name: feedHealth.feed,
                    'summary': feedHealth.feed,
                    message: 'UNHEALTHY',
                    severity: 'FATAL',
                    sinceTime: feedHealth.lastUnhealthyTime,
                    count: feedHealth.unhealthyCount,
                    since: feedHealth.sinceTimeString
                };
                _this.feedFailureAlerts[feedHealth.feed] = alert;
                _this.alertsById[alertId] = alert;
                _this.alerts.push(alert);
            };
            this.addServiceAlert = function (service) {
                if (_this.serviceAlerts[service.serviceName] == undefined) {
                    var alertId = _this.IDGenerator.generateId('service');
                    var alert = {
                        id: alertId,
                        type: 'Service',
                        name: service.serviceName,
                        'summary': service.serviceName,
                        message: service.alertsCount + " alerts",
                        severity: 'FATAL',
                        count: 1,
                        sinceTime: service.latestAlertTimestamp,
                        since: moment(service.latestAlertTimestamp).fromNow()
                    };
                    _this.serviceAlerts[service.serviceName] = alert;
                    _this.alertsById[alertId] = alert;
                    _this.alerts.push(alert);
                }
                else {
                    _this.serviceAlerts[service.serviceName].sinceTime = service.latestAlertTimestamp;
                    _this.serviceAlerts[service.serviceName].since = moment(service.latestAlertTimestamp).fromNow();
                }
            };
            this.removeFeedFailureAlertByName = function (feed) {
                var alert = _this.feedFailureAlerts[feed];
                if (alert) {
                    delete _this.feedFailureAlerts[feed];
                    delete _this.alertsById[alert.id];
                    var matchingIndex = _this.findFeedFailureAlertIndex(feed);
                    if (matchingIndex != -1) {
                        _this.alerts.splice(matchingIndex, 1);
                    }
                }
            };
            this.removeServiceAlert = function (service) {
                var alert = _this.serviceAlerts[service.serviceName];
                if (alert) {
                    delete _this.serviceAlerts[service.serviceName];
                    delete _this.alertsById[alert.id];
                    var index = _this.alerts.indexOf(alert);
                    _this.alerts.splice(index, 1);
                }
            };
        }
        return AlertsService;
    }());
    exports.default = AlertsService;
    angular.module(module_name_1.moduleName).service('AlertsService', [AlertsService]);
});
//# sourceMappingURL=AlertsService.js.map