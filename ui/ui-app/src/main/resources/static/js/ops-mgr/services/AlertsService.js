define(["require", "exports", "angular", "../module-name", "moment", "underscore"], function (require, exports, angular, module_name_1, moment, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AlertsService = /** @class */ (function () {
        function AlertsService() {
            this.feedFailureAlerts = {};
            this.serviceAlerts = {};
            this.alerts = [];
            this.alertsById = {};
            this.findFeedFailureAlert = function (feedName) {
                return _.find(this.alerts, function (alert) {
                    return alert.type == 'Feed' && alert.name == feedName;
                });
            };
            this.findFeedFailureAlertIndex = function (feedName) {
                return _.findIndex(this.alerts, function (alert) {
                    return alert.type == 'Feed' && alert.name == feedName;
                });
            };
            this.replaceFeedAlert = function (feedName, feedHealth) {
            };
            this.addFeedHealthFailureAlert = function (feedHealth) {
                //first remove it and add a new entry
                if (this.feedFailureAlerts[feedHealth.feed] != undefined) {
                    this.removeFeedFailureAlertByName(feedHealth.feed);
                }
                var alertId = this.IDGenerator.generateId('alert');
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
                this.feedFailureAlerts[feedHealth.feed] = alert;
                this.alertsById[alertId] = alert;
                this.alerts.push(alert);
            };
            this.addServiceAlert = function (service) {
                if (this.serviceAlerts[service.serviceName] == undefined) {
                    var alertId = this.IDGenerator.generateId('service');
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
                    this.serviceAlerts[service.serviceName] = alert;
                    this.alertsById[alertId] = alert;
                    this.alerts.push(alert);
                }
                else {
                    this.serviceAlerts[service.serviceName].sinceTime = service.latestAlertTimestamp;
                    this.serviceAlerts[service.serviceName].since = moment(service.latestAlertTimestamp).fromNow();
                }
            };
            this.removeFeedFailureAlertByName = function (feed) {
                var alert = this.feedFailureAlerts[feed];
                if (alert) {
                    delete this.feedFailureAlerts[feed];
                    delete this.alertsById[alert.id];
                    var matchingIndex = this.findFeedFailureAlertIndex(feed);
                    if (matchingIndex != -1) {
                        this.alerts.splice(matchingIndex, 1);
                    }
                }
            };
            this.removeServiceAlert = function (service) {
                var alert = this.serviceAlerts[service.serviceName];
                if (alert) {
                    delete this.serviceAlerts[service.serviceName];
                    delete this.alertsById[alert.id];
                    var index = this.alerts.indexOf(alert);
                    this.alerts.splice(index, 1);
                }
            };
        }
        return AlertsService;
    }());
    exports.default = AlertsService;
    angular.module(module_name_1.moduleName).service('AlertsService', [AlertsService]);
});
//# sourceMappingURL=AlertsService.js.map