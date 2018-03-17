import * as angular from "angular";
import {moduleName} from "./module-name";

export class controller implements ng.IComponentController{
    executionId: any;
    constructor(private $transition$: any){
       this.executionId = $transition$.params().executionId;
    }
}

  angular.module(moduleName).controller('JobDetailsController',['$transition$',controller]);
