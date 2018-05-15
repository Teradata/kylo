import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');



var directive = function (FieldPolicyRuleOptionsFactory:any, PolicyInputFormService:any) {
    return {
        restrict: "EA",
        scope: {
            ngModel: '=',
            policyParameter: '@',
            selectLabel: '@',
            defaultValue: '@'
        },
        templateUrl: 'js/feed-mgr/shared/feed-field-policy-rules/inline-field-policy-form.html',
        link: function ($scope:any, element:any, attrs:any) {
            $scope.options = [];
            $scope.field = $scope.ngModel;
            $scope.ruleMode = 'NEW'
            $scope.showAdvancedOptions = false;
            $scope.expandAdvancedOptions = false;
            $scope.policyForm = {};
            $scope.loadingPolicies = true;
            $scope.options = [];
            $scope.ruleType = null;
            $scope.skipChangeHandler = false;

            FieldPolicyRuleOptionsFactory.getOptionsForType($scope.policyParameter).then(function (response:any) {
                var currentFeedValue = null;
                var results = [];
                if (response.data) {
                    results = _.sortBy(response.data, function (r) {
                        return r.name;
                    });
                }
                $scope.options = PolicyInputFormService.groupPolicyOptions(results, currentFeedValue);
                ruleTypesAvailable();


                if ($scope.defaultValue && (angular.isUndefined($scope.field) || $scope.field == null)) {
                    var defaultOption = $scope.options.filter(function (v:any) { return (v.name == $scope.defaultValue); })
                    if (defaultOption.length > 0) {
                        $scope.ruleType = $scope.field = defaultOption[0];
                        $scope.onRuleTypeChange();
                    }
                }
                else if(angular.isDefined($scope.field)){
                    $scope.skipChangeHandler = true;
                    $scope.ruleType = $scope.field
                //    PolicyInputFormService.updatePropertyIndex(rule);
                    $scope.showAdvancedOptions = ($scope.field.properties && $scope.field.properties.length > 0);

                }

                //$scope.ngModel = $scope.field = rule;
                $scope.loadingPolicies = false;
            });

            function findRuleType(ruleName:any) {
                return _.find($scope.options, function (opt:any) {
                    return opt.name == ruleName;
                });
            }

            function ruleTypesAvailable() {
                if ($scope.field != null) {
                    $scope.ruleType = findRuleType($scope.field.name);
                }
            }

            $scope.toggleAdvancedOptions = function() {
                $scope.expandAdvancedOptions = !$scope.expandAdvancedOptions;
            }


            $scope.onRuleTypeChange = function () {
                $scope.expandAdvancedOptions = false;
                $scope.showAdvancedOptions = false;
                if ($scope.ruleType != null) {
                    if( !$scope.skipChangeHandler) {
                        var rule = angular.copy($scope.ruleType);
                        rule.groups = PolicyInputFormService.groupProperties(rule);
                        PolicyInputFormService.updatePropertyIndex(rule);
                        //make all rules editable
                        rule.editable = true;
                        $scope.ngModel = $scope.field = rule;

                    }
                    $scope.showAdvancedOptions = ($scope.ruleType.properties && $scope.ruleType.properties.length > 0);
                    $scope.skipChangeHandler = false;
                }
                else {
                    $scope.field = null;
                }
            }

            function validateForm() {
                var validForm = PolicyInputFormService.validateForm($scope.policyForm, $scope.field.properties, false);
                return validForm;
            }

        }
    }

}

angular.module(moduleName)
    .directive('inlineFieldPolicyForm',["FieldPolicyRuleOptionsFactory", "PolicyInputFormService", directive]);


