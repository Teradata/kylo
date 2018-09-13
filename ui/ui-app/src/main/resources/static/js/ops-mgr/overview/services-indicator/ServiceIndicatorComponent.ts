import * as angular from "angular";
import "pascalprecht.translate";
import * as _ from 'underscore';
import * as moment from "moment";
import { Component, Inject, ElementRef } from "@angular/core";
import {MatDialog, MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import StateService from "../../../services/StateService";
import OpsManagerDashboardService from "../../services/OpsManagerDashboardService";
import BroadcastService from "../../../services/broadcast-service";
import ServicesStatusData from "../../services/ServicesStatusService";
import HttpService from "../../../services/HttpService";
import { TranslateService } from "@ngx-translate/core";

class Indicator {

    chartData: any[] = []
    openAlerts: any[] = [];
    allServices: any[] = [];
    counts: any = {errorCount: 0, allCount: 0, upCount: 0, downCount: 0, warningCount: 0}
    percent: number = 0;
    dateTime: any = null;
    healthClass:string;

    grouped: any = {
        "HEALTHY": {label: "Healthy", styleClass: "status-healthy", count: 0, data: []},
        "WARNING": {label: "Warnings", styleClass: "status-warnings", count: 0, data: []},
        "UNHEALTHY": {label: "UNHEALTHY", styleClass: "status-errors", count: 0, data: []}
    }

    constructor(private chartOptions: any, private translate: TranslateService) {

        this.chartData.push({key: "HEALTHY", value: 0});
        this.chartData.push({key: "UNHEALTHY", value: 0});
        this.chartData.push({key: "WARNING", value: 0});

    }

    toggleComponentAlert(event: any, component: any) {
        var target = event.target;
        var parentTdWidth = $(target).parents('td:first').width();
        component.alertDetailsStyle = 'width:' + parentTdWidth + 'px;';
        if (component.showAlerts == true) {

            var alertIndex = _.indexOf(this.openAlerts, component);
            if (alertIndex >= 0) {
                this.openAlerts.splice(alertIndex, 1);
            }
            component.showAlerts = false;
        }
        else {
            this.openAlerts.push(component);
            component.showAlerts = true;
        }
    }

    reset() {
        this.openAlerts = [];
        this.counts = {errorCount: 0, allCount: 0, upCount: 0, downCount: 0, warningCount: 0};
        this.percent = 0;
        this.dateTime = null;
        this.allServices = [];
        angular.forEach(this.grouped, (groupData, status) => {
            groupData.data = [];
            groupData.count = 0;
        })
    }


    addService(service: any) {
        var displayState = service.state == "UP" ? "HEALTHY" : (service.state == "DOWN" ? "UNHEALTHY" : service.state);
        this.grouped[displayState].data.push(service);
        this.grouped[displayState].count++;
        service.latestAlertTimeAgo = null;
        //update timeAgo text
        if (service.latestAlertTimestamp != null) {
            service.latestAlertTimeAgo = moment(service.latestAlertTimestamp).from(moment());
        }
    }

    checkToShowClusterName(service: any) {
        if (service && service.components) {
            var componentNames = _.map(service.components, (component: any) => {
                return component.name;
            });
            var unique = _.uniq(componentNames);
            if (componentNames.length != unique.length) {
                service.showClusterName = true;
            }
            else {
                service.showClusterName = false;
            }
        }
    }

    addServices(services: any) {
        if (this.openAlerts.length == 0) {
            this.reset();
            this.allServices = services;
            angular.forEach(services, (service: any, i: any) => {
                this.addService(service);
                service.componentCount = service.components.length;
                service.healthyComponentCount = service.healthyComponents.length;
                service.unhealthyComponentCount = service.unhealthyComponents.length;
                this.checkToShowClusterName(service);
            });

            this.updateCounts();
            this.updatePercent();
            this.dateTime = new Date();
        }
    }

    updateCounts() {
        this.counts.upCount = this.grouped["HEALTHY"].count;
        this.counts.allCount = this.allServices.length;
        this.counts.downCount = this.grouped["UNHEALTHY"].count;
        this.counts.warningCount = this.grouped["WARNING"].count;
        this.counts.errorCount = this.counts.downCount + this.counts.warningCount;
        angular.forEach(this.chartData, (item, i) => {
            item.value = this.grouped[item.key].count;
        })
        this.chartOptions.chart.title = this.counts.allCount + " " + this.translate.instant('Total');
    }

    updatePercent() {
        if (this.counts.upCount > 0) {
            this.percent = (this.counts.upCount / this.counts.allCount) * 100;
            this.percent = Math.round(this.percent);
        }
        if (this.percent <= 50) {
            this.healthClass = "errors";
        }
        else if (this.percent < 100) {
            this.healthClass = "warnings";
        }
        else {
            this.healthClass = "success";
        }
    }
}

@Component({
    selector: 'tba-services-indicator',
    templateUrl: 'js/ops-mgr/overview/services-indicator/services-indicator-template.html'
})
export class ServiceIndicatorComponent {
    /**
     * flag to indicate the data is loaded
     */
    dataLoaded: boolean = false;
    /**
     * The chart API object
     */
    chartApi: any = {};
    /**
     * options for how to render the chart
     */
    chartOptions: any;
    /**
     * Indicator to store the data and stats for the chart
     */
    indicator: Indicator;

    /**
     * Timeout promise for updating the chart title
     */
    validateTitleTimeout: any;

    constructor(private dialog: MatDialog,
                private element: ElementRef,
                private ServicesStatusData: ServicesStatusData,
                private OpsManagerDashboardService: OpsManagerDashboardService,
                private BroadcastService: BroadcastService,
                private translate: TranslateService) {

    }// end of constructor

    private watchDashboard() {
        this.BroadcastService.subscribe(null,
            this.OpsManagerDashboardService.DASHBOARD_UPDATED,
            (dashboard: any) => {

                this.ServicesStatusData.transformServicesResponse(this.OpsManagerDashboardService.dashboard.serviceStatus);
                var services = this.ServicesStatusData.services;
                var servicesArr = [];
                for (var k in services) {
                    servicesArr.push(services[k]);
                }
                this.indicator.addServices(servicesArr);
                this.dataLoaded = true;
            });
    }


    openDetailsDialog(key: any) {
        let dialogRef = this.dialog.open(ServiceDetailsDialogComponent, {
            data: { status: key,
                    selectedStatusData: this.indicator.grouped[key] },
            panelClass: "full-screen-dialog"
        });

    }

    updateChart() {
        var title = (this.indicator.counts.allCount) + " " + this.translate.instant('Total');
        this.chartOptions.chart.title = title
        if (this.chartApi.update) {
            this.chartApi.update();
        }
    }

    private validateTitle() {
        if (this.validateTitleTimeout != null) {
            clearTimeout(this.validateTitleTimeout);
        }
        var txt = $(this.element.nativeElement).find('.nv-pie-title').text();
        if ($.trim(txt) == "0 Total" && this.indicator.counts.allCount > 0) {
            this.updateChart();
        }
        this.validateTitleTimeout = setTimeout(() => {
            this.validateTitle()
        }, 1000);

    }

    ngOnInit() {
        this.watchDashboard();

        this.chartOptions = {
            chart: {
                type: 'pieChart',
                x: (d: any) => {
                    return d.key;
                },
                y: (d: any) => {
                    return d.value;
                },
                showLabels: false,
                duration: 100,
                "height": 150,
                labelThreshold: 0.01,
                labelSunbeamLayout: false,
                "margin": {"top": 10, "right": 10, "bottom": 10, "left": 10},
                donut: true,
                donutRatio: 0.65,
                showLegend: false,
                valueFormat: (d: any) => {
                    return parseInt(d);
                },
                color: (d: any) => {
                    if (d.key == 'HEALTHY') {
                        return '#009933';
                    }
                    else if (d.key == 'UNHEALTHY') {
                        return '#FF0000';
                    }
                    else if (d.key == 'WARNING') {
                        return '#FF9901';
                    }
                },
                pie: {
                    dispatch: {
                        'elementClick': (e: any) => {
                            this.openDetailsDialog(e.data.key);
                        }
                    }
                },
                dispatch: {
                    renderEnd: () => {

                    }
                }
            }
        };


        this.validateTitle();

        this.indicator = new Indicator(this.chartOptions, this.translate);
    }
}

@Component({
    templateUrl: 'js/ops-mgr/overview/services-indicator/services-details-dialog.html'
})
export class ServiceDetailsDialogComponent {

    services:any[];
    css: string;
    status: string;
    allChartData: any;
           
    constructor (private dialogRef: MatDialogRef<ServiceDetailsDialogComponent>,
                 @Inject(MAT_DIALOG_DATA) private data: any,
                 private stateService: StateService) {}

    ngOnInit() {

        this.css = status == "UNHEALTHY" ? "md-warn" : "";
        this.status = this.data.status;
        this.services = this.data.selectedStatusData.data;

        this.services.forEach((service: any) => {
            service.componentMessage = null;
            if (service.components.length == 1) {
                service.componentName = service.components[0].name;
                service.componentMessage = service.components[0].message;
            }
        });

    }

    hide() {
        this.dialogRef.close();
    }

    cancel() {
        this.dialogRef.close();
    }

    gotoServiceDetails(serviceName: any) {
        this.dialogRef.close();
        this.stateService.OpsManager().ServiceStatus().navigateToServiceDetails(serviceName);
    }
            
}