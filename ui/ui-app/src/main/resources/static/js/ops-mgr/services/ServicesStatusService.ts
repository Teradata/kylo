import * as angular from "angular";
import {moduleName} from "../module-name";

export default class ServicesStatusData{
    module: ng.IModule;
    constructor(private $q: any,
                private $http: any,
                private $interval: any,
                private $timeout: any,
                private AlertsService: any,
                private IconService: any,
                private OpsManagerRestUrlService: any
    ){
    this.module = angular.module(moduleName,[]);
    this.module.factory('ServicesStatusData',['$q', '$http', '$interval', '$timeout', 'AlertsService', 'IconService', 'OpsManagerRestUrlService',this.factoryFn.bind(this)]);
    }

      factoryFn() {
      var ServicesStatusData: any = {};
            ServicesStatusData.SERVICES_URL = this.OpsManagerRestUrlService.SERVICES_URL;
            ServicesStatusData.fetchServiceInterval = null;
            ServicesStatusData.services = {};
            ServicesStatusData.FETCH_INTERVAL = 5000;

            ServicesStatusData.fetchServiceStatusErrorCount = 0;

            ServicesStatusData.setFetchTimeout = function(timeout: any){
                ServicesStatusData.FETCH_INTERVAL = timeout;
            }

            ServicesStatusData.transformServicesResponse = function(services: any) {
                    var data = services;
                    ServicesStatusData.fetchServiceStatusErrorCount = 0;
                    angular.forEach(data, function (service: any, i: any) {
                        service.componentsCount = service.components.length;
                        service.alertsCount = (service.alerts != null && service.alerts.length ) ? service.alerts.length : 0;
                        if (service.state == 'UNKNOWN') {
                            service.state = 'WARNING';
                        }
                        var serviceHealth = this.IconService.iconForHealth(service.state)
                        service.healthText = serviceHealth.text;
                        service.icon = serviceHealth.icon;
                        service.iconstyle = serviceHealth.style;
                        if (service.components) {
                            service.componentMap = {};
                            angular.forEach(service.components, function (component: any, i: any) {
                                service.componentMap[component.name] = component;
                                if (component.state == 'UNKNOWN') {
                                    component.state = 'WARNING';
                                }

                                if (component.alerts != null) {
                                    angular.forEach(component.alerts, function (alert: any) {
                                        var alertHealth = this.IconService.iconForServiceComponentAlert(alert.state);
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
                                var componentHealth = this.IconService.iconForHealth(component.state)
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

            ServicesStatusData.fetchServiceStatus = function (successCallback: any, errorCallback: any) {

                var successFn = function (response: any) {
                    var data = response.data;
                    ServicesStatusData.transformServicesResponse(data);
                    if (successCallback) {
                        successCallback(data);
                    }
                }
                var errorFn = function (err: any) {
                    ServicesStatusData.fetchServiceStatusErrorCount++;

                    if (errorCallback) {
                        errorCallback(err);
                    }
                    if (ServicesStatusData.fetchServiceStatusErrorCount >= 10) {
                        ServicesStatusData.FETCH_INTERVAL = 20000;
                    }

                }

                return this.$http.get(ServicesStatusData.SERVICES_URL).then(successFn,errorFn);

            }
                return ServicesStatusData;
      }
}