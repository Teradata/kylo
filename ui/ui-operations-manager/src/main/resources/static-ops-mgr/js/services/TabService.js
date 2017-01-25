/*-
 * #%L
 * thinkbig-ui-operations-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
angular.module(MODULE_OPERATIONS).service('TabService',function(PaginationDataService) {

    var self = this;
    this.tabs = {};
    this.tabMetadata = {};

    this.getTab = function(pageName,tabName){
        var tabs = self.tabs[pageName];
        var tab = null;
        if(tabs){
            tab = _.find(tabs,function(tab){
                return tab.title == tabName;
            });
        }
        return tab;
    }
    this.getTabs = function(pageName){
        return self.tabs[pageName];
    }

    this.getActiveTab = function(pageName){
        var tabs = self.tabs[pageName];
        var tab = null;
        if(tabs){
            tab = _.find(tabs,function(tab){
                return tab.active == true;
            });
        }
        return tab;
    }

    this.metadata = function(pageName){
        return self.tabMetadata[pageName];
    }

    this.tabNameMap = function(pageName) {
        var tabs = self.tabs[pageName];
        var tabMap= {};
        if(tabs) {
          tabMap =  tabs.reduce(function (obj, tab) {
                obj[tab.title] = tab;
                return obj;
            }, {});
        }
        return tabMap;
    }

    this.addContent = function(pageName,tabName,content) {
        var tab = self.getTab(pageName,tabName);
        if(tab){
            tab.addContent(content);
        }
    }

    this.setTotal = function(pageName,tabName,total) {
        var tab = self.getTab(pageName,tabName);
        if(tab){
            tab.setTotal(total)
        }
    }

    this.clearTabs = function(pageName){
        var tabs = self.tabs[pageName];
        if(tabs){
            angular.forEach(tabs,function(tab,i) {
               tab.clearContent();
            })
        }
    }

    this.registerTabs = function(pageName,tabNamesArray, currentTabName) {
        if(self.tabs[pageName] === undefined) {
            var tabs = [];
            self.tabs[pageName] = tabs;
            self.tabMetadata[pageName] = {selectedIndex:0};
            angular.forEach(tabNamesArray,function(name,i){
                var data = {total:0,content:[]};
                //if there is a currentPage saved... use it
                var currentPage = PaginationDataService.currentPage(self.pageName,name) || 1;
                var tab = {title:name, 
                		   data:data, 
                		   currentPage:currentPage, 
                		   active:false, 
                		   setTotal:function(total) { this.data.total = total;}, 
                		   clearContent:function() {this.data.content = []; this.data.total = 0;}, 
                		   addContent:function(content) { this.data.content.push(content);}};
                if(currentTabName && name == currentTabName) {
                    self.tabMetadata[pageName].selectedIndex = i;
                    tab.active = true;
                }
                else if(currentTabName === undefined && i ==0){
                    tab.active = true;
                }
                tabs.push(tab);
            });
        }
        return self.tabs[pageName];
    }

   this.selectedTab = function(pageName,tab) {
       angular.forEach(this.tabs[pageName],function(aTab,i){
           aTab.active = false;
       });
       tab.active = true;
       PaginationDataService.activateTab(self.pageName,tab.title);
       var currentPage = PaginationDataService.currentPage(self.pageName,tab.title);
       tab.currentPage =currentPage;
    };



});
