define(["require", "exports", "angular", "../module-name", "./OpsManagerRestUrlService", "../services/AlertsService", "../services/IconStatusService"], function (require, exports, angular, module_name_1, OpsManagerRestUrlService_1, AlertsService_1, IconStatusService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ServicesStatusData = /** @class */ (function () {
        //static $inject = ['OpsManagerRestUrlService'];
        function ServicesStatusData($q, $http, $interval, $timeout, AlertsService, IconService, OpsManagerRestUrlService) {
            var _this = this;
            this.$q = $q;
            this.$http = $http;
            this.$interval = $interval;
            this.$timeout = $timeout;
            this.AlertsService = AlertsService;
            this.IconService = IconService;
            this.OpsManagerRestUrlService = OpsManagerRestUrlService;
            this.ServicesStatusData = {};
            this.ServicesStatusData.SERVICES_URL = this.OpsManagerRestUrlService.SERVICES_URL;
            this.ServicesStatusData.fetchServiceInterval = null;
            this.ServicesStatusData.services = {};
            this.ServicesStatusData.FETCH_INTERVAL = 5000;
            this.ServicesStatusData.fetchServiceStatusErrorCount = 0;
            this.ServicesStatusData.setFetchTimeout = function (timeout) {
                _this.ServicesStatusData.FETCH_INTERVAL = timeout;
            };
            this.ServicesStatusData.fetchServiceStatus = function (successCallback, errorCallback) {
                var successFn = function (response) {
                    var data = response.data;
                    _this.ServicesStatusData.transformServicesResponse(data);
                    if (successCallback) {
                        successCallback(data);
                    }
                };
                var errorFn = function (err) {
                    _this.ServicesStatusData.fetchServiceStatusErrorCount++;
                    if (errorCallback) {
                        errorCallback(err);
                    }
                    if (_this.ServicesStatusData.fetchServiceStatusErrorCount >= 10) {
                        _this.ServicesStatusData.FETCH_INTERVAL = 20000;
                    }
                };
                return _this.$http.get(_this.ServicesStatusData.SERVICES_URL).then(successFn, errorFn);
            };
            this.ServicesStatusData.transformServicesResponse = function (services) {
                var data = services;
                _this.ServicesStatusData.fetchServiceStatusErrorCount = 0;
                angular.forEach(data, function (service, i) {
                    service.componentsCount = service.components.length;
                    service.alertsCount = (service.alerts != null && service.alerts.length) ? service.alerts.length : 0;
                    if (service.state == 'UNKNOWN') {
                        service.state = 'WARNING';
                    }
                    var serviceHealth = _this.IconService.iconForHealth(service.state);
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
                                    var alertHealth = _this.IconService.iconForServiceComponentAlert(alert.state);
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
                            var componentHealth = _this.IconService.iconForHealth(component.state);
                            component.healthText = componentHealth.text;
                            component.icon = componentHealth.icon;
                            component.iconstyle = componentHealth.style;
                        });
                    }
                    if (_this.ServicesStatusData.services[service.serviceName]) {
                        angular.extend(_this.ServicesStatusData.services[service.serviceName], service);
                    }
                    else {
                        _this.ServicesStatusData.services[service.serviceName] = service;
                    }
                });
            };
            return this.ServicesStatusData;
        }
        return ServicesStatusData;
    }());
    exports.default = ServicesStatusData;
    angular.module(module_name_1.moduleName)
        .service("AlertsService", [AlertsService_1.default])
        .service("IconService", [IconStatusService_1.default])
        .service("OpsManagerRestUrlService", [OpsManagerRestUrlService_1.default])
        .factory('ServicesStatusData', ["$q", '$http', '$interval', '$timeout', 'AlertsService', 'IconService',
        'OpsManagerRestUrlService',
        ServicesStatusData]);
});
//# sourceMappingURL=ServicesStatusService.js.map