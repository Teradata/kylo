import * as angular from 'angular';
import * as _ from 'underscore';
import { Injectable, Inject } from '@angular/core';
import { RestUrlService } from '../../services/RestUrlService';

@Injectable()
export default class SlaEmailTemplateService {
    data: any = {};
    template: any = null;
    templates: any[];
    templateMap: any;
    availableActions: any[];
    
    constructor(private RestUrlService: RestUrlService,
                @Inject("$injector") private $injector: any) {
        
        this.data = {
            template:this.template,
            templates:this.templates,
            templateMap:{},
            availableActions:[]
        }            
        this.getExistingTemplates();

    };
    newTemplate = () => {
        this.data.template = this.newTemplateModel();
    };
    getTemplateVariables = () => {
        var injectables = [{ "item": "$sla.name", "desc": "The SLA Name." },
        { "item": "$sla.description", "desc": "The SLA Description." },
        { "item": "$assessmentDescription", "desc": "The SLA Assessment and result." }];
        return injectables;
    }
    getExistingTemplates = () => {
        var promise = this.$injector.get("$http").get("/proxy/v1/feedmgr/sla/email-template");
        promise.then((response: any) => {
            if (response.data) {
                this.data.templates = response.data;
                this.data.templateMap = {};
                _.each(this.data.templates, (template: any) => {
                    this.data.templateMap[template.id] = template;
                })
            }
        });
        return promise;
    };

    getRelatedSlas = (id: any) => {
        return this.$injector.get("$http").get("/proxy/v1/feedmgr/sla/email-template-sla-references", { params: { "templateId": id } });
    };
    getTemplate = (id: any) => {
        return this.data.templateMap[id];
    };
    getAvailableActionItems = () => {
        var def = this.$injector.get("$q").defer();
        if (this.data.availableActions == undefined || this.data.availableActions == null || this.data.availableActions.length == 0) {
            this.$injector.get("$http").get("/proxy/v1/feedmgr/sla/available-sla-template-actions").then((response: any) => {
                if (response.data) {
                    this.data.availableActions = response.data;
                    def.resolve(this.data.availableActions);
                }
            });
        }
        else {
            def.resolve(this.data.availableActions);
        }
        return def.promise;
    }
    validateTemplate = (subject: any, templateString: any) => {
        if (angular.isUndefined(subject)) {
            subject = this.template.subject;
        }
        if (angular.isUndefined(templateString)) {
            templateString = this.template.template;
        }
        var testTemplate = { subject: subject, body: templateString };
        return this.$injector.get("$http")({
            url: "/proxy/v1/feedmgr/sla/test-email-template",
            method: "POST",
            data: angular.toJson(testTemplate),
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }
        });
    };
    sendTestEmail = (address: any, subject: any, templateString: any) => {
        if (angular.isUndefined(subject)) {
            subject = this.template.subject;
        }
        if (angular.isUndefined(templateString)) {
            templateString = this.template.template;
        }
        var testTemplate = { emailAddress: address, subject: subject, body: templateString };
        return this.$injector.get("$http")({
            url: "/proxy/v1/feedmgr/sla/send-test-email-template",
            method: "POST",
            data: angular.toJson(testTemplate),
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }
        });
    };
    save = (template: any) => {
        if (angular.isUndefined(template)) {
            template = this.data.template;
        }
        if (template != null) {
            return this.$injector.get("$http")({
                url: "/proxy/v1/feedmgr/sla/email-template",
                method: "POST",
                data: angular.toJson(template),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            });
        }
    };
    accessDeniedDialog = () => {
        this.$injector.get("$mdDialog").show(
            this.$injector.get("$mdDialog").alert()
                .clickOutsideToClose(true)
                .title("Access Denied")
                .textContent("You do not have access to edit templates.")
                .ariaLabel("Access denied to edit templates")
                .ok("OK")
        );
    }
    newTemplateModel = () => {
        this.template = { name: '', subject: '', template: '' };
        return this.template;
    }
}
