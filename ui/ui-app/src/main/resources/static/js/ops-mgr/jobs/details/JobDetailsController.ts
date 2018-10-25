import * as angular from "angular";
import {moduleName} from "./module-name";
import '../module-require';
import './module-require';

export class controller implements ng.IComponentController{
    executionId: any;
    constructor(private $transition$: any){
       this.executionId = $transition$.params().executionId;
    }
}

const module = angular.module(moduleName).controller('JobDetailsController',['$transition$',controller]);
export default module;