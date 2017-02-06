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
angular.module(COMMON_APP_MODULE_NAME).directive('stringToNumber', function() {
    return {
        require: 'ngModel',
        link: function(scope, element, attrs, ngModel) {
            ngModel.$parsers.push(function(value) {
                return '' + value;
            });
            ngModel.$formatters.push(function(value) {
                return parseFloat(value, 10);
            });
        }
    };
});

angular.module(COMMON_APP_MODULE_NAME).directive('currentTime', function ($http, $interval, $filter) {
    return {
        restrict: 'EA',
        scope: {
            dateFormat: '@',
            refreshInterval: '@'
        },
        template: "<span>{{currentTime}}</span>",
        link: function (scope, element, attrs) {

            /** query the server for its time on this interval... in between use javascript offset **/
            var fullRefreshInterval = 60000; // refresh query the end point every 60 sec

            var lastFullRefresh = null;
            var currentTimeInMillis = null;


            if (scope.dateFormat == null) {
                scope.dateFormat = 'MMM d, yyyy HH:mm:ss';
            }

            function setTimeUsingJavaScript() {
                if (currentTimeInMillis != null) {
                    //diff
                    var millisDiff = new Date().getTime() - currentTimeInMillis;
                    // add these millis to the systemTime
                    currentTimeInMillis = currentTimeInMillis + millisDiff;
                    scope.currentTime = $filter('date')(currentTimeInMillis, scope.dateFormat)
                }
            }

            /**
             *  set the currentTime
             */
            function setTime() {
                if (shouldPerformFullRefresh()) {
                    lastFullRefresh = new Date().getTime();
                    refreshTime();
                }
                else {
                    setTimeUsingJavaScript();
                }
            }

            function refreshTime() {
                $http.get('/proxy/v1/configuration/system-time').then(function (response) {
                    scope.currentTime = $filter('date')(response.data, scope.dateFormat)
                    currentTimeInMillis = response.data;
                });
            }

            /**
             * should it query to the server again?
             * @returns {boolean}
             */
            function shouldPerformFullRefresh() {
                return lastFullRefresh == null || (new Date().getTime() - lastFullRefresh > fullRefreshInterval);
            }

            setTime();
            if (scope.refreshInterval == null) {
                scope.refreshInterval = 5000;
            }
            if (scope.refreshInterval > 1000) {
                $interval(setTime, scope.refreshInterval);
            }
        }
    };
});
