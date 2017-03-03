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
define(['angular','common/module-name'], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            scope: {
                stepIndex: '@',
                canContinue: '=',
                onClickFinalButton: '&',
                beforeNext: '&?beforeNextStep',
                beforePrevious: '&?beforePreviousStep'

            },
            require: '^thinkbigStepper',
            templateUrl: 'js/common/stepper/step-buttons.html',
            link: function ($scope, element, attrs, stepperController) {
                if (attrs.finalStep != undefined && attrs.finalStep != null) {
                    $scope.finalStep = true;
                    $scope.finalStepText = attrs.finalStep;
                }
                else {
                    $scope.finalStep = false;
                }
                $scope.stepIndex = parseInt($scope.stepIndex);
                var self = this;

                if ($scope.canContinue == undefined || $scope.canContinue == null) {
                    $scope.canContinue = true;
                }
                $scope.nextActiveStep = stepperController.nextActiveStep($scope.stepIndex);
                $scope.previousActiveStep = stepperController.previousActiveStep($scope.stepIndex);

                $scope.step = stepperController.getStep($scope.stepIndex);

                if ($scope.step.nextActiveStepIndex != null) {
                    var nextStep = stepperController.getStep($scope.step.nextActiveStepIndex);
                    $scope.nextStepNumber = nextStep.number;
                }

                $scope.gotoNextStep = function () {
                    if ($scope.showNext()) {
                        if ($scope.beforeNext && angular.isFunction($scope.beforeNext)) {
                            $scope.beforeNext()($scope.stepIndex, $scope.step.nextActiveStepIndex);
                        }
                        //stepperController.selectedStepIndex = $scope.stepIndex + 1;
                        stepperController.selectedStepIndex = $scope.step.nextActiveStepIndex;
                    }
                };
                $scope.gotoPreviousStep = function () {
                    if ($scope.showPrevious()) {
                        if ($scope.beforePrevious && angular.isFunction($scope.beforePrevious)) {
                            $scope.beforePrevious()($scope.stepIndex, $scope.step.previousActiveStepIndex);
                        }
                        //stepperController.selectedStepIndex =$scope.stepIndex - 1;
                        stepperController.selectedStepIndex = $scope.step.previousActiveStepIndex;
                    }
                };
                $scope.showPrevious = function () {

                    return $scope.stepIndex != 0 && $scope.step.previousActiveStepIndex != null;
                }

                $scope.showNext = function () {
                    return $scope.finalStep == false && $scope.stepIndex < (stepperController.totalSteps - 1) && $scope.step.nextActiveStepIndex != null;
                }

                $scope.showCancel = function () {
                    return stepperController.showCancel() && $scope.stepIndex == 0;
                }

                $scope.cancelStepper = function () {
                    stepperController.cancelStepper();
                }

                $scope.finalButtonClick = function () {
                    if ($scope.finalStep == true && $scope.onClickFinalButton) {
                        $scope.onClickFinalButton();
                    }
                }

                var nextActiveStepIndexWatch = $scope.$watch('step.nextActiveStepIndex', function (newVal) {

                    if (newVal != null) {
                        $scope.nextStepNumber = stepperController.getStep(newVal).number;
                    }
                });

                var previousActiveStepIndex = $scope.$watch('step.previousActiveStepIndex', function (newVal) {

                    if (newVal != null) {
                        $scope.previousStepNumber = stepperController.getStep(newVal).number;
                    }
                });

                function canContinueToNextStep() {
                    //You can only continue if you are valid and if you have visited the step.
                    if ($scope.canContinue && $scope.step.visited == true) {
                        stepperController.stepEnabled($scope.step.nextActiveStepIndex);
                        stepperController.completeStep($scope.step.index);
                    }
                    else {
                        stepperController.stepDisabled($scope.step.nextActiveStepIndex);
                        stepperController.incompleteStep($scope.step.index);
                    }
                };

                var stepVisitedWatch = $scope.$watch('step.visited', function (newVal) {
                    canContinueToNextStep();
                });

                var canContinueWatch = $scope.$watch('canContinue', function (newVal) {
                    canContinueToNextStep();
                });

                $scope.$on('$destroy', function () {
                    stepVisitedWatch();
                    canContinueWatch();
                    nextActiveStepIndexWatch();
                    previousActiveStepIndex();
                });

            }

        };
    }

    angular.module(moduleName)
        .directive('thinkbigStepButtons', directive);

});
