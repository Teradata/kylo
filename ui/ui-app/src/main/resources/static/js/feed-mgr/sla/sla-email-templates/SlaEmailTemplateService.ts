import * as angular from 'angular';
import {moduleName} from '../module-name';
import * as _ from 'underscore';


export default class SlaEmailTemplateService{
    module: ng.IModule;
    constructor(private $http: any,
                private $q: any,
                private $mdToast: any,
                private $mdDialog: any,
                private RestUrlService: any,
    ){
    this.module = angular.module(moduleName,[]);
    this.module.factory('SlaEmailTemplateService',["$http","$q","$mdToast","$mdDialog","RestUrlService",this.factoryFn.bind(this)]);
}
    getExistingTemplates= function() {
    }
    newTemplateModel= function() {
        return  {name: '', subject: '', template: ''};
    }
    template: any = null;
    templates:any[];
    templateMap: any;
    availableActions: any[];

factoryFn() {
        this.getExistingTemplates();


        var injectables = [{"item":"$sla.name","desc":"The SLA Name."},
            {"item":"$sla.description","desc":"The SLA Description."},
            {"item":"$assessmentDescription","desc":"The SLA Assessment and result."}];

        var data = {
            template:this.template,
            templates:this.templates,
            templateMap:{},
            availableActions:this.availableActions,
            newTemplate:function(){
                data.template = this.newTemplateModel();
            },
            getTemplateVariables: function() {
                return injectables;
            },
            getExistingTemplates: function () {
                var promise = this.$http.get("/proxy/v1/feedmgr/sla/email-template");
                promise.then(function (response :any) {
                    if (response.data) {
                        data.templates = response.data;
                        data.templateMap = {};
                        _.each(data.templates,function(template :any){
                            data.templateMap[template.id] = template;
                        })
                    }
                });
                return promise;
            },

            getRelatedSlas: function(id: any) {
               return this.$http.get("/proxy/v1/feedmgr/sla/email-template-sla-references",{params:{"templateId":id}});
            },
            getTemplate:function(id: any){
                return data.templateMap[id];
            },
            getAvailableActionItems: function() {
                var def = this.$q.defer();
                if(data.availableActions.length == 0) {
                    this.$http.get("/proxy/v1/feedmgr/sla/available-sla-template-actions").then(function (response: any) {
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
            validateTemplate:function(subject: any,templateString: any) {
                if(angular.isUndefined(subject)){
                    subject = data.template.subject;
                }
                if(angular.isUndefined(templateString)){
                    templateString = data.template.template;
                }
                var testTemplate =  {subject:subject,body:templateString};
                return this.$http({
                        url: "/proxy/v1/feedmgr/sla/test-email-template",
                        method: "POST",
                        data:angular.toJson(testTemplate),
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    }
                    });
            },
            sendTestEmail: function(address: any, subject: any, templateString: any) {
                if(angular.isUndefined(subject)){
                    subject = data.template.subject;
                }
                if(angular.isUndefined(templateString)){
                    templateString = data.template.template;
                }
                var testTemplate =  {emailAddress:address,subject:subject,body:templateString};
                return this.$http({
                    url: "/proxy/v1/feedmgr/sla/send-test-email-template",
                    method: "POST",
                    data:angular.toJson(testTemplate),
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    }
                });
            },
            save:function(template: any){
                if(angular.isUndefined(template)){
                    template = data.template;
                }
                if(template != null) {
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
            accessDeniedDialog:function() {
                this.$mdDialog.show(
                    this.$mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Access Denied")
                        .textContent("You do not have access to edit templates.")
                        .ariaLabel("Access denied to edit templates")
                        .ok("OK")
                );
            }


        }
        return data;

}
}