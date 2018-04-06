import * as angular from "angular";
import {moduleName} from "./module-name";
import OpsManagerRestUrlService from "../services/OpsManagerRestUrlService";
export class controller implements ng.IComponentController{
    constructor(private $transition$: any,
                private $http: any,
                private OpsManagerRestUrlService: any,
                private StateService: any){
                    
    var assessmentId: any = this.$transition$.params().assessmentId;

    var loading:boolean = false;
    var assessment: any = {};
    var assessmentNotFound: boolean = false;
    var agreementNotFound: boolean = false;
     let getSlaById: any = function(slaId: any) {
            var successFn = (response: any)=> {
                return response.data;
            };
            var errorFn = (err: any) =>{
                console.log('ERROR ', err)
            };
            var promise = this.$http.get(this.OpsManagerRestUrlService.GET_SLA_BY_ID_URL(slaId), {acceptStatus: 404});
            promise.then(successFn, errorFn);
            return promise;
        }
          let  serviceLevelAgreement: any= function(){
            this.StateService.FeedManager().Sla().navigateToServiceLevelAgreement(this.assessment.agreement.id);
        };
    if(assessmentId != null){
            var successFn = (response: any)=> {
                if (response.data && response.data != '') {
                    assessment = response.data;
                    assessmentNotFound = false;
                    getSlaById(assessment.agreement.id).then((response: any)=> {
                        agreementNotFound = response.status === 404;
                    }, function(){
                        this.agreementNotFound = true;
                    });
                }
                else {
                    assessmentNotFound = true;
                }
                loading = false;

            };
            var errorFn = (err: any) =>{
                loading = false;
            };

            loading = true;
            $http.get(OpsManagerRestUrlService.GET_SLA_ASSESSMENT_URL(assessmentId)).then(successFn, errorFn);
            }
     }   

    
}

angular.module(moduleName)
        .service('OpsManagerRestUrlService', [OpsManagerRestUrlService])
        .controller('ServiceLevelAssessmentController',['$transition$','$http','OpsManagerRestUrlService','StateService',controller]);

