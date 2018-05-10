import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "./module-name";
import AccessControlService from '../../services/AccessControlService';


export class RegisterNewTemplateController {

    /**
    * List of methods for registering a new template.
    *
    * @type {Array.<{name: string, description: string, icon: string, iconColor: string, onClick: function}>}
    */
    registrationMethods:any = [];

    ngOnInit() {
        // Fetch the allowed actions
        this.accessControlService.getUserAllowedActions()
		        .then((actionSet:any) =>{
		        	if (this.accessControlService.hasAction(AccessControlService.TEMPLATES_IMPORT, actionSet.actions)) {
		        		this.registrationMethods.push({
		        			name: "Import from NiFi", description: "Import a NiFi template directly from the current environment", icon: "near_me", 
		        			iconColor: "#3483BA", onClick: this.createFromNifi()
		        		});
		        	}
		        });

        // Fetch the allowed actions
        this.accessControlService.getUserAllowedActions()
                .then((actionSet:any) =>{
                    if (this.accessControlService.hasAction(AccessControlService.TEMPLATES_IMPORT, actionSet.actions)) {
                        this.registrationMethods.push({
                            name: "Import from a file", description: "Import from a Kylo archive or NiFi template file", icon: "file_upload",
                            iconColor: "#F08C38", onClick: this.importFromFile()
                        });
                    }
                });
    }

    $onInit() {
        this.ngOnInit();
    }
    /**
     * Displays the page for registering a new Feed Manager template.
     *
     * @constructor
     * @param {Object} $scope the application model
     * @param {AccessControlService} AccessControlService the access control service
     * @param StateService
     */
    static readonly $inject = ["$scope", "AccessControlService", "StateService", "RegisterTemplateService"];
    constructor(private $scope:IScope, private accessControlService:AccessControlService, private StateService:any, private RegisterTemplateService:any) {
        
    }

    /**
         * Creates a new Feed Manager template from a NiFi template.
         */
        createFromNifi() {
            this.RegisterTemplateService.resetModel();
            this.StateService.FeedManager().Template().navigateToRegisterNiFiTemplate();
        };

        /**
         * Imports a Feed Manager template from a file.
         */
        importFromFile() {
            this.RegisterTemplateService.resetModel();
            this.StateService.FeedManager().Template().navigateToImportTemplate();
        };

}
angular.module(moduleName).component("registerNewTemplateController",{
    templateUrl: 'js/feed-mgr/templates/new-template/register-new-template.html',
    controller:   RegisterNewTemplateController,
    controllerAs:'vm'
});
