import * as angular from "angular";
import {moduleName} from "../module-name";
import { DefaultTableOptionsService } from "../../services/TableOptionsService";

export default class CardFilterHeader {

    cardTitle: any;
    viewType: any;
    filterModel: any;
    filterModelOptions: any;
    sortOptions: any;
    pageName: any;
    onSelectedOption: any;
    additionalOptions: any;
    onSelectedAdditionalOption: any;
    onMenuOpen: any;
    onShowFilterHelp: any;
    renderFilter: any;
    cardController: any;
    customFilterTemplate: any;
    renderHelp:any;

    $postLink() {
        this.$element.parents('.md-toolbar-tools:first').addClass('card-filter-header');
    }

    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {

        this.filterModelOptions = this.filterModelOptions || {};
        this.renderFilter = angular.isUndefined(this.renderFilter) ? true : this.renderFilter;
        this.renderHelp = angular.isDefined(this.onShowFilterHelp);
        this.customFilterTemplate = angular.isUndefined(this.customFilterTemplate) ? '' : this.customFilterTemplate;
    
    }

    static readonly $inject = ["$element","TableOptionsService"];

    constructor(private $element: JQuery, 
                private TableOptionsService: DefaultTableOptionsService) {}

    /**
     * Called when a user Clicks on a table Option
     * @param option
     */
    selectedOption = (option: any) => {
        if (option.type == 'sort') {
            var currentSort = this.TableOptionsService.toggleSort(this.pageName, option);
            if (this.onSelectedOption) {
                this.onSelectedOption()(option);
            }
        }
    }

    selectedAdditionalOption = (option: any) => {
        if (this.onSelectedAdditionalOption) {
            this.onSelectedAdditionalOption()(option);
        }
    }

    showFilterHelpPanel = (ev: any) => {
        if (this.onShowFilterHelp) {
            this.onShowFilterHelp()(ev);
        }
    }

    /**
     *
     * @param options {sortOptions:this.sortOptions,additionalOptions:this.additionalOptions}
     */
    menuOpen = (options: any) => {
        if (this.onMenuOpen) {
            this.onMenuOpen()(options);
        }
    }

}


angular.module(moduleName).component("tbaCardFilterHeader",{
    controller: CardFilterHeader,
    bindings: {
        cardTitle: '=',
        viewType: '=',
        filterModel: '=',
        filterModelOptions: '=?',
        sortOptions: '=',
        pageName: '@',
        onSelectedOption: '&',
        additionalOptions: '=?',
        onSelectedAdditionalOption: "&?",
        onMenuOpen: '&?',
        onShowFilterHelp: '&?',
        renderFilter:'=?',
        cardController:'=?',
        customFilterTemplate:'@?'
    },
    controllerAs: '$cardFilterHeader',
    templateUrl: './card-filter-header-template.html'
    
});
