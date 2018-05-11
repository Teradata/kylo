define(["require", "exports", "angular", "./module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ViewType;
    (function (ViewType) {
        ViewType[ViewType["TABLE"] = 0] = "TABLE";
        ViewType[ViewType["LIST"] = 1] = "LIST";
    })(ViewType = exports.ViewType || (exports.ViewType = {}));
    var DefaultPaginationDataService = /** @class */ (function () {
        function DefaultPaginationDataService() {
            this.data = {};
        }
        DefaultPaginationDataService.prototype.paginationData = function (pageName, tabName, defaultRowsPerPage) {
            if (this.data[pageName] === undefined) {
                if (defaultRowsPerPage == undefined) {
                    defaultRowsPerPage = 5;
                }
                this.data[pageName] = { rowsPerPage: defaultRowsPerPage, tabs: {}, filter: '', sort: '', sortDesc: false, viewType: 'list', activeTab: tabName, total: 0 };
            }
            if (tabName == undefined) {
                tabName = pageName;
            }
            if (tabName && this.data[pageName].tabs[tabName] == undefined) {
                this.data[pageName].tabs[tabName] = { paginationId: pageName + '_' + tabName, pageInfo: {} };
            }
            if (tabName && this.data[pageName].tabs[tabName].currentPage === undefined) {
                this.data[pageName].tabs[tabName].currentPage = 1;
            }
            return this.data[pageName];
        };
        /**
         * Sets the total number of items for the page
         * @param {string} pageName
         * @param {number} total
         */
        DefaultPaginationDataService.prototype.setTotal = function (pageName, total) {
            this.paginationData(pageName).total = total;
        };
        /**
         * Save the Options for choosing the rows per page
         * @param pageName
         * @param rowsPerPageOptions
         */
        DefaultPaginationDataService.prototype.setRowsPerPageOptions = function (pageName, rowsPerPageOptions) {
            this.paginationData(pageName).rowsPerPageOptions = rowsPerPageOptions;
        };
        /**
         * get/save the viewType
         * @param pageName
         * @param viewType
         * @returns {string|Function|*|string|string}
         */
        DefaultPaginationDataService.prototype.viewType = function (pageName, viewType) {
            if (viewType != undefined) {
                this.paginationData(pageName).viewType = viewType;
            }
            return this.paginationData(pageName).viewType;
        };
        /**
         * Toggle the View Type between list and table
         * @param pageName
         */
        DefaultPaginationDataService.prototype.toggleViewType = function (pageName) {
            var viewType = this.paginationData(pageName).viewType;
            if (viewType == 'list') {
                viewType = 'table';
            }
            else {
                viewType = 'list';
            }
            this.viewType(pageName, viewType);
        };
        /**
         * Store the active Tab
         * @param pageName
         * @param tabName
         */
        DefaultPaginationDataService.prototype.activateTab = function (pageName, tabName) {
            var pageData = this.paginationData(pageName, tabName);
            //deactivate the tab
            angular.forEach(pageData.tabs, function (tabData, name) {
                tabData.active = false;
                if (name == tabName) {
                    tabData.active = true;
                    pageData.activeTab = name;
                }
            });
        };
        /**
         * get the Active Tab
         * @param pageName
         * @returns {{}}
         */
        DefaultPaginationDataService.prototype.getActiveTabData = function (pageName) {
            var activeTabData = { paginationId: '', currentPage: 0, active: false, title: '' };
            var pageData = this.paginationData(pageName);
            angular.forEach(pageData.tabs, function (tabData, name) {
                if (tabData.active) {
                    activeTabData = tabData;
                    return false;
                }
            });
            return activeTabData;
        };
        /**
         * get/set the Filter componenent
         * @param pageName
         * @param value
         * @returns {string|Function|*|number}
         */
        DefaultPaginationDataService.prototype.filter = function (pageName, value) {
            if (value != undefined) {
                this.paginationData(pageName).filter = value;
            }
            return this.paginationData(pageName).filter;
        };
        /**
         * get/set the Rows Per Page
         * @param pageName
         * @param value
         * @returns {string|Function|*|number}
         */
        DefaultPaginationDataService.prototype.rowsPerPage = function (pageName, value) {
            if (value != undefined) {
                this.paginationData(pageName).rowsPerPage = value;
            }
            return this.paginationData(pageName).rowsPerPage;
        };
        /**
         * get/set the active Sort
         * @param pageName
         * @param value
         * @returns {*}
         */
        DefaultPaginationDataService.prototype.sort = function (pageName, value) {
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
        };
        /**
         * Check if the current sort is descending
         * @param pageName
         * @returns {boolean}
         */
        DefaultPaginationDataService.prototype.isSortDescending = function (pageName) {
            return this.paginationData(pageName).sortDesc;
        };
        /**
         * get a unique Pagination Id for the Page and Tab
         * @param pageName
         * @param tabName
         * @returns {*|Function|string}
         */
        DefaultPaginationDataService.prototype.paginationId = function (pageName, tabName) {
            if (tabName == undefined) {
                tabName = pageName;
            }
            return this.paginationData(pageName, tabName).tabs[tabName].paginationId;
        };
        /**
         * get/set the Current Page Number for a Page and Tab
         * @param pageName
         * @param tabName
         * @param value
         * @returns {Function|*|currentPage|number}
         */
        DefaultPaginationDataService.prototype.currentPage = function (pageName, tabName, value) {
            if (tabName == undefined || tabName == null) {
                tabName = pageName;
            }
            if (value) {
                this.paginationData(pageName, tabName).tabs[tabName].currentPage = value;
            }
            return this.paginationData(pageName, tabName).tabs[tabName].currentPage;
        };
        return DefaultPaginationDataService;
    }());
    exports.DefaultPaginationDataService = DefaultPaginationDataService;
    angular.module(module_name_1.moduleName).service('PaginationDataService', DefaultPaginationDataService);
});
//# sourceMappingURL=PaginationDataService.js.map