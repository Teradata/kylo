/*-
 * #%L
 * thinkbig-ui-common
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

    var directive = function ($mdDialog, PaginationDataService) {
        return {
            restrict: "E",
            scope: {
                sortOptions: "=",
                selectedOption: "&",
                openedMenu:"&",
                menuIcon: "@",
                menuKey:"@",
                tabs:'=',
                rowsPerPageOptions:"=",
                showViewType:'=',
                showPagination: '=',
                additionalOptions: '=?',
                selectedAdditionalOption: "&?",
            },
            templateUrl: 'js/shared/options-menu/options-menu-template.html',
            link: function ($scope, element, attrs) {
               if($scope.showViewType) {
                   $scope.viewType = {label:'List View', icon:'list', value:'list', type:'viewType'};
               }

                $scope.getPaginationId = function(tab){
                    return PaginationDataService.paginationId($scope.menuKey,tab.title);
                }

                $scope.getCurrentPage = function(tab){
                    return PaginationDataService.currentPage($scope.menuKey, tab.title);
                }


                function setViewTypeOption(toggle){
                    $scope.viewType.value =  PaginationDataService.viewType($scope.menuKey);

                    if(toggle == true) {
                        $scope.viewType.value =  $scope.viewType.value  == 'list' ? 'table' : 'list';
                    }
                    if( $scope.viewType.value == 'list'){
                        $scope.viewType.label = 'List View';
                        $scope.viewType.icon = 'list';
                    }
                    else {
                        $scope.viewType.label = 'Table View';
                        $scope.viewType.icon = 'grid_on';
                    }
                }
                if($scope.showViewType) {
                    //toggle the view Type so its opposite the current view type
                    setViewTypeOption(true);
                }




                $scope.rowsPerPage = 5;
                $scope.paginationData = PaginationDataService.paginationData($scope.menuKey);
                var originatorEv;
                $scope.openMenu = function($mdOpenMenu, ev) {

                    originatorEv = ev;
                    if($scope.openedMenu) {
                        $scope.openedMenu()({sortOptions: $scope.sortOptions, additionalOptions: $scope.additionalOptions});
                    }
                    if($scope.showPagination) {
                        var tabData = PaginationDataService.getActiveTabData($scope.menuKey);
                        $scope.currentPage = tabData.currentPage;
                        $scope.paginationId = tabData.paginationId;
                    }
                    $mdOpenMenu(ev);
                };

                /**
                 * Selected an additional option
                 * @param item
                 */
                $scope.selectAdditionalOption = function (item) {
                    var itemCopy = {};
                    angular.extend(itemCopy, item);

                    if ($scope.selectedAdditionalOption) {
                        $scope.selectedAdditionalOption()(itemCopy);
                    }
                }

                /**
                 * Selected a Sort Option
                 * @param item
                 */
                $scope.selectOption = function(item) {

                    var itemCopy = {};
                    angular.extend(itemCopy,item);
                    if(item.type == 'viewType'){
                        PaginationDataService.toggleViewType($scope.menuKey);
                        setViewTypeOption(true);
                    }

                    if($scope.selectedOption) {
                        $scope.selectedOption()(itemCopy);
                    }

                       originatorEv = null;
                }

                $scope.$on('$destroy', function () {

                });






            }

        }
    };


    angular.module(COMMON_APP_MODULE_NAME)
        .directive('tbaOptionsMenu', ['$mdDialog','PaginationDataService',directive]);

}());
