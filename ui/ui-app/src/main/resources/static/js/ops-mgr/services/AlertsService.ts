import * as angular from "angular";
import {moduleName} from "../module-name";
import * as moment from "moment";
import * as _ from 'underscore';

export default class AlertsService{
    feedFailureAlerts: any = {};
    serviceAlerts: any = {};
    alerts: any = [];
    alertsById: any = {};
    IDGenerator: any;
    findFeedFailureAlert =  (feedName: any)=> {
            return _.find(this.alerts,  (alert: any)=>{
                return alert.type == 'Feed' && alert.name == feedName;
            });
        }
     findFeedFailureAlertIndex =  (feedName: any) =>{
            return _.findIndex(this.alerts,  (alert: any)=> {
                return alert.type == 'Feed' && alert.name == feedName;
            });
        }
     replaceFeedAlert =  (feedName: any, feedHealth: any) =>{

     }
       addFeedHealthFailureAlert =  (feedHealth: any)=> {
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
       }

        addServiceAlert =  (service: any) =>{
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
        }
    
    
    removeFeedFailureAlertByName =  (feed: any)=> {
            var alert = this.feedFailureAlerts[feed];
            if (alert) {
                delete this.feedFailureAlerts[feed];
                delete this.alertsById[alert.id];
                var matchingIndex = this.findFeedFailureAlertIndex(feed);
                if (matchingIndex != -1) {
                    this.alerts.splice(matchingIndex, 1);
                }
            }
        }

    removeServiceAlert =  (service: any)=>{
            var alert = this.serviceAlerts[service.serviceName];
            if (alert) {
                delete this.serviceAlerts[service.serviceName];
                delete this.alertsById[alert.id];
                var index = this.alerts.indexOf(alert);
                this.alerts.splice(index, 1);
            }
        }
    constructor(){}

}

  angular.module(moduleName).service('AlertsService',[AlertsService]);
