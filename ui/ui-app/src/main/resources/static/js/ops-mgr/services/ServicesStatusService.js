define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ServicesStatusData = /** @class */ (function () {
        function ServicesStatusData($q, $http, $interval, $timeout, AlertsService, IconService, OpsManagerRestUrlService) {
            this.$q = $q;
            this.$http = $http;
            this.$interval = $interval;
            this.$timeout = $timeout;
            this.AlertsService = AlertsService;
            this.IconService = IconService;
            this.OpsManagerRestUrlService = OpsManagerRestUrlService;
            this.module = angular.module(module_name_1.moduleName, []);
            this.module.factory('ServicesStatusData', ['$q', '$http', '$interval', '$timeout', 'AlertsService', 'IconService', 'OpsManagerRestUrlService', this.factoryFn.bind(this)]);
        }
        ServicesStatusData.prototype.factoryFn = function () {
            var ServicesStatusData = {};
            ServicesStatusData.SERVICES_URL = this.OpsManagerRestUrlService.SERVICES_URL;
            ServicesStatusData.fetchServiceInterval = null;
            ServicesStatusData.services = {};
            ServicesStatusData.FETCH_INTERVAL = 5000;
            ServicesStatusData.fetchServiceStatusErrorCount = 0;
            ServicesStatusData.setFetchTimeout = function (timeout) {
                ServicesStatusData.FETCH_INTERVAL = timeout;
            };
            ServicesStatusData.transformServicesResponse = function (services) {
                var data = services;
                ServicesStatusData.fetchServiceStatusErrorCount = 0;
                angular.forEach(data, function (service, i) {
                    service.componentsCount = service.components.length;
                    service.alertsCount = (service.alerts != null && service.alerts.length) ? service.alerts.length : 0;
                    if (service.state == 'UNKNOWN') {
                        service.state = 'WARNING';
                    }
                    var serviceHealth = this.IconService.iconForHealth(service.state);
                    service.healthText = serviceHealth.text;
                    service.icon = serviceHealth.icon;
                    service.iconstyle = serviceHealth.style;
                    if (service.components) {
                        service.componentMap = {};
                        angular.forEach(service.components, function (component, i) {
                            service.componentMap[component.name] = component;
                            if (component.state == 'UNKNOWN') {
                                component.state = 'WARNING';
                            }
                            if (component.alerts != null) {
                                angular.forEach(component.alerts, function (alert) {
                                    var alertHealth = this.IconService.iconForServiceComponentAlert(alert.state);
                                    alert.icon = alertHealth.icon;
                                    alert.iconstyle = alertHealth.style;
                                    alert.healthText = alertHealth.text;
                                });
                            }
                            else {
                                component.alerts = [];
                            }
                            var maxAlertState = component.highestAlertState;
                            if (maxAlertState != null) {
                                component.state = maxAlertState;
                            }
                            var componentHealth = this.IconService.iconForHealth(component.state);
                            component.healthText = componentHealth.text;
                            component.icon = componentHealth.icon;
                            component.iconstyle = componentHealth.style;
                        });
                    }
                    if (ServicesStatusData.services[service.serviceName]) {
                        angular.extend(ServicesStatusData.services[service.serviceName], service);
                    }
                    else {
                        ServicesStatusData.services[service.serviceName] = service;
                    }
                });
            };
            ServicesStatusData.fetchServiceStatus = function (successCallback, errorCallback) {
                var successFn = function (response) {
                    var data = response.data;
                    ServicesStatusData.transformServicesResponse(data);
                    if (successCallback) {
                        successCallback(data);
                    }
                };
                var errorFn = function (err) {
                    ServicesStatusData.fetchServiceStatusErrorCount++;
                    if (errorCallback) {
                        errorCallback(err);
                    }
                    if (ServicesStatusData.fetchServiceStatusErrorCount >= 10) {
                        ServicesStatusData.FETCH_INTERVAL = 20000;
                    }
                };
                return this.$http.get(ServicesStatusData.SERVICES_URL).then(successFn, errorFn);
            };
            return ServicesStatusData;
        };
        return ServicesStatusData;
    }());
    exports.default = ServicesStatusData;
});
//# sourceMappingURL=ServicesStatusService.js.map