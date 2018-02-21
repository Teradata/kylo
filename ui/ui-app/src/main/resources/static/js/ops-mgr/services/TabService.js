define(["require", "exports", "angular", "../module-name", "underscore"], function (require, exports, angular, module_name_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var TabService = /** @class */ (function () {
        function TabService(PaginationDataService) {
            this.PaginationDataService = PaginationDataService;
            this.tabs = {};
            this.tabMetadata = {};
            this.getTab = function (pageName, tabName) {
                var tabs = this.tabs[pageName];
                var tab = null;
                if (tabs) {
                    tab = _.find(tabs, function (tab) {
                        return tab.title == tabName;
                    });
                }
                return tab;
            };
            this.getTabs = function (pageName) {
                return this.tabs[pageName];
            };
            this.getActiveTab = function (pageName) {
                var tabs = this.tabs[pageName];
                var tab = null;
                if (tabs) {
                    tab = _.find(tabs, function (tab) {
                        return tab.active == true;
                    });
                }
                return tab;
            };
            this.metadata = function (pageName) {
                return this.tabMetadata[pageName];
            };
            this.tabNameMap = function (pageName) {
                var tabs = this.tabs[pageName];
                var tabMap = {};
                if (tabs) {
                    tabMap = tabs.reduce(function (obj, tab) {
                        obj[tab.title] = tab;
                        return obj;
                    }, {});
                }
                return tabMap;
            };
            this.addContent = function (pageName, tabName, content) {
                var tab = this.getTab(pageName, tabName);
                if (tab) {
                    tab.addContent(content);
                }
            };
            this.setTotal = function (pageName, tabName, total) {
                var tab = this.getTab(pageName, tabName);
                if (tab) {
                    tab.setTotal(total);
                }
            };
            this.clearTabs = function (pageName) {
                var tabs = this.tabs[pageName];
                if (tabs) {
                    angular.forEach(tabs, function (tab, i) {
                        tab.clearContent();
                    });
                }
            };
            this.tabPageDat = function (pageName) {
                if (angular.isUndefined(this.tabPageData[pageName])) {
                    var data = {
                        total: 0,
                        content: this.tabContent,
                        setTotal: function (total) {
                            this.total = total;
                        },
                        clearContent: function () {
                            this.content = [];
                            this.total = 0;
                        },
                        addContent: function (content) {
                            this.content.push(content);
                        }
                    };
                    this.tabPageData[pageName] = data;
                }
                return this.tabPageData[pageName];
            };
            this.selectedTab = function (pageName, tab) {
                angular.forEach(this.tabs[pageName], function (aTab, i) {
                    aTab.active = false;
                });
                tab.active = true;
                this.PaginationDataService.activateTab(this.pageName, tab.title);
                var currentPage = this.PaginationDataService.currentPage(this.pageName, tab.title);
                tab.currentPage = currentPage;
            };
        }
        TabService.prototype.registerTabs = function (pageName, tabNamesArray, currentTabName) {
            var _this = this;
            var PaginationDataService = this.PaginationDataService;
            if (this.tabs[pageName] === undefined) {
                var tabs = [];
                this.tabs[pageName] = tabs;
                this.tabMetadata[pageName] = { selectedIndex: 0 };
                angular.forEach(tabNamesArray, function (name, i) {
                    var data = { total: 0, content: [] };
                    //if there is a currentPage saved... use it
                    var currentPage = PaginationDataService.currentPage(pageName, name) || 1;
                    var tab = {
                        title: name,
                        data: data,
                        currentPage: currentPage,
                        active: false,
                        setTotal: function (total) {
                            this.data.total = total;
                        },
                        clearContent: function () {
                            this.data.content = [];
                            this.data.total = 0;
                        },
                        addContent: function (content) {
                            this.data.content.push(content);
                        }
                    };
                    if (currentTabName && name == currentTabName) {
                        _this.tabMetadata[pageName].selectedIndex = i;
                        tab.active = true;
                    }
                    else if (currentTabName === undefined && i == 0) {
                        tab.active = true;
                    }
                    tabs.push(tab);
                });
            }
            return this.tabs[pageName];
        };
        return TabService;
    }());
    exports.default = TabService;
    angular.module(module_name_1.moduleName).service('TabService', ['PaginationDataService', TabService]);
});
//# sourceMappingURL=TabService.js.map