import * as angular from "angular";
const moduleName: string = require("feed-mgr/visual-query/module-name");
  /**
     * Controls the Profile dialog of the Visual Query Transform page.
     *
     * @param $scope the application model
     * @param $mdDialog the dialog service
     * @param profile the profile model data
     * @constructor
     */

export default class VisualQueryProfileStatsController implements ng.IComponentController {
     constructor(private $scope: any,
                private $mdDialog: any,
                private profile: any) {

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
    }
}

    // Register the controller
angular.module(moduleName).controller("VisualQueryProfileStatsController", ["$scope", "$mdDialog", "profile", VisualQueryProfileStatsController]);