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
        template: "<span>{{currentTimeUtc}}</span>",
        link: function (scope, element, attrs) {

            if (scope.dateFormat == null) {
                scope.dateFormat = 'MMM d, yyyy HH:mm:ss';
            }

            function getTime() {
                $http.get('/proxy/v1/configuration/system-time').then(function (response) {
                    scope.currentTimeUtc = $filter('date')(response.data, scope.dateFormat)
                });
            }

            getTime();
            if (scope.refreshInterval == null) {
                scope.refreshInterval = 5000;
            }
            if (scope.refreshInterval > 1000) {
                $interval(getTime, scope.refreshInterval);
            }
        }
    };
});
