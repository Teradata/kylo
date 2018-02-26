define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $mdDialog, $http) {
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.$http = $http;
            $http({
                method: "GET",
                url: "/proxy/v1/about/version"
            }).then(function callSuccess(response) {
                $scope.version = response.data;
            }, function callFailure(response) {
                $scope.version = "Not Available";
            });
            $scope.hide = function () {
                $mdDialog.hide();
            };
            $scope.cancel = function () {
                $mdDialog.cancel();
            };
        }
        return controller;
    }());
    exports.default = controller;
    var AboutKyloService = /** @class */ (function () {
        function AboutKyloService($mdDialog) {
            var _this = this;
            this.$mdDialog = $mdDialog;
            this.showAboutDialog = function () {
                _this.$mdDialog.show({
                    controller: 'AboutKyloDialogController',
                    templateUrl: 'js/common/about-kylo/about.html',
                    parent: angular.element(document.body),
                    clickOutsideToClose: false,
                    escapeToClose: true,
                    fullscreen: false,
                    locals: {}
                }).then(function (msg) {
                    //callback (success)
                }, function () {
                    //callback (failure)
                });
            };
        }
        return AboutKyloService;
    }());
    exports.AboutKyloService = AboutKyloService;
    angular.module(module_name_1.moduleName).service('AboutKyloService', AboutKyloService);
    angular.module(module_name_1.moduleName).controller('AboutKyloDialogController', controller);
});
//# sourceMappingURL=AboutKyloService.js.map