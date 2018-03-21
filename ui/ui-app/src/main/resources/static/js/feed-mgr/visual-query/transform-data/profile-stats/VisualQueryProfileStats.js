define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require("feed-mgr/visual-query/module-name");
    /**
       * Controls the Profile dialog of the Visual Query Transform page.
       *
       * @param $scope the application model
       * @param $mdDialog the dialog service
       * @param profile the profile model data
       * @constructor
       */
    var VisualQueryProfileStatsController = /** @class */ (function () {
        function VisualQueryProfileStatsController($scope, $mdDialog, profile) {
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.profile = profile;
            /**
             * The profile model data.
             */
            $scope.profile = profile;
            /**
             * Closes the dialog.
             */
            $scope.cancel = function () {
                $mdDialog.hide();
            };
        }
        return VisualQueryProfileStatsController;
    }());
    exports.default = VisualQueryProfileStatsController;
    // Register the controller
    angular.module(moduleName).controller("VisualQueryProfileStatsController", ["$scope", "$mdDialog", "profile", VisualQueryProfileStatsController]);
});
//# sourceMappingURL=VisualQueryProfileStats.js.map