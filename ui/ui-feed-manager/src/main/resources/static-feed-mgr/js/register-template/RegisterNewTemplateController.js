/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
(function() {

    /**
     * Displays the page for registering a new Feed Manager template.
     *
     * @constructor
     * @param {Object} $scope the application model
     * @param {AccessControlService} AccessControlService the access control service
     * @param StateService
     */
    function RegisterNewTemplateController($scope, AccessControlService, StateService, RegisterTemplateService) {
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
            StateService.navigateToRegisterNifiTemplate();
        };

        /**
         * Imports a Feed Manager template from a file.
         */
        self.importFromFile = function() {
            RegisterTemplateService.resetModel();
            StateService.navigateToImportTemplate();
        };

        // Fetch the allowed actions
        AccessControlService.getAllowedActions()
		        .then(function(actionSet) {
		        	if (AccessControlService.hasAction(AccessControlService.TEMPLATES_IMPORT, actionSet.actions)) {
		        		self.registrationMethods.push({
		        			name: "Import from NiFi", description: "Import a NiFi template directly from the current environment", icon: "near_me", 
		        			iconColor: "#3483BA", onClick: self.createFromNifi
		        		});
		        	}
		        });

        // Fetch the allowed actions
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    if (AccessControlService.hasAction(AccessControlService.TEMPLATES_IMPORT, actionSet.actions)) {
                        self.registrationMethods.push({
                            name: "Import from a file", description: "Import from a Kylo archive or NiFi template file", icon: "file_upload",
                            iconColor: "#F08C38", onClick: self.importFromFile
                        });
                    }
                });
    }

    angular.module(MODULE_FEED_MGR).controller("RegisterNewTemplateController", RegisterNewTemplateController);
}());
