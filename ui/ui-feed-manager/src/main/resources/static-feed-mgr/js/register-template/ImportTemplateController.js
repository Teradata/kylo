(function () {

    var controller = function ($scope, $http,$mdDialog, FileUpload, RestUrlService) {

        var self = this;
        this.templateFile = null;
        this.fileName = null;
        this.importBtnDisabled = false;
        this.overwrite = false;
        this.createReusableFlow = false;
        this.xmlType = false;

        this.verifiedToCreateConnectingReusableTemplate = false;
        this.createConnectingReusableTemplate = false;

        self.importResult = null;
        self.importResultIcon = "check_circle";
        self.importResultIconColor = "#009933";

        self.errorMap = null;
        self.errorCount = 0;


        function showVerifyReplaceReusableTemplateDialog(ev) {
            // Appending dialog to document.body to cover sidenav in docs app
            var confirm = $mdDialog.confirm()
                .title('Import Connecting Reusable Flow')
                .textContent(' The Template you are importing also contains its reusable flow.  Do you want to also import the reusable flow and version that as well?')
                .ariaLabel('Import Connecting Reusable Flow')
                .targetEvent(ev)
                .ok('Please do it!')
                .cancel('Nope');
            $mdDialog.show(confirm).then(function() {
                self.verifiedToCreateConnectingReusableTemplate = true;
                self.createConnectingReusableTemplate = true;
                self.importTemplate();
            }, function() {
                self.verifiedToCreateConnectingReusableTemplate = true;
                self.createConnectingReusableTemplate = false;
                self.importTemplate();

            });
        };



        this.importTemplate = function () {
            self.importBtnDisabled = true;
            self.importResult = null;
            showProgress();
            var file = self.templateFile;
            var uploadUrl = RestUrlService.ADMIN_IMPORT_TEMPLATE_URL;
            var successFn = function (response) {

                if(response.verificationToReplaceConnectingResuableTemplateNeeded){
                    showVerifyReplaceReusableTemplateDialog();
                    return;
                }


                var count = 0;
                var errorMap = {"FATAL": [], "WARN": []};
                self.importResult = response;
                //if(response.templateResults.errors) {
                if (response.controllerServiceErrors) {
                    //angular.forEach(response.templateResults.errors, function (processor) {
                    angular.forEach(response.controllerServiceErrors, function (processor) {
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
                    if (response.zipFile == true) {
                        self.message = "Successfully imported and registered the template " + response.templateName;
                    }
                    else {
                        self.message = "Successfully imported the template " + response.templateName + " into Nifi"
                    }
                    self.createReusableFlow = false;
                    self.overwrite = false;
                    self.verifiedToCreateConnectingReusableTemplate = false;
                    self.createConnectingReusableTemplate = false;
                }
                else {
                    if (response.success) {
                        self.message = "Successfully imported " + (response.zipFile == true ? "and registered " : "") + " the template " + response.templateName + " but some errors were found. Please review these errors";
                        self.importResultIcon = "warning";
                        self.importResultIconColor = "#FF9901";
                        self.createReusableFlow = false;
                        self.overwrite = false;
                        self.verifiedToCreateConnectingReusableTemplate = false;
                        self.createConnectingReusableTemplate = false;
                    }
                    else {
                        self.importResultIcon = "error";
                        self.importResultIconColor = "#FF0000";
                        self.message = "Unable to import " + (response.zipFile == true ? "and register " : "") + " the template " + response.templateName + ".  Errors were found.  You may need to fix the template or go to Nifi to fix the Controller Services and then try to import again.";
                        self.verifiedToCreateConnectingReusableTemplate = false;
                        self.createConnectingReusableTemplate = false;
                    }
                }

                self.importBtnDisabled = false;
            }
            var errorFn = function (data) {
                hideProgress();
                self.importBtnDisabled = false;
            }
            var createConnectingReusableFlow = 'NOT_SET';
            if(self.verifiedToCreateConnectingReusableTemplate && self.createConnectingReusableTemplate) {
                createConnectingReusableFlow = 'YES';
            }
            else if(self.verifiedToCreateConnectingReusableTemplate && !self.createConnectingReusableTemplate) {
                createConnectingReusableFlow = 'NO';
            }

            FileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, {
                overwrite: self.overwrite,
                createReusableFlow: self.createReusableFlow,
                importConnectingReusableFlow:createConnectingReusableFlow
            });
        };

        function showProgress() {

        }

        function hideProgress() {

        }


        $scope.$watch(function () {
            return self.templateFile;
        }, function (newVal) {
            if (newVal != null) {
                self.fileName = newVal.name;
                self.xmlType = self.fileName.toLowerCase().endsWith(".xml");
            }
            else {
                self.fileName = null;
                self.xmlType = false;
                self.createReusableFlow = false;
            }

            if (!self.xmlType) {
                self.createReusableFlow = false;
            }
        })

    };

    angular.module(MODULE_FEED_MGR).controller('ImportTemplateController', controller);


}());

