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
(function () {

    var controller = function ($scope,$http, $filter, $stateParams, $interval, $timeout, $q,ServicesStatusData, TableOptionsService, PaginationDataService, AlertsService, StateService, IconService, TabService) {
        var self = this;
        this.pageName = 'service-component-details';
        this.cardTitle = 'Service Component Alerts';
        //Page State
        this.loading = true;
        this.showProgress = true;
        this.component = {alerts:[]};
        this.totalAlerts = 0;
        this.componentName = $stateParams.componentName;
        this.serviceName = $stateParams.serviceName;

        //Pagination and view Type (list or table)
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        PaginationDataService.setRowsPerPageOptions(this.pageName,['5','10','20','50','All']);
        this.viewType = PaginationDataService.viewType(this.pageName);
        this.currentPage =PaginationDataService.currentPage(self.pageName)||1;
        this.filter = PaginationDataService.filter(self.pageName);
        this.sortOptions = loadSortOptions();

        this.service = ServicesStatusData.services[this.serviceName];
        if(this.service){
            self.component = this.service.componentMap[self.componentName];
        };

        $scope.$watch(function(){
            return ServicesStatusData.services;
        },function(newVal) {
            if(newVal[self.serviceName]){
                var updatedComponent = newVal[self.serviceName].componentMap[self.componentName];
                if(updatedComponent != self.component){
                    self.component = updatedComponent;
                }
            }
        });



        this.paginationId = function(){
            return PaginationDataService.paginationId(self.pageName);
        }


        $scope.$watch(function(){
            return self.viewType;
        },function(newVal) {
            self.onViewTypeChange(newVal);
        })

        this.onViewTypeChange = function(viewType) {
            PaginationDataService.viewType(this.pageName, self.viewType);
        }

        //Tab Functions

        this.onOrderChange = function (order) {
            PaginationDataService.sort(self.pageName,order);
            TableOptionsService.setSortOption(self.pageName,order);
        };

        this.onPaginationChange = function (page, limit) {
            PaginationDataService.currentPage(self.pageName,null,page);
            self.currentPage = page;
        };


        //Sort Functions
        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Component Name':'componentName','Alert':'alert','Update Date':'latestAlertTimestamp'};

            var sortOptions = TableOptionsService.newSortOptions(self.pageName,options,'latestAlertTimestamp','asc');
            TableOptionsService.initializeSortOption(self.pageName);
            return sortOptions;

        }



        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        this.selectedTableOption = function(option) {
            console.log('SET SORT TO BE ', option, self.pageName)
            var sortString = TableOptionsService.toSortString(option);
            var savedSort = PaginationDataService.sort(self.pageName, sortString);
            var updatedOption = TableOptionsService.toggleSort(self.pageName, option);
            TableOptionsService.setSortOption(self.pageName, sortString);
            console.log('SET SORT TO BE ', option, sortString, self.pageName)
        }



    };


    angular.module(MODULE_OPERATIONS).controller('ServiceComponentHealthDetailsController',controller);



}());


