define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/define-feed/module-name');
    /**
     * An individual step in the Define Feed wizard.
     */
    var kyloDefineFeedStep = function (StepperService) {
        return {
            restrict: "E",
            scope: {
                step: "=",
                title: "@"
            },
            require: ['^thinkbigStepper'],
            templateUrl: "js/feed-mgr/feeds/define-feed/define-feed-step.html",
            transclude: true,
            link: function link($scope, element, attrs, controller, $transclude) {
                $scope.$transclude = $transclude;
                var stepperController = controller[0];
                if ($scope.step != undefined) {
                    stepperController.assignStepName($scope.step, $scope.title);
                }
                else {
                    console.error("UNDEFINED STEP!!!", $scope);
                }
            }
        };
    };
    /**
     * Transcludes the HTML contents of a <kylo-define-feed-step/> into the template of kyloDefineFeedStep.
     */
    var kyloDefineFeedStepTransclude = function () {
        return {
            restrict: "E",
            link: function ($scope, $element) {
                $scope.$transclude(function (clone) {
                    $element.empty();
                    $element.append(clone);
                });
            }
        };
    };
    var DefineFeedStep = /** @class */ (function () {
        function DefineFeedStep() {
        }
        return DefineFeedStep;
    }());
    exports.DefineFeedStep = DefineFeedStep;
    angular.module(moduleName).directive("kyloDefineFeedStep", ['StepperService', kyloDefineFeedStep]);
    angular.module(moduleName).directive("kyloDefineFeedStepTransclude", kyloDefineFeedStepTransclude);
});
//# sourceMappingURL=DefineFeedStepController.js.map