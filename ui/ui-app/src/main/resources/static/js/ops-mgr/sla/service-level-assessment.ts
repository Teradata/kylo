import * as angular from "angular";
import {moduleName} from "./module-name";
import OpsManagerRestUrlService from "../services/OpsManagerRestUrlService";
import { Transition } from "@uirouter/core";
export class controller implements ng.IComponentController{

    $transition$: Transition;
    static readonly $inject = ['$http','OpsManagerRestUrlService','StateService'];
    loading: boolean = false;
    assessment: any = {};
    assessmentNotFound: boolean = false;
    agreementNotFound: boolean = false;

    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {

        var assessmentId: any = this.$transition$.params().assessmentId;

        

        let getSlaById: any = function(slaId: any) {
            var successFn = (response: any)=> {
                return response.data;
            };
            var errorFn = (err: any) =>{
                console.log('ERROR ', err)
            };
            var promise = this.$http.get(this.opsManagerRestUrlService.GET_SLA_BY_ID_URL(slaId), {acceptStatus: 404});
            promise.then(successFn, errorFn);
            return promise;
        }

        let  serviceLevelAgreement: any= function() {
            this.StateService.FeedManager().Sla().navigateToServiceLevelAgreement(this.assessment.agreement.id);
        };

        if(assessmentId != null){
            var successFn = (response: any)=> {
                if (response.data && response.data != '') {
                    this.assessment = response.data;
                    this.assessmentNotFound = false;
                    getSlaById(this.assessment.agreement.id).then((response: any)=> {
                        this.agreementNotFound = response.status === 404;
                    }, function(){
                        this.agreementNotFound = true;
                    });
                }
                else {
                    this.assessmentNotFound = true;
                }
                this.loading = false;

            };
            var errorFn = (err: any) =>{
                this.loading = false;
            };

            this.loading = true;
            this.$http.get(this.opsManagerRestUrlService.GET_SLA_ASSESSMENT_URL(assessmentId)).then(successFn, errorFn);
        
        }
    }

    constructor(private $http: angular.IHttpService,
                private opsManagerRestUrlService: OpsManagerRestUrlService,
                private StateService: any){
                    
     }   

    
}

angular.module(moduleName).component("serviceLevelAssessmentController", {
    controller: controller,
    bindings: {
        $transition$: "<"
    },
    controllerAs: "vm",
    templateUrl: "js/ops-mgr/sla/assessment.html"
});

