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

    var directive = function ($compile, $templateRequest) {
        return {
            restrict: "EA",
            bindToController: {
                totalSteps: '@',
                stepperName: '@',
                onCancelStepper: '&?',
                showCancelButton: '@',
                coreDataModel: '=?',
                templateUrl: '@',
                selectedStepIndex: '@',
                onInitialized:'&?'
            },
            controllerAs: 'vm',
            require: ['thinkbigStepper'],
            scope: {},

            controller: "StepperController",

            compile: function (element, attrs) {
                return {
                    pre: function preLink($scope, $element, iAttrs, controller) {
                    },
                    post: function postLink($scope, $element, iAttrs, controller) {
                        $templateRequest(iAttrs.templateUrl).then(function (html) {
                            // Convert the html to an actual DOM node
                            var template = angular.element(html);
                            // Append it to the directive element
                            $element.append(template);
                            // And let Angular $compile it
                            $compile(template)($scope);
                            $element.find('md-tabs-wrapper:first').append('  <div class="step-progressbar"  style="display:block;"></div>')
                            var progressBar = $compile('<md-progress-linear md-mode="indeterminate" ng-if="vm.showProgress"></md-progress-linear>')($scope);
                            $element.find('.step-progressbar').append(progressBar)

                        });

                    }
                }
            }


        };
    }

    var controller = function ($scope,$attrs, $element, StepperService, Utils, BroadcastService, WindowUnloadService) {
        var self = this;
        this.showProgress = false;
        this.height = 80;

        /**
         * Array of all steps for the stepper
         * @type {Array}
         */
        this.steps = [];

        /**
         * Any steps to be rendered at the beginning
         * @type {Array}
         */
        this.preSteps = [];

        this.getStartingIndex = function(index){
            return index+self.preSteps.length;
        }



        function initialize() {
            $scope.templateUrl = self.templateUrl;
            $scope.stepperName = self.stepperName;
            $scope.totalSteps = self.totalSteps;

            Utils.waitForDomElementReady('md-tab-item', function () {
                $element.find('md-tab-item:not(:last)').addClass('arrow-tab')
            })



            this.previousStepIndex = null;
            if (self.stepperName == undefined || self.stepperName == '') {
                self.stepperName = StepperService.newStepperName();
            }
            StepperService.registerStepper(self.stepperName, self.totalSteps);
            self.steps = StepperService.getSteps(self.stepperName);

            //set the pre-steps
            if(angular.isDefined(self.coreDataModel) && angular.isDefined(self.coreDataModel.totalPreSteps) && self.coreDataModel.totalPreSteps >0) {
                self.preSteps = self.steps.slice(0,self.coreDataModel.totalPreSteps);
            }




            if (angular.isNumber(self.selectedStepIndex) || angular.isString(self.selectedStepIndex)) {
                // Complete previous steps
                for (var i=0; i < self.selectedStepIndex; ++i) {
                    self.steps[i].complete = true;
                    self.steps[i].disabled = false;
                    self.steps[i].visited = true;
                    self.steps[i].updateStepType();
                }

                // Active current step
                self.steps[self.selectedStepIndex].disabled = false;
                self.steps[self.selectedStepIndex].visited = true;
                self.steps[self.selectedStepIndex].updateStepType();
            } else {
                self.selectedStepIndex = 0;
            }



            $scope.$watch(function () {
                return self.selectedStepIndex;
            }, function (current, old) {
                //Broadcast that we changed steps
                BroadcastService.notify(StepperService.STEP_CHANGED_EVENT, {newStep: current, oldStep: old});

                WindowUnloadService.clear();
                self.previousStepIndex = old;

                // Update step
                var step = self.getStep(current);
                if(step != null) {
                    var shouldSkip = (step.skip && !step.visited);
                    step.visited = true;
                    step.updateStepType();
                    BroadcastService.notify(StepperService.ACTIVE_STEP_EVENT, current);

                    // Skip if necessary
                    if (shouldSkip) {
                        self.stepEnabled(step.nextActiveStepIndex);
                        self.completeStep(step.index);
                        ++self.selectedStepIndex;
                    }
                }
            });

            if(self.onInitialized && angular.isFunction(self.onInitialized())) {
                self.onInitialized()(self);
            }
        }

        this.getCountOfActiveSteps = function(){
            return _.filter(StepperService.getSteps(self.stepperName),function(step) { return step.active;}).length;
        }


        this.goToFirstStep = function () {
            self.selectedStepIndex = 0;
        }

        this.onStepSelect = function (index) {
        }

        this.resetAndGoToFirstStep = function () {
            angular.forEach(self.steps, function (step) {
                step.reset();
            })
            self.selectedStepIndex = 0;
        }

        this.deactivateStep = function (index) {
            StepperService.deactivateStep(self.stepperName, index);
        }

        this.activateStep = function (index) {
            StepperService.activateStep(self.stepperName, index);
        }

        this.resetStep = function(index){
           var step = StepperService.getStep(self.stepperName, index);
           if(angular.isDefined(step)) {
               step.reset();
               BroadcastService.notify(StepperService.STEP_STATE_CHANGED_EVENT, index);
           }
        }

        this.stepDisabled = function (index) {
            StepperService.stepDisabled(self.stepperName, index);
            BroadcastService.notify(StepperService.STEP_STATE_CHANGED_EVENT, index);
        }
        this.stepEnabled = function (index) {
            StepperService.stepEnabled(self.stepperName, index);
            BroadcastService.notify(StepperService.STEP_STATE_CHANGED_EVENT, index);
        }

        this.getStep = function (index) {
            if (typeof index == 'string') {
                index = parseInt(index);
            }
            return StepperService.getStep(self.stepperName, index);
        }
        this.nextActiveStep = function (index) {
            return StepperService.nextActiveStep(self.stepperName, index)

        }

        this.previousActiveStep = function (index) {
            return StepperService.previousActiveStep(self.stepperName, index)

        }

        this.arePreviousStepsDisabled = function (index) {
            return StepperService.arePreviousStepsDisabled(self.stepperName, index);
        }

        this.arePreviousStepsComplete = function (index) {
            return StepperService.arePreviousStepsComplete(self.stepperName, index);
        }

        this.cancelStepper = function () {
            if (self.onCancelStepper) {
                self.onCancelStepper();
            }
        }

        this.showCancel = function () {
            return (self.showCancelButton != undefined ? self.showCancelButton : true);
        }

        this.assignStepName = function(step,name){
            step.stepName = name;
            StepperService.assignedStepName(self.stepperName,step)
        }

        this.getStepByName = function(stepName){
            return StepperService.getStepByName(self.stepperName,stepName);
        }

        this.completeStep = function (index) {
            var step = self.getStep(index);
            step.complete = true;
            step.updateStepType();
        }

        this.incompleteStep = function (index) {
            var step = self.getStep(index);
            step.complete = false;
            step.updateStepType();
        }

        initialize();

        $scope.$on('$destroy', function () {
            StepperService.deRegisterStepper(self.stepperName);
            self.steps = [];
        })
    };

    angular.module(moduleName).controller('StepperController', ["$scope","$attrs","$element","StepperService","Utils","BroadcastService","WindowUnloadService",controller]);

    angular.module(moduleName)
        .directive('thinkbigStepper', ['$compile', '$templateRequest', directive]);

});






