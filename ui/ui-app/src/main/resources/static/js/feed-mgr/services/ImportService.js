define(['angular','services/module-name'], function (angular,moduleName) {

    angular.module(moduleName).factory('ImportService', [function () {

        var importComponentTypes = {NIFI_TEMPLATE:"NIFI_TEMPLATE",
            TEMPLATE_DATA:"TEMPLATE_DATA",
            FEED_DATA:"FEED_DATA",
            REUSABLE_TEMPLATE:"REUSABLE_TEMPLATE"};

        function guid() {
            function s4() {
                return Math.floor((1 + Math.random()) * 0x10000)
                    .toString(16)
                    .substring(1);
            }
            return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
                   s4() + '-' + s4() + s4() + s4();
        }

        var data = {

            importComponentTypes : importComponentTypes,

            newImportComponentOption: function(component) {
                var option = {importComponent:component,overwriteSelectValue:null,overwrite:false,userAcknowledged:false,shouldImport:true,analyzed:false,continueIfExists:false,properties:[]}
                return option;
            },
            newReusableTemplateImportOption: function(){
                return data.newImportComponentOption(importComponentTypes.REUSABLE_TEMPLATE);
            },
            newTemplateDataImportOption: function(){
                return data.newImportComponentOption(importComponentTypes.TEMPLATE_DATA);
            },
            newFeedDataImportOption: function(){
                return data.newImportComponentOption(importComponentTypes.FEED_DATA);
            },
            newNiFiTemplateImportOption: function(){
                return data.newImportComponentOption(importComponentTypes.NIFI_TEMPLATE);
            },
            newUploadKey:function(){
                return _.uniqueId("upload_")+new Date().getTime()+guid();
            },

            /**
             * Update properties when a user chooses to overwrite or not
             * @param importComponentOption
             */
            onOverwriteSelectOptionChanged :function(importComponentOption){
                importComponentOption.userAcknowledged = true;
                if(importComponentOption.overwriteSelectValue == "true"){
                    importComponentOption.overwrite = true


                }
                else if(importComponentOption.overwriteSelectValue == "false"){
                    importComponentOption.overwrite = false;
                    importComponentOption.continueIfExists = true;
                }
                else {
                    importComponentOption.userAcknowledged = false;
                }
            },

            /**
             * return the map of options as an array ready for upload/import
             * @param importOptionsMap a map of {ImportType: importOption}
             * @returns {Array} the array of options to be imported
             */
            getImportOptionsForUpload:function(importOptionsMap){
                var importComponentOptions = []
                _.each(importOptionsMap,function(option,key){
                    //set defaults for options
                    option.errorMessages = [];

                    if(option.overwrite){
                        option.userAcknowledged = true;
                        option.shouldImport = true;
                    }

                    if(option.userAcknowledged && !option.overwrite){
                        option.continueIfExists = true;
                    }
                    //reset the errors
                    option.errorMessages = [];
                    importComponentOptions.push(option);
                });
                return importComponentOptions;
            },
            /**
             * Check if an importOption is a specific type
             * @param importOption the option to check
             * @param importComponentType the type of the option
             * @returns {boolean} true if match, false if not
             */
            isImportOption: function(importOption, importComponentType){
                return importComponent.importComponent == importComponentType;
            }
    }
        return data;
    }]);
});


