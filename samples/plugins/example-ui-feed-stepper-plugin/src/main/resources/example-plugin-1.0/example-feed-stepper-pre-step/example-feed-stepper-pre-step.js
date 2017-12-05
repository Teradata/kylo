define(["angular", "/example-plugin-1.0/module-name"], function (angular, moduleName) {

    var directive = function () {
        return {
            bindToController: {
                stepIndex: "@"
            },
            controller: "ExampleFeedStepperPreStepController",
            controllerAs: "vm",
            require: ["exampleFeedStepperPreStep", "^thinkbigStepper"],
            templateUrl: "/example-plugin-1.0/example-feed-stepper-pre-step/example-feed-stepper-pre-step.html",
            link: function ($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                thisController.stepperController = stepperController;
                thisController.totalSteps = stepperController.totalSteps;
            }
        }
    };

    function controller(FeedService) {
        var self= this;
        this.model = FeedService.createFeedModel;
        this.stepNumber = parseInt(this.stepIndex) + 1;
        this.form = {};

        /**
         * Enforce min age limit
         */
        this.ageChanged = function(){
            var age = self.model.tableOption.preStepAge;
            if(parseInt(age) >=21) {
                self.form['preStepAge'].$setValidity('ageError', true);
            }
            else {
                self.form['preStepAge'].$setValidity('ageError', false);
            }

        }

        this.magicWordChanged = function(){
            var magicWord = self.model.tableOption.preStepMagicWord;
            if(angular.isDefined(magicWord) && magicWord != null) {
                if (magicWord.toLowerCase() == 'kylo') {
                    self.form['preStepMagicWord'].$setValidity('magicWordError', true);
                }
                else {
                    self.form['preStepMagicWord'].$setValidity('magicWordError', false);
                }
            }
        }
    }

    angular.module(moduleName)
        .controller("ExampleFeedStepperPreStepController", ["FeedService", controller])
        .directive("exampleFeedStepperPreStep", directive);
});
