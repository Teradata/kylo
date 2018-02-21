define(["require", "exports", "angular", "./module-name", "../services/OpsManagerRestUrlService"], function (require, exports, angular, module_name_1, OpsManagerRestUrlService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($transition$, $http, OpsManagerRestUrlService, StateService) {
            this.$transition$ = $transition$;
            this.$http = $http;
            this.OpsManagerRestUrlService = OpsManagerRestUrlService;
            this.StateService = StateService;
            var assessmentId = this.$transition$.params().assessmentId;
            var loading = false;
            var assessment = {};
            var assessmentNotFound = false;
            var agreementNotFound = false;
            var getSlaById = function (slaId) {
                var successFn = function (response) {
                    return response.data;
                };
                var errorFn = function (err) {
                    console.log('ERROR ', err);
                };
                var promise = this.$http.get(this.OpsManagerRestUrlService.GET_SLA_BY_ID_URL(slaId), { acceptStatus: 404 });
                promise.then(successFn, errorFn);
                return promise;
            };
            var serviceLevelAgreement = function () {
                this.StateService.FeedManager().Sla().navigateToServiceLevelAgreement(this.assessment.agreement.id);
            };
            if (assessmentId != null) {
                var successFn = function (response) {
                    if (response.data && response.data != '') {
                        assessment = response.data;
                        assessmentNotFound = false;
                        getSlaById(assessment.agreement.id).then(function (response) {
                            agreementNotFound = response.status === 404;
                        }, function () {
                            this.agreementNotFound = true;
                        });
                    }
                    else {
                        assessmentNotFound = true;
                    }
                    loading = false;
                };
                var errorFn = function (err) {
                    loading = false;
                };
                loading = true;
                $http.get(OpsManagerRestUrlService.GET_SLA_ASSESSMENT_URL(assessmentId)).then(successFn, errorFn);
            }
        }
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName)
        .service('OpsManagerRestUrlService', [OpsManagerRestUrlService_1.default])
        .controller('ServiceLevelAssessmentController', ['$transition$', '$http', 'OpsManagerRestUrlService', 'StateService', controller]);
});
//# sourceMappingURL=service-level-assessment.js.map