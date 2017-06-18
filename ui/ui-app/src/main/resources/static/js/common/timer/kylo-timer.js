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
                    var displayStr = DateTimeUtils.formatMillisAsText(ms,$scope.truncatedFormat,false);
                    if($scope.addAgoSuffix) {
                        displayStr += " ago";
                    }

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

