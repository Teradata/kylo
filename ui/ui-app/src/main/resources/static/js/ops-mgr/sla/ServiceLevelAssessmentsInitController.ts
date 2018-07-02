import * as angular from "angular";
import {moduleName} from "./module-name";
import {Transition} from "@uirouter/core";

export class controller implements ng.IComponentController{

    $transition$: Transition;

    constructor(){}

    filter: any = this.$transition$.params().filter;
    slaId: any = this.$transition$.params().slaId;
}

angular.module(moduleName).component("serviceLevelAssessmentsInitController", {
    controller: controller,
    bindings: {
        $transition$: "<"
    },
    controllerAs: "vm",
    templateUrl: "js/ops-mgr/sla/assessments.html"
});
