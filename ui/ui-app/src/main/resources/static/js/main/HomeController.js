define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var HomeController = /** @class */ (function () {
        function HomeController($scope, $mdDialog, AccessControlService, StateService) {
            var _this = this;
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.AccessControlService = AccessControlService;
            this.StateService = StateService;
            /**
             * Indicates that the page is currently being loaded.
             * @type {boolean}
             */
            this.loading = true;
            //$scope, $mdDialog, AccessControlService, StateService
            // Fetch the list of allowed actions
            this.AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                _this.onLoad(actionSet.actions);
            });
        }
        /**
         * Determines the home page based on the specified allowed actions.
         *
         * @param actions the allowed actions
         */
        HomeController.prototype.onLoad = function (actions) {
            // Determine the home page
            if (this.AccessControlService.hasAction(this.AccessControlService.FEEDS_ACCESS, actions)) {
                return this.StateService.FeedManager().Feed().navigateToFeeds();
            }
            if (this.AccessControlService.hasAction(this.AccessControlService.OPERATIONS_MANAGER_ACCESS, actions)) {
                return this.StateService.OpsManager().dashboard();
            }
            if (this.AccessControlService.hasAction(this.AccessControlService.CATEGORIES_ACCESS, actions)) {
                return this.StateService.FeedManager().Category().navigateToCategories();
            }
            if (this.AccessControlService.hasAction(this.AccessControlService.TEMPLATES_ACCESS, actions)) {
                return this.StateService.FeedManager().Template().navigateToRegisteredTemplates();
            }
            if (this.AccessControlService.hasAction(this.AccessControlService.USERS_ACCESS, actions)) {
                return this.StateService.Auth().navigateToUsers();
            }
            if (this.AccessControlService.hasAction(this.AccessControlService.GROUP_ACCESS, actions)) {
                return this.StateService.Auth().navigateToGroups();
            }
            /*
            // Determine if Feed Manager is allowed at all
            if (!AccessControlService.hasAction(AccessControlService.FEED_MANAGER_ACCESS, actions) && !AccessControlService.hasAction(AccessControlService.USERS_GROUPS_ACCESS, actions)) {
                self.loading = false;
                $mdDialog.show(
                    $mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Access Denied")
                        .textContent("You do not have access to the Feed Manager.")
                        .ariaLabel("Access denied to feed manager")
                        .ok("OK")
                );
                return;
            }
            */
            // Otherwise, let the user pick
            this.loading = false;
        };
        return HomeController;
    }());
    exports.HomeController = HomeController;
    angular.module('kylo').controller('HomeController', ['$scope',
        '$mdDialog',
        'AccessControlService',
        'StateService',
        HomeController]);
});
//# sourceMappingURL=HomeController.js.map