import OpsManagerRestUrlService from "./OpsManagerRestUrlService";
import IconService from "./IconStatusService";
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";

@Injectable()
export default class ServicesStatusData {

    SERVICES_URL: string = this.opsManagerRestUrlService.SERVICES_URL;
    fetchServiceInterval: any = null;
    services: any = {};
    FETCH_INTERVAL: number = 5000;

    fetchServiceStatusErrorCount: number = 0;

    constructor(private http: HttpClient,
        private iconService: IconService,
        private opsManagerRestUrlService: OpsManagerRestUrlService) {

    }
    setFetchTimeout = (timeout: any) => {
        this.FETCH_INTERVAL = timeout;
    }
    fetchServiceStatus(successCallback: any, errorCallback: any) : Promise<any> {

        var successFn = (response: any) => {
            var data = response.data;
            this.transformServicesResponse(data);
            if (successCallback) {
                successCallback(data);
            }
        }
        var errorFn = (err: any) => {
            this.fetchServiceStatusErrorCount++;

            if (errorCallback) {
                errorCallback(err);
            }
            if (this.fetchServiceStatusErrorCount >= 10) {
                this.FETCH_INTERVAL = 20000;
            }

        }
        return this.http.get(this.SERVICES_URL).toPromise().then(successFn, errorFn);
    }

    transformServicesResponse(services: any) {
        var data = services;
        this.fetchServiceStatusErrorCount = 0;
        Object.keys(data).forEach((dataKey: any) => {
            var service = data[dataKey];
            service.componentsCount = service.components.length;
            service.alertsCount = (service.alerts != null && service.alerts.length) ? service.alerts.length : 0;
            if (service.state == 'UNKNOWN') {
                service.state = 'WARNING';
            }
            var serviceHealth = this.iconService.iconForHealth(service.state)
            service.healthText = serviceHealth.text;
            service.icon = serviceHealth.icon;
            service.iconstyle = serviceHealth.style;
            if (service.components) {
                service.componentMap = {};
                Object.keys(service.components).forEach((key: any) => {
                    var component = service.components[key];
                    service.componentMap[component.name] = component;
                    if (component.state == 'UNKNOWN') {
                        component.state = 'WARNING';
                    }

                    if (component.alerts != null) {
                        component.alerts.forEach((alert: any) => {
                            var alertHealth = this.iconService.iconForServiceComponentAlert(alert.state);
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
                    var componentHealth = this.iconService.iconForHealth(component.state)
                    component.healthText = componentHealth.text;
                    component.icon = componentHealth.icon;
                    component.iconstyle = componentHealth.style;
                });
            }

            if (this.services[service.serviceName]) {
                this.services[service.serviceName] = [...this.services[service.serviceName], ...service];
            }
            else {
                this.services[service.serviceName] = service;

            }
        });
    }
}