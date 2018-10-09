import "jquery";
import { Injectable } from "@angular/core";
import {Subject} from 'rxjs/Subject';

/**
 * Allow different controllers/services to subscribe and notify each other
 *
 * to subscribe include the BroadcastService in your controller and call this method:
 *  - BroadcastService.subscribe($scope, 'SOME_EVENT', someFunction);
 *
 * to notify call this:
 * -  BroadcastService.notify('SOME_EVENT,{optional data object},### optional timeout);
 */
@Injectable()
export default class BroadcastService {

    /**
    * map to check if multiple events come in for those that {@code data.notifyAfterTime}
    * to ensure multiple events are not fired.
    * @type {{}}
    */
    waitingEvents: any = {};

    subscribers = {};
    eventsMap : Map<String, Subject<any>> = new Map<String,Subject<any>>();

    constructor() {

    }
    /**
    * notify subscribers of this event passing an optional data object and optional wait time (millis)
    * @param event
    * @param data
    * @param waitTime
    */
    notify (event: any, data?: any, waitTime?: any) {
        if (waitTime == undefined) {
            waitTime = 0;
        }
        if (this.waitingEvents[event] == undefined) {
            this.waitingEvents[event] = event;
            setTimeout(() => {
                if(!this.eventsMap.has(event)){
                    this.eventsMap.set(event, new Subject<any>());
                }
                this.eventsMap.get(event).next(data);
                // this.$rootScope.$emit(event, data);
                delete this.waitingEvents[event];
            }, waitTime);
        }
    };
    /**
     * Subscribe to some event
     */
    subscribe (scope: any, event: any, callback: any) {
        // const handler: any = this.$rootScope.$on(event, callback);
        if(!this.eventsMap.has(event)){
            this.eventsMap.set(event, new Subject<any>());
        }
        var subs  = this.eventsMap.get(event).subscribe(callback);
        if(this.subscribers[event] == undefined){
            this.subscribers[event] = 0;
        }
        this.subscribers[event] +=1;
        if (scope != null) {
            scope.$on('$destroy', ()=>{
                subs.unsubscribe();
                this.subscribers[event] -= 1;
            });
        }
    };
    /**
     * Subscribe to some event
     */
    subscribeOnce (event: any, callback: any) {
        if(!this.eventsMap.has(event)){
            return;
        }
        var subs  = this.eventsMap.get(event).subscribe((nextValue : any) => {
            try{
                callback();
            }catch(err){
                console.error("error calling callback for ", event);
            }
            subs.unsubscribe();
        });
    //     const handler: any = this.$rootScope.$on(event, () => {
    //         try {
    //             callback();
    //         } catch (err) {
    //             console.error("error calling callback for ", event);
    //         }
    //         //deregister the listener
    //         handler();
    //     });
    }
}