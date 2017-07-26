define(["angular", "feed-mgr/visual-query/module-name"], function (angular, moduleName) {
    /**
     * Controls the Profile dialog of the Visual Query Transform page.
     *
     * @param $scope the application model
     * @param $mdDialog the dialog service
     * @param profile the profile model data
     * @constructor
     */
    var VisualQueryProfileStatsController = function ($scope, $mdDialog, profile) {

        /**
         * The profile model data.
         */
        $scope.profile = profile;

        /**
         * Closes the dialog.
         */
        $scope.cancel = function () {
            $mdDialog.hide();
        }
    };

    // Register the controller
    angular.module(moduleName).controller("VisualQueryProfileStatsController", ["$scope", "$mdDialog", "profile", VisualQueryProfileStatsController]);
});
