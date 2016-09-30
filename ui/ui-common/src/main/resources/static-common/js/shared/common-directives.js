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
        template: "<span>{{currentTimeUtc}} UTC</span>",
        link: function (scope, element, attrs) {

            if (scope.dateFormat == null) {
                scope.dateFormat = 'MMM d, yyyy HH:mm:ss';
            }

            function getTime() {
                $http.get('/proxy/v1/configuration/system-time').then(function (response) {
                    scope.currentTimeUtc = $filter('date')(response.data, scope.dateFormat, 'UTC/GMT ')
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