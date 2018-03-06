define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    var directive = function ($http, RestUrlService) {
        return {
            restrict: "EA",
            scope: {
                cronExpression: '='
            },
            templateUrl: 'js/feed-mgr/shared/cron-expression-preview/cron-expression-preview.html',
            link: function ($scope, element, attrs) {
                $scope.nextDates = [];
                function getNextDates() {
                    $http.get(RestUrlService.PREVIEW_CRON_EXPRESSION_URL, { params: { cronExpression: $scope.cronExpression } }).then(function (response) {
                        $scope.nextDates = response.data;
                    });
                }
                $scope.$watch('cronExpression', function (newVal) {
                    if (newVal != null && newVal != '') {
                        getNextDates();
                    }
                    else {
                        $scope.nextDates = [];
                    }
                });
                getNextDates();
            }
        };
    };
    var CronExpressionPreview = /** @class */ (function () {
        function CronExpressionPreview() {
        }
        return CronExpressionPreview;
    }());
    exports.CronExpressionPreview = CronExpressionPreview;
    angular.module(moduleName)
        .directive('cronExpressionPreview', ['$http', 'RestUrlService', directive]);
});
//# sourceMappingURL=cron-expression-preview.js.map