import { Component } from "@angular/core";
import { StateService } from "@uirouter/core";

@Component({
    selector: 'jobs-page-controller',
    templateUrl: './jobs.html'
})
export class JobsPageComponent implements ng.IComponentController{

    filter: any;
    tab: any;
    
    constructor(private stateService: StateService){
        this.filter = this.stateService.params.filter;
        this.tab = this.stateService.params.tab;
    }
}
