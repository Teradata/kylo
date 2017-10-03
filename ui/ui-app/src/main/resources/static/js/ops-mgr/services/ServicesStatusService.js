define(['angular','ops-mgr/module-name'], function (angular,moduleName) {
    /**
     * Service to call out to Feed REST.
     *
     */
angular.module(moduleName).factory('ServicesStatusData',
        ['$q', '$http', '$interval', '$timeout', 'AlertsService', 'IconService', 'OpsManagerRestUrlService', function ($q, $http, $interval, $timeout, AlertsService, IconService, OpsManagerRestUrlService) {
            var ServicesStatusData = {};
            ServicesStatusData.SERVICES_URL = OpsManagerRestUrlService.SERVICES_URL;
            ServicesStatusData.fetchServiceInterval = null;
            ServicesStatusData.services = {};
            ServicesStatusData.FETCH_INTERVAL = 5000;

            ServicesStatusData.fetchServiceStatusErrorCount = 0;

            ServicesStatusData.setFetchTimeout = function(timeout){
                ServicesStatusData.FETCH_INTERVAL = timeout;
            }

            ServicesStatusData.transformServicesResponse = function(services) {
                    var data = services;
                    ServicesStatusData.fetchServiceStatusErrorCount = 0;
                    angular.forEach(data, function (service, i) {
                        service.componentsCount = service.components.length;
                        service.alertsCount = (service.alerts != null && service.alerts.length ) ? service.alerts.length : 0;
                        if (service.state == 'UNKNOWN') {
                            service.state = 'WARNING';
                        }
                        var serviceHealth = IconService.iconForHealth(service.state)
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
                                        var alertHealth = IconService.iconForServiceComponentAlert(alert.state);
                                        alert.icon = alertHealth.icon;
                                        alert.iconstyle = alertHealth.style;
                                        alert.healthText = alertHealth.text
                                    });
                                }
                                else {
                                    component.alerts = [];
                                }
                                var maxAlertState = component.highestAlertState;
                                if (maxAlertState != null) {
                                    component.state = maxAlertState;
                                }
                                var componentHealth = IconService.iconForHealth(component.state)
                                component.healthText = componentHealth.text;
                                component.icon = componentHealth.icon;
                                component.iconstyle = componentHealth.style;

                            })
                        }

                        if (ServicesStatusData.services[service.serviceName]) {
                            angular.extend(ServicesStatusData.services[service.serviceName], service);
                        }
                        else {
                            ServicesStatusData.services[service.serviceName] = service;
                        }
                    });

            }

            ServicesStatusData.fetchServiceStatus = function (successCallback, errorCallback) {

                var successFn = function (response) {
                    var data = response.data;
                    ServicesStatusData.transformServicesResponse(data);
                    if (successCallback) {
                        successCallback(data);
                    }
                }
                var errorFn = function (err) {
                    ServicesStatusData.fetchServiceStatusErrorCount++;

                    if (errorCallback) {
                        errorCallback(err);
                    }
                    if (ServicesStatusData.fetchServiceStatusErrorCount >= 10) {
                        ServicesStatusData.FETCH_INTERVAL = 20000;
                    }

                }

                return $http.get(ServicesStatusData.SERVICES_URL).then(successFn,errorFn);

            }
                return ServicesStatusData;
        }]);
});