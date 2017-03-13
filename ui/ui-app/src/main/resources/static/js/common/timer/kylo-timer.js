define(['angular','common/module-name'], function (angular,moduleName) {
    var directive = function ($interval) {
        return {
            restrict: "EA",
            scope: {
                startTime: "=",
                refreshTime: "@",
                truncatedFormat: '=?',
                addAgoSuffix: '=?'
            },
            link: function ($scope, element, attrs) {

                $scope.truncatedFormat = angular.isDefined($scope.truncatedFormat) ? $scope.truncatedFormat : false;
                $scope.addAgoSuffix = angular.isDefined($scope.addAgoSuffix) ? $scope.addAgoSuffix : false;

                $scope.time = $scope.startTime;
                $scope.previousDisplayStr = '';
                $scope.$watch('startTime', function (newVal, oldVal) {
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
                if ($scope.refreshTime == undefined) {
                    $scope.refreshTime = 1000;
                }
                function update() {
                    $scope.time += $scope.refreshTime;
                    //format it
                    format();

                }

                function format() {
                    var ms = $scope.time;
                    days = Math.floor(ms / (24 * 60 * 60 * 1000));
                    var daysms = ms % (24 * 60 * 60 * 1000);
                    hours = Math.floor((daysms) / (60 * 60 * 1000));
                    var hoursms = ms % (60 * 60 * 1000);
                    minutes = Math.floor((hoursms) / (60 * 1000));
                    var minutesms = ms % (60 * 1000);
                    seconds = Math.floor((minutesms) / (1000));

                    var secondsStr = '';
                    var minutesStr = '';
                    var hoursStr = '';
                    var daysStr = '';
                    var agoSuffix = $scope.addAgoSuffix ? 'ago' : '';

                    var str = seconds + ' sec ';
                    secondsStr = str;
                    var truncateFormatStr = str + agoSuffix;
                    if (hours > 0 || (hours == 0 && minutes > 0)) {
                        minutesStr = minutes + ' min ';
                        str = minutesStr + str;
                        truncateFormatStr = minutesStr + agoSuffix;
                    }
                    if (days > 0 || days == 0 && hours > 0) {
                        hoursStr = hours + ' hrs ';
                        str = hoursStr + str;
                        truncateFormatStr = hoursStr + agoSuffix;
                    }
                    if (days > 0) {
                        daysStr = days + " days ";
                        str = daysStr + str;
                        truncateFormatStr = daysStr + agoSuffix;
                    }

                    var displayStr = $scope.truncatedFormat ? truncateFormatStr : str;

                    if ($scope.previousDisplayStr == '' || $scope.previousDisplayStr != displayStr) {
                        element.html(displayStr);
                        element.attr('title', displayStr);
                    }
                    $scope.previousDisplayStr = displayStr;

                }

                var interval = $interval(update, $scope.refreshTime);

                var clearInterval = function () {
                    $interval.cancel(interval);
                    interval = null;
                }
                $scope.$on('$destroy', function () {
                    clearInterval()
                });
            }

        }
    };

    return angular.module(moduleName)
        .directive('kyloTimer', ['$interval', directive]);
});

