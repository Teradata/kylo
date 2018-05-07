define(["require", "exports", "angular", "../../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/feed-mgr/templates/template-stepper/access-control/template-access-control.html',
            controller: "TemplateAccessControlController",
            link: function ($scope, element, attrs, controller) {
            }
        };
    };
    var TemplateAccessControlController = /** @class */ (function () {
        function TemplateAccessControlController($scope, RegisterTemplateService, AccessControlService, EntityAccessControlService) {
            this.$scope = $scope;
            this.RegisterTemplateService = RegisterTemplateService;
            this.AccessControlService = AccessControlService;
            this.EntityAccessControlService = EntityAccessControlService;
            /**
             * ref back to this controller
             * @type {TemplateAccessControlController}
             */
            var self = this;
            this.templateAccessControlForm = {};
            this.model = RegisterTemplateService.model;
            this.allowEdit = false;
            //allow edit if the user has ability to change permissions on the entity if its an existing registered template, or if it is new
            if (this.model.id && RegisterTemplateService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.TEMPLATE.CHANGE_TEMPLATE_PERMISSIONS)) {
                self.allowEdit = true;
            }
            else {
                self.allowEdit = this.model.id == undefined;
            }
        }
        return TemplateAccessControlController;
    }());
    exports.TemplateAccessControlController = TemplateAccessControlController;
    angular.module(module_name_1.moduleName).controller("TemplateAccessControlController", ["$scope", "RegisterTemplateService", "AccessControlService", "EntityAccessControlService", TemplateAccessControlController]);
    angular.module(module_name_1.moduleName).directive("thinkbigTemplateAccessControl", directive);
});
//# sourceMappingURL=template-access-control.js.map