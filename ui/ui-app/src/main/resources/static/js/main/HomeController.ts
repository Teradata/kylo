import * as angular from 'angular';

export class HomeController implements ng.IComponentController{
constructor(
        private $scope:angular.IScope,
        private $mdDialog:any,
        private AccessControlService:any,
        private StateService:any,  
        ){
        //$scope, $mdDialog, AccessControlService, StateService
          // Fetch the list of allowed actions
       this.AccessControlService.getUserAllowedActions()
                                .then((actionSet: any)=>{
                                    this.onLoad(actionSet.actions);
                                });
        }
        /**
         * Indicates that the page is currently being loaded.
         * @type {boolean}
         */
        loading = true;

        /**
         * Determines the home page based on the specified allowed actions.
         *
         * @param actions the allowed actions
         */
        onLoad(actions: any) {
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
        }
}


  angular.module('kylo').controller('HomeController', 
                                    ['$scope',
                                    '$mdDialog', 
                                    'AccessControlService', 
                                    'StateService',
                                    HomeController]);
