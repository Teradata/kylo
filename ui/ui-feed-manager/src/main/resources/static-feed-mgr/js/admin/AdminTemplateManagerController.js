(function () {

    var controller = function($scope,$http,FileUpload,RestUrlService,PaginationDataService,TableOptionsService){

        var self = this;
        this.templateFile = null;
        this.importBtnDisabled = false;
        this.overwrite = false;
        self.registeredTemplates = [];
        this.exportCardTitle = "Export Template"

        self.importResult = null;
        self.errorMap = null;
        this.importTemplate = function(){
            self.importBtnDisabled = true;
            self.importResult = null;
            showProgress();
            var file = self.templateFile;
            var uploadUrl = RestUrlService.ADMIN_IMPORT_TEMPLATE_URL;
            var successFn = function(response) {
                console.log('templateName',response,response.templateName)
                var count = 0;
                var errorMap = {"FATAL":[],"WARN":[]};
                self.importResult = response;
                if(!response.success) {
                    angular.forEach(response.templateResults.errors, function (processor) {
                        if (processor.validationErrors) {
                            angular.forEach(processor.validationErrors, function (error) {
                                var copy = {};
                                angular.extend(copy, error);
                                angular.extend(copy, processor);
                                copy.validationErrors = null;
                                errorMap[error.severity].push(copy);
                                count++;
                            });
                        }
                    });
                    self.errorMap = errorMap;
                }
                else {
                    //refresh the list
                    getRegisteredTemplates();
                }



                hideProgress();

                self.importBtnDisabled = false;
            }
            var errorFn = function(data){
                hideProgress();
                self.importBtnDisabled = false;
            }
            FileUpload.uploadFileToUrl(file, uploadUrl,successFn,errorFn,{overwrite:self.overwrite});
        };

        function showProgress(){

        }

        function hideProgress(){

        }

        this.exportTemplate = function(event,template){
            console.log('exportTemplate',template,RestUrlService.ADMIN_EXPORT_TEMPLATE_URL+"/"+template.id)
            var promise = $http.get( RestUrlService.ADMIN_EXPORT_TEMPLATE_URL+"/"+template.id);
        }



        //Pagination DAta
        this.pageName="template-mgr";
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        this.paginationId = 'template-templates';
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



        function getRegisteredTemplates(){

            var successFn = function (response) {
                self.loading = false;
                if(response.data){
                    angular.forEach(response.data,function(template){
                       template.exportUrl = RestUrlService.ADMIN_EXPORT_TEMPLATE_URL+"/"+template.id;
                    });
                }
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

    angular.module(MODULE_FEED_MGR).controller('AdminTemplateManagerController',controller);



}());

