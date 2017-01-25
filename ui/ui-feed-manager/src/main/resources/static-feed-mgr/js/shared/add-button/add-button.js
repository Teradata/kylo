/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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




    angular.module(MODULE_FEED_MGR)
        .directive('addButton', ['$http','$rootScope','StateService','AddButtonService','BroadcastService',directive]);

})();

