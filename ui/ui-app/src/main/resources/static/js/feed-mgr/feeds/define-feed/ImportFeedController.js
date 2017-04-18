define(['angular','feed-mgr/feeds/define-feed/module-name'], function (angular,moduleName) {

    var controller = function ($scope, $http, $interval,$timeout, $mdDialog, FileUpload, RestUrlService, FeedCreationErrorService, CategoriesService, ImportService, DatasourcesService) {

        /**
         * reference to the controller
         * @type {controller}
         */
        var self = this;

        /**
         * The file to upload
         * @type {null}
         */
        this.feedFile = null;
        /**
         * the name of the file to upload
         * @type {null}
         */
        this.fileName = null;

        /**
         * flag if uploading
         * @type {boolean}
         */
        this.uploadInProgress = false;

        /**
         * Flag to indicate the upload produced validation errors
         * @type {boolean}
         */
        this.validationErrors = false;

        /**
         * unique key to track upload status
         * @type {null}
         */
        this.uploadKey = null;

        /**
         * Status of the current upload
         * @type {Array}
         */
        this.uploadStatusMessages = [];

        /**
         * handle on the $interval object to cancel later
         * @type {null}
         */
        this.uploadStatusCheck = undefined;

        /**
         * Percent upload complete
         * @type {number}
         */
        this.uploadProgress = 0;

        /**
         * Flag to indicate additional properties exist and a header show be shown for template options
         */
        this.additionalInputNeeded = false;

        /**
         * flag to force the feed to be DISABLED upon importing
         * @type {boolean}
         */
        this.disableFeedUponImport = false;

        /**
         * All the importOptions that will be uploaded
         * @type {{}}
         */
        this.importComponentOptions = {};

        /**
         * Feed ImportOptions
         */
        this.feedDataImportOption = ImportService.newFeedDataImportOption();

        /**
         * Registered Template import options
         */
        this.templateDataImportOption = ImportService.newTemplateDataImportOption();

        /**
         * NiFi template options
         */
        this.nifiTemplateImportOption = ImportService.newNiFiTemplateImportOption();

        /**
         * Reusable template options
         */
        this.reusableTemplateImportOption = ImportService.newReusableTemplateImportOption();

        /**
         * User data sources options
         */
        this.userDatasourcesOption = ImportService.newUserDatasourcesImportOption();

        /**
         * Called when a user changes a import option for overwriting
         */
        this.onOverwriteSelectOptionChanged = ImportService.onOverwriteSelectOptionChanged;

        /**
         * The angular Form for validation
         * @type {{}}
         */
        this.importFeedForm = {};

        /**
         * List of available data sources.
         * @type {Array.<JdbcDatasource>}
         */
        self.availableDatasources = [];



        self.importResult = null;
        self.importResultIcon = "check_circle";
        self.importResultIconColor = "#009933";

        self.errorMap = null;
        self.errorCount = 0;

        self.categorySelectedItemChange = selectedItemChange;
        self.categorySearchTextChanged = searchTextChange;
        self.categoriesService = CategoriesService;


        self.model = {
            category: {}
        };


        this.importFeed = function () {
            //reset flags
            self.uploadInProgress = true;
            self.importResult = null;

            var file = self.feedFile;
            var uploadUrl = RestUrlService.ADMIN_IMPORT_FEED_URL;

            var successFn = function (response) {
                var responseData = response.data;
                //reassign the options back from the response data
                var importComponentOptions = responseData.importOptions.importComponentOptions;
                //map the options back to the object map
                updateImportOptions(importComponentOptions);

                if(!responseData.valid){
                    //Validation Error.  Additional Input is needed by the end user
                    self.additionalInputNeeded = true;
/*
                    if(self.showTemplateOptions == false ) {
                       var show = (self.templateDataImportOption.overwriteSelectValue == 'true' || self.templateDataImportOption.overwriteSelectValue == 'false' ) || (self.templateDataImportOption.errorMessages != null && self.templateDataImportOption.errorMessages.length >0);
                       if(show) {
                           //if this is the first time we show the options, set the overwrite flag to be empty
                           self.templateDataImportOption.overwriteSelectValue = '';
                       }
                        self.showTemplateOptions = show;
                    }

                    if(self.showReusableTemplateOptions == false) {
                        self.showReusableTemplateOptions = (self.reusableTemplateImportOption.overwriteSelectValue == 'true' || self.reusableTemplateImportOption.overwriteSelectValue == 'false' )|| (self.reusableTemplateImportOption.errorMessages != null && self.reusableTemplateImportOption.errorMessages.length >0);
                    }
                    */

                }
                else {

                    var count = 0;
                    var errorMap = {"FATAL": [], "WARN": []};
                    self.importResult = responseData;
                    //if(responseData.templateResults.errors) {
                    if (responseData.template.controllerServiceErrors) {
                        //angular.forEach(responseData.templateResults.errors, function (processor) {
                        angular.forEach(responseData.template.controllerServiceErrors, function (processor) {
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

                    }
                    if (responseData.nifiFeed == null || responseData.nifiFeed == undefined) {
                        errorMap["FATAL"].push({message: "Unable to import feed"});
                    }
                    if (responseData.nifiFeed && responseData.nifiFeed) {
                        count += FeedCreationErrorService.parseNifiFeedErrors(responseData.nifiFeed, errorMap);
                    }
                    self.errorMap = errorMap;
                    self.errorCount = count;

                    var feedName = responseData.feedName != null ? responseData.feedName : "";

                    if (count == 0) {
                        self.importResultIcon = "check_circle";
                        self.importResultIconColor = "#009933";

                        self.message = "Successfully imported the feed " + feedName + ".";

                        resetImportOptions();

                    }
                    else {
                        if (responseData.success) {
                            resetImportOptions();

                            self.message = "Successfully imported and registered the feed " + feedName + " but some errors were found. Please review these errors";
                            self.importResultIcon = "warning";
                            self.importResultIconColor = "#FF9901";
                               }
                        else {
                            self.importResultIcon = "error";
                            self.importResultIconColor = "#FF0000";
                            self.message = "Unable to import and register the feed.  Errors were found. ";
                        }
                    }
                }

                self.uploadInProgress = false;
                stopUploadStatus(1000);
            }
            var errorFn = function (response) {
                var data = response.data
                //reset the flags
                self.importResult = {};
                self.uploadInProgress = false;

                //set error indicators and messages
                self.importResultIcon = "error";
                self.importResultIconColor = "#FF0000";
                var msg =  "Unable to import and register the feed.  Errors were found. Ensure you are trying to upload a valid feed export file and not a template export file. ";
                if(data.developerMessage){
                    msg += data.developerMessage;
                }
                self.message = msg;
                stopUploadStatus(1000);
            }


            if (angular.isDefined(self.categorySearchText) && self.categorySearchText != null && self.categorySearchText != "" && self.model.category.systemName == null) {
                //error the category has text in it, but not selected
                //attempt to get it
                var category = CategoriesService.findCategoryByName(self.categorySearchText);
                if (category != null) {
                    self.model.category = category;
                }
            }

            //build up the options from the Map and into the array for uploading
            var importComponentOptions = ImportService.getImportOptionsForUpload(self.importComponentOptions);

            //generate a new upload key for status tracking
            self.uploadKey = ImportService.newUploadKey();

            var params = {
                uploadKey : self.uploadKey,
                categorySystemName: angular.isDefined(self.model.category.systemName) && self.model.category.systemName != null ? self.model.category.systemName : "",
                disableFeedUponImport:self.disableFeedUponImport,
                importComponents:angular.toJson(importComponentOptions)
            };


            startUploadStatus();
            FileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, params);

        };

        function searchTextChange(text) {
        }
        function selectedItemChange(item) {
            if(item != null && item != undefined) {
                self.model.category.name = item.name;
                self.model.category.id = item.id;
                self.model.category.systemName = item.systemName;
            }
            else {
                self.model.category.name = null;
                self.model.category.id = null;
                self.model.category.systemName = null;
            }
        }



        /**
         * Set the default values for the import options
         */
        function setDefaultImportOptions(){
            self.nifiTemplateImportOption.continueIfExists = true;
          //  self.reusableTemplateImportOption.userAcknowledged = false;
            //it is assumed with a feed you will be importing the template with the feed
            self.templateDataImportOption.userAcknowledged=true;
            self.feedDataImportOption.continueIfExists=false;
        }

        function indexImportOptions(){
            var arr = [self.feedDataImportOption,self.templateDataImportOption,self.nifiTemplateImportOption,self.reusableTemplateImportOption];
            self.importComponentOptions = _.indexBy(arr,'importComponent')
        }

        /**
         * Initialize the Controller
         */
        function init() {
            indexImportOptions();
            setDefaultImportOptions();

            // Get the list of data sources
            DatasourcesService.findAll()
                .then(function (datasources) {
                    self.availableDatasources = datasources;
                });
        }

        /**
         * Reset the options back to their orig. state
         */
        function resetImportOptions(){
            self.importComponentOptions = {};

            self.feedDataImportOption = ImportService.newFeedDataImportOption();

            self.templateDataImportOption = ImportService.newTemplateDataImportOption();

            self.nifiTemplateImportOption = ImportService.newNiFiTemplateImportOption();

            self.reusableTemplateImportOption = ImportService.newReusableTemplateImportOption();

            self.userDatasourcesOption = ImportService.newUserDatasourcesImportOption();

            indexImportOptions();
            setDefaultImportOptions();

            self.additionalInputNeeded = false;
            self.disableFeedUponImport = false;

        }

        /**
         *
         * @param importOptionsArr array of importOptions
         */
        function updateImportOptions(importOptionsArr){
            var map = _.indexBy(importOptionsArr,'importComponent');
            _.each(importOptionsArr, function(option) {
                if(option.userAcknowledged){
                    option.overwriteSelectValue = ""+option.overwrite;
                }

                if(option.importComponent == ImportService.importComponentTypes.FEED_DATA){
                    self.feedDataImportOption= option;
                }
                else if(option.importComponent == ImportService.importComponentTypes.TEMPLATE_DATA){
                    self.templateDataImportOption= option;
                }
                else if(option.importComponent == ImportService.importComponentTypes.REUSABLE_TEMPLATE){
                    self.reusableTemplateImportOption= option;
                }
                else if(option.importComponent == ImportService.importComponentTypes.NIFI_TEMPLATE){
                    self.nifiTemplateImportOption= option;
                } else if (option.importComponent === ImportService.importComponentTypes.USER_DATASOURCES) {
                    self.userDatasourcesOption = option;
                }
                self.importComponentOptions[option.importComponent] = option;
            });
        }

        /**
         * Stop the upload status check,
         * @param delay wait xx millis before stopping (allows for the last status to be queried)
         */
        function stopUploadStatus(delay){

            function stopStatusCheck(){
                self.uploadProgress = 0;
                if (angular.isDefined(self.uploadStatusCheck)) {
                    $interval.cancel(self.uploadStatusCheck);
                    self.uploadStatusCheck = undefined;
                }
            }

            if(delay != undefined) {
                $timeout(function(){
                    stopStatusCheck();
                },delay)
            }
            else {
                stopStatusCheck();
            }

        }

        /**
         * starts the upload status check
         */
        function startUploadStatus(){
            stopUploadStatus();
            self.uploadStatusMessages =[];
           self.uploadStatusCheck = $interval(function() {
                //poll for status
                $http.get(RestUrlService.ADMIN_UPLOAD_STATUS_CHECK(self.uploadKey)).then(function(response) {
                    if(response && response.data && response.data != null) {
                        self.uploadStatusMessages = response.data.messages;
                        self.uploadProgress = response.data.percentComplete;
                    }
                }, function(err){
                  //  self.uploadStatusMessages = [];
                });
            },500);
        }


        $scope.$watch(function () {
            return self.feedFile;
        }, function (newVal,oldValue) {
            if (newVal != null) {
                self.fileName = newVal.name;
            }
            else {
                self.fileName = null;
            }
            //reset them if changed
            if(newVal != oldValue){
                resetImportOptions();
            }
        })

        init();

    };

    angular.module(moduleName).controller('ImportFeedController', ["$scope","$http","$interval","$timeout","$mdDialog","FileUpload","RestUrlService","FeedCreationErrorService","CategoriesService","ImportService","DatasourcesService",controller]);

});

