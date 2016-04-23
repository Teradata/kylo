
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

    function IconPickerDialog($scope, $mdDialog,  $http, iconModel ){

        $scope.selectedIconTile = null;
        $scope.iconTiles = [];
        $scope.iconModel = iconModel;

        $scope.selectedColorTile = null;
        $scope.colorTiles = [];


        var icons=['local_airport','phone_android','web','forward','star','attach_money','location_city','style','insert_chart','merge_type','local_dining','people','directions_run','traffic','format_paint','email','cloud','build','favorite','face','http','info','input','lock','message','highlight','computer','toys','security'];
        var colors = [{name:'Purple',color:'#AB47BC'},{name:'Orange',color:'#FFCA28'},{name:'Deep Orange',color:'#FF8A65'},{name:'Red',color:'#FF5252'},{name:'Blue',color:'#90CAF9'},{name:'Green',color:'#66BB6A'},{name:'Blue Grey',color:'#90A4AE'},{name:'Teal',color:'#80CBC4'},{name:'Pink',color:'#F06292'},{name:'Yellow',color:'#FFF176'}]


        angular.forEach(icons,function(icon){
            var tile = {title:icon};
            $scope.iconTiles.push(tile);
            if( iconModel.icon != null && iconModel.icon == icon){
                $scope.selectedIconTile = tile;
            }
        });

        angular.forEach(colors,function(color){
            var tile = {title:color.name,background:color.color};
            $scope.colorTiles.push(tile);
            if( iconModel.iconColor != null && iconModel.iconColor == color.color){
                $scope.selectedColorTile = tile;
            }
        });

        //set defaults

        if( $scope.selectedColorTile == null){
            $scope.selectedColorTile = _.find(colors,function(c){
                return c.name == 'Teal';
            })
        }


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