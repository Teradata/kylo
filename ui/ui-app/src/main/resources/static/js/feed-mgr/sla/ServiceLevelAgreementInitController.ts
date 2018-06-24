import * as angular from 'angular';
import {moduleName} from './module-name';
import { Transition } from '@uirouter/core';

export default class ServiceLevelAgreementInitController  implements ng.IComponentController{
    
    $transition$: Transition;
    slaId: any;

    ngOnInit(){
        this.slaId = this.$transition$.params().slaId    
    }
    $onInit() {
            this.ngOnInit();
    }    
    constructor(){
    
    }

}

angular.module(moduleName).component('serviceLevelAgreementInitComponent',{
    bindings: {
        $transition$: '<'
    },
    controller: ServiceLevelAgreementInitController,
    controllerAs: "vm",
    templateUrl: "js/feed-mgr/sla/service-level-agreements-view.html"
});
