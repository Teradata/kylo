define(['angular','ops-mgr/service-health/module-name'], function (angular,moduleName) {

    var controller = function ($scope,$http, $filter, $transition$, $interval, $timeout, $q,ServicesStatusData, TableOptionsService, PaginationDataService, AlertsService, StateService) {
        var self = this;
        this.pageName = 'service-details';
        this.cardTitle = 'Service Components';
        //Page State
        this.loading = true;
        this.showProgress = true;
        this.service = {components:[]};
        this.totalComponents = 0;
        this.serviceName = $transition$.params().serviceName;

        //Pagination and view Type (list or table)
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        PaginationDataService.setRowsPerPageOptions(this.pageName,['5','10','20','50']);
        this.viewType = PaginationDataService.viewType(this.pageName);
        this.currentPage =PaginationDataService.currentPage(self.pageName)||1;
        this.filter = PaginationDataService.filter(self.pageName);
        this.sortOptions = loadSortOptions();



        this.paginationId = function(){
            return PaginationDataService.paginationId(self.pageName);
        }


        this.service = ServicesStatusData.services[this.serviceName];
        if(_.isEmpty(ServicesStatusData.services)){
            ServicesStatusData.fetchServiceStatus();
        }

        $scope.$watch(function(){
            return ServicesStatusData.services;
        },function(newVal) {
            if(newVal[self.serviceName]){
                if(newVal[self.serviceName] != self.service){
                    self.service = newVal[self.serviceName];
                }
            }
        },true);

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
            //   return loadJobs(true).promise;
            //return self.deferred.promise;
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
            var options = {'Component Name': 'name', 'Components': 'componentsCount', 'Alerts': 'alertsCount', 'Update Date': 'latestAlertTimestamp'};
            var sortOptions = TableOptionsService.newSortOptions(self.pageName,options,'serviceName','asc');
            TableOptionsService.initializeSortOption(self.pageName);
            return sortOptions;
        }

        this.serviceComponentDetails = function(event,component) {
            StateService.OpsManager().ServiceStatus().navigateToServiceComponentDetails(self.serviceName, component.name);
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

    angular.module(moduleName).controller('ServiceHealthDetailsController',["$scope","$http","$filter","$transition$","$interval","$timeout","$q","ServicesStatusData","TableOptionsService","PaginationDataService","AlertsService","StateService",controller]);



});


