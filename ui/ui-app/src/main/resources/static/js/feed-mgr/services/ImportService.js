define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    var ImportComponentType;
    (function (ImportComponentType) {
        ImportComponentType["NIFI_TEMPLATE"] = "NIFI_TEMPLATE";
        ImportComponentType["TEMPLATE_DATA"] = "TEMPLATE_DATA";
        ImportComponentType["FEED_DATA"] = "FEED_DATA";
        ImportComponentType["REUSABLE_TEMPLATE"] = "REUSABLE_TEMPLATE";
        ImportComponentType["REMOTE_INPUT_PORT"] = "REMOTE_INPUT_PORT";
        ImportComponentType["USER_DATASOURCES"] = "USER_DATASOURCES";
        ImportComponentType["TEMPLATE_CONNECTION_INFORMATION"] = "TEMPLATE_CONNECTION_INFORMATION";
    })(ImportComponentType = exports.ImportComponentType || (exports.ImportComponentType = {}));
    var DefaultImportService = /** @class */ (function () {
        function DefaultImportService() {
        }
        DefaultImportService.prototype.guid = function () {
            function s4() {
                return Math.floor((1 + Math.random()) * 0x10000)
                    .toString(16)
                    .substring(1);
            }
            return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
                s4() + '-' + s4() + s4() + s4();
        };
        DefaultImportService.prototype.importComponentTypes = function () {
            return Object.keys(ImportComponentType);
        };
        /**
         * return a new component option.
         * Defaults to not overwrite.
         * @param component
         * @return {{importComponent: *, overwriteSelectValue: string, overwrite: boolean, userAcknowledged: boolean, shouldImport: boolean, analyzed: boolean, continueIfExists: boolean, properties: Array}}
         */
        DefaultImportService.prototype.newImportComponentOption = function (component) {
            var nameOfType = component;
            var option = { importComponent: nameOfType, overwrite: false, userAcknowledged: true, shouldImport: true, analyzed: false, continueIfExists: false, properties: [] };
            return option;
        };
        DefaultImportService.prototype.newReusableTemplateImportOption = function () {
            return this.newImportComponentOption("REUSABLE_TEMPLATE" /* REUSABLE_TEMPLATE */);
        };
        DefaultImportService.prototype.newTemplateConnectionInfoImportOption = function () {
            return this.newImportComponentOption("TEMPLATE_CONNECTION_INFORMATION" /* TEMPLATE_CONNECTION_INFORMATION */);
        };
        DefaultImportService.prototype.newTemplateDataImportOption = function () {
            return this.newImportComponentOption("TEMPLATE_DATA" /* TEMPLATE_DATA */);
        };
        DefaultImportService.prototype.newFeedDataImportOption = function () {
            return this.newImportComponentOption("FEED_DATA" /* FEED_DATA */);
        };
        DefaultImportService.prototype.newRemoteProcessGroupImportOption = function () {
            var option = this.newImportComponentOption("REMOTE_INPUT_PORT" /* REMOTE_INPUT_PORT */);
            option.shouldImport = false;
            option.userAcknowledged = false;
            return option;
        };
        DefaultImportService.prototype.newNiFiTemplateImportOption = function () {
            return this.newImportComponentOption("NIFI_TEMPLATE" /* NIFI_TEMPLATE */);
        };
        DefaultImportService.prototype.newUserDatasourcesImportOption = function () {
            return this.newImportComponentOption("USER_DATASOURCES" /* USER_DATASOURCES */);
        };
        DefaultImportService.prototype.newUploadKey = function () {
            return _.uniqueId("upload_") + new Date().getTime() + this.guid();
        };
        /**
         * Update properties when a user chooses to overwrite or not
         * @param importComponentOption
         */
        DefaultImportService.prototype.onOverwriteSelectOptionChanged = function (importComponentOption) {
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
        DefaultImportService.prototype.getImportOptionsForUpload = function (importOptionsMap) {
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
        DefaultImportService.prototype.isImportOption = function (importOption, importComponentType) {
            var nameOfType = importComponentType;
            return importOption.importComponent == nameOfType;
        };
        return DefaultImportService;
    }());
    exports.DefaultImportService = DefaultImportService;
    angular.module(moduleName).factory('ImportService', function () { return new DefaultImportService(); });
});
//# sourceMappingURL=ImportService.js.map