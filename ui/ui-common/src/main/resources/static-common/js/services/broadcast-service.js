/*-
 * #%L
 * thinkbig-ui-common
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
/**
 * Allow different controllers/services to subscribe and notify each other
 *
 * to subscribe include the BroadcastService in your controller and call this method:
 *  - BroadcastService.subscribe($scope, 'SOME_EVENT', someFunction);
 *
 * to notify call this:
 * -  BroadcastService.notify('SOME_EVENT,{optional data object},### optional timeout);
 */
angular.module(COMMON_APP_MODULE_NAME).factory('BroadcastService', function ($rootScope, $timeout) {
    /**
     * map to check if multiple events come in for those that {@code data.notifyAfterTime}
     * to ensure multiple events are not fired.
     * @type {{}}
     */
    var waitingEvents = {};

    var data = {
        /**
         * notify subscribers of this event passing an optional data object and optional wait time (millis)
         * @param event
         * @param data
         * @param waitTime
         */
        notify: function (event, data, waitTime) {
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
        subscribe: function(scope, event, callback) {
            var handler = $rootScope.$on(event, callback);
            scope.$on('$destroy', handler);
        }

    }
    return data;
});

var BroadcastConstants = (function () {
    function BroadcastConstants() {
    }

    BroadcastConstants.CONTENT_WINDOW_RESIZED = 'CONTENT_WINDOW_RESIZED';

    return BroadcastConstants;
})();

