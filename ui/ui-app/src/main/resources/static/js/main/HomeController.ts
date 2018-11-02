import * as angular from 'angular';
import {StateService} from  "../services/StateService";
import {AccessControlService} from "../services/AccessControlService";
import AccessConstants from "../constants/AccessConstants";
export class HomeController implements ng.IComponentController{
    static readonly $inject = ['$scope', '$mdDialog', 'AccessControlService','StateService'];
    constructor(
        private $scope:angular.IScope,
        private $mdDialog:angular.material.IDialogService,
        private accessControlService:AccessControlService,
        private StateService:StateService
        ){
        // Fetch the list of allowed actions
       accessControlService.getUserAllowedActions()
                                .then((actionSet: any)=>{
                                    this.onLoad(actionSet.actions);
                                });
        }
        /**
         * Indicates that the page is currently being loaded.
         * @type {boolean}
         */
        loading: boolean = true;

        /**
         * Determines the home page based on the specified allowed actions.
         *
         * @param actions the allowed actions
         */
        onLoad(actions: any) {
            // Determine the home page
            if (this.accessControlService.hasAction(AccessConstants.FEEDS_ACCESS, actions)) {
                return this.StateService.FeedManager().Feed().navigateToFeeds();
            }
            if (this.accessControlService.hasAction(AccessConstants.OPERATIONS_MANAGER_ACCESS, actions)) {
                return this.StateService.OpsManager().dashboard();
            }
            if (this.accessControlService.hasAction(AccessConstants.CATEGORIES_ACCESS, actions)) {
                return this.StateService.FeedManager().Category().navigateToCategories();
            }
            if (this.accessControlService.hasAction(AccessConstants.TEMPLATES_ACCESS, actions)) {
                return this.StateService.FeedManager().Template().navigateToRegisteredTemplates();
            }
            if (this.accessControlService.hasAction(AccessConstants.USERS_ACCESS, actions)) {
                return this.StateService.Auth.navigateToUsers();
            }
            if (this.accessControlService.hasAction(AccessConstants.GROUP_ACCESS, actions)) {
                return this.StateService.Auth.navigateToGroups();
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
  angular.module('kylo').component("homeController", { 
        controller: HomeController,
        controllerAs: "vm",
        templateUrl: "./home.html"
    });
//  .controller('HomeController', [HomeController]);
