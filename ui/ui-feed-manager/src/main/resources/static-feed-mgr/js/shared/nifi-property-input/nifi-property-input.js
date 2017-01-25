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




    angular.module(MODULE_FEED_MGR)
        .directive('nifiPropertyInput', directive);

})();
