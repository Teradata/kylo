import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "../module-name";


export class RegisterNewTemplateController {

    registrationMethods:any;
    createFromNifi:any;
    importFromFile:any;
    /**
     * Displays the page for registering a new Feed Manager template.
     *
     * @constructor
     * @param {Object} $scope the application model
     * @param {AccessControlService} AccessControlService the access control service
     * @param StateService
     */
    constructor(private $scope:any, private AccessControlService:any, private StateService:any, private RegisterTemplateService:any) {
        var self = this;

        /**
         * List of methods for registering a new template.
         *
         * @type {Array.<{name: string, description: string, icon: string, iconColor: string, onClick: function}>}
         */
        self.registrationMethods = [];

        /**
         * Creates a new Feed Manager template from a NiFi template.
         */
        self.createFromNifi = function() {
            RegisterTemplateService.resetModel();
            StateService.FeedManager().Template().navigateToRegisterNifiTemplate();
        };

        /**
         * Imports a Feed Manager template from a file.
         */
        self.importFromFile = function() {
            RegisterTemplateService.resetModel();
            StateService.FeedManager().Template().navigateToImportTemplate();
        };

        // Fetch the allowed actions
        AccessControlService.getUserAllowedActions()
		        .then(function(actionSet:any) {
		        	if (AccessControlService.hasAction(AccessControlService.TEMPLATES_IMPORT, actionSet.actions)) {
		        		self.registrationMethods.push({
		        			name: "Import from NiFi", description: "Import a NiFi template directly from the current environment", icon: "near_me", 
		        			iconColor: "#3483BA", onClick: self.createFromNifi
		        		});
		        	}
		        });

        // Fetch the allowed actions
        AccessControlService.getUserAllowedActions()
                .then(function(actionSet:any) {
                    if (AccessControlService.hasAction(AccessControlService.TEMPLATES_IMPORT, actionSet.actions)) {
                        self.registrationMethods.push({
                            name: "Import from a file", description: "Import from a Kylo archive or NiFi template file", icon: "file_upload",
                            iconColor: "#F08C38", onClick: self.importFromFile
                        });
                    }
                });
    }

}

angular.module(moduleName).controller("RegisterNewTemplateController",["$scope","AccessControlService","StateService","RegisterTemplateService",RegisterNewTemplateController]);