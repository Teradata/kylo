import * as angular from "angular";
import {moduleName} from "../module-name";

angular.module(moduleName)
        .directive('addButton', ['$http','$rootScope','$transitions','StateService','AddButtonService','BroadcastService',
 ($http: any,$rootScope: any,$transitions: any,StateService: any, AddButtonService: any,BroadcastService: any)=> {
        return {
            restrict: "EA",
            scope:{},
            template: '<md-button class="md-fab md-fab-bottom-right kylo-add-button" aria-label="Add" ng-click="onClickAddButton($event)"><ng-md-icon icon="add"></ng-md-icon></md-button>',
            link: function ($scope: any, element: any, attrs: any) {
                $scope.currentState = '';
                $scope.onClickAddButton= function(event: any){
                    this.AddButtonService.onClick($scope.currentState);
                }

                function isShowAddButton(){
                     return this.AddButtonService.isShowAddButton($scope.currentState);
                }

                this.$transitions.onSuccess({},function(transition: any){
                  var toState = transition.to();
                  if(toState != undefined) {
                      var state = toState.name;
                      if(toState.name == 'home'){
                          state = 'feeds';
                      }
                      $scope.currentState = state;
                      updateShowState();
                  }
                });

                function hideButton() {
                    element.hide();
                }

                function showButton() {
                    element.show();
                }

                function updateShowState(){
                     if(isShowAddButton()){
                        element.show();
                    }
                    else {
                        element.hide();
                    }
                }

                this.BroadcastService.subscribe($scope,this.AddButtonService.NEW_ADD_BUTTON_EVENT,updateShowState)
                this.BroadcastService.subscribe($scope, this.AddButtonService.HIDE_ADD_BUTTON_EVENT, hideButton)
                this.BroadcastService.subscribe($scope, this.AddButtonService.SHOW_ADD_BUTTON_EVENT, showButton)
            }
        };
    }
]);