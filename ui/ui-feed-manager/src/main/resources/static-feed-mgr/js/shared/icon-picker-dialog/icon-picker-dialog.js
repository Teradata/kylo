/*-
 * #%L
 * thinkbig-ui-feed-manager
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

(function () {

    /**
     * locals: iconModel = {name:'',iconColor:'color',icon:'icon'}
     * usage in another controller:
     *
     *       this.showIconPicker= function() {

            $mdDialog.show({
                controller: 'IconPickerDialog',
                templateUrl: 'js/shared/icon-picker-dialog/icon-picker-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose:false,
                fullscreen: true,
                locals : {
                    iconModel:self.editModel
                }
            })
                .then(function(msg) {
                    if(msg) {
                        self.editModel.icon = msg.icon;
                        self.editModel.iconColor=msg.color;
                    }

                }, function() {

                });
        };
     *
     *
     *
     * @param $scope
     * @param $mdDialog
     * @param $http
     * @param iconModel
     * @constructor
     */

    function IconPickerDialog($scope, $mdDialog, $http, iconModel, RestUrlService) {

        $scope.selectedIconTile = null;
        $scope.iconTiles = [];
        $scope.iconModel = iconModel;

        $scope.selectedColorTile = null;
        $scope.colorTiles = [];
        $scope.loadingIcons = false;
        $scope.loadingColors = false;

        function fetchIcons() {
            $scope.loadingIcons = true;
            $http.get(RestUrlService.ICONS_URL).then(function (response) {

                var icons = response.data;
                angular.forEach(icons, function (icon) {
                    var tile = {title: icon};
                    $scope.iconTiles.push(tile);
                    if (iconModel.icon != null && iconModel.icon == icon) {
                        $scope.selectedIconTile = tile;
                    }
                    $scope.loadingIcons = false;
                });
            });
        }

        function fetchColors() {
            $scope.loadingColors = true;
            $http.get(RestUrlService.ICON_COLORS_URL).then(function (response) {
                var colors = response.data;
                angular.forEach(colors, function (color) {

                    var tile = {title: color.name, background: color.color};
                    $scope.colorTiles.push(tile);
                    if (iconModel.iconColor != null && iconModel.iconColor == color.color) {
                        $scope.selectedColorTile = tile;
                    }
                    $scope.loadingColors = false;
                });

                if ($scope.selectedColorTile == null) {
                    $scope.selectedColorTile = _.find($scope.colorTiles, function (c) {
                        return c.title == 'Teal';
                    })
                }
            });
        }

        fetchIcons();
        fetchColors();




        $scope.selectIcon = function(tile){
            $scope.selectedIconTile = tile;
        }
        $scope.selectColor = function(tile){
            $scope.selectedColorTile = tile;
        }
        $scope.save = function(){
            var data = {icon:$scope.selectedIconTile.title, color:$scope.selectedColorTile.background}
            $mdDialog.hide(data);
        }

        $scope.hide = function() {
            $mdDialog.hide();
        };

        $scope.cancel = function() {
            $mdDialog.cancel();
        };


    };

    angular.module(MODULE_FEED_MGR).controller('IconPickerDialog',IconPickerDialog);


}());
