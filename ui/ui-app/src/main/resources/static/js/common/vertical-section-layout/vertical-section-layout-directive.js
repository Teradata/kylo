define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    angular.module(module_name_1.moduleName).directive("verticalSectionLayout", [function () {
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
                link: function ($scope, iElem, iAttrs, ctrl, transcludeFn) {
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
                    $scope.edit = function (ev) {
                        $scope.editable = true;
                        $scope.onEdit(ev);
                    };
                    $scope.cancel = function (ev) {
                        $scope.onCancelEdit(ev);
                        $scope.editable = false;
                    };
                    $scope.save = function (ev) {
                        $scope.onSaveEdit(ev);
                        if (!$scope.keepEditableAfterSave) {
                            $scope.editable = false;
                        }
                    };
                    $scope.delete = function (ev) {
                        if ($scope.onDelete) {
                            $scope.onDelete(ev);
                        }
                    };
                }
            };
        }
    ]);
});
//# sourceMappingURL=vertical-section-layout-directive.js.map