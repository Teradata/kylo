define(["require", "exports", "angular", "underscore", "../module-name"], function (require, exports, angular, _, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                templateId: '=',
                templateName: '=',
                model: '=?',
                addAsNew: '=?',
                addSaveBtn: '=?'
            },
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/feed-mgr/templates/template-order/template-order.html',
            controller: "TemplateOrderController",
            link: function ($scope, element, attrs, controller) {
            }
        };
    };
    var TemplateOrderController = /** @class */ (function () {
        function TemplateOrderController($http, $mdToast, RestUrlService, RegisterTemplateService) {
            this.$http = $http;
            this.$mdToast = $mdToast;
            this.RestUrlService = RestUrlService;
            this.RegisterTemplateService = RegisterTemplateService;
            var self = this;
            self.model = [];
            //order list
            RegisterTemplateService.getRegisteredTemplates().then(function (response) {
                //order by .order
                var templates = _.sortBy(response.data, 'order');
                if (self.addAsNew && (self.templateId == null || self.templateId == undefined)) {
                    //append to bottom
                    templates.push({ id: 'NEW', templateName: self.templateName, currentTemplate: true });
                }
                else {
                    var currentTemplate = _.filter(templates, function (template) {
                        return template.id == self.templateId;
                    });
                    if (currentTemplate && currentTemplate.length == 1) {
                        currentTemplate[0].currentTemplate = true;
                    }
                }
                self.model = templates;
            });
            self.saveOrder = function () {
                var order = [];
                _.each(self.model, function (template) {
                    order.push(template.id);
                });
                var successFn = function (response) {
                    //toast created!!!
                    var message = 'Saved the template order';
                    self.message = message;
                    $mdToast.show($mdToast.simple()
                        .textContent(message)
                        .hideDelay(3000));
                };
                var errorFn = function (err) {
                    var message = 'Error ordering templates ' + err;
                    self.message = message;
                    $mdToast.simple()
                        .textContent(message)
                        .hideDelay(3000);
                };
                var obj = { templateIds: order };
                var promise = $http({
                    url: RestUrlService.SAVE_TEMPLATE_ORDER_URL,
                    method: "POST",
                    data: angular.toJson(obj),
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    }
                }).then(successFn, errorFn);
            };
        }
        return TemplateOrderController;
    }());
    exports.TemplateOrderController = TemplateOrderController;
    angular.module(module_name_1.moduleName).controller("TemplateOrderController", ["$http", "$mdToast", "RestUrlService", "RegisterTemplateService", TemplateOrderController]);
    angular.module(module_name_1.moduleName).directive("thinkbigTemplateOrder", directive);
});
//# sourceMappingURL=template-order.js.map