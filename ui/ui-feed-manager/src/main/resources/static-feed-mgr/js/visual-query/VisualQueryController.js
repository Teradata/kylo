(function () {

    var controller = function($scope,$http, SideNavService, StateService, VisualQueryService){

        var self = this;

        SideNavService.hideSideNav();

        this.model = VisualQueryService.model;

        $scope.$on('$destroy',function(){
            SideNavService.showSideNav();
        })
    };



    angular.module(MODULE_FEED_MGR).controller('VisualQueryController',controller);

}());