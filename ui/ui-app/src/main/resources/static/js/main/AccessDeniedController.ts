import * as angular from 'angular';
import {StateService} from  "../services/StateService";
import AccessControlService from "../services/AccessControlService";
import {Transition} from "@uirouter/core";

export class AccessDeniedController implements ng.IComponentController{
    $transition$: Transition;
    static readonly $inject = ["$scope","$mdDialog","AccessControlService","StateService"];
    constructor(
        private $scope:angular.IScope,
        private $mdDialog:angular.material.IDialogService,
        private AccessControlService:AccessControlService,
        private StateService:StateService,  
       // private  $transition$:any
        ){
        var attemptedState = this.$transition$.params().attemptedState;
        if( attemptedState == undefined){
            attemptedState = {displayName:'the page'};
        }
        else if( attemptedState.displayName == undefined){
            attemptedState.displayName = attemptedState.name;
        }
        }
}

  angular.module('kylo').component("accessDeniedController", { 
        controller: AccessDeniedController,
        controllerAs: "vm",
        templateUrl: "./access-denied.html"
    });
  //.controller('AccessDeniedController',[AccessDeniedController]);

