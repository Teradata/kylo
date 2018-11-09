import * as _ from "underscore";
import AccessControlService from '../../../services/AccessControlService';
import StateService from '../../../services/StateService';
import { RegisterTemplateServiceFactory } from '../../services/RegisterTemplateServiceFactory';
import { Component } from '@angular/core';
import { TranslateService } from "@ngx-translate/core";

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
                        name:this.translate.instant("FEEDMGR.TEMPLATES.IMPORT.IMPORT_FROM_NIFI_TITLE"), description:this.translate.instant("FEEDMGR.TEMPLATES.IMPORT.IMPORT_FROM_NIFI_DESCRIPTION"), icon: "near_me",
                        iconColor: "#3483BA", onClick: () => this.createFromNifi()
                    });
                }
            });

        // Fetch the allowed actions
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                if (this.accessControlService.hasAction(AccessControlService.TEMPLATES_IMPORT, actionSet.actions)) {
                    this.registrationMethods.push({
                        name:this.translate.instant("FEEDMGR.TEMPLATES.IMPORT.IMPORT_FROM_FILE_TITLE"), description:this.translate.instant("FEEDMGR.TEMPLATES.IMPORT.IMPORT_FROM_FILE_DESCRIPTION"), icon: "file_upload",
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
                private registerTemplateService: RegisterTemplateServiceFactory,
                private translate : TranslateService) {}
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
