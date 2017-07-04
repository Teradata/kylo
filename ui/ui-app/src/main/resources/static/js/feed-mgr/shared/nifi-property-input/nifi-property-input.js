define(['angular',"feed-mgr/module-name"], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            scope: {
                property:'=',
                theForm:'=',
                propertyDisabled:'=?',
                onPropertyChange:'&?'
            },
            templateUrl: 'js/feed-mgr/shared/nifi-property-input/nifi-property-input.html',
            link: function ($scope, element, attrs) {
                element.addClass('nifi-property-input layout-padding-top-bottom')
                if($scope.property.formKey == null) {
                    var formKey = $scope.property.key.split(' ').join('_')+$scope.property.processorName.split(' ').join('_')
                    $scope.property.formKey = formKey.toLowerCase();
                }
                if($scope.propertyDisabled == undefined){
                    $scope.propertyDisabled = false;
                }

                $scope.onPropertyChanged = function(){
                    if($scope.onPropertyChange != undefined){
                        $scope.onPropertyChange($scope.property);
                    }
                }

                if( $scope.property.renderType == 'select' && $scope.property.value != null) {
                    if($scope.onPropertyChange != undefined){
                        $scope.onPropertyChange($scope.property);
                    }
                }

                if( $scope.property.renderType == 'select'){
                    if($scope.property.renderOptions == null || $scope.property.renderOptions == undefined){
                        $scope.property.renderOptions ={};
                    }
                    if($scope.property.renderOptions['selectCustom'] == 'true' ) {
                    if($scope.property.renderOptions['selectOptions']){
                        $scope.property.selectOptions = angular.fromJson($scope.property.renderOptions['selectOptions']);
                    }
                    else {
                        $scope.property.selectOptions = [];
                    }
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




    angular.module(moduleName)
        .directive('nifiPropertyInput', directive);

});
