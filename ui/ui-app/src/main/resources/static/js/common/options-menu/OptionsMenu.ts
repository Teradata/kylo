import * as angular from "angular";
import {moduleName} from "../module-name";
import { DefaultPaginationDataService } from "../../services/PaginationDataService";

export default class OptionsMenu {

    sortOptions: any;
    selectedOption: any;
    openedMenu: any;
    menuIcon: any;
    menuKey: any;
    tabs: any;
    rowsPerPageOptions: any;
    showViewType: any;
    showPagination: any;
    additionalOptions: any;
    selectedAdditionalOption: any;
    originatorEv: any;
    rowsPerPage: any;
    paginationData: any;
    viewType: any;
    currentPage: any;
    paginationId: any;

    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {

        if (this.showViewType) {
            this.viewType = {label: 'List View', icon: 'list', value: 'list', type: 'viewType'};
        }

        if (this.showViewType) {
            //toggle the view Type so its opposite the current view type
            this.setViewTypeOption(true);
        }

        this.rowsPerPage = 5;
        this.paginationData = this.PaginationDataService.paginationData(this.menuKey);
    }

    static readonly $inject = ["PaginationDataService"];

    constructor(private PaginationDataService: DefaultPaginationDataService) {
    }

    getPaginationId(tab: any) {
        return this.PaginationDataService.paginationId(this.menuKey, tab.title);
    };

    getCurrentPage(tab: any) {
        return this.PaginationDataService.currentPage(this.menuKey, tab.title);
    };

    openMenu($mdOpenMenu: any, ev: any) {

        this.originatorEv = ev;
        if (angular.isFunction(this.openedMenu)) {
            let openedMenuFn = this.openedMenu();
            if (angular.isFunction(openedMenuFn)) {
                openedMenuFn({sortOptions: this.sortOptions, additionalOptions: this.additionalOptions});
            }
        }
        if (this.showPagination) {
            let tabData = this.PaginationDataService.getActiveTabData(this.menuKey);
            this.currentPage = tabData.currentPage;
            this.paginationId = tabData.paginationId;
        }
        $mdOpenMenu(ev);
    };

    /**
     * Selected an additional option
     * @param item
     */
    selectAdditionalOption(item: any) {
        if (this.selectedAdditionalOption) {
            this.originatorEv = null;
            this.selectedAdditionalOption()(item);
        }
    };

    /**
     * Selected a Sort Option
     * @param item
     */
    selectOption(item: any) {

        let itemCopy = {};
        angular.extend(itemCopy, item);
        if (item.type === 'viewType') {
            this.PaginationDataService.toggleViewType(this.menuKey);
            this.setViewTypeOption(true);
        }

        if (this.selectedOption) {
            this.selectedOption()(itemCopy);
        }

        this.originatorEv = null;
    };

    setViewTypeOption(toggle: any) {

        this.viewType.value = this.PaginationDataService.viewType(this.menuKey);

        if (toggle === true) {
            this.viewType.value = this.viewType.value === 'list' ? 'table' : 'list';
        }
        if (this.viewType.value === 'list') {
            this.viewType.label = 'List View';
            this.viewType.icon = 'list';
        }
        else {
            this.viewType.label = 'Table View';
            this.viewType.icon = 'grid_on';
        }
    }
}

angular.module(moduleName).component('tbaOptionsMenu', {
    controller: OptionsMenu,
    bindings: {
        sortOptions: "=",
        selectedOption: "&",
        openedMenu: "&",
        menuIcon: "@",
        menuKey: "@",
        tabs: '=',
        rowsPerPageOptions: "=",
        showViewType: '=',
        showPagination: '=',
        additionalOptions: '=?',
        selectedAdditionalOption: "&?"
    },
    templateUrl: './options-menu-template.html'
});
