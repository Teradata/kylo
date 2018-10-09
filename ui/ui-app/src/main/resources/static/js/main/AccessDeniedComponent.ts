import * as angular from 'angular';
import { Component } from '@angular/core';
import { StateService } from '@uirouter/core';

@Component({
    selector: 'access-denied-controller',
    templateUrl:'js/main/access-denied.html'
})
export class AccessDeniedComponent {
    
    ngOnInit() {
        var attemptedState = this.stateService.params.attemptedState;
        if( attemptedState == undefined){
            attemptedState = {displayName:'the page'};
        }
        else if( attemptedState.displayName == undefined){
            attemptedState.displayName = attemptedState.name;
        }
    }
    
    constructor(private stateService: StateService) {}
}

