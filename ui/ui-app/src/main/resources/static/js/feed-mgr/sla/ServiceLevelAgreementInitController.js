define(['angular','feed-mgr/sla/module-name'], function (angular,moduleName) {

    var controller = function($transition$){
        var self = this;
        this.slaId = $transition$.params().slaId;

        /*self.loading = false;
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
        */


    };

    angular.module(moduleName).controller('ServiceLevelAgreementInitController',['$transition$',controller]);

});
