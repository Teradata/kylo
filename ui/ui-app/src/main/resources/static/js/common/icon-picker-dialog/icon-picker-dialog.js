define(["require", "exports", "angular", "../module-name", "underscore"], function (require, exports, angular, module_name_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var IconPickerDialog = /** @class */ (function () {
        function IconPickerDialog($scope, $mdDialog, $http, iconModel, RestUrlService) {
            var _this = this;
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.$http = $http;
            this.iconModel = iconModel;
            this.RestUrlService = RestUrlService;
            this.fetchIcons = function () {
                _this.$scope.loadingIcons = true;
                _this.$http.get(_this.RestUrlService.ICONS_URL).then(function (response) {
                    var icons = response.data;
                    angular.forEach(icons, function (icon) {
                        var tile = { title: icon };
                        _this.$scope.iconTiles.push(tile);
                        if (_this.iconModel.icon !== null && _this.iconModel.icon === icon) {
                            _this.$scope.selectedIconTile = tile;
                        }
                    });
                    _this.$scope.loadingIcons = false;
                });
            };
            this.fetchColors = function () {
                _this.$scope.loadingColors = true;
                _this.$http.get(_this.RestUrlService.ICON_COLORS_URL).then(function (response) {
                    var colors = response.data;
                    angular.forEach(colors, function (color) {
                        var tile = { title: color.name, background: color.color };
                        _this.$scope.colorTiles.push(tile);
                        if (_this.iconModel.iconColor !== null && _this.iconModel.iconColor === color.color) {
                            _this.$scope.selectedColorTile = tile;
                        }
                    });
                    if (_this.$scope.selectedColorTile === null) {
                        _this.$scope.selectedColorTile = _.find(_this.$scope.colorTiles, function (c) {
                            return c.title === 'Teal';
                        });
                    }
                    _this.$scope.loadingColors = false;
                });
            };
            $scope.fillStyle = { 'fill': '#90A4AE' };
            $scope.selectedIconTile = null;
            $scope.iconTiles = [];
            $scope.iconModel = iconModel;
            $scope.selectedColorTile = null;
            $scope.colorTiles = [];
            $scope.loadingIcons = false;
            $scope.loadingColors = false;
            this.fetchIcons();
            this.fetchColors();
            $scope.selectIcon = function (tile) {
                $scope.selectedIconTile = tile;
            };
            $scope.selectColor = function (tile) {
                $scope.selectedColorTile = tile;
                $scope.fillStyle = { 'fill': tile.background };
            };
            $scope.getBackgroundStyle = function (tile) {
                return { 'background-color': tile.background };
            };
            $scope.save = function () {
                var data = { icon: $scope.selectedIconTile.title, color: $scope.selectedColorTile.background };
                $mdDialog.hide(data);
            };
            $scope.hide = function () {
                $mdDialog.hide();
            };
            $scope.cancel = function () {
                $mdDialog.cancel();
            };
        }
        return IconPickerDialog;
    }());
    exports.default = IconPickerDialog;
    angular.module(module_name_1.moduleName).controller('IconPickerDialog', ["$scope", "$mdDialog", "$http", "iconModel", "RestUrlService", IconPickerDialog]);
});
//# sourceMappingURL=icon-picker-dialog.js.map