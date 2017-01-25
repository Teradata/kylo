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

    var directive = function (FieldPolicyRuleOptionsFactory, PolicyInputFormService) {
        return {
            restrict: "EA",
            scope: {
                ngModel: '=',
                policyParameter: '@',
                selectLabel: '@',
                defaultValue: '@'
            },
            templateUrl: 'js/shared/feed-field-policy-rules/inline-field-policy-form.html',
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

    angular.module(MODULE_FEED_MGR)
        .directive('inlineFieldPolicyForm', directive);

})();
