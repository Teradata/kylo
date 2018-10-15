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
import "pascalprecht.translate";
import * as moment from "moment";
import * as $ from "jquery";
import { Injectable } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";

@Injectable()
export default class Utils {
    waitForDomRetryCounts: any = {};
    constructor(private translate: TranslateService) {
    }
    stickTabHeader($element: any) {
        var tabsWrapper: any = $element.find('md-tabs-wrapper');
        this.stickTabHeader2($element, tabsWrapper);
    };
    stickTabHeader2($element: any, $tabsWrapper: any) {
        var header: any = $('.page-header');
        var headerHeight: any = header.height();

        var window_top: any = 0;
        var div_top: any = $element.find('.sticky-anchor').offset().top;
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
    };
    maskProfanity(str: any) {
        return str;
    };

    /**
     *
     * @param selector  element selector (i.e. #TableId)
     * @param callbackFn  // the function to execute when the selector element is found in the DOM
     */
    waitForDomElementReady(selector: any, callbackFn: any) {
        if (this.waitForDomRetryCounts[selector] == undefined) {
            this.waitForDomRetryCounts[selector] = 0;
        }
        var $ele: any = $(selector);
        if ($ele.length > 0) {
            delete this.waitForDomRetryCounts[selector];
            callbackFn();
        }
        else {

            this.waitForDomRetryCounts[selector] += 1;
            if (this.waitForDomRetryCounts[selector] <= 50) {
                setTimeout(() => {this.waitForDomElementReady}, 5, false, selector, callbackFn);
            }
        }
    };

    strEndsWith(str: any, suffix: any) {
        return str.indexOf(suffix, str.length - suffix.length) !== -1;
    };
    endsWith(str: any, suffix: any) {
        return this.strEndsWith(str, suffix);
    };
    startsWith(str: any, prefix: any) {
        return str.indexOf(prefix) === 0;
    };
    camelCaseToWords(str: any) {
        return str.match(/^[a-z]+|[A-Z][a-z]*/g).map(function (x: any) {
            return x[0].toUpperCase() + x.substr(1).toLowerCase();
        }).join(' ');
    };

    capitalizeFirstLetter(string: any) {
        if (string && string != '') {
            return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();
        }
        return '';
    };

    convertDate(date: any) {
        if (date == null) {
            return "--";
        }
        var local: any = new Date(date);
        local.setMinutes(local.getMinutes() - local.getTimezoneOffset());
        return local.toISOString().slice(0, 10) + " " + local.toISOString().slice(11, 19);
    };
    dateDifference(startDate: any, endDate: any) {
        if (startDate == null || endDate == null) {
            return "N/A";
        }
        else {
            var msec: any = endDate - startDate;
            return this.formatTimeMinSec(msec);
        }
    };

    dateDifferenceMs(startDate: any, endDate: any) {
        var diff: any = endDate - startDate;
        return diff;
    };
    formatTimeMinSec(timeInMSec: any) {
        //       return this.TimeDiffString(timeInMSec);
        if (timeInMSec == null) {
            return "N/A";
        }
        else {
            if (timeInMSec < 0) {
                return "0 sec";
            }
            var sec_num: any = timeInMSec / 1000;
            sec_num = parseInt(sec_num, 10);
            var hours: any = Math.floor(sec_num / 3600);
            var minutes: any = Math.floor((sec_num - (hours * 3600)) / 60);
            var seconds: any = sec_num - (hours * 3600) - (minutes * 60);

            var str: any = seconds + " " + this.translate.instant('views.Utils.sec');
            if (hours == 0) {
                if (minutes != 0) {
                    str = minutes + " " + this.translate.instant('views.Utils.min') + " " + str;
                }
            }
            else {
                str = hours + " " + this.translate.instant('views.Utils.hrs') + " " + minutes + " " + this.translate.instant('views.Utils.min') + " " + str;
            }
            return str;
        }

    };

    TimeDiffString(timeInMs: any) {
        var diffString: any = moment.utc(timeInMs).format('HH:mm:ss'); // duration
        if (diffString == '00') {
            diffString = '0';
        }
        var parts: any = diffString.split(':');
        var suffixes: any = ['hr', 'min', 'sec'];
        var len: any = parts.length;
        var suffixIndex: any = Math.abs(len - 3);

        var startIndex: any = parts.length;
        var timeString: any = '';
        for (var i = 0; i < parts.length; i++) {
            if (i > 0) {
                timeString += ' ';
            }
            timeString += parts[i] + ' ' + suffixes[suffixIndex];
            suffixIndex++;
        }
        return timeString;
    };
    DateDiffString(date1: any, date2: any) {
        var diff: any = date2.getTime() - date2.getTime();
        return this.TimeDiffString(diff);
    };

    getParameterByName(name: any) {
        var match: any = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
        return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
    };
    toBoolean(str: any) {
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
    };

    resolveStatusClass(status: any, exitCode: any) {

        var statusClass: any = 'status-info';
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
    };
}