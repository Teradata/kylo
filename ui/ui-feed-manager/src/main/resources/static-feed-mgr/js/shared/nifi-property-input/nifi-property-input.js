(function () {

    var directive = function () {
        return {
            restrict: "EA",
            scope: {
                property:'=',
                propertyDisabled:'=?'
            },
            templateUrl: 'js/shared/nifi-property-input/nifi-property-input.html',
            link: function ($scope, element, attrs) {
                element.addClass('nifi-property-input layout-padding-top-bottom')
                if($scope.propertyDisabled == undefined){
                    $scope.propertyDisabled = false;
                }

            }

        };
    }




    angular.module(MODULE_FEED_MGR)
        .directive('nifiPropertyInput', directive);

})();
