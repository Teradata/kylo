define(["require", "exports", "angular", "../module-name", "underscore"], function (require, exports, angular, module_name_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var SlaEmailTemplateService = /** @class */ (function () {
        function SlaEmailTemplateService($http, $q, $mdToast, $mdDialog, RestUrlService) {
            var _this = this;
            this.$http = $http;
            this.$q = $q;
            this.$mdToast = $mdToast;
            this.$mdDialog = $mdDialog;
            this.RestUrlService = RestUrlService;
            this.data = {};
            this.template = null;
            this.getExistingTemplates = function () {
            };
            this.newTemplateModel = function () {
                return { name: '', subject: '', template: '' };
            };
            this.getExistingTemplates();
            var injectables = [{ "item": "$sla.name", "desc": "The SLA Name." },
                { "item": "$sla.description", "desc": "The SLA Description." },
                { "item": "$assessmentDescription", "desc": "The SLA Assessment and result." }];
            this.data = {
                template: this.template,
                templates: this.templates,
                templateMap: {},
                availableActions: [],
                newTemplate: function () {
                    _this.data.template = _this.newTemplateModel();
                },
                getTemplateVariables: function () {
                    return injectables;
                },
                getExistingTemplates: function () {
                    var promise = $http.get("/proxy/v1/feedmgr/sla/email-template");
                    promise.then(function (response) {
                        if (response.data) {
                            _this.data.templates = response.data;
                            _this.data.templateMap = {};
                            _.each(_this.data.templates, function (template) {
                                _this.data.templateMap[template.id] = template;
                            });
                        }
                    });
                    return promise;
                },
                getRelatedSlas: function (id) {
                    return $http.get("/proxy/v1/feedmgr/sla/email-template-sla-references", { params: { "templateId": id } });
                },
                getTemplate: function (id) {
                    return _this.data.templateMap[id];
                },
                getAvailableActionItems: function () {
                    var def = $q.defer();
                    if (_this.data.availableActions.length == 0) {
                        $http.get("/proxy/v1/feedmgr/sla/available-sla-template-actions").then(function (response) {
                            if (response.data) {
                                _this.data.availableActions = response.data;
                                def.resolve(_this.data.availableActions);
                            }
                        });
                    }
                    else {
                        def.resolve(_this.data.availableActions);
                    }
                    return def.promise;
                },
                validateTemplate: function (subject, templateString) {
                    if (angular.isUndefined(subject)) {
                        subject = _this.data.template.subject;
                    }
                    if (angular.isUndefined(templateString)) {
                        templateString = _this.data.template.template;
                    }
                    var testTemplate = { subject: subject, body: templateString };
                    return $http({
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
                        subject = _this.data.template.subject;
                    }
                    if (angular.isUndefined(templateString)) {
                        templateString = _this.data.template.template;
                    }
                    var testTemplate = { emailAddress: address, subject: subject, body: templateString };
                    return $http({
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
                        template = _this.data.template;
                    }
                    if (template != null) {
                        return $http({
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
                    $mdDialog.show($mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Access Denied")
                        .textContent("You do not have access to edit templates.")
                        .ariaLabel("Access denied to edit templates")
                        .ok("OK"));
                }
            };
            return this.data;
        }
        return SlaEmailTemplateService;
    }());
    exports.default = SlaEmailTemplateService;
    angular.module(module_name_1.moduleName)
        .factory('SlaEmailTemplateService', ["$http", "$q", "$mdToast", "$mdDialog", "RestUrlService", SlaEmailTemplateService]);
});
//# sourceMappingURL=SlaEmailTemplateService.js.map