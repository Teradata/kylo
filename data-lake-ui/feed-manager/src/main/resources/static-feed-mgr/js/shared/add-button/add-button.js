(function () {

    var directive = function ($http,$rootScope,StateService, AddButtonService,BroadcastService) {
        return {
            restrict: "EA",
            scope:{},
            template: '<md-button class="md-fab md-fab-top-right" aria-label="Add" ng-click="onClickAddButton($event)"><ng-md-icon icon="add"></ng-md-icon></md-button>',
            link: function ($scope, element, attrs) {
                function AddButtonDirectiveTag() {
                }

                this.__tag = new AddButtonDirectiveTag();


                $scope.currentState = '';


                $scope.onClickAddButton= function(event){
                    AddButtonService.onClick($scope.currentState);
                }

                function isShowAddButton(){
                     return AddButtonService.isShowAddButton($scope.currentState);
                }

                $rootScope.$on('$stateChangeSuccess',function(event,toState){
                    var state = toState.name;
                    if(toState.name == 'home'){
                        state = 'feeds';
                    }
                    $scope.currentState = state;
                    updateShowState();
                });

                function updateShowState(){
                     if(isShowAddButton()){
                        element.show();
                    }
                    else {
                        element.hide();
                    }
                }

                BroadcastService.subscribe($scope,AddButtonService.NEW_ADD_BUTTON_EVENT,updateShowState)

                BroadcastService.subscribe($scope,AddButtonService.NEW_ADD_BUTTON_EVENT,updateShowState)

            }

        };
    }




    angular.module(MODULE_FEED_MGR)
        .directive('addButton', ['$http','$rootScope','StateService','AddButtonService','BroadcastService',directive]);

})();

