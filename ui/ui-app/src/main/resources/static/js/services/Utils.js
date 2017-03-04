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
define(['angular','services/module-name'], function (angular,moduleName) {
    angular.module(moduleName).factory('Utils', ["$timeout", function ($timeout) {

        var waitForDomRetryCounts = {};

        var data = {

            stickTabHeader: function ($element) {
                var self = this;
                var tabsWrapper = element.find('md-tabs-wrapper');
                self.stickTabHeader($element, tabsWrapper);

            },
            stickTabHeader: function ($element, $tabsWrapper) {
                var header = angular.element('.page-header');
                var headerHeight = header.height();

                var window_top = 0;
                var div_top = $element.find('.sticky-anchor').offset().top;
                if (div_top < headerHeight) {
                    if (div_top < 0) {
                        $tabsWrapper.css('top', Math.abs(div_top) + headerHeight + 'px')
                    }
                    else {
                        $tabsWrapper.css('top', Math.abs(headerHeight - Math.abs(div_top)) + 'px')
                    }
                } else {
                    $tabsWrapper.css('top', '0px')
                }
            },
            maskProfanity: function (str) {
                return str;
            },

            /**
             *
             * @param selector  element selector (i.e. #TableId)
             * @param callbackFn  // the function to execute when the selector element is found in the DOM
             */
            waitForDomElementReady: function (selector, callbackFn) {
                if (waitForDomRetryCounts[selector] == undefined) {
                    waitForDomRetryCounts[selector] = 0;
                }
                var $ele = angular.element(selector);
                if ($ele.length > 0) {
                    delete waitForDomRetryCounts[selector];
                    callbackFn();
                }
                else {

                    waitForDomRetryCounts[selector] += 1;
                    if (waitForDomRetryCounts[selector] <= 50) {
                        $timeout(data.waitForDomElementReady, 5, false, selector, callbackFn);
                    }
                }
            },

            strEndsWith: function (str, suffix) {
                return str.indexOf(suffix, str.length - suffix.length) !== -1;
            },
            endsWith: function (str, suffix) {
                return this.strEndsWith(str, suffix);
            },
            startsWith: function (str, prefix) {
                return str.indexOf(prefix) === 0;
            },
            camelCaseToWords: function (str) {
                return str.match(/^[a-z]+|[A-Z][a-z]*/g).map(function (x) {
                    return x[0].toUpperCase() + x.substr(1).toLowerCase();
                }).join(' ');
            },

            capitalizeFirstLetter: function (string) {
                if (string && string != '') {
                    return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();
                }
                return '';
            },

            convertDate: function (date) {
                if (date == null) {
                    return "--";
                }
                var local = new Date(date);
                local.setMinutes(local.getMinutes() - local.getTimezoneOffset());
                return local.toISOString().slice(0, 10) + " " + local.toISOString().slice(11, 19);
            },
            dateDifference: function (startDate, endDate) {
                if (startDate == null || endDate == null) {
                    return "N/A";
                }
                else {
                    var msec = endDate - startDate;
                    return this.formatTimeMinSec(msec);
                }
            },

            dateDifferenceMs: function (startDate, endDate) {
                var diff = endDate - startDate;
                return diff;
            },
            formatTimeMinSec: function (timeInMSec) {
                //       return self.TimeDiffString(timeInMSec);
                if (timeInMSec == null) {
                    return "N/A";
                }
                else {
                    if (timeInMSec < 0) {
                        return "0 sec";
                    }
                    var sec_num = timeInMSec / 1000;
                    sec_num = parseInt(sec_num, 10);
                    var hours = Math.floor(sec_num / 3600);
                    var minutes = Math.floor((sec_num - (hours * 3600)) / 60);
                    var seconds = sec_num - (hours * 3600) - (minutes * 60);

                    var str = seconds + " sec";
                    if (hours == 0) {
                        if (minutes != 0) {
                            str = minutes + " min " + str;
                        }
                    }
                    else {
                        str = hours + " hrs " + minutes + " min " + str;
                    }
                    return str;
                }

            },

            TimeDiffString: function (timeInMs) {
                var diffString = moment.duration(timeInMs).format('HH:mm:ss');
                if (diffString == '00') {
                    diffString = '0';
                }
                var parts = diffString.split(':');
                var suffixes = ['hr', 'min', 'sec'];
                var len = parts.length;
                var suffixIndex = Math.abs(len - 3);

                var startIndex = parts.length;
                var timeString = '';
                for (var i = 0; i < parts.length; i++) {
                    if (i > 0) {
                        timeString += ' ';
                    }
                    timeString += parts[i] + ' ' + suffixes[suffixIndex];
                    suffixIndex++;
                }
                return timeString;
            },
            DateDiffString: function (date1, date2) {
                var diff = date2.getTime() - date2.getTime();
                return this.TimeDiffString(diff);
            },

            getParameterByName: function (name) {
                var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
                return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
            },
            toBoolean: function (str) {
                if (typeof str == "boolean") {
                    return str;
                }
                else if (typeof str == "string") {
                    return str.toLowerCase() == "true";
                }
                else if (typeof str == "number") {
                    return str == 1;
                }
                return false;
            },

            resolveStatusClass: function (status, exitCode) {

                var statusClass = 'status-info';
                switch (status) {
                    case "STARTED":
                    case "RUNNING":
                    case "COMPLETED":
                        if (exitCode && exitCode == 'FAILED') {
                            statusClass = 'status-errors';
                        }
                        else {
                            statusClass = 'status-healthy';
                        }
                        break;
                    case "FAILED":
                        statusClass = 'status-errors';
                        break;
                    default:
                }
                return statusClass;
            }

        };
        return data;

    }]);
});