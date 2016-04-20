/*
 * Copyright (c) 2015.
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
                showPagination:'='
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
                        $scope.openedMenu();
                    }
                    if($scope.showPagination) {
                        var tabData = PaginationDataService.getActiveTabData($scope.menuKey);
                        $scope.currentPage = tabData.currentPage;
                        $scope.paginationId = tabData.paginationId;
                    }
                    $mdOpenMenu(ev);
                };

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
