(function () {

    var directive = function () {
        return {
            restrict: "EA",
            scope: {
                stepIndex:'@',
                canContinue: '=',
                onClickFinalButton:'&'

            },
            require:'^thinkbigStepper',
            templateUrl: 'js/shared/stepper/step-buttons.html',
            link: function ($scope, element, attrs, stepperController) {
                if(attrs.finalStep != undefined && attrs.finalStep != null){
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
                        //stepperController.selectedStepIndex = $scope.stepIndex + 1;
                        stepperController.selectedStepIndex = $scope.step.nextActiveStepIndex;
                    }
                };
                $scope.gotoPreviousStep = function () {
                    if ($scope.showPrevious()) {
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

                var stepVisitedWatch =  $scope.$watch('step.visited', function (newVal) {
                    canContinueToNextStep();
                });


                var canContinueWatch = $scope.$watch('canContinue', function (newVal) {
                    canContinueToNextStep();
                });


                $scope.$on('$destroy',function () {
                    stepVisitedWatch();
                    canContinueWatch();
                    nextActiveStepIndexWatch();
                    previousActiveStepIndex();
                });

            }

        };
    }




    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigStepButtons', directive);

})();
