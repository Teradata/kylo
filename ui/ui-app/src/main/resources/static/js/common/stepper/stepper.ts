import * as angular from "angular";
import {moduleName} from "../module-name";
import * as _ from "underscore";
export default class controller implements ng.IComponentController{
    showProgress: any;
    height: any;
    steps: any[];
    preSteps:any;
    templateUrl: any;
    stepperName: any;
    totalSteps: any;
    getStartingIndex: any;
    getCountOfActiveSteps: any;
    goToFirstStep: any;
    onStepSelect: any;
    resetAndGoToFirstStep: any;
    deactivateStep: any;
    activateStep: any;
    resetStep: any;
    stepDisabled: any;
    stepEnabled: any;
    getStep: any;
    nextActiveStep: any;
    previousActiveStep: any;
    arePreviousStepsDisabled: any;
    arePreviousStepsComplete: any;
    cancelStepper: any;
    showCancel: any;
    assignStepName: any;
    getStepByName: any;
    completeStep: any;
    incompleteStep: any;
    selectedStepIndex: any;
    coreDataModel: any;    
    previousStepIndex: any;
    onInitialized: any;

    constructor(private $scope: any,
                private $attrs: any,
                private $element: any,
                private StepperService: any,
                private Utils: any,
                private BroadcastService: any,
                private WindowUnloadService: any){
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
                this.getStartingIndex = function(index: any){
                    return index+this.preSteps.length;
                }

                
            $scope.$watch( ()=> {
                return this.selectedStepIndex;
            }, (current: any, old: any)=> {
                //Broadcast that we changed steps
                BroadcastService.notify(StepperService.STEP_CHANGED_EVENT, {newStep: current, oldStep: old});

                WindowUnloadService.clear();
                this.previousStepIndex = old;

                // Update step
                var step = this.getStep(current);
                if(step != null) {
                    var shouldSkip = (step.skip && !step.visited);
                    step.visited = true;
                    step.updateStepType();
                    BroadcastService.notify(StepperService.ACTIVE_STEP_EVENT, current);

                    // Skip if necessary
                    if (shouldSkip) {
                        this.stepEnabled(step.nextActiveStepIndex);
                        this.completeStep(step.index);
                        ++this.selectedStepIndex;
                    }
                }
            });

            if(this.onInitialized && angular.isFunction(this.onInitialized())) {
                this.onInitialized()(this);
            }
        
        this.getCountOfActiveSteps = function(){
            return _.filter(StepperService.getSteps(this.stepperName),function(step: any) { return step.active;}).length;
        }


        this.goToFirstStep = function () {
            this.selectedStepIndex = 0;
        }
        
        this.onStepSelect = function (index: any) {
        }

        this.resetAndGoToFirstStep = function () {
            angular.forEach(this.steps, function (step) {
                step.reset();
            })
            this.selectedStepIndex = 0;
        }

        this.deactivateStep = function (index: any) {
            StepperService.deactivateStep(this.stepperName, index);
        }

        this.activateStep = function (index: any) {
            StepperService.activateStep(this.stepperName, index);
        }

        this.resetStep = function(index: any){
           var step = StepperService.getStep(this.stepperName, index);
           if(angular.isDefined(step)) {
               step.reset();
               BroadcastService.notify(StepperService.STEP_STATE_CHANGED_EVENT, index);
           }
        }
 
        this.stepDisabled = function (index: any) {
            StepperService.stepDisabled(this.stepperName, index);
            BroadcastService.notify(StepperService.STEP_STATE_CHANGED_EVENT, index);
        }
        this.stepEnabled = function (index: any) {
            StepperService.stepEnabled(this.stepperName, index);
            BroadcastService.notify(StepperService.STEP_STATE_CHANGED_EVENT, index);
        }

        this.getStep = function (index: any) {
            if (typeof index == 'string') {
                index = parseInt(index);
            }
            return StepperService.getStep(this.stepperName, index);
        }
        this.nextActiveStep = function (index: any) {
            return StepperService.nextActiveStep(this.stepperName, index)

        }

        this.previousActiveStep = function (index: any) {
            return StepperService.previousActiveStep(this.stepperName, index)

        }

        this.arePreviousStepsDisabled = function (index: any) {
            return StepperService.arePreviousStepsDisabled(this.stepperName, index);
        }

        this.arePreviousStepsComplete = function (index: any) {
            return StepperService.arePreviousStepsComplete(this.stepperName, index);
        }

        this.cancelStepper = function () {
            if (this.onCancelStepper) {
                this.onCancelStepper();
            }
        }

        this.showCancel = function () {
            return (this.showCancelButton != undefined ? this.showCancelButton : true);
        }

        this.assignStepName = function(step: any,name: any){
            step.stepName = name;
            StepperService.assignedStepName(this.stepperName,step)
        }

        this.getStepByName = function(stepName: any){
            return StepperService.getStepByName(this.stepperName,stepName);
        }

        this.completeStep = function (index: any) {
            var step = this.getStep(index);
            step.complete = true;
            step.updateStepType();
        }

        this.incompleteStep = function (index: any) {
            var step = this.getStep(index);
            step.complete = false;
            step.updateStepType();
        }

        this.initialize();

        $scope.$on('$destroy', ()=> {
            StepperService.deRegisterStepper(this.stepperName);
            this.steps = [];
        })
        }

        initialize=()=>{
            this.$scope.templateUrl = this.templateUrl;
            this.$scope.stepperName = this.stepperName;
            this.$scope.totalSteps = this.totalSteps;

            this.Utils.waitForDomElementReady('md-tab-item', ()=> {
                this.$element.find('md-tab-item:not(:last)').addClass('arrow-tab')
            })


            this.previousStepIndex = null;
            if (this.stepperName == undefined || this.stepperName == '') {
                this.stepperName = this.StepperService.newStepperName();
            }
            this.StepperService.registerStepper(this.stepperName, this.totalSteps);
            this.steps = this.StepperService.getSteps(this.stepperName);

            //set the pre-steps
            if(angular.isDefined(this.coreDataModel) && angular.isDefined(this.coreDataModel.totalPreSteps) && this.coreDataModel.totalPreSteps >0) {
                this.preSteps = this.steps.slice(0,this.coreDataModel.totalPreSteps);
            }

            if (angular.isNumber(this.selectedStepIndex) || angular.isString(this.selectedStepIndex)) {
                // Complete previous steps
                for (var i=0; i < this.selectedStepIndex; ++i) {
                    this.steps[i].complete = true;
                    this.steps[i].disabled = false;
                    this.steps[i].visited = true;
                    this.steps[i].updateStepType();
                }

                // Active current step
                this.steps[this.selectedStepIndex].disabled = false;
                this.steps[this.selectedStepIndex].visited = true;
                this.steps[this.selectedStepIndex].updateStepType();
            } else {
                this.selectedStepIndex = 0;
            }
        }        
}


angular.module(moduleName).controller('StepperController', ["$scope","$attrs","$element","StepperService","Utils","BroadcastService","WindowUnloadService",controller]);

angular.module(moduleName).directive("thinkbigStepper",
  ['$compile', '$templateRequest',($compile, $templateRequest) => {
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

            compile: function (element: any, attrs: any) {
                return {
                    pre: function preLink($scope: any, $element: any, iAttrs: any, controller: any) {
                    },
                    post: function postLink($scope: any, $element: any, iAttrs: any, controller: any) {
                        $templateRequest(iAttrs.templateUrl).then(function (html: any) {
                            // Convert the html to an actual DOM node
                            var template = angular.element(html);
                            // Append it to the directive element
                            $element.append(template);
                            // And let Angular $compile it
                            $compile(template)($scope);
                            $element.find('md-tabs-wrapper:first').append('  <div class="step-progressbar"  style="display:block;"></div>')
                            var progressBar = this.$compile('<md-progress-linear md-mode="indeterminate" ng-if="vm.showProgress"></md-progress-linear>')($scope);
                            $element.find('.step-progressbar').append(progressBar)

                        });

                    }
                }
            }
          };
  }
  ]);

