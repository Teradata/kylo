(function () {

    var controller = function($scope,$http,RestUrlService, PaginationDataService,TableOptionsService, AddButtonService, StateService, RegisterTemplateService){

        var self = this;
        self.registeredTemplates = [];
        this.loading = true;
        this.cardTitle = 'Registered Templates'
        AddButtonService.registerAddButton('registered-templates',function() {
            RegisterTemplateService.resetModel();
            StateService.navigateToRegisterTemplate();
        });

        //Pagination DAta
        this.pageName="registered-templates";
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        this.paginationId = 'registered-templates';
        PaginationDataService.setRowsPerPageOptions(this.pageName,['5','10','20','50','All']);
        this.viewType = PaginationDataService.viewType(this.pageName);
        this.sortOptions = loadSortOptions();

        this.filter = PaginationDataService.filter(self.pageName);


        $scope.$watch(function(){
            return self.viewType;
        },function(newVal) {
            self.onViewTypeChange(newVal);
        })

        this.onViewTypeChange = function(viewType) {
            PaginationDataService.viewType(this.pageName, self.viewType);
        }

        this.onOrderChange = function (order) {
            PaginationDataService.sort(self.pageName,order);
            TableOptionsService.setSortOption(self.pageName,order);
        };

        this.onPaginationChange = function (page, limit) {
            PaginationDataService.currentPage(self.pageName,null,page);
        };


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

        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Template':'templateName','Last Modified':'updateDate'};

            var sortOptions = TableOptionsService.newSortOptions(self.pageName,options,'templateName','asc');
            var currentOption = TableOptionsService.getCurrentSort(self.pageName);
            if(currentOption) {
                TableOptionsService.saveSortOption(self.pageName,currentOption)
            }
            return sortOptions;
        }

        this.templateDetails = function(event,template){
            RegisterTemplateService.resetModel();
            StateService.navigateToRegisteredTemplate(template.id, template.nifiTemplateId);
        }



        function getRegisteredTemplates(){

            var successFn = function (response) {
                self.loading = false;
                 self.registeredTemplates = response.data;
            }
            var errorFn = function (err) {
                self.loading = false;

            }
            var promise = $http.get(RestUrlService.GET_REGISTERED_TEMPLATES_URL);
            promise.then(successFn, errorFn);
            return promise;

        }





        getRegisteredTemplates();
    };

    angular.module(MODULE_FEED_MGR).controller('RegisteredTemplatesController',controller);



}());