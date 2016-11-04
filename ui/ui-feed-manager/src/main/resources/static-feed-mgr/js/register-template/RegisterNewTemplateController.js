(function() {

    /**
     * Displays the page for registering a new Feed Manager template.
     *
     * @constructor
     * @param {Object} $scope the application model
     * @param {AccessControlService} AccessControlService the access control service
     * @param StateService
     */
    function RegisterNewTemplateController($scope, AccessControlService, StateService) {
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
            StateService.navigateToRegisterNifiTemplate();
        };

        /**
         * Imports a Feed Manager template from a file.
         */
        self.importFromFile = function() {
            StateService.navigateToImportTemplate();
        };

        // Add default method
        self.registrationMethods.push({
            name: "Import from NiFi", description: "Import a NiFi template directly from the current environment", icon: "near_me", iconColor: "#3483BA", onClick: self.createFromNifi
        });

        // Fetch the allowed actions
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    if (AccessControlService.hasAction(AccessControlService.FEEDS_IMPORT, actionSet.actions)) {
                        self.registrationMethods.push({
                            name: "Import from a file", description: "Import from a Kylo archive or NiFi template file", icon: "file_upload",
                            iconColor: "#F08C38", onClick: self.importFromFile
                        });
                    }
                });
    }

    angular.module(MODULE_FEED_MGR).controller("RegisterNewTemplateController", RegisterNewTemplateController);
}());
