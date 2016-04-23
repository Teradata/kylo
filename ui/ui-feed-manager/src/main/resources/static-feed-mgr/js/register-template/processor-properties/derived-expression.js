/*
 * Copyright (c) 2015.
 */

/**
 * This Directive is wired in to the FeedStatusIndicatorDirective.
 * It uses the OverviewService to watch for changes and update after the Indicator updates
 */
(function () {

    var directive = function (RegisterTemplateService) {
        return {
            restrict: "A",
            scope: {
                ngModel:'='
            },
            link: function ($scope, $element, attrs, controller) {

                $scope.$watch(
                    'ngModel',
                    function (newValue) {
                        if(newValue != null) {
                            var derivedValue = RegisterTemplateService.deriveExpression(newValue)
                            $element.html(derivedValue);
                        }
                        else {
                            $element.html('');
                        }
                    });



            }

        };
    }

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigDerivedExpression',['RegisterTemplateService', directive]);

})();
