import * as angular from "angular";
import {moduleName} from "./module-name";

export class controller implements ng.IComponentController{

    constructor(private $transition$: any){}

    filter: any = this.$transition$.params().filter;
    slaId: any = this.$transition$.params().slaId;
}


angular.module(moduleName).controller('ServiceLevelAssessmentsInitController',['$transition$',controller]);
