import * as angular from 'angular';

export class AccessDeniedController implements ng.IComponentController{
constructor(
        private $scope:angular.IScope,
        private $mdDialog:any,
        private AccessControlService:any,
        private StateService:any,  
        private  $transition$:any){

        var attemptedState = this.$transition$.params().attemptedState;
        if( attemptedState == undefined){
            attemptedState = {displayName:'the page'};
        }
        else if( attemptedState.displayName == undefined){
            attemptedState.displayName = attemptedState.name;
        }
        }
}

  angular.module('kylo').controller('AccessDeniedController', 
                                    ["$scope", 
                                    "$mdDialog", 
                                    "AccessControlService", 
                                    "StateService", 
                                    "$transition$",
                                    AccessDeniedController]);

