import { Component } from "@angular/core";
import { StateService } from "@uirouter/core";

@Component({
    selector: 'jobs-page-controller',
    templateUrl: 'js/ops-mgr/jobs/jobs.html'
})
export class JobsPageComponent implements ng.IComponentController{

    filter: any;
    tab: any;
    
    constructor(private stateService: StateService){
        this.filter = stateService.params.filter;
        this.tab = stateService.params.tab;
    }
}