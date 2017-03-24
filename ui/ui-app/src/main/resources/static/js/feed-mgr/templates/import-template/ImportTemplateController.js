define(['angular',"feed-mgr/templates/module-name"], function (angular,moduleName) {

    var controller = function ($scope, $http,$mdDialog, FileUpload, RestUrlService) {

        /**
         * Pointer to this controller
         * @type {controller}
         */
        var self = this;
        /**
         * The file to upload
         * @type {null}
         */
        this.templateFile = null;
        /**
         * The name of the file to upload
         * @type {null}
         */
        this.fileName = null;
        /**
         * true if uploading, false if not
         * @type {boolean}
         */
        this.importInProgress = false;

        /**
         * true to overwrite the template
          * @type {boolean}
         */
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

        self.showReorderList = false;

        /**
         * User supplied properties required before the feed is imported
         * @type {undefined}
         */
        self.userSuppliedTemplateProperties = undefined;
        /**
         * The input type, either password or text.  Changed via the checkbox in the UI
         * @type {string}
         */
        self.userSuppliedTemplatePropertyInputType = 'password';
        /**
         * Show the text string, or keep it as a password showing asterisks
         * @type {boolean}
         */
        self.showUserSuppliedTemplatePropertyValues = false;


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

        self.showHidePassword = function(){
            if (self.userSuppliedPropertyInputType == 'password')
                self.userSuppliedPropertyInputType= 'text';
            else
                self.userSuppliedPropertyInputType = 'password';
        };


        this.importTemplate = function () {
            self.showReorderList = false;
            self.importInProgress = true;
            self.importResult = null;
            showProgress();
            var file = self.templateFile;
            var uploadUrl = RestUrlService.ADMIN_IMPORT_TEMPLATE_URL;
            var successFn = function (response) {
                var responseData = response.data;
                if(responseData.verificationToReplaceConnectingResuableTemplateNeeded){
                    showVerifyReplaceReusableTemplateDialog();
                    return;
                }

                if(responseData.importOptions.properties){
                    _.each(responseData.importOptions.properties,function(prop){
                        var inputName = prop.processorName.split(' ').join('_')+prop.propertyKey.split(' ').join('_');
                        prop.inputName = inputName.toLowerCase();
                    });
                }
                self.userSuppliedTemplateProperties = responseData.importOptions.templateProperties;



                var count = 0;
                var errorMap = {"FATAL": [], "WARN": []};
                self.importResult = responseData;
                //if(responseData.templateResults.errors) {
                if (responseData.templateResults.errors) {
                    //angular.forEach(responseData.templateResults.errors, function (processor) {
                    angular.forEach(responseData.templateResults.errors, function (processor) {
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
                    self.showReorderList = true;
                    self.importResultIcon = "check_circle";
                    self.importResultIconColor = "#009933";
                    if (responseData.zipFile == true) {
                        self.message = "Successfully imported and registered the template " + responseData.templateName;
                    }
                    else {
                        self.message = "Successfully imported the template " + responseData.templateName + " into Nifi"
                    }
                    self.createReusableFlow = false;
                    self.overwrite = false;
                    self.verifiedToCreateConnectingReusableTemplate = false;
                    self.createConnectingReusableTemplate = false;
                }
                else {
                    if (responseData.success) {
                        self.showReorderList = true;
                        self.message = "Successfully imported " + (responseData.zipFile == true ? "and registered " : "") + " the template " + responseData.templateName + " but some errors were found. Please review these errors";
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
                        self.message = "Unable to import " + (responseData.zipFile == true ? "and register " : "") + " the template " + responseData.templateName + ".  Errors were found.  You may need to fix the template or go to Nifi to fix the Controller Services and then try to import again.";
                        self.verifiedToCreateConnectingReusableTemplate = false;
                        self.createConnectingReusableTemplate = false;
                    }
                }

                self.importInProgress = false;
            }
            var errorFn = function (response) {
                hideProgress();
                self.importResult = response.data;
                self.importInProgress = false;
                self.importResultIcon = "error";
                self.importResultIconColor = "#FF0000";
                var msg = response.data.message != undefined ? response.data.message : "Unable to import the template.";
                self.message = msg;
                self.verifiedToCreateConnectingReusableTemplate = false;
                self.createConnectingReusableTemplate = false;
            }
            var createConnectingReusableFlow = 'NOT_SET';
            if(self.verifiedToCreateConnectingReusableTemplate && self.createConnectingReusableTemplate) {
                createConnectingReusableFlow = 'YES';
            }
            else if(self.verifiedToCreateConnectingReusableTemplate && !self.createConnectingReusableTemplate) {
                createConnectingReusableFlow = 'NO';
            }



            var params = {
                overwrite: self.overwrite,
                createReusableFlow: self.createReusableFlow,
                importConnectingReusableFlow: createConnectingReusableFlow
            };
            if(self.userSuppliedTemplateProperties != undefined && ! _.isEmpty(self.userSuppliedTemplateProperties)  ) {
                params.templateProperties = angular.toJson(self.userSuppliedTemplateProperties);
            }

            FileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, params);
        };

        function showProgress() {

        }

        function hideProgress() {

        }

        function resetUserSuppliedValues(){

            self.userSuppliedTemplateProperties = undefined;
            self.userSuppliedTemplatePropertyInputType = 'password';
            self.showUserSuppliedTemplatePropertyValues = false;

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
            resetUserSuppliedValues();
        })

    };

    angular.module(moduleName).controller('ImportTemplateController', ["$scope","$http","$mdDialog","FileUpload","RestUrlService",controller]);


});

