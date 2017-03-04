define(['angular',"feed-mgr/visual-query/module-name"], function (angular,moduleName) {

    /**
     * Displays the Visual Query page.
     *
     * @param {Object} $scope the application model
     * @param SideNavService the sidebar navigation service
     * @param StateService the state service
     * @param VisualQueryService the visual query service
     * @constructor
     */
    function VisualQueryController($scope, SideNavService, StateService, VisualQueryService) {
        var self = this;

        /**
         * The visual query model.
         * @type {Object}
         */
        self.model = VisualQueryService.model;

        /**
         * Navigates to the Feeds page when the stepper is cancelled.
         */
        self.cancelStepper = function() {
            VisualQueryService.resetModel();
            StateService.navigateToHome();
        };

        // Manage the sidebar navigation
        SideNavService.hideSideNav();

        $scope.$on("$destroy", function() {
            SideNavService.showSideNav();
        });
    }

    angular.module(moduleName).controller("VisualQueryController", ["$scope","SideNavService","StateService","VisualQueryService",VisualQueryController]);
});
