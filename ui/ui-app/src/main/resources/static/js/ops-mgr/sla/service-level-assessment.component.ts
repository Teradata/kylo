import {OpsManagerRestUrlService} from "../services/OpsManagerRestUrlService";
import { HttpClient } from "@angular/common/http";
import { Component } from "@angular/core";
import { StateService } from "@uirouter/angular";
import {StateService as StateServices} from '../../services/StateService';

@Component({
    selector: 'service-level-assessment',
    templateUrl: './assessment.html'
})
export class serviceLevelAssessmentComponent {


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

    constructor(private http: HttpClient,
                private opsManagerRestUrlService: OpsManagerRestUrlService,
                private transition: StateService,
                private stateService: StateServices) {


    }

    ngOnInit() {
        this.assessmentId = this.transition.params.assessmentId;

        if (this.assessmentId != null) {
            let successFn = (response: any) => {
                if (response && response != '') {
                    this.assessment = response;
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
            this.http.get(this.opsManagerRestUrlService.GET_SLA_ASSESSMENT_URL(this.assessmentId))
            .toPromise().then(successFn, errorFn);
        }

    }

    private getSlaById(slaId: string) {
        let successFn = (response: any) => {
            return response;
        };
        let errorFn = (err: any) => {
            console.log('ERROR ', err)
        };
        return this.http.get(this.opsManagerRestUrlService.GET_SLA_BY_ID_URL(slaId))
        .toPromise().then(successFn, errorFn);
    }

    serviceLevelAgreement() {
        this.stateService.FeedManager().Sla().navigateToServiceLevelAgreement(this.assessment.agreement.id);
    }

}
