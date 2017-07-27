/*
 * Service used to get/set Pagination Data, Sorting Data, and view Type on the tables
 */
define(['angular','services/module-name'], function (angular,moduleName) {
    return   angular.module(moduleName).service('PaginationDataService', function () {

        var self = this;
        this.data = {};

        this.paginationData = function (pageName, tabName,defaultRowsPerPage) {
            if (self.data[pageName] === undefined) {
                if(defaultRowsPerPage == undefined) {
                    defaultRowsPerPage = 5;
                }
                self.data[pageName] = {rowsPerPage: ''+defaultRowsPerPage, tabs: {}, filter: '', sort: '', sortDesc: false, viewType: 'list', activeTab: tabName, total: 0}
            }
            if (tabName == undefined) {
                tabName = pageName;
            }

            if (tabName && self.data[pageName].tabs[tabName] == undefined) {
                self.data[pageName].tabs[tabName] = {paginationId: pageName + '_' + tabName, pageInfo: {}};
            }
            if (tabName && self.data[pageName].tabs[tabName].currentPage === undefined) {
                self.data[pageName].tabs[tabName].currentPage = 1;
            }
            return self.data[pageName];
        }

        this.setTotal = function (pageName, total) {
            self.paginationData(pageName).total = total;
        }

        /**
         * Save the Options for choosing the rows per page
         * @param pageName
         * @param rowsPerPageOptions
         */
        this.setRowsPerPageOptions = function (pageName, rowsPerPageOptions) {
            self.paginationData(pageName).rowsPerPageOptions = rowsPerPageOptions;
        }

        /**
         * get/save the viewType
         * @param pageName
         * @param viewType
         * @returns {string|Function|*|string|string}
         */
        this.viewType = function (pageName, viewType) {
            if (viewType != undefined) {
                self.paginationData(pageName).viewType = viewType;
            }
            return self.paginationData(pageName).viewType;
        }

        /**
         * Toggle the View Type between list and table
         * @param pageName
         */
        this.toggleViewType = function (pageName) {
            var viewType = self.paginationData(pageName).viewType;
            if (viewType == 'list') {
                viewType = 'table';
            }
            else {
                viewType = 'list';
            }
            self.viewType(pageName, viewType);
        }

        /**
         * Store the active Tab
         * @param pageName
         * @param tabName
         */
        this.activateTab = function (pageName, tabName) {
            var pageData = self.paginationData(pageName, tabName);

            //deactivate the tab
            angular.forEach(pageData.tabs, function (tabData, name) {
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
        this.getActiveTabData = function (pageName) {
            var activeTabData = {};
            var pageData = self.paginationData(pageName);
            angular.forEach(pageData.tabs, function (tabData, name) {
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
        this.filter = function (pageName, value) {
            if (value != undefined) {
                self.paginationData(pageName).filter = value;
            }
            return self.paginationData(pageName).filter;
        }

        /**
         * get/set the Rows Per Page
         * @param pageName
         * @param value
         * @returns {string|Function|*|number}
         */
        this.rowsPerPage = function (pageName, value) {
            if (value != undefined) {
                self.paginationData(pageName).rowsPerPage = value;
            }
            return self.paginationData(pageName).rowsPerPage;
        }

        /**
         * get/set the active Sort
         * @param pageName
         * @param value
         * @returns {*}
         */
        this.sort = function (pageName, value) {
            if (value) {
                self.paginationData(pageName).sort = value;
                if (value.indexOf('-') == 0) {
                    self.paginationData(pageName).sortDesc = true;
                }
                else {
                    self.paginationData(pageName).sortDesc = false;
                }
            }
            return self.paginationData(pageName).sort;
        }

        /**
         * Check if the current sort is descending
         * @param pageName
         * @returns {boolean}
         */
        this.isSortDescending = function (pageName) {
            return self.paginationData(pageName).sortDesc;
        }

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
            return self.paginationData(pageName, tabName).tabs[tabName].paginationId;
        }

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
                self.paginationData(pageName, tabName).tabs[tabName].currentPage = value;
            }
            return self.paginationData(pageName, tabName).tabs[tabName].currentPage;
        }

    });
});