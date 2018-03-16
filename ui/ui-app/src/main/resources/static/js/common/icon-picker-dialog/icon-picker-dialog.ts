import * as angular from "angular";
import {moduleName} from "../module-name";
import * as _ from "underscore";

export default class IconPickerDialog implements ng.IComponentController{
constructor(private $scope: any,
            private $mdDialog: any,
            private $http: any,
            private iconModel: any,
            private RestUrlService: any){
        $scope.fillStyle = {'fill':'#90A4AE'};
        $scope.selectedIconTile = null;
        $scope.iconTiles = [];
        $scope.iconModel = iconModel;

        $scope.selectedColorTile = null;
        $scope.colorTiles = [];
        $scope.loadingIcons = false;
        $scope.loadingColors = false;

        this.fetchIcons();
        this.fetchColors();

        $scope.selectIcon = function(tile: any){
            $scope.selectedIconTile = tile;
        };
        $scope.selectColor = function(tile: any){
            $scope.selectedColorTile = tile;
            $scope.fillStyle = {'fill': tile.background };
        };
        $scope.getBackgroundStyle = function(tile: any){
            return {'background-color': tile.background };
        };
        $scope.save = function(){
            var data = {icon:$scope.selectedIconTile.title, color:$scope.selectedColorTile.background};
            $mdDialog.hide(data);
        };

        $scope.hide = function() {
            $mdDialog.hide();
        };

        $scope.cancel = function() {
            $mdDialog.cancel();
        };
}

      fetchIcons=()=> {
            this.$scope.loadingIcons = true;
            this.$http.get(this.RestUrlService.ICONS_URL).then((response: any) =>{

                var icons = response.data;
                angular.forEach(icons,  (icon: any) =>{
                    var tile = {title: icon};
                    this.$scope.iconTiles.push(tile);
                    if (this.iconModel.icon !== null && this.iconModel.icon === icon) {
                        this.$scope.selectedIconTile = tile;
                    }
                });
                this.$scope.loadingIcons = false;
            });
        }

        fetchColors=()=> {
            this.$scope.loadingColors = true;
            this.$http.get(this.RestUrlService.ICON_COLORS_URL).then( (response: any) =>{
                var colors = response.data;
                angular.forEach(colors, (color: any)=>{

                    var tile = {title: color.name, background: color.color};
                    this.$scope.colorTiles.push(tile);
                    if (this.iconModel.iconColor !== null && this.iconModel.iconColor === color.color) {
                        this.$scope.selectedColorTile = tile;
                    }
                });

                if (this.$scope.selectedColorTile === null) {
                    this.$scope.selectedColorTile = _.find(this.$scope.colorTiles, (c: any)=> {
                        return c.title === 'Teal';
                    })
                }
                this.$scope.loadingColors = false;
            });
        }


}
 angular.module(moduleName).controller('IconPickerDialog',
 ["$scope","$mdDialog","$http","iconModel","RestUrlService",IconPickerDialog]);
