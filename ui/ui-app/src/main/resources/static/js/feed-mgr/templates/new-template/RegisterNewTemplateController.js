define(["require", "exports", "angular", "../module-name", "../../../services/AccessControlService"], function (require, exports, angular, module_name_1, AccessControlService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var RegisterNewTemplateController = /** @class */ (function () {
        /**
         * Displays the page for registering a new Feed Manager template.
         *
         * @constructor
         * @param {Object} $scope the application model
         * @param {AccessControlService} AccessControlService the access control service
         * @param StateService
         */
        function RegisterNewTemplateController($scope, accessControlService, StateService, RegisterTemplateService) {
            this.$scope = $scope;
            this.accessControlService = accessControlService;
            this.StateService = StateService;
            this.RegisterTemplateService = RegisterTemplateService;
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
            self.createFromNifi = function () {
                RegisterTemplateService.resetModel();
                StateService.FeedManager().Template().navigateToRegisterNifiTemplate();
            };
            /**
             * Imports a Feed Manager template from a file.
             */
            self.importFromFile = function () {
                RegisterTemplateService.resetModel();
                StateService.FeedManager().Template().navigateToImportTemplate();
            };
            // Fetch the allowed actions
            accessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                if (accessControlService.hasAction(AccessControlService_1.default.TEMPLATES_IMPORT, actionSet.actions)) {
                    self.registrationMethods.push({
                        name: "Import from NiFi", description: "Import a NiFi template directly from the current environment", icon: "near_me",
                        iconColor: "#3483BA", onClick: self.createFromNifi
                    });
                }
            });
            // Fetch the allowed actions
            accessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                if (accessControlService.hasAction(AccessControlService_1.default.TEMPLATES_IMPORT, actionSet.actions)) {
                    self.registrationMethods.push({
                        name: "Import from a file", description: "Import from a Kylo archive or NiFi template file", icon: "file_upload",
                        iconColor: "#F08C38", onClick: self.importFromFile
                    });
                }
            });
        }
        return RegisterNewTemplateController;
    }());
    exports.RegisterNewTemplateController = RegisterNewTemplateController;
    angular.module(module_name_1.moduleName).controller("RegisterNewTemplateController", ["$scope", "AccessControlService", "StateService", "RegisterTemplateService", RegisterNewTemplateController]);
});
//# sourceMappingURL=RegisterNewTemplateController.js.map