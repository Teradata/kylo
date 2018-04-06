import * as angular from "angular";
import {moduleName} from "../module-name";
import OpsManagerRestUrlService from "./OpsManagerRestUrlService";

export default class ServicesStatusData{
    ServicesStatusData: any;
    
    //static $inject = ['OpsManagerRestUrlService'];
    constructor(private $q: any,
                private $http: any,
                private $interval: any,
                private $timeout: any,
                private IconService: any,
                private OpsManagerRestUrlService: OpsManagerRestUrlService){       
                this.ServicesStatusData = {};
                this.ServicesStatusData.SERVICES_URL = this.OpsManagerRestUrlService.SERVICES_URL;
                this.ServicesStatusData.fetchServiceInterval = null;
                this.ServicesStatusData.services = {};
                this.ServicesStatusData.FETCH_INTERVAL = 5000;

                this.ServicesStatusData.fetchServiceStatusErrorCount = 0;
                this.ServicesStatusData.setFetchTimeout = (timeout: any)=>{
                this.ServicesStatusData.FETCH_INTERVAL = timeout;
                }

             this.ServicesStatusData.fetchServiceStatus = (successCallback: any, errorCallback: any)=>{

                var successFn =  (response: any)=> {
                    var data = response.data;
                    this.ServicesStatusData.transformServicesResponse(data);
                    if (successCallback) {
                        successCallback(data);
                    }
                }
                var errorFn =  (err: any)=> {
                    this.ServicesStatusData.fetchServiceStatusErrorCount++;

                    if (errorCallback) {
                        errorCallback(err);
                    }
                    if (this.ServicesStatusData.fetchServiceStatusErrorCount >= 10) {
                        this.ServicesStatusData.FETCH_INTERVAL = 20000;
                    }

                }
                return this.$http.get(this.ServicesStatusData.SERVICES_URL).then(successFn,errorFn);
            }
            
             this.ServicesStatusData.transformServicesResponse = (services: any)=>{
                    var data = services;
                    this.ServicesStatusData.fetchServiceStatusErrorCount = 0;
                    angular.forEach(data,  (service: any, i: any) =>{
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
                            angular.forEach(service.components,  (component: any, i: any)=> {
                                service.componentMap[component.name] = component;
                                if (component.state == 'UNKNOWN') {
                                    component.state = 'WARNING';
                                }

                                if (component.alerts != null) {
                                    angular.forEach(component.alerts,  (alert: any) =>{
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

                        if (this.ServicesStatusData.services[service.serviceName]) {
                            angular.extend(this.ServicesStatusData.services[service.serviceName], service);
                        }
                        else {
                            this.ServicesStatusData.services[service.serviceName] = service;
                        }
                    });
            }
            return this.ServicesStatusData;
        }   
}


angular.module(moduleName)
        .factory('ServicesStatusData',
                ["$q", '$http', '$interval', '$timeout', 'IconService',
                'OpsManagerRestUrlService',
                ServicesStatusData]);
