import * as angular from 'angular';
const moduleName = require('feed-mgr/feeds/define-feed/module-name');
    /**
     * An individual step in the Define Feed wizard.
     */
    var kyloDefineFeedStep = function (StepperService:any) {
        return {
            restrict: "E",
            scope: {
                step: "=",
                title: "@"
            },
            require: ['^thinkbigStepper'],
            templateUrl: "js/feed-mgr/feeds/define-feed/define-feed-step.html",
            transclude: true,
            link: function link($scope:any, element:any, attrs:any, controller:any, $transclude:any) {
                $scope.$transclude = $transclude;
                var stepperController = controller[0];
                if($scope.step != undefined) {
                    stepperController.assignStepName($scope.step, $scope.title);
                }
                else {
                    console.error("UNDEFINED STEP!!!",$scope);
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
            link: ($scope:any, $element:any) => {
                $scope.$transclude((clone:any) => {
                    $element.empty();
                    $element.append(clone);
                });
            }
        };
    };


export class DefineFeedStep {


}
angular.module(moduleName).directive("kyloDefineFeedStep",['StepperService', kyloDefineFeedStep]);
angular.module(moduleName).directive("kyloDefineFeedStepTransclude", kyloDefineFeedStepTransclude);
