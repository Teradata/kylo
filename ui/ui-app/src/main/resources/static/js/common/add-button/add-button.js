define(['angular','common/module-name'], function (angular,moduleName) {

    var directive = function ($http,$rootScope,$transitions,StateService, AddButtonService,BroadcastService) {
        return {
            restrict: "EA",
            scope:{},
            template: '<md-button class="md-fab md-fab-bottom-right kylo-add-button" aria-label="Add" ng-click="onClickAddButton($event)"><ng-md-icon icon="add"></ng-md-icon></md-button>',
            link: function ($scope, element, attrs) {

                $scope.currentState = '';


                $scope.onClickAddButton= function(event){
                    AddButtonService.onClick($scope.currentState);
                }

                function isShowAddButton(){
                     return AddButtonService.isShowAddButton($scope.currentState);
                }


                $transitions.onSuccess({},function(transition){
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

                BroadcastService.subscribe($scope,AddButtonService.NEW_ADD_BUTTON_EVENT,updateShowState)

                BroadcastService.subscribe($scope, AddButtonService.HIDE_ADD_BUTTON_EVENT, hideButton)
                BroadcastService.subscribe($scope, AddButtonService.SHOW_ADD_BUTTON_EVENT, showButton)

            }

        };
    }




    angular.module(moduleName)
        .directive('addButton', ['$http','$rootScope','$transitions','StateService','AddButtonService','BroadcastService',directive]);

});

