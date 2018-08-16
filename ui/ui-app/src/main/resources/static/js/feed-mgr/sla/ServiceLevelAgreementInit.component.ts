import { Transition, StateService } from '@uirouter/core';
import { Component } from '@angular/core';

@Component({
    selector: 'service-level-agreement',
    templateUrl: 'js/feed-mgr/sla/service-level-agreements-view.html'
})
export default class ServiceLevelAgreementInit {
    
    slaId: any;

    ngOnInit(){
        this.slaId = this.state.params.slaId    
    }
       
    constructor(private state: StateService){}

}

