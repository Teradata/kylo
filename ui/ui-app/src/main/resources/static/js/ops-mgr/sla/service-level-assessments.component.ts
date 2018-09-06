import {Transition, StateService} from "@uirouter/core";
import { Component } from "@angular/core";

@Component({
    selector: 'service-level-assessments',
    templateUrl: 'js/ops-mgr/sla/assessments.html'
})
export class serviceLevelAssessmentsComponent {

    filter: string;
    slaId: string;
    constructor(private stateService: StateService){}

    ngOnInit() {
        this.filter = this.stateService.params.filter;
        this.slaId = this.stateService.params.slaId;
    }
    
}
