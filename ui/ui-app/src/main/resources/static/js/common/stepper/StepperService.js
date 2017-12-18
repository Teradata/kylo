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
    return angular.module(moduleName).service('StepperService', function () {

        var self = this;
        /**
         * subscribers of this event will get the following data passed to them
         * current  - number datatype
         * @type {string}
         */
        this.ACTIVE_STEP_EVENT = 'ACTIVE_STEP_EVENT';
        /**
         * subscribers of this event will get the following data passed to them:
         * {newStep:current,oldStep:old}
         * newStep - number datatype
         * oldStep - number datatype
         * @type {string}
         */
        this.STEP_CHANGED_EVENT = 'STEP_CHANGED_EVENT';

        /**
         * Event called when a step is enabled or disabled
         * @type {string}
         */
        this.STEP_STATE_CHANGED_EVENT = 'STEP_STATE_CHANGED_EVENT';

        /**
         * Map of stepper Name  -> to Array of Steps
         * @type {{}}
         */
        this.steppers = {};

        /**
         * Map of stepper name  -> {stepName,step}
         * populated only with self.assignStepNames
         * @type {{}}
         */
        this.stepperNameMap = {};

        this.newNameIndex = 0;
        this.registerStepper = function (name, totalSteps) {
            var steps = [];
            self.steppers[name] = steps;
            this.stepperNameMap[name] = {};
            var lastIndex = totalSteps - 1;
            var disabled = false;
            for (var i = 0; i < totalSteps; i++) {
                if (i > 0) {
                    disabled = true;
                }

                steps[i] = {
                    disabled: disabled,
                    stepName:null,
                    active: true,
                    number: (i + 1),
                    index: i,
                    icon: 'number' + (i + 1),
                    iconSize: 30,
                    nextActiveStepIndex: (i != lastIndex) ? (i + 1) : null,
                    previousActiveStepIndex: (i != 0) ? i - 1 : null,
                    visited: false,
                    complete: false,
                    updateStepType: function () {
                        if (this.complete == true && this.visited == true && this.active && !this.disabled) {
                            this.type = 'step-complete';
                            this.icon = 'check'
                            this.iconSize = 20;
                            this.iconStyle = '#F08C38';
                        }
                        else {
                            this.type = 'step-default';
                            this.icon = 'number'+ (this.number);
                            this.iconSize = 30;
                            this.iconStyle = '';
                        }
                    },
                    reset: function () {
                        this.complete = false;
                        this.visited = false;
                        this.updateStepType();
                    }
                };
            }
        }
        this.assignStepName = function(name, index, stepName){
            var step = self.getStep(name,index);
            if(step != null && step.stepName == null){
                step.stepName = stepName;
                self.assignedStepName(name,step)
            }
        };
        this.assignedStepName = function(name, step){
            if(step != null && step.stepName != null){
                 //register the step to the name map index
                self.stepperNameMap[name][step.stepName] = step;
            }
        };
        /**
         * get a step by its name.
         * Could be null
         * @param stepperName
         * @param stepName
         */
        this.getStepByName=function(stepperName,stepName){
            if(angular.isDefined(self.stepperNameMap[stepperName])){
                return  self.stepperNameMap[stepperName][stepName];
            }
            else {
                return null;
            }
        };
        this.deRegisterStepper = function (name) {
            delete self.steppers[name];
            delete self.stepperNameMap[name]
        }
        this.newStepperName = function () {
            self.newNameIndex++;
            return 'stepper_' + self.newNameIndex;
        }
        this.getStep = function (stepperName, index) {
            if (typeof index == 'string') {
                index = parseInt(index);
            }
            var steps = self.steppers[stepperName];
            if (steps != null && steps.length > 0) {
                if (index != null && index >= 0 && index < steps.length) {
                    return steps[index]
                }

            }
            return null;
        }
        this.getSteps = function (stepper) {
            return this.steppers[stepper];
        }

        this.updatePreviousNextActiveStepIndexes = function (stepper) {
            var steps = self.steppers[stepper];
            if (steps != null && steps.length > 0) {
                for (var i = 0; i < steps.length; i++) {
                    var step = steps[i];
                    var previousActiveStep = self.previousActiveStep(stepper, i);
                    if (previousActiveStep != null) {
                        step.previousActiveStepIndex = previousActiveStep.index;
                    }
                    else {
                        step.previousActiveStepIndex = null;
                    }

                    var nextActiveStep = self.nextActiveStep(stepper, i);
                    if (nextActiveStep != null) {
                        step.nextActiveStepIndex = nextActiveStep.index;
                    }
                    else {
                        step.nextActiveStepIndex = null;
                    }
                }
            }
        }

        this.deactivateStep = function (stepper, index) {
            var step = self.getStep(stepper, index);
            if (step != null) {
                step.active = false;
                step.disabled = true;
                self.updatePreviousNextActiveStepIndexes(stepper, index);
            }
        }

        this.activateStep = function (stepper, index) {
            var step = self.getStep(stepper, index);
            if (step != null) {
                step.active = true;
                self.updatePreviousNextActiveStepIndexes(stepper, index);
            }
        }

        this.stepDisabled = function (stepper, index) {
            var step = self.getStep(stepper, index);
            if (step != null) {
                step.disabled = true;
            }

        }
        this.stepEnabled = function (stepper, index) {
            var step = self.getStep(stepper, index);
            if (step != null && step.active) {
                step.disabled = false;
            }

        }

        this.arePreviousVisitedStepsComplete = function (stepper, index) {
            var complete = true;
            var steps = self.steppers[stepper];
            for (var i = 0; i < index; i++) {
                var step = steps[i];
                if (step.active && !step.complete && step.visited) {
                    complete = false;
                    break;
                }
            }
            return complete;
        }

        this.arePreviousStepsComplete = function (stepper, index) {
            var complete = true;
            var steps = self.steppers[stepper];
            for (var i = 0; i < index; i++) {
                var step = steps[i];
                if (step.active && !step.disabled && !step.complete) {
                    complete = false;
                    break;
                }
            }
            return complete;
        }

        this.arePreviousStepsVisited = function (stepper, index) {
            var complete = true;
            var steps = self.steppers[stepper];
            for (var i = 0; i < index; i++) {
                var step = steps[i];
                if (step.active && !step.visited) {
                    complete = false;
                    break;
                }
            }
            return complete;
        }

        this.arePreviousStepsDisabled = function (stepper, index) {
            var disabled = false;
            var steps = self.steppers[stepper];
            for (var i = 0; i < index; i++) {
                var step = steps[i];
                if (step.disabled) {
                    disabled = true;
                    break;
                }
            }
            return disabled;
        }

        this.previousActiveStep = function (stepperName, index) {
            var previousIndex = index - 1;
            var previousEnabledStep = null;
            var previousEnabledStepIndex = null;
            if (previousIndex >= 0) {
                var steps = self.steppers[stepperName];
                for (var i = previousIndex; i >= 0; i--) {
                    var step = steps[i];
                    if (step.active) {
                        previousEnabledStep = step;
                        previousEnabledStepIndex = i;
                        break;
                    }
                }
                return previousEnabledStep;
            }
            else {
                return null;
            }
        }

        this.nextActiveStep = function (stepperName, index) {
            var nextIndex = index + 1;
            var nextEnabledStep = null;
            var nextEnabledStepIndex = null;
            var steps = self.steppers[stepperName];
            if (nextIndex < steps.length) {
                for (var i = nextIndex; i < steps.length; i++) {
                    var step = steps[i];
                    if (step.active) {
                        nextEnabledStep = step;
                        nextEnabledStepIndex = i;
                        break;
                    }
                }
                return nextEnabledStep;
            }
            else {
                return null;
            }

        }

    });
});