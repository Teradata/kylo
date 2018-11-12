import * as angular from 'angular';
import {AccessControlService} from "../services/AccessControlService";
import {Transition} from "@uirouter/core";

export class AccessDeniedController implements ng.IComponentController{
    static readonly $inject = ["$scope","AccessControlService"];
    constructor(
        private $scope:angular.IScope,
        private AccessControlService:AccessControlService
        ){

        }
}

  angular.module('kylo').component("accessDeniedController", { 
        controller: AccessDeniedController,
        controllerAs: "vm",
        templateUrl: "./access-denied.html"
    });

