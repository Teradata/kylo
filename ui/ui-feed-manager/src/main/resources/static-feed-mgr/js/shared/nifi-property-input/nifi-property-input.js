(function () {

    var directive = function () {
        return {
            restrict: "EA",
            scope: {
                property:'=',
                propertyDisabled:'=?',
                onPropertyChange:'&?'
            },
            templateUrl: 'js/shared/nifi-property-input/nifi-property-input.html',
            link: function ($scope, element, attrs) {
                element.addClass('nifi-property-input layout-padding-top-bottom')
                if($scope.propertyDisabled == undefined){
                    $scope.propertyDisabled = false;
                }

                $scope.onPropertyChanged = function(){
                    if($scope.onPropertyChange != undefined){
                        $scope.onPropertyChange()($scope.property);
                    }
                }

                if( $scope.property.renderType == 'select' && $scope.property.value != null) {
                    if($scope.onPropertyChange != undefined){
                        $scope.onPropertyChange()($scope.property);
                    }
                }

                if( $scope.property.renderType == 'checkbox-custom' ) {
                    if($scope.property.renderOptions == null || $scope.property.renderOptions == undefined){
                       $scope.property.renderOptions ={};
                    }
                    var trueValue =  $scope.property.renderOptions['trueValue'];
                  if(StringUtils.isBlank(trueValue)){
                      $scope.property.renderOptions['trueValue'] = 'true';
                  }
                    var falseValue =  $scope.property.renderOptions['falseValue'];
                    if(StringUtils.isBlank(falseValue)){
                        $scope.property.renderOptions['falseValue'] = 'false';
                    }
                }

            }

        };
    }




    angular.module(MODULE_FEED_MGR)
        .directive('nifiPropertyInput', directive);

})();
