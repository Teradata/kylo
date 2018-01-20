define(['angular','feed-mgr/sla/module-name'], function (angular,moduleName) {
    angular.module(moduleName).factory('SlaEmailTemplateService', ["$http","$q","$mdToast","$mdDialog","RestUrlService",function ($http, $q, $mdToast, $mdDialog, RestUrlService) {

        function getExistingTemplates() {

        }

        getExistingTemplates();


        var injectables = [{"item":"$sla.name","desc":"The SLA Name."},
            {"item":"$sla.description","desc":"The SLA Description."},
            {"item":"$assessmentDescription","desc":"The SLA Assessment and result."}];


        function newTemplateModel() {
            return  {name: '', subject: '', template: ''};
        }

        var data = {

            template:null,
            templates:[],
            templateMap:{},
            availableActions:[],
            newTemplate:function(){
                data.template = newTemplateModel();
            },
            getTemplateVariables: function() {
                return injectables;
            },
            getExistingTemplates: function () {
                var promise = $http.get("/proxy/v1/feedmgr/sla/email-template");
                promise.then(function (response) {
                    if (response.data) {
                        data.templates = response.data;
                        data.templateMap = {};
                        _.each(data.templates,function(template){
                            data.templateMap[template.id] = template;
                        })
                    }
                });
                return promise;
            },

            getRelatedSlas: function(id) {
               return $http.get("/proxy/v1/feedmgr/sla/email-template-sla-references",{params:{"templateId":id}});
            },
            getTemplate:function(id){
                return data.templateMap[id];
            },
            getAvailableActionItems: function() {
                var def = $q.defer();
                if(data.availableActions.length == 0) {
                    $http.get("/proxy/v1/feedmgr/sla/available-sla-template-actions").then(function (response) {
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
            validateTemplate:function(subject,templateString) {
                if(angular.isUndefined(subject)){
                    subject = data.template.subject;
                }
                if(angular.isUndefined(templateString)){
                    templateString = data.template.template;
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
            sendTestEmail: function(address, subject, templateString) {
                if(angular.isUndefined(subject)){
                    subject = data.template.subject;
                }
                if(angular.isUndefined(templateString)){
                    templateString = data.template.template;
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
            save:function(template){
                if(angular.isUndefined(template)){
                    template = data.template;
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
            accessDeniedDialog:function() {
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
        return data;
    }]);


});