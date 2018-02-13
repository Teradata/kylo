import * as angular from "angular";
import {moduleName} from "../module-name";
import * as _ from 'underscore';
import { Injectable } from '@angular/core';

export default class TabService{
    tabs: any = {};
    tabMetadata: any = {};
    tabPageData: any;
    constructor(private PaginationDataService: any){}
    registerTabs(pageName: any, tabNamesArray: any, currentTabName: any): any {
       let PaginationDataService = this.PaginationDataService;
            if (this.tabs[pageName] === undefined) {
                var tabs: any[] = [];
                this.tabs[pageName] = tabs;
                this.tabMetadata[pageName] = {selectedIndex: 0};
                angular.forEach(tabNamesArray,(name: any, i: any)=>{
                        var data: any = {total: 0, content: []};
                        //if there is a currentPage saved... use it
                        var currentPage = PaginationDataService.currentPage(pageName, name) || 1;
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
                            this.tabMetadata[pageName].selectedIndex = i;
                            tab.active = true;
                        }
                        else if (currentTabName === undefined && i == 0) {
                            tab.active = true;
                        }
                        tabs.push(tab);
                });
            }

            return this.tabs[pageName];
        }
        getTab = function (pageName: any, tabName: any) {
            var tabs = this.tabs[pageName];
            var tab = null;
            if (tabs) {
                tab = _.find(tabs, function (tab: any) {
                    return tab.title == tabName;
                });
            }
            return tab;
        }
        getTabs = function (pageName: any) {
            return this.tabs[pageName];
        }

        getActiveTab = function (pageName: any) {
            var tabs = this.tabs[pageName];
            var tab = null;
            if (tabs) {
                tab = _.find(tabs, function (tab: any) {
                    return tab.active == true;
                });
            }
            return tab;
        }

        metadata = function (pageName: any) {
            return this.tabMetadata[pageName];
        }

        tabNameMap = function (pageName: any) {
            var tabs = this.tabs[pageName];
            var tabMap = {};
            if (tabs) {
                tabMap = tabs.reduce(function (obj: any, tab: any) {
                    obj[tab.title] = tab;
                    return obj;
                }, {});
            }
            return tabMap;
        }

        addContent = function (pageName: any, tabName: any, content: any) {
            var tab = this.getTab(pageName, tabName);
            if (tab) {
                tab.addContent(content);
            }
        }

        setTotal = function (pageName: any, tabName: any, total: any) {
            var tab = this.getTab(pageName, tabName);
            if (tab) {
                tab.setTotal(total)
            }
        }

        clearTabs = function (pageName: any) {
            var tabs = this.tabs[pageName];
            if (tabs) {
                angular.forEach(tabs, function (tab: any, i: any) {
                    tab.clearContent();
                })
            }
        }

     

        tabContent: any[];
        tabPageDat = function(pageName: any){// tabPageData
            if(angular.isUndefined(this.tabPageData[pageName])){
                var data = {
                    total: 0,
                    content: this.tabContent,
                    setTotal: function (total: any) {
                        this.total = total;
                    },
                    clearContent: function () {
                        this.content = [];
                        this.total = 0;
                    },
                    addContent: function (content: any) {
                        this.content.push(content);
                    }
                };
                this.tabPageData[pageName] = data;
            }
            return this.tabPageData[pageName];
        }

        selectedTab = function (pageName: any, tab: any) {
            angular.forEach(this.tabs[pageName], function (aTab: any, i: any) {
                aTab.active = false;
            });
            tab.active = true;
            this.PaginationDataService.activateTab(this.pageName, tab.title);
            var currentPage = this.PaginationDataService.currentPage(this.pageName, tab.title);
            tab.currentPage = currentPage;
        }
}

  angular.module(moduleName).service('TabService',['PaginationDataService',TabService]);