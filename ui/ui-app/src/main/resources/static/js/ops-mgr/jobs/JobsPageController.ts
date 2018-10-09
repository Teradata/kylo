import * as angular from "angular";
import {moduleName} from "./module-name";
import 'ops-mgr/jobs/module-require';

export class controller implements ng.IComponentController{
    filter: any;
    tab: any;
    constructor(private $transition$: any){
        this.filter = $transition$.params().filter;
        this.tab = $transition$.params().tab;
    }
}

const module = angular.module(moduleName).controller('JobsPageController',['$transition$',controller]);
export default module;