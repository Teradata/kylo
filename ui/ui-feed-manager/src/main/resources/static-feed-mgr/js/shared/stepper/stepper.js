
(function () {

    var directive = function ($compile, $templateRequest) {
        return {
            restrict: "EA",
            bindToController: {
                totalSteps:'@',
                stepperName:'@',
                onCancelStepper:'&',
                showCancelButton:'@',
                coreDataModel:'=?',
                templateUrl:'@',
                selectedStepIndex:'@'
            },
            controllerAs: 'vm',
            require:['thinkbigStepper'],
            scope:{},

            controller: "StepperController",
            compile:function(element,attrs) {
                return {
                    pre: function preLink($scope, iElement, iAttrs, controller) {


                    },
                    post: function postLink($scope, $element, iAttrs, controller) {
                        $templateRequest($scope.templateUrl).then(function(html){
                            // Convert the html to an actual DOM node
                            var template = angular.element(html);
                            // Append it to the directive element
                            $element.append(template);
                            // And let Angular $compile it
                            $compile(template)($scope);
                            $element.find('md-tabs-wrapper:first').append('  <div class="step-progressbar"  style="display:block;"></div>')
                            var progressBar =  $compile('<md-progress-linear md-mode="indeterminate" ng-if="vm.showProgress"></md-progress-linear>')($scope);
                            $element.find('.step-progressbar').append( progressBar)

                        });

                    }
                }
            }

        };
    }

    var controller =  function($scope, $element,StepperService, Utils, BroadcastService, WindowUnloadService) {
        function StepperControllerTag() {
        }

        this.__tag = new StepperControllerTag();

        this.showProgress = false;
        this.height = 80;
        $scope.templateUrl = this.templateUrl;
        $scope.stepperName = this.stepperName;
        $scope.totalSteps= this.totalSteps;

        Utils.waitForDomElementReady('md-tab-item',function(){
            $element.find('md-tab-item:not(:last)').addClass('arrow-tab')
        })

        var self = this;

        this.previousStepIndex =null;
        if(self.stepperName == undefined || self.stepperName == '') {
            self.stepperName = StepperService.newStepperName();
        }
        StepperService.registerStepper(self.stepperName,self.totalSteps);
        this.steps = StepperService.getSteps(self.stepperName);

        if (typeof(this.selectedStepIndex) !== "undefined") {
            angular.forEach(this.steps, function(step) {
                step.complete = true;
                step.disabled = false;
                step.visited = true;
                step.updateStepType();
            });
        } else {
            this.selectedStepIndex = 0;
        }

        $scope.$watch(function(){
            return self.selectedStepIndex;
        }, function(current, old){
            WindowUnloadService.clear();
            self.previousStepIndex = old;
            self.getStep(current).visited = true;
            self.getStep(current).updateStepType();
            BroadcastService.notify(StepperService.ACTIVE_STEP_EVENT,current);
        });

        this.goToFirstStep = function(){
            self.selectedStepIndex = 0;
        }

        this.onStepSelect = function(){
          //  console.log('SELECTED ',self.selectedStepIndex);
        }

        this.resetAndGoToFirstStep = function(){
            angular.forEach(self.steps,function(step) {
                step.reset();
            })
            self.selectedStepIndex = 0;
        }

        this.deactivateStep = function(index){
            StepperService.deactivateStep(self.stepperName,index);
        }

        this.activateStep = function(index){
            StepperService.activateStep(self.stepperName,index);
        }

        this.stepDisabled = function(index) {
            StepperService.stepDisabled(self.stepperName,index);
        }
        this.stepEnabled = function(index) {
            StepperService.stepEnabled(self.stepperName,index);
        }


        this.getStep = function(index){
            if(typeof index == 'string') {
                index = parseInt(index);
            }
            return StepperService.getStep(self.stepperName,index);
        }
        this.nextActiveStep = function(index){
        return StepperService.nextActiveStep(self.stepperName,index)

        }

        this.previousActiveStep = function(index){
            return StepperService.previousActiveStep(self.stepperName,index)

        }

        this.cancelStepper = function(){
            if(self.onCancelStepper){
                self.onCancelStepper();
            }
        }

        this.showCancel = function() {
            return (self.showCancelButton != undefined ? self.showCancelButton : true);
        }

        this.completeStep = function(index) {
            var step = self.getStep(index);
            step.complete = true;
            step.updateStepType();
        }

        this.incompleteStep = function(index) {
            var step = self.getStep(index);
            step.complete = false;
            step.updateStepType();
        }

        $scope.$on('$destroy',function(){
            StepperService.deRegisterStepper(self.stepperName);
            self.steps = [];
        })
    };


    angular.module(MODULE_FEED_MGR).controller('StepperController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigStepper',['$compile','$templateRequest',directive]);

})();






