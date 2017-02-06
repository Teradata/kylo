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
(function () {

    var directive = function ($interval) {
        return {
            restrict: "EA",
            scope: {
               startTime:"=",
               refreshTime:"@"
            },
            link: function ($scope, element, attrs) {

                //flag if true it will truncate output
                //if hours it will show hours/min (remove sec)
                //if days it will show days/hrs  (remove min)

                var truncateFormat = true;


                $scope.time = $scope.startTime;
                $scope.previousDisplayStr = '';
                $scope.$watch('startTime',function(newVal,oldVal){
                    $scope.time = $scope.startTime;
                    format();
                })
                format();
                var seconds = 0;
                var minutes = 0;
                var hours = 0;
                var days = 0;
                var months = 0;
                var years = 0;
                if($scope.refreshTime == undefined){
                    $scope.refreshTime = 1000;
                }
                function update() {
                    $scope.time +=$scope.refreshTime;
                    //format it
                    format();

                }

                function format() {
                    var ms = $scope.time;
                    days = Math.floor(ms / (24*60*60*1000));
                    var daysms = ms % (24 * 60 * 60 * 1000);
                    hours = Math.floor((daysms)/(60*60*1000));
                    var hoursms = ms % (60 * 60 * 1000);
                    minutes = Math.floor((hoursms)/(60*1000));
                    var minutesms = ms % (60 * 1000);
                    seconds = Math.floor((minutesms) / (1000));

                    var secondsStr = '';
                    var minutesStr = '';
                    var hoursStr = '';
                    var daysStr = '';

                    var str = seconds + ' sec';
                    secondsStr = str;
                    var truncateFormatStr = str + 'ago';
                    if (hours > 0 || (hours == 0 && minutes > 0)) {
                        minutesStr = minutes + ' min ';
                        str = minutesStr + str;
                        truncateFormatStr = minutesStr + 'ago';
                    }
                    if (days > 0 || days == 0 && hours > 0) {
                        hoursStr = hours + ' hrs ';
                        str = hoursStr + str;
                        truncateFormatStr = hoursStr + " ago";
                    }
                    if (days > 0) {
                        daysStr = days + " days ";
                        str = daysStr + str;
                        truncateFormatStr = daysStr + " ago";
                    }

                    var displayStr = truncateFormat ? truncateFormatStr : str;

                    if ($scope.previousDisplayStr == '' || $scope.previousDisplayStr != displayStr) {
                        element.html(displayStr);
                        element.attr('title', displayStr);
                    }
                    $scope.previousDisplayStr = displayStr;

                }


                var interval = $interval(update, $scope.refreshTime);

                var clearInterval = function() {
                    $interval.cancel(interval);
                   interval = null;
                }
                $scope.$on('$destroy', function() {
                    clearInterval()
                });
            }

        }
    };


    angular.module(COMMON_APP_MODULE_NAME)
        .directive('tbaTimer', ['$interval', directive]);

}());
