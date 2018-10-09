import * as angular from 'angular';
import StateService from  "../services/StateService";
import AccessControlService from "../services/AccessControlService";
import AccessConstants from "../constants/AccessConstants";
import { Component } from '@angular/core';

@Component({
    selector:'home-controller',
    templateUrl: 'js/main/home.html'
})
export class HomeComponent {
    
    loading: boolean = true;

    ngOnInit() {
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any)=>{
                this.onLoad(actionSet.actions);
            });
    }

    constructor(private accessControlService:AccessControlService,
                private StateService:StateService){}
        

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
            return this.StateService.Auth().navigateToUsers();
        }
        if (this.accessControlService.hasAction(AccessConstants.GROUP_ACCESS, actions)) {
            return this.StateService.Auth().navigateToGroups();
        }

        this.loading = false;
    }
}
