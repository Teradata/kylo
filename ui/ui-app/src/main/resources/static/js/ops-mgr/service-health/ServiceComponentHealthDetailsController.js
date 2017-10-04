define(['angular','ops-mgr/service-health/module-name'], function (angular,moduleName) {

    var controller = function ($scope,$http, $filter, $transition$, $interval, $timeout, $q,ServicesStatusData, TableOptionsService, PaginationDataService) {
        var self = this;
        this.pageName = 'service-component-details';
        this.cardTitle = 'Service Component Alerts';
        //Page State
        this.loading = true;
        this.showProgress = true;
        this.component = {alerts:[]};
        this.totalAlerts = 0;
        this.componentName = $transition$.params().componentName;
        this.serviceName = $transition$.params().serviceName;

        //Pagination and view Type (list or table)
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        PaginationDataService.setRowsPerPageOptions(this.pageName,['5','10','20','50']);
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
            var sortString = TableOptionsService.toSortString(option);
            var savedSort = PaginationDataService.sort(self.pageName, sortString);
            var updatedOption = TableOptionsService.toggleSort(self.pageName, option);
            TableOptionsService.setSortOption(self.pageName, sortString);
        }



    };


    angular.module(moduleName).controller('ServiceComponentHealthDetailsController',["$scope","$http","$filter","$transition$","$interval","$timeout","$q","ServicesStatusData","TableOptionsService","PaginationDataService",controller]);



});


