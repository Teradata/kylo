define(['angular','ops-mgr/sla/module-name'], function (angular,moduleName) {

    var controller = function($transition$, $http,OpsManagerRestUrlService,StateService){
        var self = this;
        this.assessmentId = $transition$.params().assessmentId;

        self.loading = false;
        self.assessment = {};

        if(this.assessmentId != null){

            var successFn = function(response) {
                if (response.data) {
                    self.assessment = response.data;
                }
                self.loading = false;

            }
            var errorFn = function(err) {
                self.loading = false;
            }


            self.loading = true;
            $http.get(OpsManagerRestUrlService.GET_SLA_ASSESSMENT_URL(self.assessmentId)).then(successFn, errorFn);
        }

        self.serviceLevelAgreement= function(){
            StateService.FeedManager().Sla().navigateToServiceLevelAgreement(self.assessment.agreement.id);
        }


    };

    angular.module(moduleName).controller('ServiceLevelAssessmentController',['$transition$','$http','OpsManagerRestUrlService','StateService',controller]);

});
