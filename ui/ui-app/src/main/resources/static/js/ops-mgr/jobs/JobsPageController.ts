import * as angular from "angular";
import {moduleName} from "./module-name";

export class controller implements ng.IComponentController{
    filter: any;
    tab: any;
    constructor(private $transition$: any){
        this.filter = $transition$.params().filter;
        this.tab = $transition$.params().tab;
    }
}

  angular.module(moduleName).controller('JobsPageController',['$transition$',controller]);
