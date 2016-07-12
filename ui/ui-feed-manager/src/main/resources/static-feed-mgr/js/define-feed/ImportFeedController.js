(function () {

    var controller = function ($scope, $http, FileUpload, RestUrlService) {

        var self = this;
        this.feedFile = null;
        this.fileName = null;
        this.importBtnDisabled = false;
        this.overwrite = false;

        self.importResult = null;
        self.importResultIcon = "check_circle";
        self.importResultIconColor = "#009933";

        self.errorMap = null;
        self.errorCount = 0;
        this.importFeed = function () {
            self.importBtnDisabled = true;
            self.importResult = null;
            showProgress();
            var file = self.feedFile;
            var uploadUrl = RestUrlService.ADMIN_IMPORT_FEED_URL;
            var successFn = function (response) {
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
                    self.errorMap = errorMap;
                    self.errorCount = count;
                }
                hideProgress();

                if (count == 0) {
                    self.importResultIcon = "check_circle";
                    self.importResultIconColor = "#009933";

                    self.message = "Successfully imported the feed " + response.feedName + ".";
                    self.overwrite = false;
                }
                else {
                    if (response.success) {
                        self.message = "Successfully imported and registered the feed " + response.feedName + " but some errors were found. Please review these errors";
                        self.importResultIcon = "warning";
                        self.importResultIconColor = "#FF9901";
                        self.overwrite = false;
                    }
                    else {
                        self.importResultIcon = "error";
                        self.importResultIconColor = "#FF0000";
                        self.message = "Unable to import and register the feed " + response.feedName + ".  Errors were found. ";
                    }
                }

                self.importBtnDisabled = false;
            }
            var errorFn = function (data) {
                hideProgress();
                self.importBtnDisabled = false;
            }
            FileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, {
                overwrite: self.overwrite
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

