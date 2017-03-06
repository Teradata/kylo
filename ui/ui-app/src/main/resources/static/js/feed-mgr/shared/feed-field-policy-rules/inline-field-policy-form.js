define(['angular',"feed-mgr/module-name"], function (angular,moduleName) {

    var directive = function (FieldPolicyRuleOptionsFactory, PolicyInputFormService) {
        return {
            restrict: "EA",
            scope: {
                ngModel: '=',
                policyParameter: '@',
                selectLabel: '@',
                defaultValue: '@'
            },
            templateUrl: 'js/feed-mgr/shared/feed-field-policy-rules/inline-field-policy-form.html',
            link: function ($scope, element, attrs) {
                $scope.options = [];
                $scope.field = $scope.ngModel;
                $scope.ruleMode = 'NEW'
                $scope.showAdvancedOptions = false;
                $scope.expandAdvancedOptions = false;
                $scope.policyForm = {};
                $scope.loadingPolicies = true;
                $scope.options = [];
                FieldPolicyRuleOptionsFactory.getOptionsForType($scope.policyParameter).then(function (response) {
                    var currentFeedValue = null;
                    var results = [];
                    if (response.data) {
                        results = _.sortBy(response.data, function (r) {
                            return r.name;
                        });
                    }
                    $scope.options = PolicyInputFormService.groupPolicyOptions(results, currentFeedValue);
                    ruleTypesAvailable();

                    if ($scope.defaultValue) {
                        var defaultOption = $scope.options.filter(function (v) { return (v.name == $scope.defaultValue); })
                        if (defaultOption.length > 0) {
                            $scope.ruleType = $scope.field = defaultOption[0];
                            $scope.onRuleTypeChange();
                        }
                    }

                    //$scope.ngModel = $scope.field = rule;
                    $scope.loadingPolicies = false;
                });

                function findRuleType(ruleName) {
                    return _.find($scope.options, function (opt) {
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

                $scope.ruleType = null;
                $scope.onRuleTypeChange = function () {
                    $scope.expandAdvancedOptions = false;
                    $scope.showAdvancedOptions = false;
                    if ($scope.ruleType != null) {
                        var rule = angular.copy($scope.ruleType);
                        rule.groups = PolicyInputFormService.groupProperties(rule);
                        PolicyInputFormService.updatePropertyIndex(rule);
                        //make all rules editable
                        rule.editable = true;
                        $scope.ngModel = $scope.field = rule;
                        $scope.showAdvancedOptions = (rule.properties && rule.properties.length > 0);
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

});
