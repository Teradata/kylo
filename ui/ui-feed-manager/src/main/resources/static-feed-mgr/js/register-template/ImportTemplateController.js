(function () {

    var controller = function($scope,$http,FileUpload,RestUrlService){

        var self = this;
        this.templateFile = null;
        this.importBtnDisabled = false;
        this.overwrite = false;

        self.importResult = null;
        self.importResultIcon = "check_circle";
        self.importResultIconColor="#009933";

        self.errorMap = null;
        self.errorCount = 0;
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
                //if(response.templateResults.errors) {
                if(response.controllerServiceErrors) {
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

                if(count ==0){
                    self.importResultIcon = "check_circle";
                    self.importResultIconColor="#009933";
                    if(response.zipFile == true)
                    {
                        self.message = "Successfully imported and registered the template "+response.templateName;
                    }
                    else {
                        self.message = "Successfully imported the template "+response.templateName+" into Nifi"
                    }
                }
                else {
                    if(response.success){
                        self.message = "Successfully imported "+(response.zipFile == true ? "and registered " : "")+" the template "+response.templateName+" but some errors were found. Please review these errors";
                        self.importResultIcon = "warning";
                        self.importResultIconColor="#FF9901";
                    }
                    else {
                        self.importResultIcon = "error";
                        self.importResultIconColor="#FF0000";
                        self.message = "Unable to import "+(response.zipFile == true ? "and register " : "")+" the template "+response.templateName+".  Errors were found.  You may need to fix the template or go to Nifi to fix the Controller Services and then try to import again.";
                    }
                }

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

    };

    angular.module(MODULE_FEED_MGR).controller('ImportTemplateController',controller);



}());

