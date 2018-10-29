import * as _ from "underscore";
import AccessControlService from '../../../services/AccessControlService';
import StateService from '../../../services/StateService';
import { RegisterTemplateServiceFactory } from '../../services/RegisterTemplateServiceFactory';
import { Component } from '@angular/core';

@Component({
    selector:'register-new-template-controller',
    templateUrl: 'js/feed-mgr/templates/new-template/register-new-template.html'
})
export class RegisterNewTemplateController {

    /**
    * List of methods for registering a new template.
    *
    * @type {Array.<{name: string, description: string, icon: string, iconColor: string, onClick: function}>}
    */
    registrationMethods: any = [];

    ngOnInit() {
        // Fetch the allowed actions
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                if (this.accessControlService.hasAction(AccessControlService.TEMPLATES_IMPORT, actionSet.actions)) {
                    this.registrationMethods.push({
                        name: "Import from NiFi", description: "Import a NiFi template directly from the current environment", icon: "near_me",
                        iconColor: "#3483BA", onClick: () => this.createFromNifi()
                    });
                }
            });

        // Fetch the allowed actions
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                if (this.accessControlService.hasAction(AccessControlService.TEMPLATES_IMPORT, actionSet.actions)) {
                    this.registrationMethods.push({
                        name: "Import from a file", description: "Import from a Kylo archive or NiFi template file", icon: "file_upload",
                        iconColor: "#F08C38", onClick: () => this.importFromFile()
                    });
                }
            });
    }


    /**
     * Displays the page for registering a new Feed Manager template.
     *
     * @constructor
     * @param {Object} $scope the application model
     * @param {AccessControlService} AccessControlService the access control service
     * @param StateService
     */
    constructor(private accessControlService: AccessControlService,
                private stateService: StateService,
                private registerTemplateService: RegisterTemplateServiceFactory) {}
    /**
         * Creates a new Feed Manager template from a NiFi template.
         */
    createFromNifi () {
        this.registerTemplateService.resetModel();
        this.stateService.FeedManager().Template().navigateToRegisterNifiTemplate();
    }

    /**
     * Imports a Feed Manager template from a file.
     */
    importFromFile () {
        this.registerTemplateService.resetModel();
        this.stateService.FeedManager().Template().navigateToImportTemplate();
    }

}
