define(['angular','ops-mgr/service-health/module-name'], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                cardTitle: "@",
                refreshIntervalTime:"@"
            },
            controllerAs: 'vm',
            scope: true,
            templateUrl: 'js/ops-mgr/service-health/service-health-template.html',
            controller: "ServiceHealthController",
            link: function ($scope, element, attrs, controller) {

            }
        };
    }

    var controller = function ($scope,$http, $filter, $interval, $timeout, $q,ServicesStatusData, TableOptionsService, PaginationDataService, AlertsService, StateService) {
        var self = this;
        this.pageName = 'service-health';
        //Page State
        this.loading = true;
        this.showProgress = true;

        this.services = [];
        this.allServices = [];
        this.totalServices = 0;

        $scope.$watch(function(){
            return self.filter;
        },function(newVal){
            if(newVal && newVal != '') {
           //     self.services = $filter('filter')(self.allServices, newVal);
                self.totalServices = self.services.length;
            }
            else {
            //    self.services = self.allServices;
            }
        })


        //Pagination and view Type (list or table)
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        PaginationDataService.setRowsPerPageOptions(this.pageName,['5','10','20','50']);
        this.viewType = PaginationDataService.viewType(this.pageName);
        this.currentPage =PaginationDataService.currentPage(self.pageName)||1;
        this.filter = PaginationDataService.filter(self.pageName);
        this.sortOptions = loadSortOptions();

        //Load the data
        loadData();


        //Refresh Intervals
        this.setRefreshInterval = setRefreshInterval;
        this.clearRefreshInterval = clearRefreshInterval;

        setRefreshInterval();

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
         //   return loadJobs(true).promise;
            //return self.deferred.promise;
        };

        this.onPaginationChange = function (page, limit) {
            PaginationDataService.currentPage(self.pageName,null,page);
            self.currentPage = page;
           // return loadJobs(true).promise;
        };


        //Sort Functions
        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Service Name':'serviceName','Components':'componentsCount','Alerts':'alertsCount','Update Date':'latestAlertTimestamp'};

            var sortOptions = TableOptionsService.newSortOptions(self.pageName,options,'serviceName','asc');
            TableOptionsService.initializeSortOption(self.pageName);
            return sortOptions;

        }



        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        this.selectedTableOption = function(option) {
            var sortString = TableOptionsService.toSortString(option);
            PaginationDataService.sort(self.pageName,sortString);
            var updatedOption = TableOptionsService.toggleSort(self.pageName,option);
            TableOptionsService.setSortOption(self.pageName,sortString);
        }

        //Load Jobs

        function loadData() {
                var successFn = function (data) {
                    self.services = data;
                    self.totalServices = self.services.length;
                    self.allServices = data;
                    self.loading == false;
                    self.showProgress = false;
                }
                var errorFn = function (err) {
                    console.log('error', err);
                }
                var finallyFn = function () {

                }
                //Only Refresh if the modal dialog does not have any open alerts
            ServicesStatusData.fetchServiceStatus(successFn,errorFn);

        }



        this.serviceDetails = function(event, service){
            StateService.OpsManager().ServiceStatus().navigateToServiceDetails(service.serviceName);
        }

        function clearRefreshInterval() {
            if (self.refreshInterval != null) {
                $interval.cancel(self.refreshInterval);
                self.refreshInterval = null;
            }
        }

        function setRefreshInterval() {
            self.clearRefreshInterval();
            if (self.refreshIntervalTime) {
                self.refreshInterval = $interval(loadData, self.refreshIntervalTime);

            }
        }

        $scope.$on('$destroy', function(){
            clearRefreshInterval();
        });





    };


    angular.module(moduleName).controller('ServiceHealthController', ["$scope","$http","$filter","$interval","$timeout","$q","ServicesStatusData","TableOptionsService","PaginationDataService","AlertsService","StateService",controller]);

    angular.module(moduleName)
        .directive('tbaServiceHealth', directive);

});
