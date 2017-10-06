define(['angular','common/module-name'], function (angular,moduleName) {
    angular.module(moduleName).filter('words', function () {
        return function (input, words) {
            if (isNaN(words)) {
                return input;
            }
            if (words <= 0) {
                return '';
            }
            if (input) {
                var inputWords = input.split(/\s+/);
                if (inputWords.length > words) {
                    input = inputWords.slice(0, words).join(' ') + '\u2026';
                }
            }
            return input;
        };
    });

//previously nifiTimeUnit

    angular.module(moduleName).filter('timeAsSeconds', function () {

        return function (seconds) {

            //convert back and forth to seconds
            var SECONDS_PER_YEAR = 31536000;
            var SECONDS_PER_MONTH = 2592000;
            var SECONDS_PER_WEEK = 604800;
            var SECONDS_PER_DAY = 86400;
            var SECONDS_PER_HOUR = 3600;
            var SECONDS_PER_MINUTE = 60;
            var timePeriod = '';
            var units = '';
            seconds = seconds.substring(0, seconds.indexOf("sec"));
            if (!isNaN(seconds)) {
                seconds = parseInt(seconds);
            }

            var numyears = Math.floor(seconds / SECONDS_PER_YEAR);
            var nummonths = Math.floor((seconds % SECONDS_PER_YEAR) / SECONDS_PER_MONTH);
            var numweeks = Math.floor(((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) / SECONDS_PER_WEEK);
            var numdays = Math.floor((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) / SECONDS_PER_DAY);
            var numhours = Math.floor(((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) % SECONDS_PER_DAY) / SECONDS_PER_HOUR);
            var numminutes = Math.floor((((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) % SECONDS_PER_DAY) % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
            var numseconds = ((((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) % SECONDS_PER_DAY) % SECONDS_PER_HOUR) % SECONDS_PER_MINUTE);

            if (numyears > 0) {
                timePeriod = "years";
                units = numyears;
            } else if (nummonths > 0) {
                timePeriod = "months";
                units = nummonths;
            } else if (numweeks > 0) {
                timePeriod = "weeks";
                units = numweeks;
            }
            else if (numdays > 0) {
                timePeriod = "days";
                units = numdays;
            }
            else if (numhours > 0) {
                timePeriod = "hours";
                units = numhours;
            }
            else if (numminutes > 0) {
                timePeriod = "minutes";
                units = numminutes;
            } else if (numseconds > 0) {
                timePeriod = "seconds";
                units = numseconds;
            }
            return units + " " + timePeriod;

        }
    });

    angular.module(moduleName).filter('characters', function () {
        return function (input, chars) {
            if (input == null || input == undefined) {
                input = '';
            }
            if (isNaN(chars)) {
                return input;
            }
            if (chars) {
                if (input.length > chars) {
                    input = input.substring(0, chars) + "...";
                }
            }
            return input;
        };
    });

    angular.module(moduleName).filter('maskProfanity', ['Utils', function (Utils) {
        return function (input) {
            return Utils.maskProfanity(input)
        };
    }]);

    /**
     * Filters out all duplicate items from an array by checking the specified key
     * @param [key] {string} the name of the attribute of each object to compare for uniqueness
     if the key is empty, the entire object will be compared
     if the key === false then no filtering will be performed
     * @return {array}
     */
    angular.module(moduleName).filter('unique', function () {

        return function (items, filterOn) {

            if (filterOn === false) {
                return items;
            }

            if ((filterOn || angular.isUndefined(filterOn)) && angular.isArray(items)) {
                var hashCheck = {}, newItems = [];

                var extractValueToCompare = function (item) {
                    if (angular.isObject(item) && angular.isString(filterOn)) {
                        return item[filterOn];
                    } else {
                        return item;
                    }
                };

                angular.forEach(items, function (item) {
                    var valueToCheck, isDuplicate = false;

                    for (var i = 0; i < newItems.length; i++) {
                        if (angular.equals(extractValueToCompare(newItems[i]), extractValueToCompare(item))) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (!isDuplicate) {
                        newItems.push(item);
                    }

                });
                items = newItems;
            }
            return items;
        };
    });

    angular
        .module(moduleName)
        .filter('trust', [
            '$sce',
            function ($sce) {
                return function (value, type) {
                    // Defaults to treating trusted text as `html`
                    return $sce.trustAs(type || 'html', text);
                }
            }
        ])
    ;

    angular.module(moduleName).filter('highlight', ["$sce",function ($sce) {
        return function (text, phrase) {
            if (phrase) text = text.replace(new RegExp('(' + phrase + ')', 'gi'),
                '<span class="highlighted">$1</span>')

            return $sce.trustAsHtml(text)
        }
    }]);

    angular.module(moduleName).filter('join', function () {
        return function (arr, joinChar, childProperty) {

            if (joinChar === undefined) {
                joinChar = ',';
            }

            if (angular.isArray(arr)) {
                if (!childProperty) {
                    return arr.join(joinChar);
                }
                else {
                    return _.map(arr, function (item) {
                        return item[childProperty];
                    }).join(joinChar);
                }
            }
            return arr;
        }
    });

    angular.module(moduleName).filter('moment', function () {
        return function (timeInMs) {
            return moment(timeInMs).fromNow();
        };
    });

    angular.module(moduleName).filter('time', ['Utils', function (Utils) {
        return function (timeInMs) {
            var time = Utils.formatTimeMinSec(timeInMs);
            return time;
        };
    }]);


    angular.module(moduleName).filter('timeWithMillis', ['Utils', function (Utils) {
        return function (timeInMs) {
            if(angular.isDefined(timeInMs)) {
                return moment.utc(timeInMs).format("mm:ss.SSS");
            }
            return '';
        };
    }]);

    angular.module(moduleName).filter('titleCase', [function() {
        return function(input) {
            input = input || '';
            return input.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
        };
    }]);





});