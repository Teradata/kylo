import * as angular from "angular";
import {moduleName} from "./module-name";
import {Transition} from "@uirouter/core";

export class controller implements ng.IComponentController{

    filter: string
    slaId: string
    static readonly $inject = ['$transition$']
    constructor(private $transition$: Transition){
        this.filter = this.$transition$.params().filter;
        this.slaId = this.$transition$.params().slaId;
    }



}

const module = angular.module(moduleName).controller("serviceLevelAssessmentsInitController", controller);
export default module;