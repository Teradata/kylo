define(["require", "exports", "angular", "./module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $transition$) {
            this.$scope = $scope;
            this.$transition$ = $transition$;
            this.feedName = $transition$.params().feedName;
        }
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName).controller('FeedStatsController', ["$scope", "$transition$", controller]);
});
//# sourceMappingURL=feed-stats.js.map