import * as angular from "angular";
import {moduleName} from "../module-name";

export default class StepperService{
    ACTIVE_STEP_EVENT: any;
    STEP_CHANGED_EVENT: any;
    STEP_STATE_CHANGED_EVENT: any;
    steppers: any;
    stepperNameMap: any;
    newNameIndex: any;  
    registerStepper: any;
    assignStepName: any;
    assignedStepName: any;
    getStepByName: any;
    deRegisterStepper: any;
    newStepperName: any;
    getStep: any;
    getSteps: any;
    updatePreviousNextActiveStepIndexes: any;
    deactivateStep: any;
    activateStep: any;
    stepDisabled: any;
    stepEnabled: any;
    arePreviousVisitedStepsComplete: any;
    arePreviousStepsComplete: any;
    arePreviousStepsVisited: any;
    arePreviousStepsDisabled: any;
    previousActiveStep: any;
    nextActiveStep: any;

    constructor(){
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
         * populated only with this.assignStepNames
         * @type {{}}
         */
        this.stepperNameMap = {};

        this.newNameIndex = 0;
        this.registerStepper = function (name: any, totalSteps: any) {
            var steps: any[] = [];
            this.steppers[name] = steps;
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
        this.assignStepName = function(name: any, index: any, stepName: any){
            var step = this.getStep(name,index);
            if(step != null && step.stepName == null){
                step.stepName = stepName;
                this.assignedStepName(name,step)
            }
        };
        this.assignedStepName = function(name: any, step: any){
            if(step != null && step.stepName != null){
                 //register the step to the name map index
                this.stepperNameMap[name][step.stepName] = step;
            }
        };
        /**
         * get a step by its name.
         * Could be null
         * @param stepperName
         * @param stepName
         */
        this.getStepByName=function(stepperName: any,stepName: any){
            if(angular.isDefined(this.stepperNameMap[stepperName])){
                return  this.stepperNameMap[stepperName][stepName];
            }
            else {
                return null;
            }
        };
        this.deRegisterStepper = function (name: any) {
            delete this.steppers[name];
            delete this.stepperNameMap[name]
        }
        this.newStepperName = function () {
            this.newNameIndex++;
            return 'stepper_' + this.newNameIndex;
        }
        this.getStep = function (stepperName: any, index: any) {
            if (typeof index == 'string') {
                index = parseInt(index);
            }
            var steps = this.steppers[stepperName];
            if (steps != null && steps.length > 0) {
                if (index != null && index >= 0 && index < steps.length) {
                    return steps[index]
                }

            }
            return null;
        }
    
        this.getSteps = function (stepper: any) {
            return this.steppers[stepper];
        }

        this.updatePreviousNextActiveStepIndexes = function (stepper: any) {
            var steps = this.steppers[stepper];
            if (steps != null && steps.length > 0) {
                for (var i = 0; i < steps.length; i++) {
                    var step = steps[i];
                    var previousActiveStep = this.previousActiveStep(stepper, i);
                    if (previousActiveStep != null) {
                        step.previousActiveStepIndex = previousActiveStep.index;
                    }
                    else {
                        step.previousActiveStepIndex = null;
                    }

                    var nextActiveStep = this.nextActiveStep(stepper, i);
                    if (nextActiveStep != null) {
                        step.nextActiveStepIndex = nextActiveStep.index;
                    }
                    else {
                        step.nextActiveStepIndex = null;
                    }
                }
            }
        }

        this.deactivateStep = function (stepper: any, index: any) {
            var step = this.getStep(stepper, index);
            if (step != null) {
                step.active = false;
                step.disabled = true;
                this.updatePreviousNextActiveStepIndexes(stepper, index);
            }
        }

        this.activateStep = function (stepper: any, index: any) {
            var step = this.getStep(stepper, index);
            if (step != null) {
                step.active = true;
                this.updatePreviousNextActiveStepIndexes(stepper, index);
            }
        }

        this.stepDisabled = function (stepper: any, index: any) {
            var step = this.getStep(stepper, index);
            if (step != null) {
                step.disabled = true;
            }
        }

        this.stepEnabled = function (stepper: any, index: any) {
            var step = this.getStep(stepper, index);
            if (step != null && step.active) {
                step.disabled = false;
            }
        }

        this.arePreviousVisitedStepsComplete = function (stepper: any, index: any) {
            var complete = true;
            var steps = this.steppers[stepper];
            for (var i = 0; i < index; i++) {
                var step = steps[i];
                if (step.active && !step.complete && step.visited) {
                    complete = false;
                    break;
                }
            }
            return complete;
        }
        this.arePreviousStepsComplete = function (stepper: any, index: any) {
            var complete = true;
            var steps = this.steppers[stepper];
            for (var i = 0; i < index; i++) {
                var step = steps[i];
                if (step.active && !step.disabled && !step.complete) {
                    complete = false;
                    break;
                }
            }
            return complete;
        }

        this.arePreviousStepsVisited = function (stepper: any, index: any) {
            var complete = true;
            var steps = this.steppers[stepper];
            for (var i = 0; i < index; i++) {
                var step = steps[i];
                if (step.active && !step.visited) {
                    complete = false;
                    break;
                }
            }
            return complete;
        }

        this.arePreviousStepsDisabled = function (stepper: any, index: any) {
            var disabled = false;
            var steps = this.steppers[stepper];
            for (var i = 0; i < index; i++) {
                var step = steps[i];
                if (step.disabled) {
                    disabled = true;
                    break;
                }
            }
            return disabled;
        }

        this.previousActiveStep = function (stepperName: any, index: any) {
            var previousIndex = index - 1;
            var previousEnabledStep = null;
            var previousEnabledStepIndex = null;
            if (previousIndex >= 0) {
                var steps = this.steppers[stepperName];
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

        this.nextActiveStep = function (stepperName: any, index: any) {
            var nextIndex = index + 1;
            var nextEnabledStep = null;
            var nextEnabledStepIndex = null;
            var steps = this.steppers[stepperName];
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
    }
}


angular.module(moduleName).service('StepperService',StepperService);