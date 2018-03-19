define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    var ImportComponentType;
    (function (ImportComponentType) {
        ImportComponentType[ImportComponentType["NIFI_TEMPLATE"] = 0] = "NIFI_TEMPLATE";
        ImportComponentType[ImportComponentType["TEMPLATE_DATA"] = 1] = "TEMPLATE_DATA";
        ImportComponentType[ImportComponentType["FEED_DATA"] = 2] = "FEED_DATA";
        ImportComponentType[ImportComponentType["REUSABLE_TEMPLATE"] = 3] = "REUSABLE_TEMPLATE";
        ImportComponentType[ImportComponentType["REMOTE_INPUT_PORT"] = 4] = "REMOTE_INPUT_PORT";
        ImportComponentType[ImportComponentType["USER_DATASOURCES"] = 5] = "USER_DATASOURCES";
        ImportComponentType[ImportComponentType["TEMPLATE_CONNECTION_INFORMATION"] = 6] = "TEMPLATE_CONNECTION_INFORMATION";
    })(ImportComponentType = exports.ImportComponentType || (exports.ImportComponentType = {}));
    var ImportService = /** @class */ (function () {
        function ImportService() {
        }
        ImportService.prototype.guid = function () {
            function s4() {
                return Math.floor((1 + Math.random()) * 0x10000)
                    .toString(16)
                    .substring(1);
            }
            return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
                s4() + '-' + s4() + s4() + s4();
        };
        ImportService.prototype.importComponentTypes = function () {
            return Object.keys(ImportComponentType);
        };
        /**
         * return a new component option.
         * Defaults to not overwrite.
         * @param component
         * @return {{importComponent: *, overwriteSelectValue: string, overwrite: boolean, userAcknowledged: boolean, shouldImport: boolean, analyzed: boolean, continueIfExists: boolean, properties: Array}}
         */
        ImportService.prototype.newImportComponentOption = function (component) {
            var nameOfType = ImportComponentType[component];
            var option = { importComponent: nameOfType, overwrite: false, userAcknowledged: true, shouldImport: true, analyzed: false, continueIfExists: false, properties: [] };
            return option;
        };
        ImportService.prototype.newReusableTemplateImportOption = function () {
            return this.newImportComponentOption(3 /* REUSABLE_TEMPLATE */);
        };
        ImportService.prototype.newTemplateConnectionInfoImportOption = function () {
            return this.newImportComponentOption(6 /* TEMPLATE_CONNECTION_INFORMATION */);
        };
        ImportService.prototype.newTemplateDataImportOption = function () {
            return this.newImportComponentOption(1 /* TEMPLATE_DATA */);
        };
        ImportService.prototype.newFeedDataImportOption = function () {
            return this.newImportComponentOption(2 /* FEED_DATA */);
        };
        ImportService.prototype.newRemoteProcessGroupImportOption = function () {
            var option = this.newImportComponentOption(4 /* REMOTE_INPUT_PORT */);
            option.userAcknowledged = false;
            return option;
        };
        ImportService.prototype.newNiFiTemplateImportOption = function () {
            return this.newImportComponentOption(0 /* NIFI_TEMPLATE */);
        };
        ImportService.prototype.newUserDatasourcesImportOption = function () {
            return this.newImportComponentOption(5 /* USER_DATASOURCES */);
        };
        ImportService.prototype.newUploadKey = function () {
            return _.uniqueId("upload_") + new Date().getTime() + this.guid();
        };
        /**
         * Update properties when a user chooses to overwrite or not
         * @param importComponentOption
         */
        ImportService.prototype.onOverwriteSelectOptionChanged = function (importComponentOption) {
            importComponentOption.userAcknowledged = true;
            if (importComponentOption.overwriteSelectValue == "true") {
                importComponentOption.overwrite = true;
            }
            else if (importComponentOption.overwriteSelectValue == "false") {
                importComponentOption.overwrite = false;
                importComponentOption.continueIfExists = true;
            }
            else {
                importComponentOption.userAcknowledged = false;
            }
        };
        /**
         * return the map of options as an array ready for upload/import
         * @param importOptionsMap a map of {ImportType: importOption}
         * @returns {Array} the array of options to be imported
         */
        ImportService.prototype.getImportOptionsForUpload = function (importOptionsMap) {
            var importComponentOptions = [];
            Object.keys(importOptionsMap).forEach(function (key) {
                var option = importOptionsMap[key];
                //set defaults for options
                option.errorMessages = [];
                if (option.overwrite) {
                    option.userAcknowledged = true;
                    option.shouldImport = true;
                    option.continueIfExists = true;
                }
                //reset the errors
                option.errorMessages = [];
                importComponentOptions.push(option);
            });
            return importComponentOptions;
        };
        /**
         * Check if an importOption is a specific type
         * @param importOption the option to check
         * @param importComponentType the type of the option
         * @returns {boolean} true if match, false if not
         */
        ImportService.prototype.isImportOption = function (importOption, importComponentType) {
            var nameOfType = ImportComponentType[importComponentType];
            return importOption.importComponent == nameOfType;
        };
        return ImportService;
    }());
    exports.ImportService = ImportService;
    angular.module(moduleName).factory('ImportService', function () { return new ImportService(); });
});
//# sourceMappingURL=ImportService.js.map