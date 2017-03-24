define(['angular','feed-mgr/feeds/define-feed/module-name'], function (angular,moduleName) {

    var controller = function ($scope, $http, $mdDialog, FileUpload, RestUrlService, FeedCreationErrorService, CategoriesService) {

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



        var importComponentTypes = {NIFI_TEMPLATE:"NIFI_TEMPLATE",
                                     TEMPLATE_DATA:"TEMPLATE_DATA",
                                     FEED_DATA:"FEED_DATA",
                                     REUSABLE_TEMPLATE:"REUSABLE_TEMPLATE"};

        this.importComponentOptions = {};

        this.feedDataImportOption = newImportComponentOption(importComponentTypes.FEED_DATA);

        this.templateDataImportOption = newImportComponentOption(importComponentTypes.TEMPLATE_DATA);

        this.nifiTemplateImportOption = newImportComponentOption(importComponentTypes.NIFI_TEMPLATE);

        this.reusableTemplateImportOption = newImportComponentOption(importComponentTypes.REUSABLE_TEMPLATE);


        function newImportComponentOption(component) {
            var option = {importComponent:component,overwrite:false,userAcknowledged:true,shouldImport:true,analyzed:false,continueIfExist:false,properties:[]}
            self.importComponentOptions[component] = option;
            return option;
        }

        function init() {
            setDefaultImportOptions();
        }

        /**
         *
         * @param importOptionsArr array of importOptions
         */
        function updateImportOptions(importOptionsArr){
            var map = _.indexBy(importOptionsArr,'importComponent');
            _.each(importOptionsArr, function(option) {

                if(option.importComponent == importComponentTypes.FEED_DATA){
                    self.feedDataImportOption= option;
                }
                else if(option.importComponent == importComponentTypes.TEMPLATE_DATA){
                    self.templateDataImportOption= option;
                }
                else if(option.importComponent == importComponentTypes.REUSABLE_TEMPLATE){
                    self.reusableTemplateImportOption= option;
                }
                else if(option.importComponent == importComponentTypes.NIFI_TEMPLATE){
                    self.nifiTemplateImportOption= option;
                }
                self.importComponentOptions[option.importComponent] = option;
            });
        }




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

        function showVerifyReplaceReusableTemplateDialog(ev) {
            // Appending dialog to document.body to cover sidenav in docs app
            var confirm = $mdDialog.confirm()
                .title('Import Connecting Reusable Flow')
                .textContent(' The Feed you are importing also contains its reusable flow.  Do you want to also import the reusable flow?')
                .ariaLabel('Import Connecting Reusable Flow')
                .targetEvent(ev)
                .ok('Please do it!')
                .cancel('Nope');
            $mdDialog.show(confirm).then(function () {
                self.verifiedToCreateConnectingReusableTemplate = true;
                self.createConnectingReusableTemplate = true;
                self.importFeed();
            }, function () {
                self.verifiedToCreateConnectingReusableTemplate = true;
                self.createConnectingReusableTemplate = false;
                self.importFeed();

            });
        };

        this.importFeed = function () {
            //reset flags
            self.uploadInProgress = true;
            self.importResult = null;

            showProgress();

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

                    hideProgress();
                    var feedName = responseData.feedName != null ? responseData.feedName : "";

                    if (count == 0) {
                        self.importResultIcon = "check_circle";
                        self.importResultIconColor = "#009933";

                        self.message = "Successfully imported the feed " + feedName + ".";
                        self.overwrite = false;
                        self.overwriteFeedTemplate = false;
                        self.verifiedToCreateConnectingReusableTemplate = false;
                        self.createConnectingReusableTemplate = false;
                    }
                    else {
                        if (responseData.success) {
                            self.message = "Successfully imported and registered the feed " + feedName + " but some errors were found. Please review these errors";
                            self.importResultIcon = "warning";
                            self.importResultIconColor = "#FF9901";
                            self.overwrite = false;
                            self.overwriteFeedTemplate = false;
                            self.verifiedToCreateConnectingReusableTemplate = false;
                            self.createConnectingReusableTemplate = false;
                        }
                        else {
                            self.importResultIcon = "error";
                            self.importResultIconColor = "#FF0000";
                            self.message = "Unable to import and register the feed.  Errors were found. ";
                            self.verifiedToCreateConnectingReusableTemplate = false;
                            self.createConnectingReusableTemplate = false;

                        }
                    }
                }

                self.uploadInProgress = false;
            }
            var errorFn = function (response) {
                var data = response.data
                hideProgress();
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
            var importComponentOptions = []
            _.each(self.importComponentOptions,function(option,key){
                //set defaults for options
                if(option.overwrite){
                    option.userAcknowledged = true;
                    option.shouldImport = true;
                }
                //reset the errors
                option.errorMessages = [];
                importComponentOptions.push(option);
            })

            var params = {
                categorySystemName: angular.isDefined(self.model.category.systemName) && self.model.category.systemName != null ? self.model.category.systemName : "",
                importComponents:angular.toJson(importComponentOptions)
            };

            FileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, params);

        };


        function showProgress() {

        }

        function hideProgress() {

        }

        $scope.$watch(function () {
            return self.feedFile;
        }, function (newVal) {
            if (newVal != null) {
                self.fileName = newVal.name;
            }
            else {
                self.fileName = null;
            }
        })

    };

    angular.module(moduleName).controller('ImportFeedController', ["$scope","$http","$mdDialog","FileUpload","RestUrlService","FeedCreationErrorService","CategoriesService",controller]);

});

