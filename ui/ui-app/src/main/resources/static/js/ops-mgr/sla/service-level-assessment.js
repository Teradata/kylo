define(['angular','ops-mgr/sla/module-name'], function (angular,moduleName) {

    var controller = function($transition$, $http,OpsManagerRestUrlService,StateService){
        var self = this;
        this.assessmentId = $transition$.params().assessmentId;

        self.loading = false;
        self.assessment = {};
        self.assessmentNotFound = false;
        self.agreementNotFound = false;

        if(this.assessmentId != null){

            var successFn = function(response) {
                if (response.data && response.data != '') {
                    self.assessment = response.data;
                    self.assessmentNotFound = false;

                    self.getSlaById(self.assessment.agreement.id).then(function(response) {
                        self.agreementNotFound = response.status === 404;
                    }, function(){
                        self.agreementNotFound = true;
                    });
                }
                else {
                    self.assessmentNotFound = true;
                }
                self.loading = false;

            };
            var errorFn = function(err) {
                self.loading = false;
            };


            self.loading = true;
            $http.get(OpsManagerRestUrlService.GET_SLA_ASSESSMENT_URL(self.assessmentId)).then(successFn, errorFn);
        }

        self.serviceLevelAgreement= function(){
            StateService.FeedManager().Sla().navigateToServiceLevelAgreement(self.assessment.agreement.id);
        };

        self.getSlaById = function (slaId) {
            var successFn = function (response) {
                return response.data;
            };
            var errorFn = function (err) {
                console.log('ERROR ', err)
            };
            var promise = $http.get(OpsManagerRestUrlService.GET_SLA_BY_ID_URL(slaId), {acceptStatus: 404});
            promise.then(successFn, errorFn);
            return promise;
        }
    };

    angular.module(moduleName).controller('ServiceLevelAssessmentController',['$transition$','$http','OpsManagerRestUrlService','StateService',controller]);

});
