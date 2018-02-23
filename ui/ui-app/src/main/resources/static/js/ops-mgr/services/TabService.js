define(["require", "exports", "angular", "../module-name", "underscore"], function (require, exports, angular, module_name_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var TabService = /** @class */ (function () {
        //tabPageData: any;
        function TabService(PaginationDataService) {
            var _this = this;
            this.PaginationDataService = PaginationDataService;
            this.tabs = {};
            this.tabMetadata = {};
            this.getTab = function (pageName, tabName) {
                var tabs = _this.tabs[pageName];
                var tab = null;
                if (tabs) {
                    tab = _.find(tabs, function (tab) {
                        return tab.title == tabName;
                    });
                }
                return tab;
            };
            this.getTabs = function (pageName) {
                return _this.tabs[pageName];
            };
            this.getActiveTab = function (pageName) {
                var tabs = _this.tabs[pageName];
                var tab = null;
                if (tabs) {
                    tab = _.find(tabs, function (tab) {
                        return tab.active == true;
                    });
                }
                return tab;
            };
            this.metadata = function (pageName) {
                return _this.tabMetadata[pageName];
            };
            this.tabNameMap = function (pageName) {
                var tabs = _this.tabs[pageName];
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
                var tab = _this.getTab(pageName, tabName);
                if (tab) {
                    tab.addContent(content);
                }
            };
            this.setTotal = function (pageName, tabName, total) {
                var tab = _this.getTab(pageName, tabName);
                if (tab) {
                    tab.setTotal(total);
                }
            };
            this.clearTabs = function (pageName) {
                var tabs = _this.tabs[pageName];
                if (tabs) {
                    angular.forEach(tabs, function (tab, i) {
                        tab.clearContent();
                    });
                }
            };
            this.tabPageData = function (pageName) {
                if (angular.isUndefined(_this.tabPageData[pageName])) {
                    var data = {
                        total: 0,
                        content: _this.tabContent,
                        setTotal: function (total) {
                            _this.total = total;
                        },
                        clearContent: function () {
                            _this.content = [];
                            _this.total = 0;
                        },
                        addContent: function (content) {
                            _this.content.push(content);
                        }
                    };
                    _this.tabPageData[pageName] = data;
                }
                return _this.tabPageData[pageName];
            };
            this.selectedTab = function (pageName, tab) {
                angular.forEach(_this.tabs[pageName], function (aTab, i) {
                    aTab.active = false;
                });
                tab.active = true;
                _this.PaginationDataService.activateTab(pageName, tab.title);
                var currentPage = _this.PaginationDataService.currentPage(pageName, tab.title);
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