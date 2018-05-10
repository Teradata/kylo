define(["require", "exports", "angular", "./module-name", "../../services/AccessControlService"], function (require, exports, angular, module_name_1, AccessControlService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var RegisterNewTemplateController = /** @class */ (function () {
        function RegisterNewTemplateController($scope, accessControlService, StateService, RegisterTemplateService) {
            this.$scope = $scope;
            this.accessControlService = accessControlService;
            this.StateService = StateService;
            this.RegisterTemplateService = RegisterTemplateService;
            /**
            * List of methods for registering a new template.
            *
            * @type {Array.<{name: string, description: string, icon: string, iconColor: string, onClick: function}>}
            */
            this.registrationMethods = [];
        }
        RegisterNewTemplateController.prototype.ngOnInit = function () {
            var _this = this;
            // Fetch the allowed actions
            this.accessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                if (_this.accessControlService.hasAction(AccessControlService_1.default.TEMPLATES_IMPORT, actionSet.actions)) {
                    _this.registrationMethods.push({
                        name: "Import from NiFi", description: "Import a NiFi template directly from the current environment", icon: "near_me",
                        iconColor: "#3483BA", onClick: _this.createFromNifi()
                    });
                }
            });
            // Fetch the allowed actions
            this.accessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                if (_this.accessControlService.hasAction(AccessControlService_1.default.TEMPLATES_IMPORT, actionSet.actions)) {
                    _this.registrationMethods.push({
                        name: "Import from a file", description: "Import from a Kylo archive or NiFi template file", icon: "file_upload",
                        iconColor: "#F08C38", onClick: _this.importFromFile()
                    });
                }
            });
        };
        RegisterNewTemplateController.prototype.$onInit = function () {
            this.ngOnInit();
        };
        /**
             * Creates a new Feed Manager template from a NiFi template.
             */
        RegisterNewTemplateController.prototype.createFromNifi = function () {
            this.RegisterTemplateService.resetModel();
            this.StateService.FeedManager().Template().navigateToRegisterNiFiTemplate();
        };
        ;
        /**
         * Imports a Feed Manager template from a file.
         */
        RegisterNewTemplateController.prototype.importFromFile = function () {
            this.RegisterTemplateService.resetModel();
            this.StateService.FeedManager().Template().navigateToImportTemplate();
        };
        ;
        /**
         * Displays the page for registering a new Feed Manager template.
         *
         * @constructor
         * @param {Object} $scope the application model
         * @param {AccessControlService} AccessControlService the access control service
         * @param StateService
         */
        RegisterNewTemplateController.$inject = ["$scope", "AccessControlService", "StateService", "RegisterTemplateService"];
        return RegisterNewTemplateController;
    }());
    exports.RegisterNewTemplateController = RegisterNewTemplateController;
    angular.module(module_name_1.moduleName).component("registerNewTemplateController", {
        templateUrl: 'js/feed-mgr/templates/new-template/register-new-template.html',
        controller: RegisterNewTemplateController,
        controllerAs: 'vm'
    });
});
//# sourceMappingURL=RegisterNewTemplateController.js.map