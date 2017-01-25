(function () {

    var directive = function () {
        return {
            restrict: "EA",
            scope: true,
            controllerAs: 'loadingController',
            bindToController: {
                loadingTitle: "@"
            },
            templateUrl: 'js/shared/loading-directive/loading-template.html',
            controller: "LoadingIndicatorController",
            link: function ($scope, element, attrs) {
                $scope.$on('$destroy', function () {
                });
            }
        }
    };

        var controller = function ($scope, $element) {
            var self = this;
        };
        angular.module(MODULE_OPERATIONS).controller('LoadingIndicatorController', controller);

        angular.module(MODULE_OPERATIONS)
            .directive('tbaLoadingIndicator', directive);



    }());
