/**
 * Allow different controllers/services to subscribe and notify each other
 *
 * to subscribe include the BroadcastService in your controller and call this method:
 *  - BroadcastService.subscribe($scope, 'SOME_EVENT', someFunction);
 *
 * to notify call this:
 * -  BroadcastService.notify('SOME_EVENT,{optional data object},### optional timeout);
 */
import * as angular from 'angular';
import {moduleName} from './module-name';
import "jquery";

export default class BroadcastService{
constructor (private $rootScope: any,
             private $timeout: any){
        /**
         * map to check if multiple events come in for those that {@code data.notifyAfterTime}
         * to ensure multiple events are not fired.
         * @type {{}}
         */
        var waitingEvents: any = {};

        var data: any = {
            /**
             * notify subscribers of this event passing an optional data object and optional wait time (millis)
             * @param event
             * @param data
             * @param waitTime
             */
            notify: function (event: any, data: any, waitTime: any) {
                if (waitTime == undefined) {
                    waitTime = 0;
                }
                if (waitingEvents[event] == undefined) {
                    waitingEvents[event] = event;
                    $timeout(function () {
                        $rootScope.$emit(event, data);
                        delete waitingEvents[event];
                    }, waitTime);
                }
            },
            /**
             * Subscribe to some event
             * @param scope
             * @param event
             * @param callback
             */
            subscribe: function (scope: any, event: any, callback: any) {
                var handler: any = $rootScope.$on(event, callback);
                scope.$on('$destroy', handler);
            },
            /**
             * Subscribe to some event
             * @param scope
             * @param event
             * @param callback
             */
            subscribeOnce: function (event: any, callback: any) {
                var handler: any = $rootScope.$on(event, function() {
                   try{
                       callback();
                   } catch(err){
                       console.error("error calling callback for ",event);
                   }
                   //deregister the listener
                    handler();
                });
            }

        }
        return data;
             }
}


angular.module(moduleName).factory('BroadcastService', ["$rootScope", "$timeout",BroadcastService]);