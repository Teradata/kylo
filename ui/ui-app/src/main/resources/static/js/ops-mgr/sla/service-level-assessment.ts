import * as angular from "angular";
import {moduleName} from "./module-name";
import OpsManagerRestUrlService from "../services/OpsManagerRestUrlService";

export class controller implements ng.IComponentController {


    /**
     * flag indicating we are loading the SLA assessment
     * @type {boolean}
     */
    loading: boolean = false;

    assessmentId: string;
    /**
     * the assessment
     * @type {{}}
     */
    assessment: any = {};
    /**
     * assessment not found flag
     * @type {boolean}
     */
    assessmentNotFound: boolean = false;
    /**
     * agreement not found flag
     * @type {boolean}
     */
    agreementNotFound: boolean = false;


    static readonly $inject = ['$transition$', '$http', 'OpsManagerRestUrlService', 'StateService']

    constructor(private $transition$: any,
                private $http: angular.IHttpService,
                private opsManagerRestUrlService: any,
                private stateService: any) {


    }

    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {
        this.assessmentId = this.$transition$.params().assessmentId;

        if (this.assessmentId != null) {
            let successFn = (response: any) => {
                if (response.data && response.data != '') {
                    this.assessment = response.data;
                    this.assessmentNotFound = false;
                    this.getSlaById(this.assessment.agreement.id).then((response: any) => {
                        this.agreementNotFound = response.status === 404;
                    }, () => {
                        this.agreementNotFound = true;
                    });
                }
                else {
                    this.assessmentNotFound = true;
                }
                this.loading = false;

            };
            var errorFn = (err: any) => {
                this.loading = false;
            };

            this.loading = true;
            this.$http.get(this.opsManagerRestUrlService.GET_SLA_ASSESSMENT_URL(this.assessmentId)).then(successFn, errorFn);
        }

    }

    private getSlaById(slaId: string) {
        let successFn = (response: angular.IHttpResponse<any>) => {
            return response.data;
        };
        let errorFn = (err: any) => {
            console.log('ERROR ', err)
        };
        var promise = this.$http.get(this.opsManagerRestUrlService.GET_SLA_BY_ID_URL(slaId));
        promise.then(successFn, errorFn);
        return promise;
    }

    serviceLevelAgreement() {
        this.stateService.FeedManager().Sla().navigateToServiceLevelAgreement(this.assessment.agreement.id);
    }


}

angular.module(moduleName)
    .controller('ServiceLevelAssessmentController', controller);

