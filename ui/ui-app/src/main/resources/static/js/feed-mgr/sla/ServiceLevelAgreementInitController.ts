import * as angular from 'angular';
import {moduleName} from './module-name';

export default class ServiceLevelAgreementInitController  implements ng.IComponentController{
    slaId: any = this.$transition$.params().slaId;
    constructor(private $transition$: any){
        }
}

angular.module(moduleName).controller('ServiceLevelAgreementInitController',['$transition$',ServiceLevelAgreementInitController]);
