import * as _ from 'underscore';
import { DefaultPaginationDataService } from "./PaginationDataService";
import { ObjectUtils } from '../../lib/common/utils/object-utils';
import {Injectable} from "@angular/core";

@Injectable()
export class TabService {
    tabs: any = {};
    tabMetadata: any = {};
    static $inject = ["PaginationDataService"]
    constructor(private paginationDataService: DefaultPaginationDataService) {

    }
    registerTabs(pageName: any, tabNamesArray: any, currentTabName: any): any {
        if (this.tabs[pageName] === undefined) {
            var tabs: any[] = [];
            this.tabs[pageName] = tabs;
            this.tabMetadata[pageName] = { selectedIndex: 0 };
            var indexCounter = -1;
            Object.keys(tabNamesArray).forEach((key: any) => {
                var name = tabNamesArray[key];
                indexCounter++;
                var data: any = { total: 0, content: [] };
                //if there is a currentPage saved... use it
                var currentPage = this.paginationDataService.currentPage(pageName, name) || 1;
                var tab: any = {
                    title: name,
                    data: data,
                    currentPage: currentPage,
                    active: false,
                    setTotal: function (total: any) {
                        this.data.total = total;
                    },
                    clearContent: function () {
                        this.data.content = [];
                        this.data.total = 0;
                    },
                    addContent: function (content: any) {
                        this.data.content.push(content);
                    }
                };
                if (currentTabName && name == currentTabName) {
                    this.tabMetadata[pageName].selectedIndex = indexCounter;
                    tab.active = true;
                }
                else if (currentTabName === undefined && indexCounter == 0) {
                    tab.active = true;
                }
                tabs.push(tab);
            });
        }

        return this.tabs[pageName];
    }
    getTab(pageName: any, tabName: any) {
        var tabs = this.tabs[pageName];
        var tab = null;
        if (tabs) {
            tab = _.find(tabs, function (tab: any) {
                return tab.title == tabName;
            });
        }
        return tab;
    }
    getTabs(pageName: any) {
        return this.tabs[pageName];
    }

    getActiveTab(pageName: any) {
        var tabs = this.tabs[pageName];
        var tab = null;
        if (tabs) {
            tab = _.find(tabs, (tab: any) => {
                return tab.active == true;
            });
        }
        return tab;
    }

    metadata(pageName: any) {
        return this.tabMetadata[pageName];
    }

    tabNameMap(pageName: any) {
        var tabs = this.tabs[pageName];
        var tabMap = {};
        if (tabs) {
            tabMap = tabs.reduce((obj: any, tab: any) => {
                obj[tab.title] = tab;
                return obj;
            }, {});
        }
        return tabMap;
    }

    addContent(pageName: any, tabName: any, content: any) {
        var tab = this.getTab(pageName, tabName);
        if (tab) {
            tab.addContent(content);
        }
    }

    setTotal(pageName: any, tabName: any, total: any) {
        var tab = this.getTab(pageName, tabName);
        if (tab) {
            tab.setTotal(total)
        }
    }

    clearTabs(pageName: any) {
        var tabs = this.tabs[pageName];
        if (tabs) {
            Object.keys(tabs).forEach((key: any) => {
                tabs[key].clearContent();
            })
        }
    }


    total: any;
    content: any;
    tabContent: any[];
    tabPageData(pageName: any) {
        if (_.isUndefined(this.tabPageData[pageName])) {
            var data = {
                total: 0,
                content: this.tabContent,
                setTotal: (total: any) => {
                    this.total = total;
                },
                clearContent: () => {
                    this.content = [];
                    this.total = 0;
                },
                addContent: (content: any) => {
                    this.content.push(content);
                }
            };
            this.tabPageData[pageName] = data;
        }
        return this.tabPageData[pageName];
    }

    selectedTab(pageName: any, tab: any) {
        Object.keys(this.tabs[pageName]).forEach((key: any) => {
            this.tabs[pageName][key].active = false;
        });
        tab.active = true;
        this.paginationDataService.activateTab(pageName, tab.title);
        var currentPage = this.paginationDataService.currentPage(pageName, tab.title);
        tab.currentPage = currentPage;
    }
}

