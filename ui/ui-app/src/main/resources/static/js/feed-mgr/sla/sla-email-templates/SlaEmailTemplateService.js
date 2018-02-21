define(["require", "exports", "angular", "../module-name", "underscore"], function (require, exports, angular, module_name_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var SlaEmailTemplateService = /** @class */ (function () {
        function SlaEmailTemplateService($http, $q, $mdToast, $mdDialog, RestUrlService) {
            this.$http = $http;
            this.$q = $q;
            this.$mdToast = $mdToast;
            this.$mdDialog = $mdDialog;
            this.RestUrlService = RestUrlService;
            this.getExistingTemplates = function () {
            };
            this.newTemplateModel = function () {
                return { name: '', subject: '', template: '' };
            };
            this.template = null;
            this.module = angular.module(module_name_1.moduleName, []);
            this.module.factory('SlaEmailTemplateService', ["$http", "$q", "$mdToast", "$mdDialog", "RestUrlService", this.factoryFn.bind(this)]);
        }
        SlaEmailTemplateService.prototype.factoryFn = function () {
            this.getExistingTemplates();
            var injectables = [{ "item": "$sla.name", "desc": "The SLA Name." },
                { "item": "$sla.description", "desc": "The SLA Description." },
                { "item": "$assessmentDescription", "desc": "The SLA Assessment and result." }];
            var data = {
                template: this.template,
                templates: this.templates,
                templateMap: {},
                availableActions: this.availableActions,
                newTemplate: function () {
                    data.template = this.newTemplateModel();
                },
                getTemplateVariables: function () {
                    return injectables;
                },
                getExistingTemplates: function () {
                    var promise = this.$http.get("/proxy/v1/feedmgr/sla/email-template");
                    promise.then(function (response) {
                        if (response.data) {
                            data.templates = response.data;
                            data.templateMap = {};
                            _.each(data.templates, function (template) {
                                data.templateMap[template.id] = template;
                            });
                        }
                    });
                    return promise;
                },
                getRelatedSlas: function (id) {
                    return this.$http.get("/proxy/v1/feedmgr/sla/email-template-sla-references", { params: { "templateId": id } });
                },
                getTemplate: function (id) {
                    return data.templateMap[id];
                },
                getAvailableActionItems: function () {
                    var def = this.$q.defer();
                    if (data.availableActions.length == 0) {
                        this.$http.get("/proxy/v1/feedmgr/sla/available-sla-template-actions").then(function (response) {
                            if (response.data) {
                                data.availableActions = response.data;
                                def.resolve(data.availableActions);
                            }
                        });
                    }
                    else {
                        def.resolve(data.availableActions);
                    }
                    return def.promise;
                },
                validateTemplate: function (subject, templateString) {
                    if (angular.isUndefined(subject)) {
                        subject = data.template.subject;
                    }
                    if (angular.isUndefined(templateString)) {
                        templateString = data.template.template;
                    }
                    var testTemplate = { subject: subject, body: templateString };
                    return this.$http({
                        url: "/proxy/v1/feedmgr/sla/test-email-template",
                        method: "POST",
                        data: angular.toJson(testTemplate),
                        headers: {
                            'Content-Type': 'application/json; charset=UTF-8'
                        }
                    });
                },
                sendTestEmail: function (address, subject, templateString) {
                    if (angular.isUndefined(subject)) {
                        subject = data.template.subject;
                    }
                    if (angular.isUndefined(templateString)) {
                        templateString = data.template.template;
                    }
                    var testTemplate = { emailAddress: address, subject: subject, body: templateString };
                    return this.$http({
                        url: "/proxy/v1/feedmgr/sla/send-test-email-template",
                        method: "POST",
                        data: angular.toJson(testTemplate),
                        headers: {
                            'Content-Type': 'application/json; charset=UTF-8'
                        }
                    });
                },
                save: function (template) {
                    if (angular.isUndefined(template)) {
                        template = data.template;
                    }
                    if (template != null) {
                        return this.$http({
                            url: "/proxy/v1/feedmgr/sla/email-template",
                            method: "POST",
                            data: angular.toJson(template),
                            headers: {
                                'Content-Type': 'application/json; charset=UTF-8'
                            }
                        });
                    }
                },
                accessDeniedDialog: function () {
                    this.$mdDialog.show(this.$mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Access Denied")
                        .textContent("You do not have access to edit templates.")
                        .ariaLabel("Access denied to edit templates")
                        .ok("OK"));
                }
            };
            return data;
        };
        return SlaEmailTemplateService;
    }());
    exports.default = SlaEmailTemplateService;
});
//# sourceMappingURL=SlaEmailTemplateService.js.map