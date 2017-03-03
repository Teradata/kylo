define(['angular','common/module-name'], function (angular,moduleName) {
    angular.module(moduleName).directive('tbaViewTypeSelection', function () {
        return {
            restrict: 'E',
            templateUrl: 'js/common/view-type-selection/view-type-selection-template.html',
            scope: {
                viewType: '='
            },
            link: function ($scope, elem, attr) {

                $scope.viewTypeChanged = function (viewType) {
                    $scope.viewType = viewType;
                }

            }
        }
    });
});
