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
                            var derivedValue = RegisterTemplateService.deriveExpression(newValue,true)
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
