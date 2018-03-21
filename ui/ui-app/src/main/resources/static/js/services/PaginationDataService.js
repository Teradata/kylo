define(["require", "exports", "angular", "./module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var PaginationDataService = /** @class */ (function () {
        function PaginationDataService() {
            this.data = {};
            this.paginationData = function (pageName, tabName, defaultRowsPerPage) {
                if (this.data[pageName] === undefined) {
                    if (defaultRowsPerPage == undefined) {
                        defaultRowsPerPage = 5;
                    }
                    this.data[pageName] = { rowsPerPage: '' + defaultRowsPerPage, tabs: {}, filter: '', sort: '', sortDesc: false, viewType: 'list', activeTab: tabName, total: 0 };
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
            this.setTotal = function (pageName, total) {
                this.paginationData(pageName).total = total;
            };
            /**
             * Save the Options for choosing the rows per page
             * @param pageName
             * @param rowsPerPageOptions
             */
            this.setRowsPerPageOptions = function (pageName, rowsPerPageOptions) {
                this.paginationData(pageName).rowsPerPageOptions = rowsPerPageOptions;
            };
            /**
             * get/save the viewType
             * @param pageName
             * @param viewType
             * @returns {string|Function|*|string|string}
             */
            this.viewType = function (pageName, viewType) {
                if (viewType != undefined) {
                    this.paginationData(pageName).viewType = viewType;
                }
                return this.paginationData(pageName).viewType;
            };
            /**
             * Toggle the View Type between list and table
             * @param pageName
             */
            this.toggleViewType = function (pageName) {
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
            this.activateTab = function (pageName, tabName) {
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
            this.getActiveTabData = function (pageName) {
                var activeTabData = {};
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
            this.filter = function (pageName, value) {
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
            this.rowsPerPage = function (pageName, value) {
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
            this.sort = function (pageName, value) {
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
            this.isSortDescending = function (pageName) {
                return this.paginationData(pageName).sortDesc;
            };
            /**
             * get a unique Pagination Id for the Page and Tab
             * @param pageName
             * @param tabName
             * @returns {*|Function|string}
             */
            this.paginationId = function (pageName, tabName) {
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
            this.currentPage = function (pageName, tabName, value) {
                if (tabName == undefined || tabName == null) {
                    tabName = pageName;
                }
                if (value) {
                    this.paginationData(pageName, tabName).tabs[tabName].currentPage = value;
                }
                return this.paginationData(pageName, tabName).tabs[tabName].currentPage;
            };
        }
        return PaginationDataService;
    }());
    exports.default = PaginationDataService;
    angular.module(module_name_1.moduleName).service('PaginationDataService', PaginationDataService);
});
//# sourceMappingURL=PaginationDataService.js.map