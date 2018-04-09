import * as _ from "underscore";
import * as angular from "angular";
import {Common} from "../common/CommonTypes";

declare namespace ListTableView {

    export interface Tab {
        paginationId: string;
        pageInfo?: any;
        currentPage?: number;
        title?: string;
        active?: boolean;
    }

    export interface PaginationData {
        rowsPerPage: number;
        tabs: Common.Map<Tab>,
        filter: string;
        sort: string;
        sortDesc: boolean;
        viewType: string;
        activeTab: string;
        total: number;
        rowsPerPageOptions?: string[]
    }

    export interface TableOption {
        label: string;
        type: string;
        header: boolean;
        icon: string;
        disabled: boolean;
    }

    export interface SortOption {
        label: string;
        value: string;
        default?:string;
        direction?: string;
        reverse: boolean;
        type: string;
        icon?: string;
    }


    export interface PaginationDataService {

        /**
         * returns the pagination data
         * @param {string} pageName
         * @param {string} tabName
         * @param {number} defaultRowsPerPage
         * @return {ListTableView.PaginationData}
         */
        paginationData(pageName: string, tabName?: string, defaultRowsPerPage?: number): PaginationData;

        /**
         *
         * @param {string} pageName
         * @param {number} total
         */
        setTotal(pageName: string, total: number): void;

        /**
         * Save the Options for choosing the rows per page
         * @param pageName
         * @param rowsPerPageOptions
         */
        setRowsPerPageOptions(pageName: string, rowsPerPageOptions: string[]): void;

        /**
         * get/save the viewType
         * @param pageName
         * @param viewType
         * @returns {string|Function|*|string|string}
         */
        viewType(pageName: string, viewType?: string): string;

        /**
         * Toggle the View Type between list and table
         * @param pageName
         */
        toggleViewType(pageName: string): void;

        /**
         * Store the active Tab
         * @param pageName
         * @param tabName
         */
        activateTab(pageName: string, tabName: string): void;

        /**
         * get the Active Tab
         * @param pageName
         * @returns {{}}
         */
        getActiveTabData(pageName: string): void;

        /**
         * get/set the Filter componenent
         * @param pageName
         * @param value
         * @returns {string|Function|*|number}
         */
        filter(pageName: string, value?: any): string;

        /**
         * get/set the Rows Per Page
         * @param pageName
         * @param value
         * @returns {string|Function|*|number}
         */
        rowsPerPage(pageName: string, value: number): number;

        /**
         * get/set the active Sort
         * @param pageName
         * @param value
         * @returns {*}
         */
        sort(pageName: string, value?: string): string;

        /**
         * Check if the current sort is descending
         * @param pageName
         * @returns {boolean}
         */
        isSortDescending(pageName: string): boolean;

        /**
         * get a unique Pagination Id for the Page and Tab
         * @param pageName
         * @param tabName
         * @returns {*|Function|string}
         */
        paginationId(pageName: string, tabName?: string): string;

        /**
         * get/set the Current Page Number for a Page and Tab
         * @param pageName
         * @param tabName
         * @param value
         * @returns {Function|*|currentPage|number}
         */
        currentPage(pageName: string, tabName?: string, value?: number): number;
    }


export interface TableOptionService {

    newSortOptions(key: string, labelValueMap: Common.Map<string>, defaultValue?: string, defaultDirection?: string): SortOption[]

    newOption(label: string, type: string, isHeader: boolean, disabled: boolean, icon: string): TableOption

    /**
     * Sets the sort option to either the saved value from the PaginationDataService or the default value.
     * @param key
     */
    initializeSortOption(key: string): void

    /**
     * Save the sort option back to the store
     * @param {string} key
     * @param {ListTableView.SortOption} sortOption
     */
    saveSortOption(key: string, sortOption: SortOption): void

    toggleSort(key: string, option: SortOption): void;

    toSortString(option: SortOption): string;

    setSortOption(key: string, val: string):void;

    getCurrentSort(key: string): SortOption;

}

}