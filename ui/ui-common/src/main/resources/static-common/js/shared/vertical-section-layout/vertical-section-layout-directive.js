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

angular.module(COMMON_APP_MODULE_NAME).directive("verticalSectionLayout", function()  {
    return {
        restrict: 'E',
        scope: {
            showVerticalCheck: '=?',
            allowEdit: '=?',
            sectionTitle: '@',
            formName: '@',
            onDelete: '&?',
            isDeleteVisible: '=?',
            allowDelete: '=?',
            onEdit: '&',
            onSaveEdit: '&',
            onCancelEdit: '&',
            editable: '=?',
            keepEditableAfterSave: '=?',
            isValid: '=?',
            theForm: '=?'
        },
        transclude: {
            'readonly':'?readonlySection',
            'editable':'?editableSection'
        },
        templateUrl:'js/shared/vertical-section-layout/vertical-section-layout-template.html',
        link: function ($scope, iElem, iAttrs, ctrl, transcludeFn) {
            /**
             * Delete button is visible if this flag is true and if the method onDelete is set
             */
            if ($scope.isDeleteVisible == undefined) {
                $scope.isDeleteVisible = true;
            }

             if($scope.editable == undefined ) {
                 $scope.editable = false;
            }

            if($scope.showVerticalCheck == undefined ){
                $scope.showVerticalCheck = true;
            }

            if($scope.allowEdit == undefined ){
                $scope.allowEdit = true;
            }
            if ($scope.isValid == undefined) {
                $scope.isValid = true;
            }

            if($scope.keepEditableAfterSave == undefined){
                $scope.keepEditableAfterSave = false;
            }

            $scope.edit = function(ev){
                $scope.editable = true;
                $scope.onEdit(ev);
            }

            $scope.cancel = function(ev){
                $scope.onCancelEdit(ev);
                $scope.editable = false;
            }

            $scope.save = function(ev){
                $scope.onSaveEdit(ev);
                if(!$scope.keepEditableAfterSave) {
                    $scope.editable = false;
                }
            }

            $scope.delete = function(ev) {
                if($scope.onDelete){
                    $scope.onDelete(ev);
                }
            }




        }
    };
});
