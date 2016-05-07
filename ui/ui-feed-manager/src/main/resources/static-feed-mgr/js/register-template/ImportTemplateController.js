(function () {

    var controller = function($scope,$http,FileUpload,RestUrlService){

        var self = this;
        this.templateFile = null;
        this.importBtnDisabled = false;
        this.overwrite = false;

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

    };

    angular.module(MODULE_FEED_MGR).controller('ImportTemplateController',controller);



}());

