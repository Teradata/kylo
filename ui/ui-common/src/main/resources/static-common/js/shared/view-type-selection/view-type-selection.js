/*-
 * #%L
 * thinkbig-ui-common
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

angular.module(COMMON_APP_MODULE_NAME).directive('tbaViewTypeSelection', function() {
    return {
        restrict: 'E',
        templateUrl: 'js/shared/view-type-selection/view-type-selection-template.html',
        scope: {
        viewType:'='
        },
        link: function($scope, elem, attr) {

            $scope.viewTypeChanged = function(viewType)
            {
                $scope.viewType = viewType;
             /*   if($scope.onViewTypeChanged){
                    $scope.onViewTypeChanged()(viewType);
                }
                */
            }

        }
    }
});
