import * as angular from 'angular';
import { Transition, StateService } from '@uirouter/core';
import { Component } from '@angular/core';

@Component({
    selector: 'service-level-agreement',
    templateUrl: 'js/feed-mgr/sla/service-level-agreements-view.html'
})
export default class ServiceLevelAgreementInitController {
    
    slaId: any;

    ngOnInit(){
        this.slaId = this.state.params.slaId    
    }
       
    constructor(private state: StateService){}

}

