define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    var directive = function () {
        return {
            restrict: "EA",
            scope: {
                property: '=',
                theForm: '=',
                propertyDisabled: '=?',
                onPropertyChange: '&?'
            },
            templateUrl: 'js/feed-mgr/shared/nifi-property-input/nifi-property-input.html',
            link: function ($scope, element, attrs) {
                element.addClass('nifi-property-input layout-padding-top-bottom');
                if ($scope.property.formKey == null) {
                    var formKey = $scope.property.key.split(' ').join('_') + $scope.property.processorName.split(' ').join('_');
                    $scope.property.formKey = formKey.toLowerCase();
                }
                if ($scope.propertyDisabled == undefined) {
                    $scope.propertyDisabled = false;
                }
                $scope.onPropertyChanged = function () {
                    if ($scope.onPropertyChange != undefined) {
                        $scope.onPropertyChange($scope.property);
                    }
                };
                if ($scope.property.renderType == 'select' && $scope.property.value != null) {
                    if ($scope.onPropertyChange != undefined) {
                        $scope.onPropertyChange($scope.property);
                    }
                }
                if ($scope.property.renderType == 'select') {
                    if ($scope.property.renderOptions == null || $scope.property.renderOptions == undefined) {
                        $scope.property.renderOptions = {};
                    }
                    if ($scope.property.renderOptions['selectCustom'] == 'true') {
                        if ($scope.property.renderOptions['selectOptions']) {
                            $scope.property.selectOptions = angular.fromJson($scope.property.renderOptions['selectOptions']);
                        }
                        else {
                            $scope.property.selectOptions = [];
                        }
                    }
                }
                if ($scope.property.renderType == 'checkbox-custom') {
                    if ($scope.property.renderOptions == null || $scope.property.renderOptions == undefined) {
                        $scope.property.renderOptions = {};
                    }
                    var trueValue = $scope.property.renderOptions['trueValue'];
                    if (StringUtils.isBlank(trueValue)) {
                        $scope.property.renderOptions['trueValue'] = 'true';
                    }
                    var falseValue = $scope.property.renderOptions['falseValue'];
                    if (StringUtils.isBlank(falseValue)) {
                        $scope.property.renderOptions['falseValue'] = 'false';
                    }
                }
            }
        };
    };
    var emptyStringToNull = function () {
        // There is a distinction in Nifi between empty string and null, e.g. some
        // processors consider empty string invalid where null is valid. Without
        // this directive it would be impossible to set existing values to null
        return {
            restrict: 'EA',
            require: '?ngModel',
            link: function ($scope, element, attr, ngModel) {
                if (ngModel) {
                    var replaceEmptyStringWithNull = function (value) {
                        return value === '' ? null : value;
                    };
                    ngModel.$parsers.push(replaceEmptyStringWithNull);
                }
            }
        };
    };
    angular.module(moduleName).directive('emptyStringToNull', emptyStringToNull);
    angular.module(moduleName).directive('nifiPropertyInput', directive);
});
//# sourceMappingURL=nifi-property-input.js.map