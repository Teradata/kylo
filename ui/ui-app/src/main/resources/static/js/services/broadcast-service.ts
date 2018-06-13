import * as angular from 'angular';
import "jquery";

import {moduleName} from './module-name';

import "./module"; // ensure module is loaded first

/**
 * Allow different controllers/services to subscribe and notify each other
 *
 * to subscribe include the BroadcastService in your controller and call this method:
 *  - BroadcastService.subscribe($scope, 'SOME_EVENT', someFunction);
 *
 * to notify call this:
 * -  BroadcastService.notify('SOME_EVENT,{optional data object},### optional timeout);
 */
export default class BroadcastService {

    static readonly $inject = ["$rootScope", "$timeout"];

    /**
    * map to check if multiple events come in for those that {@code data.notifyAfterTime}
    * to ensure multiple events are not fired.
    * @type {{}}
    */
    waitingEvents: any = {};

    subscribers = {};

    constructor(private $rootScope: any,
        private $timeout: angular.ITimeoutService) {

    }
    /**
    * notify subscribers of this event passing an optional data object and optional wait time (millis)
    * @param event
    * @param data
    * @param waitTime
    */
    notify = (event: any, data?: any, waitTime?: any) => {
        if (waitTime == undefined) {
            waitTime = 0;
        }
        if (this.waitingEvents[event] == undefined) {
            this.waitingEvents[event] = event;
            this.$timeout(() => {
                this.$rootScope.$emit(event, data);
                delete this.waitingEvents[event];
            }, waitTime);
        }
    };
    /**
     * Subscribe to some event
     */
    subscribe = (scope: any, event: any, callback: any) => {
        const handler: any = this.$rootScope.$on(event, callback);
        if(this.subscribers[event] == undefined){
            this.subscribers[event] = 0;
        }
        this.subscribers[event] +=1;
        if (scope != null) {
            scope.$on('$destroy', ()=>{
                handler();
                this.subscribers[event] -= 1;
            });
        }
    };
    /**
     * Subscribe to some event
     */
    subscribeOnce = (event: any, callback: any) => {
        const handler: any = this.$rootScope.$on(event, () => {
            try {
                callback();
            } catch (err) {
                console.error("error calling callback for ", event);
            }
            //deregister the listener
            handler();
        });
    }
}


angular.module(moduleName).service('BroadcastService', BroadcastService);
