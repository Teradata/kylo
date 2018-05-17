import * as angular from "angular";
import {moduleName} from "../module-name";

export default class OptionsMenu implements ng.IComponentController {

    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {

        if (this.$scope.showViewType) {
            this.$scope.viewType = {label: 'List View', icon: 'list', value: 'list', type: 'viewType'};
        }

        this.$scope.getPaginationId = function (tab: any) {
            return this.PaginationDataService.paginationId(this.$scope.menuKey, tab.title);
        };

        this.$scope.getCurrentPage = function (tab: any) {
            return this.PaginationDataService.currentPage(this.$scope.menuKey, tab.title);
        };

        if (this.$scope.showViewType) {
            //toggle the view Type so its opposite the current view type
            this.setViewTypeOption(true);
        }

        this.$scope.rowsPerPage = 5;
        this.$scope.paginationData = this.PaginationDataService.paginationData(this.$scope.menuKey);
        var originatorEv;
        this.$scope.openMenu = function ($mdOpenMenu: any, ev: any) {

            originatorEv = ev;
            if (angular.isFunction(this.$scope.openedMenu)) {
                var openedMenuFn = this.$scope.openedMenu();
                if (angular.isFunction(openedMenuFn)) {
                    openedMenuFn({sortOptions: this.$scope.sortOptions, additionalOptions: this.$scope.additionalOptions});
                }
            }
            if (this.$scope.showPagination) {
                var tabData = this.PaginationDataService.getActiveTabData(this.$scope.menuKey);
                this.$scope.currentPage = tabData.currentPage;
                this.$scope.paginationId = tabData.paginationId;
            }
            $mdOpenMenu(ev);
        };

        /**
         * Selected an additional option
         * @param item
         */
        this.$scope.selectAdditionalOption = (item: any) => {
            if (this.$scope.selectedAdditionalOption) {
                originatorEv = null;
                this.$scope.selectedAdditionalOption()(item);
            }
        };

        /**
         * Selected a Sort Option
         * @param item
         */
        this.$scope.selectOption = (item: any) => {

            var itemCopy = {};
            angular.extend(itemCopy, item);
            if (item.type === 'viewType') {
                this.PaginationDataService.toggleViewType(this.$scope.menuKey);
                this.setViewTypeOption(true);
            }

            if (this.$scope.selectedOption) {
                this.$scope.selectedOption()(itemCopy);
            }

            originatorEv = null;
        };
    }

    static readonly $inject = ["$scope","PaginationDataService"];

    constructor(private $scope: IScope,
                private PaginationDataService: any) {

        $scope.$on('$destroy', function () {

        });
    }

    setViewTypeOption(toggle: any) {
        
        this.$scope.viewType.value = this.PaginationDataService.viewType(this.$scope.menuKey);

        if (toggle === true) {
            this.$scope.viewType.value = this.$scope.viewType.value === 'list' ? 'table' : 'list';
        }
        if (this.$scope.viewType.value === 'list') {
            this.$scope.viewType.label = 'List View';
            this.$scope.viewType.icon = 'list';
        }
        else {
            this.$scope.viewType.label = 'Table View';
            this.$scope.viewType.icon = 'grid_on';
        }
    }
}

angular.module(moduleName).component('tbaOptionsMenu', {
    controller: OptionsMenu,
    templateUrl: 'js/common/options-menu/options-menu-template.html'
});
