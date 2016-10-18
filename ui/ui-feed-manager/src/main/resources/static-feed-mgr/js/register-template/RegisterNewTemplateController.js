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
            name: "Create from Nifi", description: "Register a new template that currently resides in Nifi", icon: "near_me", iconColor: "#3483BA", onClick: self.createFromNifi
        });

        // Fetch the allowed actions
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    if (AccessControlService.hasAction(AccessControlService.FEEDS_IMPORT, actionSet.actions)) {
                        self.registrationMethods.push({
                            name: "Import from file", description: "Register a new template that you exported from a different Kylo environment", icon: "file_upload",
                            iconColor: "#F08C38", onClick: self.importFromFile
                        });
                    }
                });
    }

    angular.module(MODULE_FEED_MGR).controller("RegisterNewTemplateController", RegisterNewTemplateController);
}());
