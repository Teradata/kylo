define(["require", "exports", "angular", "underscore", "../module-name"], function (require, exports, angular, _, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var TemplateOrderController = /** @class */ (function () {
        function TemplateOrderController($http, $mdToast, RestUrlService, RegisterTemplateService) {
            this.$http = $http;
            this.$mdToast = $mdToast;
            this.RestUrlService = RestUrlService;
            this.RegisterTemplateService = RegisterTemplateService;
            this.model = [];
        }
        TemplateOrderController.prototype.$onInit = function () {
            this.ngOnInit();
        };
        TemplateOrderController.prototype.ngOnInit = function () {
            //order list
            this.RegisterTemplateService.getRegisteredTemplates().then(function (response) {
                //order by .order
                var templates = _.sortBy(response.data, 'order');
                if (this.addAsNew && (this.templateId == null || this.templateId == undefined)) {
                    //append to bottom
                    templates.push({ id: 'NEW', templateName: this.templateName, currentTemplate: true });
                }
                else {
                    var currentTemplate = _.filter(templates, function (template) {
                        return template.id == this.templateId;
                    });
                    if (currentTemplate && currentTemplate.length == 1) {
                        currentTemplate[0].currentTemplate = true;
                    }
                }
                this.model = templates;
            });
        };
        TemplateOrderController.prototype.saveOrder = function () {
            var _this = this;
            var order = [];
            _.each(this.model, function (template) {
                order.push(template.id);
            });
            var successFn = function (response) {
                //toast created!!!
                var message = 'Saved the template order';
                _this.message = message;
                _this.$mdToast.show(_this.$mdToast.simple()
                    .textContent(message)
                    .hideDelay(3000));
            };
            var errorFn = function (err) {
                var message = 'Error ordering templates ' + err;
                _this.message = message;
                _this.$mdToast.simple()
                    .textContent(message)
                    .hideDelay(3000);
            };
            var obj = { templateIds: order };
            var promise = this.$http({
                url: this.RestUrlService.SAVE_TEMPLATE_ORDER_URL,
                method: "POST",
                data: angular.toJson(obj),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            }).then(successFn, errorFn);
        };
        TemplateOrderController.$inject = ["$http", "$mdToast", "RestUrlService", "RegisterTemplateService"];
        return TemplateOrderController;
    }());
    exports.TemplateOrderController = TemplateOrderController;
    angular.module(module_name_1.moduleName).component("templateOrderController", {
        bindings: {
            templateId: '=',
            templateName: '=',
            model: '=?',
            addAsNew: '=?',
            addSaveBtn: '=?'
        },
        controllerAs: 'vm',
        templateUrl: 'js/feed-mgr/templates/template-order/template-order.html',
        controller: TemplateOrderController,
    });
});
//# sourceMappingURL=template-order.js.map