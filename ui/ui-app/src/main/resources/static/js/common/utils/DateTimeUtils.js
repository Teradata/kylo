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
var DateTimeUtils = function ($translate) {
    /**
     *
     * @param nr input to pad
     * @param n total digits
     * @param str str to pad with
     * @return {*} the padded str
     */
    function padLeft(nr, n, str) {
        return Array(n - String(nr).length + 1).join(str || '0') + nr;
    }
    function formatMillis(ms) {
        var days = Math.floor(ms / (24 * 60 * 60 * 1000));
        var daysms = ms % (24 * 60 * 60 * 1000);
        var hours = Math.floor((daysms) / (60 * 60 * 1000));
        var hoursms = ms % (60 * 60 * 1000);
        var minutes = Math.floor((hoursms) / (60 * 1000));
        var minutesms = ms % (60 * 1000);
        var seconds = Math.floor((minutesms) / (1000));
        var secondsStr = '';
        var minutesStr = '';
        var hoursStr = '';
        var daysStr = '';
        var millisStr = '';
        var str = seconds + ' ' + $translate('views.Utils.sec');
        secondsStr = str;
        var truncateFormatStr = str;
        var truncatedTimeFormat = padLeft(minutes, 2, '0') + ":" + padLeft(seconds, 2, '0');
        var timeFormat = padLeft(hours, 2, '0') + ":" + padLeft(minutes, 2, '0') + ":" + padLeft(seconds, 2, '0');
        if (seconds == 0 && minutes == 0) {
            var roundedMs = Math.ceil((minutesms / 1000) * 100) / 100;
            millisStr = roundedMs + ' ' + $translate('views.Utils.sec');
        }
        if (hours > 0 || (hours == 0 && minutes > 0)) {
            minutesStr = minutes + ' ' + $translate('views.Utils.min');
            str = minutesStr + ' ' + str;
            truncateFormatStr = minutesStr;
        }
        if (days > 0 || days == 0 && hours > 0) {
            hoursStr = hours + ' ' + $translate('views.Utils.hrs');
            str = hoursStr + ' ' + str;
            truncateFormatStr = hoursStr;
            truncatedTimeFormat = padLeft(hours, 2, '0') + ':' + truncatedTimeFormat;
        }
        if (days > 0) {
            daysStr = days + ' ' + $translate('views.Utils.days');
            str = daysStr + ' ' + str;
            truncateFormatStr = daysStr;
            truncatedTimeFormat = padLeft(days, 2, '0') + ":" + truncatedTimeFormat;
            timeFormat = padLeft(days, 2, '0') + ":" + timeFormat;
        }
        return {
            str: str,
            truncatedStr: truncateFormatStr,
            timeFormat: timeFormat,
            truncatedTimeFormat: truncatedTimeFormat,
            millisStr: millisStr != '' ? millisStr : str,
            truncatedMillisStr: millisStr != '' ? millisStr : truncatedTimeFormat,
            millisOnly: millisStr != ''
        };
    }
    ;
    function DateTimeUtils($translate) {
    }
    /**
     * Return the time as a txt string  xx days xx hrs xx min xx sec  or truncated to the nearest value
     * @param ms the time in millis
     * @param truncate true to truncate, false to not
     * @return {*}
     */
    DateTimeUtils.formatMillisAsText = function (ms, truncate, showMillis) {
        var format = formatMillis(ms);
        if (truncate) {
            return showMillis ? format.truncatedMillisStr : format.truncatedStr;
        }
        else {
            return showMillis ? format.millisStr : format.str;
        }
    };
    /**
     * Returns str is time  DD:HH:MM:SS  or truncated to the nearest value
     * @param ms  millis
     * @param truncate true to truncate, false to not
     * @return {*} str is time  DD:HH:MM:SS  or truncated to the nearest value
     */
    DateTimeUtils.formatMillisAsTime = function (ms, truncate, showMillis) {
        var format = formatMillis(ms);
        if (truncate) {
            return showMillis && format.millisOnly ? format.millisStr : format.truncatedTimeFormat;
        }
        else {
            return showMillis && format.millisOnly ? format.millisStr : format.timeFormat;
        }
    };
    return DateTimeUtils;
};
//# sourceMappingURL=DateTimeUtils.js.map