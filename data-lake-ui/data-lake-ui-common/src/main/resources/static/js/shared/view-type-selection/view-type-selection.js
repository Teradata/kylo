/*
 * Copyright (c) 2015.
 */

app.directive('tbaViewTypeSelection', function() {
    return {
        restrict: 'E',
        templateUrl: 'js/shared/view-type-selection/view-type-selection-template.html',
        scope: {
        viewType:'='
        },
        link: function($scope, elem, attr) {

            $scope.viewTypeChanged = function(viewType)
            {
                $scope.viewType = viewType;
             /*   if($scope.onViewTypeChanged){
                    $scope.onViewTypeChanged()(viewType);
                }
                */
            }

        }
    }
});