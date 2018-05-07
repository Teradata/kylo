import * as angular from 'angular';
import {moduleName} from '../module-name';
import * as _ from 'underscore';


export default class SlaEmailTemplateService{
    data: any = {};
    template: any = null;
    templates:any[];
    templateMap: any;
    availableActions: any[];
    constructor(private $http: any,
                private $q: any,
                private $mdToast: any,
                private $mdDialog: any,
                private RestUrlService: any,
    ){
        this.getExistingTemplates();


        var injectables = [{"item":"$sla.name","desc":"The SLA Name."},
            {"item":"$sla.description","desc":"The SLA Description."},
            {"item":"$assessmentDescription","desc":"The SLA Assessment and result."}];

        this.data = {
            template:this.template,
            templates:this.templates,
            templateMap:{},
            availableActions:[],
            newTemplate:()=>{
                this.data.template = this.newTemplateModel();
            },
            getTemplateVariables:()=> {
                return injectables;
            },
            getExistingTemplates:  ()=> {
                var promise = $http.get("/proxy/v1/feedmgr/sla/email-template");
                promise.then( (response :any)=>{
                    if (response.data) {
                        this.data.templates = response.data;
                        this.data.templateMap = {};
                        _.each(this.data.templates,(template :any)=>{
                            this.data.templateMap[template.id] = template;
                        })
                    }
                });
                return promise;
            },

            getRelatedSlas: (id: any)=>{
               return $http.get("/proxy/v1/feedmgr/sla/email-template-sla-references",{params:{"templateId":id}});
            },
            getTemplate:(id: any)=>{
                return this.data.templateMap[id];
            },
            getAvailableActionItems:()=> {
                var def = $q.defer();
                if(this.data.availableActions.length == 0) {
                    $http.get("/proxy/v1/feedmgr/sla/available-sla-template-actions").then((response: any)=>{
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
            },
            validateTemplate:(subject: any,templateString: any)=>{
                if(angular.isUndefined(subject)){
                    subject = this.data.template.subject;
                }
                if(angular.isUndefined(templateString)){
                    templateString = this.data.template.template;
                }
                var testTemplate =  {subject:subject,body:templateString};
                return $http({
                        url: "/proxy/v1/feedmgr/sla/test-email-template",
                        method: "POST",
                        data:angular.toJson(testTemplate),
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    }
                    });
            },
            sendTestEmail:(address: any, subject: any, templateString: any)=>{
                if(angular.isUndefined(subject)){
                    subject = this.data.template.subject;
                }
                if(angular.isUndefined(templateString)){
                    templateString = this.data.template.template;
                }
                var testTemplate =  {emailAddress:address,subject:subject,body:templateString};
                return $http({
                    url: "/proxy/v1/feedmgr/sla/send-test-email-template",
                    method: "POST",
                    data:angular.toJson(testTemplate),
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    }
                });
            },
            save:(template: any)=>{
                if(angular.isUndefined(template)){
                    template = this.data.template;
                }
                if(template != null) {
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
            accessDeniedDialog:()=> {
                $mdDialog.show(
                    $mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Access Denied")
                        .textContent("You do not have access to edit templates.")
                        .ariaLabel("Access denied to edit templates")
                        .ok("OK")
                );
            }
        }
        return this.data;
   }
    getExistingTemplates=function() {
    }
    newTemplateModel=function() {
        return  {name: '', subject: '', template: ''};
    }
    

}

angular.module(moduleName)
.factory('SlaEmailTemplateService',["$http","$q","$mdToast","$mdDialog","RestUrlService",SlaEmailTemplateService]);
