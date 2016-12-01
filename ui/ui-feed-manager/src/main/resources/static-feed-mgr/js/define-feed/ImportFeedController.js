(function () {

    var controller = function ($scope, $http, $mdDialog, FileUpload, RestUrlService, FeedCreationErrorService, CategoriesService) {

        var self = this;
        this.feedFile = null;
        this.fileName = null;
        this.importBtnDisabled = false;
        this.overwrite = false;

        this.verifiedToCreateConnectingReusableTemplate = false;
        this.createConnectingReusableTemplate = false;

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
            //   $log.info('Text changed to ' + text);
        }
        function selectedItemChange(item) {
            if(item != null && item != undefined) {
                self.model.category.name = item.name;
                self.model.category.id = item.id;
                self.model.category.systemName = item.systemName;
                $http.get(RestUrlService.GET_FEEDS_URL).then(function(response) {
                    self.existingFeedNames = {};
                    angular.forEach(response.data, function(feed) {
                        if (feed.categoryId === item.id) {
                            self.existingFeedNames[feed.systemFeedName] = true;
                        }
                    });
                });
            }
            else {
                self.model.category.name = null;
                self.model.category.id = null;
                self.model.category.systemName = null;
                self.existingFeedNames = {};
            }
        }

        function showVerifyReplaceReusableTemplateDialog(ev) {
            // Appending dialog to document.body to cover sidenav in docs app
            var confirm = $mdDialog.confirm()
                .title('Import Connecting Reusable Flow')
                .textContent(' The Feed you are importing also contains its reusable flow.  Do you want to also import the reusable flow and version that as well?')
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
            self.importBtnDisabled = true;
            self.importResult = null;
            showProgress();
            var file = self.feedFile;
            var uploadUrl = RestUrlService.ADMIN_IMPORT_FEED_URL;
            var successFn = function (response) {

                if (response.template.verificationToReplaceConnectingResuableTemplateNeeded) {
                    showVerifyReplaceReusableTemplateDialog();
                    return;
                }



                var count = 0;
                var errorMap = {"FATAL": [], "WARN": []};
                self.importResult = response;
                //if(response.templateResults.errors) {
                if (response.template.controllerServiceErrors) {
                    //angular.forEach(response.templateResults.errors, function (processor) {
                    angular.forEach(response.template.controllerServiceErrors, function (processor) {
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
                if (response.nifiFeed == null || response.nifiFeed == undefined) {
                    errorMap["FATAL"].push({message: "Unable to import feed"});
                }
                if (response.nifiFeed && response.nifiFeed) {
                    count += FeedCreationErrorService.parseNifiFeedErrors(response.nifiFeed, errorMap);
                }
                self.errorMap = errorMap;
                self.errorCount = count;

                hideProgress();

                if (count == 0) {
                    self.importResultIcon = "check_circle";
                    self.importResultIconColor = "#009933";

                    self.message = "Successfully imported the feed " + response.feedName + ".";
                    self.overwrite = false;
                    self.verifiedToCreateConnectingReusableTemplate = false;
                    self.createConnectingReusableTemplate = false;
                }
                else {
                    if (response.success) {
                        self.message = "Successfully imported and registered the feed " + response.feedName + " but some errors were found. Please review these errors";
                        self.importResultIcon = "warning";
                        self.importResultIconColor = "#FF9901";
                        self.overwrite = false;
                        self.verifiedToCreateConnectingReusableTemplate = false;
                        self.createConnectingReusableTemplate = false;
                    }
                    else {
                        self.importResultIcon = "error";
                        self.importResultIconColor = "#FF0000";
                        self.message = "Unable to import and register the feed " + response.feedName + ".  Errors were found. ";
                        self.verifiedToCreateConnectingReusableTemplate = false;
                        self.createConnectingReusableTemplate = false;

                    }
                }

                self.importBtnDisabled = false;
            }
            var errorFn = function (data) {
                hideProgress();
                self.importResult = {};
                self.importBtnDisabled = false;
                self.importResultIcon = "error";
                self.importResultIconColor = "#FF0000";
                var msg =  "Unable to import and register the feed.  Errors were found. Ensure you are trying to upload a valid feed export file and not a template export file. ";
                if(data.developerMessage){
                    msg += data.developerMessage;
                }
                self.message = msg;
                self.verifiedToCreateConnectingReusableTemplate = false;
                self.createConnectingReusableTemplate = false;
            }

            var createConnectingReusableFlow = 'NOT_SET';
            if (self.verifiedToCreateConnectingReusableTemplate && self.createConnectingReusableTemplate) {
                createConnectingReusableFlow = 'YES';
            }
            else if (self.verifiedToCreateConnectingReusableTemplate && !self.createConnectingReusableTemplate) {
                createConnectingReusableFlow = 'NO';
            }
            FileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, {
                overwrite: self.overwrite,
                categorySystemName: angular.isDefined(self.model.category.systemName) ? self.model.category.systemName : "",
                importConnectingReusableFlow: createConnectingReusableFlow
            });
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

    angular.module(MODULE_FEED_MGR).controller('ImportFeedController', controller);

}());

