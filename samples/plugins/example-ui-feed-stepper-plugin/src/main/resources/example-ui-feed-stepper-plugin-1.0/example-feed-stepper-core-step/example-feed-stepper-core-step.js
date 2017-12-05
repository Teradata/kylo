define(["angular", "/example-ui-feed-stepper-plugin-1.0/module-name"], function (angular, moduleName) {

    var ExampleUiFeedStepperCard = function () {
        return {
            bindToController: {
                stepIndex: "@"
            },
            controller: "ExampleUiFeedStepperCardController",
            controllerAs: "vm",
            require: ["exampleUiFeedStepperCard", "^thinkbigStepper"],
            templateUrl: "/example-ui-feed-stepper-plugin-1.0/example-ui-feed-stepper-card.html",
            link: function ($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                thisController.stepperController = stepperController;
                thisController.totalSteps = stepperController.totalSteps;
            }
        }
    };

    function ExampleUiFeedStepperCardController(FeedService) {
        this.model = FeedService.createFeedModel;
        this.stepNumber = parseInt(this.stepIndex) + 1;
    }

    angular.module(moduleName)
        .controller("ExampleUiFeedStepperCardController", ["FeedService", ExampleUiFeedStepperCardController])
        .directive("exampleUiFeedStepperCard", ExampleUiFeedStepperCard);
});
