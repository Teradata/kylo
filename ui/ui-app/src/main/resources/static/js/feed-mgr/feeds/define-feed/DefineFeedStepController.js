define(["angular", "feed-mgr/feeds/define-feed/module-name"], function (angular, moduleName) {
    /**
     * An individual step in the Define Feed wizard.
     */
    var kyloDefineFeedStep = function () {
        return {
            restrict: "E",
            scope: {
                step: "=",
                title: "@"
            },
            templateUrl: "js/feed-mgr/feeds/define-feed/define-feed-step.html",
            transclude: true,
            link: function link($scope, element, attrs, controller, $transclude) {
                $scope.$transclude = $transclude;
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

    angular.module(moduleName).directive("kyloDefineFeedStep", kyloDefineFeedStep);
    angular.module(moduleName).directive("kyloDefineFeedStepTransclude", kyloDefineFeedStepTransclude);
});
