define(["angular", "/example-plugin-1.0/module-name"], function (angular, moduleName) {

    var directive = function () {
        return {
            bindToController: {
                stepIndex: "@"
            },
            controller: "ExampleFeedStepperCoreStepController",
            controllerAs: "vm",
            require: ["exampleFeedStepperCoreStep", "^thinkbigStepper"],
            templateUrl: "/example-plugin-1.0/example-feed-stepper-core-step/example-feed-stepper-core-step.html",
            link: function ($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                thisController.stepperController = stepperController;
                thisController.totalSteps = stepperController.totalSteps;
            }
        }
    };

    function controller(FeedService) {
        this.model = FeedService.createFeedModel;
        this.stepNumber = parseInt(this.stepIndex) + 1;
    }

    angular.module(moduleName)
        .controller("ExampleFeedStepperCoreStepController", ["FeedService", controller])
        .directive("exampleFeedStepperCoreStep", directive);
});
