import {Component, Input} from "@angular/core";
import * as _ from "underscore";
import {DefaultPaginationDataService} from "../../services/PaginationDataService";

@Component({
    selector: "tba-options-menu",
    templateUrl: "./options-menu-template.html",
    styleUrls: ["./options-menu-style.css"]
})
export class OptionsMenuComponent {

    @Input() sortOptions: any;
    @Input() selectedOption: any;
    @Input() openedMenu: any;
    @Input() menuIcon: any;
    @Input() menuKey: any;
    @Input() tabs: any;
    @Input() rowsPerPageOptions: any;
    @Input() showViewType: any;
    @Input() showPagination: any;
    @Input() additionalOptions: any;
    @Input() selectedAdditionalOption: any;

    originatorEv: any;
    rowsPerPage: any;
    paginationData: any;
    viewType: any;
    currentPage: any;
    paginationId: any;

    ngOnInit() {

        if (this.showViewType) {
            this.viewType = {label: 'List View', icon: 'list', value: 'list', type: 'viewType'};
        }

        if (this.showViewType) {
            //toggle the view Type so its opposite the current view type
            this.setViewTypeOption(true);
        }

        this.rowsPerPage = 5;
        this.paginationData = this.paginationDataService.paginationData(this.menuKey);
    }

    constructor(private paginationDataService: DefaultPaginationDataService) {
    }

    getPaginationId(tab: any) {
        return this.paginationDataService.paginationId(this.menuKey, tab.title);
    };

    getCurrentPage(tab: any) {
        return this.paginationDataService.currentPage(this.menuKey, tab.title);
    };

    openMenu(ev: any) {

        this.originatorEv = ev;
        if (_.isFunction(this.openedMenu)) {
            let openedMenuFn = this.openedMenu();
            if (_.isFunction(openedMenuFn)) {
                openedMenuFn({sortOptions: this.sortOptions, additionalOptions: this.additionalOptions});
            }
        }
        if (this.showPagination) {
            let tabData = this.paginationDataService.getActiveTabData(this.menuKey);
            this.currentPage = tabData.currentPage;
            this.paginationId = tabData.paginationId;
        }
        // $mdOpenMenu(ev);
    };

    /**
     * Selected an additional option
     * @param item
     */
    selectAdditionalOption(item: any) {
        event.stopPropagation();
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
        event.stopPropagation();
        let itemCopy = {};
        _.extend(itemCopy, item);
        if (item.type === 'viewType') {
            this.paginationDataService.toggleViewType(this.menuKey);
            this.setViewTypeOption(true);
        }

        if (this.selectedOption) {
            this.selectedOption(itemCopy);
        }

        this.originatorEv = null;
    };

    setViewTypeOption(toggle: any) {

        this.viewType.value = this.paginationDataService.viewType(this.menuKey);

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
