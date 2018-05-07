import * as angular from "angular";
import {moduleName} from "../module-name";

angular.module(moduleName).directive("verticalSectionLayout",
  [ () => {
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
                'readonly': '?readonlySection',
                'editable': '?editableSection'
            },
            templateUrl: 'js/common/vertical-section-layout/vertical-section-layout-template.html',
            link: function ($scope: any, iElem: any, iAttrs: any, ctrl: any, transcludeFn: any) {
                /**
                 * Delete button is visible if this flag is true and if the method onDelete is set
                 */
                if ($scope.isDeleteVisible == undefined) {
                    $scope.isDeleteVisible = true;
                }

                if ($scope.editable == undefined) {
                    $scope.editable = false;
                }

                if ($scope.showVerticalCheck == undefined) {
                    $scope.showVerticalCheck = true;
                }

                if ($scope.allowEdit == undefined) {
                    $scope.allowEdit = true;
                }
                if ($scope.isValid == undefined) {
                    $scope.isValid = true;
                }

                if ($scope.keepEditableAfterSave == undefined) {
                    $scope.keepEditableAfterSave = false;
                }

                $scope.edit = function (ev: any) {
                    $scope.editable = true;
                    $scope.onEdit(ev);
                }

                $scope.cancel = function (ev: any) {
                    $scope.onCancelEdit(ev);
                    $scope.editable = false;
                }

                $scope.save = function (ev: any) {
                    $scope.onSaveEdit(ev);
                    if (!$scope.keepEditableAfterSave) {
                        $scope.editable = false;
                    }
                }

                $scope.delete = function (ev: any) {
                    if ($scope.onDelete) {
                        $scope.onDelete(ev);
                    }
                }

            }
        };
  }
  ]);