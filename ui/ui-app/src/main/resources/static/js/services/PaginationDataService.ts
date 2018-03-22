import * as angular from 'angular';
import * as _ from "underscore";
import {ListTableView} from "./ListTableViewTypes";
import {Common} from "../common/CommonTypes";
import PaginationData = ListTableView.PaginationData;
import PaginationDataService = ListTableView.PaginationDataService;
import Tab = ListTableView.Tab;
import {DefaultImportService} from "../feed-mgr/services/ImportService";
const moduleName = require('services/module-name');



export enum ViewType {
    TABLE,
    LIST
}

export class DefaultPaginationDataService implements PaginationDataService{

    data:Common.Map<PaginationData> = {};

    constructor() {

    }

    paginationData(pageName:string, tabName?:string,defaultRowsPerPage?:number) :PaginationData {
        if (this.data[pageName] === undefined) {
            if(defaultRowsPerPage == undefined) {
                defaultRowsPerPage = 5;
            }
            this.data[pageName] = {rowsPerPage: defaultRowsPerPage, tabs: {}, filter: '', sort: '', sortDesc: false, viewType: 'list', activeTab: tabName, total: 0}
        }
        if (tabName == undefined) {
            tabName = pageName;
        }

        if (tabName && this.data[pageName].tabs[tabName] == undefined) {
            this.data[pageName].tabs[tabName] = {paginationId: pageName + '_' + tabName, pageInfo: {}};
        }
        if (tabName && this.data[pageName].tabs[tabName].currentPage === undefined) {
            this.data[pageName].tabs[tabName].currentPage = 1;
        }
        return this.data[pageName];
    }

    /**
     * Sets the total number of items for the page
     * @param {string} pageName
     * @param {number} total
     */
    setTotal(pageName:string, total:number) :void {
        this.paginationData(pageName).total = total;
    }

    /**
     * Save the Options for choosing the rows per page
     * @param pageName
     * @param rowsPerPageOptions
     */
    setRowsPerPageOptions(pageName:string, rowsPerPageOptions:string[]) :void {
        this.paginationData(pageName).rowsPerPageOptions = rowsPerPageOptions;
    }

    /**
     * get/save the viewType
     * @param pageName
     * @param viewType
     * @returns {string|Function|*|string|string}
     */
    viewType(pageName:string, viewType?:string):string {
        if (viewType != undefined) {
            this.paginationData(pageName).viewType = viewType;
        }
        return this.paginationData(pageName).viewType;
    }

    /**
     * Toggle the View Type between list and table
     * @param pageName
     */
    toggleViewType(pageName:string):void {
        var viewType = this.paginationData(pageName).viewType;
        if (viewType == 'list') {
            viewType = 'table';
        }
        else {
            viewType = 'list';
        }
        this.viewType(pageName, viewType);
    }

    /**
     * Store the active Tab
     * @param pageName
     * @param tabName
     */
    activateTab(pageName:string, tabName:string) :void{
        var pageData = this.paginationData(pageName, tabName);

        //deactivate the tab
        angular.forEach(pageData.tabs, (tabData, name) => {
            tabData.active = false;
            if (name == tabName) {
                tabData.active = true;
                pageData.activeTab = name;
            }
        });
    }

    /**
     * get the Active Tab
     * @param pageName
     * @returns {{}}
     */
    getActiveTabData(pageName:string) :Tab{
        var activeTabData:Tab = {paginationId:'',currentPage:0,active:false,title:''};
        var pageData = this.paginationData(pageName);
        angular.forEach(pageData.tabs,  (tabData:Tab, name:string) =>{
            if (tabData.active) {
                activeTabData = tabData;
                return false;
            }
        });
        return activeTabData;
    }

    /**
     * get/set the Filter componenent
     * @param pageName
     * @param value
     * @returns {string|Function|*|number}
     */
    filter(pageName:string, value?:string) :string{
        if (value != undefined) {
            this.paginationData(pageName).filter = value;
        }
        return this.paginationData(pageName).filter;
    }

    /**
     * get/set the Rows Per Page
     * @param pageName
     * @param value
     * @returns {string|Function|*|number}
     */
    rowsPerPage(pageName:string, value:number) :number{
        if (value != undefined) {
            this.paginationData(pageName).rowsPerPage = value;
        }
        return this.paginationData(pageName).rowsPerPage;
    }

    /**
     * get/set the active Sort
     * @param pageName
     * @param value
     * @returns {*}
     */
    sort(pageName:string, value?:string):string {
        if (value) {
            this.paginationData(pageName).sort = value;
            if (value.indexOf('-') == 0) {
                this.paginationData(pageName).sortDesc = true;
            }
            else {
                this.paginationData(pageName).sortDesc = false;
            }
        }
        return this.paginationData(pageName).sort;
    }

    /**
     * Check if the current sort is descending
     * @param pageName
     * @returns {boolean}
     */
    isSortDescending(pageName:string) :boolean{
        return this.paginationData(pageName).sortDesc;
    }

    /**
     * get a unique Pagination Id for the Page and Tab
     * @param pageName
     * @param tabName
     * @returns {*|Function|string}
     */
    paginationId(pageName:string, tabName?:string):string {
        if (tabName == undefined) {
            tabName = pageName;
        }
        return this.paginationData(pageName, tabName).tabs[tabName].paginationId;
    }

    /**
     * get/set the Current Page Number for a Page and Tab
     * @param pageName
     * @param tabName
     * @param value
     * @returns {Function|*|currentPage|number}
     */
    currentPage(pageName:string, tabName?:string, value?:number):number {
        if (tabName == undefined || tabName == null) {
            tabName = pageName;
        }
        if (value) {
            this.paginationData(pageName, tabName).tabs[tabName].currentPage = value;
        }
        return this.paginationData(pageName, tabName).tabs[tabName].currentPage;
    }

}

angular.module(moduleName).factory('PaginationDataService', () => new DefaultPaginationDataService());


