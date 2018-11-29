import * as angular from 'angular';
import "jquery";
import "rxjs/add/operator/filter";
import {Observable} from "rxjs/Observable";
import {Subject} from "rxjs/Subject";

import "./module"; // ensure module is loaded first
import {moduleName} from './module-name';
import {multicast} from "rxjs/operators/multicast";
import {filter} from "rxjs/operators/filter";
import { share } from "rxjs/operator/share";
import {AbstractControl} from "@angular/forms/src/model";
export interface BroadcastEvent {

    /**
     * Optional one or more arguments
     */
    args: any[];

    /**
     * Name of the event
     */
    name: string
}

/**
 * Allow different controllers/services to subscribe and notify each other
 *
 * to subscribe include the BroadcastService in your controller and call this method:
 *  - BroadcastService.subscribe($scope, 'SOME_EVENT', someFunction);
 *
 * to notify call this:
 * -  BroadcastService.notify('SOME_EVENT,{optional data object},### optional timeout);
 */
export class BroadcastService {

    private readonly bus  :{ [key: string]: Subject<BroadcastEvent>; } = {};
    //new Subject<BroadcastEvent>();

    /**
     * map to check if multiple events come in for those that {@code data.notifyAfterTime}
     * to ensure multiple events are not fired.
     * @type {{}}
     */
    waitingEvents: any = {};

    subscribers = {};

    static readonly $inject = ["$rootScope", "$timeout"];

    constructor(private $rootScope: any, private $timeout: any) {
    }

    /**
     * notify subscribers of this event passing an optional data object and optional wait time (millis)
     * @param event
     * @param data
     * @param waitTime
     */
    notify(event: any, data?: any, waitTime?: any): void {
        if (waitTime == undefined) {
            waitTime = 0;
        }
        if (this.waitingEvents[event] == undefined) {
            this.waitingEvents[event] = event;
            this.$timeout(() => {
                const subject :Subject<BroadcastEvent> = this.bus[event];
                if(subject) {
                    subject.next({args: data, name: event} as BroadcastEvent)
                }
                delete this.waitingEvents[event];
            }, waitTime);
        }
    };

    /**
     * Subscribe to some event
     */
    subscribe(event: string): Observable<BroadcastEvent>;
    subscribe(scope: angular.IScope, event: string, callback: ($event: any, ...args: any[]) => void): void;
    subscribe(eventOrScope: string | angular.IScope, event?: string, callback?: ($event: any, ...args: any[]) => void): Observable<BroadcastEvent> | void {
        let eventName = event != undefined ? event : (typeof eventOrScope === "string") ? (<string>eventOrScope) : undefined;
        if(eventName != undefined) {
            let subject = this.bus[eventName];
            if(subject == undefined){
                subject = new Subject<BroadcastEvent>();
                this.bus[eventName] = subject;
            }
            if (typeof eventOrScope === "string") {
                return subject.asObservable();
            } else {
                const subscription = subject.subscribe(event => callback(event, ...event.args));
                if (eventOrScope != null) {
                    (eventOrScope as angular.IScope).$on("$destroy", () => {

                        subscription.unsubscribe()
                    });
                }
            }
        }
        else {
            //WARN!!
            console.warn("Unable to subscribe event name is not supplied")
        }


    };

    /**
     * Subscribe to some event
     */
    subscribeOnce(event: string, callback: ($event: any, ...args: any[]) => void) {
        const subscription = this.subscribe(event).subscribe(event => {
            try {
                callback(event, ...event.args);
            } finally {
                subscription.unsubscribe();
            }
        });
    }
}


angular.module(moduleName).service('BroadcastService', BroadcastService);
