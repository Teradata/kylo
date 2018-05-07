define(["require", "exports", "angular", "underscore", "../../services/ImportService"], function (require, exports, angular, _, ImportService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/define-feed/module-name');
    var ImportFeedController = /** @class */ (function () {
        function ImportFeedController($scope, $http, $interval, $timeout, $mdDialog, FileUpload, RestUrlService, FeedCreationErrorService, categoriesService, ImportService, DatasourcesService, $filter) {
            this.$scope = $scope;
            this.$http = $http;
            this.$interval = $interval;
            this.$timeout = $timeout;
            this.$mdDialog = $mdDialog;
            this.FileUpload = FileUpload;
            this.RestUrlService = RestUrlService;
            this.FeedCreationErrorService = FeedCreationErrorService;
            this.categoriesService = categoriesService;
            this.ImportService = ImportService;
            this.DatasourcesService = DatasourcesService;
            this.$filter = $filter;
            /**
             * The file to upload
             * @type {null}
             */
            this.feedFile = null;
            /**
             * the name of the file to upload
             * @type {string}
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
            /**
             * unique key to track upload status
             * @type {string}
             */
            this.uploadKey = null;
            /**
             * Status of the current upload
             * @type {Array}
             */
            this.uploadStatusMessages = [];
            /**
             * handle on the $interval object to cancel later
             * @type {null}
             */
            this.uploadStatusCheck = undefined;
            /**
             * Percent upload complete
             * @type {number}
             */
            this.uploadProgress = 0;
            /**
             * Flag to indicate additional properties exist and a header show be shown for template options
             */
            this.additionalInputNeeded = false;
            /**
             * flag to force the feed to be DISABLED upon importing
             * @type {boolean}
             */
            this.disableFeedUponImport = false;
            /**
             * All the importOptions that will be uploaded
             * @type {{}}
             */
            this.importComponentOptions = {};
            /**
             * The angular Form for validation
             * @type {{}}
             */
            this.importFeedForm = {};
            /**
             * List of available data sources.
             * @type {Array.<JdbcDatasource>}
             */
            this.availableDatasources = [];
            /**
             * The icon for the result
             */
            this.importResultIcon = "check_circle";
            /**
             * the color of the icon
             */
            this.importResultIconColor = "#009933";
            /**
             * Any additional errors
             */
            this.errorMap = null;
            /**
             * the number of errors after an import
             */
            this.errorCount = 0;
            /**
             * The category model for autocomplete
             * @type {{category: {}}}
             */
            this.categoryModel = {
                category: {}
            };
            /**
             * Called when a user changes a import option for overwriting
             */
            this.onOverwriteSelectOptionChanged = this.ImportService.onOverwriteSelectOptionChanged;
            /**
             * Feed ImportOptions
             */
            this.feedDataImportOption = ImportService.newFeedDataImportOption();
            /**
             * Registered Template import options
             */
            this.templateDataImportOption = ImportService.newTemplateDataImportOption();
            /**
             * NiFi template options
             */
            this.nifiTemplateImportOption = ImportService.newNiFiTemplateImportOption();
            /**
             * Reusable template options
             */
            this.reusableTemplateImportOption = ImportService.newReusableTemplateImportOption();
            /**
             * User data sources options
             */
            this.userDatasourcesOption = ImportService.newUserDatasourcesImportOption();
            this.feedUserFieldsImportOption = ImportService.newFeedUserFieldsImportOption();
            this.categoryUserFieldsImportOption = ImportService.newFeedCategoryUserFieldsImportOption();
        }
        ImportFeedController.prototype.$onInit = function () {
            this.ngOnInit();
        };
        /**
         * Initialize the Controller
         */
        ImportFeedController.prototype.ngOnInit = function () {
            var _this = this;
            this.indexImportOptions();
            this.setDefaultImportOptions();
            this.$scope.$watch(function () {
                return _this.feedFile;
            }, function (newVal, oldValue) {
                if (newVal != null) {
                    _this.fileName = newVal.name;
                }
                else {
                    _this.fileName = null;
                }
                //reset them if changed
                if (newVal != oldValue) {
                    _this.resetImportOptions();
                }
            });
            // Get the list of data sources
            this.DatasourcesService.findAll()
                .then(function (datasources) {
                _this.availableDatasources = datasources;
            });
        };
        ImportFeedController.prototype.importFeed = function () {
            var _this = this;
            //reset flags
            this.uploadInProgress = true;
            this.importResult = null;
            var file = this.feedFile;
            var uploadUrl = this.RestUrlService.ADMIN_IMPORT_FEED_URL;
            var successFn = function (response) {
                var responseData = response.data;
                //reassign the options back from the response data
                var importComponentOptions = responseData.importOptions.importComponentOptions;
                //map the options back to the object map
                _this.updateImportOptions(importComponentOptions);
                if (!responseData.valid) {
                    //Validation Error.  Additional Input is needed by the end user
                    _this.additionalInputNeeded = true;
                }
                else {
                    var count = 0;
                    var errorMap = { "FATAL": [], "WARN": [] };
                    _this.importResult = responseData;
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
                                    this.errorMap[error.severity].push(copy);
                                    count++;
                                });
                            }
                        });
                    }
                    if (responseData.nifiFeed == null || responseData.nifiFeed == undefined) {
                        _this.errorMap["FATAL"].push({ message: "Unable to import feed" });
                    }
                    if (responseData.nifiFeed && responseData.nifiFeed) {
                        count += _this.FeedCreationErrorService.parseNifiFeedErrors(responseData.nifiFeed, errorMap);
                    }
                    _this.errorMap = errorMap;
                    _this.errorCount = count;
                    var feedName = responseData.feedName != null ? responseData.feedName : "";
                    if (count == 0) {
                        _this.importResultIcon = "check_circle";
                        _this.importResultIconColor = "#009933";
                        _this.message = _this.$filter('translate')('views.ImportFeedController.succes') + feedName + ".";
                        _this.resetImportOptions();
                    }
                    else {
                        if (responseData.success) {
                            _this.resetImportOptions();
                            _this.message = "Successfully imported and registered the feed " + feedName + " but some errors were found. Please review these errors";
                            _this.importResultIcon = "warning";
                            _this.importResultIconColor = "#FF9901";
                        }
                        else {
                            _this.importResultIcon = "error";
                            _this.importResultIconColor = "#FF0000";
                            _this.message = _this.$filter('translate')('views.ImportFeedController.error2');
                        }
                    }
                }
                _this.uploadInProgress = false;
                _this.stopUploadStatus(1000);
            };
            var errorFn = function (response) {
                var data = response.data;
                //reset the flags
                _this.importResult = {};
                _this.uploadInProgress = false;
                //set error indicators and messages
                _this.importResultIcon = "error";
                _this.importResultIconColor = "#FF0000";
                var msg = _this.$filter('translate')('views.ImportFeedController.error');
                if (data.developerMessage) {
                    msg += data.developerMessage;
                }
                _this.message = msg;
                _this.stopUploadStatus(1000);
            };
            if (angular.isDefined(this.categorySearchText) && this.categorySearchText != null && this.categorySearchText != "" && this.categoryModel.category.systemName == null) {
                //error the category has text in it, but not selected
                //attempt to get it
                var category = this.categoriesService.findCategoryByName(this.categorySearchText);
                if (category != null) {
                    this.categoryModel.category = category;
                }
            }
            //build up the options from the Map and into the array for uploading
            var importComponentOptions = this.ImportService.getImportOptionsForUpload(this.importComponentOptions);
            //generate a new upload key for status tracking
            this.uploadKey = this.ImportService.newUploadKey();
            var params = {
                uploadKey: this.uploadKey,
                categorySystemName: angular.isDefined(this.categoryModel.category.systemName) && this.categoryModel.category.systemName != null ? this.categoryModel.category.systemName : "",
                disableFeedUponImport: this.disableFeedUponImport,
                importComponents: angular.toJson(importComponentOptions)
            };
            this.startUploadStatus();
            this.FileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, params);
        };
        ImportFeedController.prototype.categorySearchTextChanged = function (text) {
        };
        ImportFeedController.prototype.categorySelectedItemChange = function (item) {
            if (item != null && item != undefined) {
                this.categoryModel.category.name = item.name;
                this.categoryModel.category.id = item.id;
                this.categoryModel.category.systemName = item.systemName;
            }
            else {
                this.categoryModel.category.name = null;
                this.categoryModel.category.id = null;
                this.categoryModel.category.systemName = null;
            }
        };
        /**
         * Set the default values for the import options
         */
        ImportFeedController.prototype.setDefaultImportOptions = function () {
            this.nifiTemplateImportOption.continueIfExists = true;
            //  this.reusableTemplateImportOption.userAcknowledged = false;
            //it is assumed with a feed you will be importing the template with the feed
            this.templateDataImportOption.userAcknowledged = true;
            this.feedDataImportOption.continueIfExists = false;
        };
        ImportFeedController.prototype.indexImportOptions = function () {
            var arr = [this.feedDataImportOption, this.templateDataImportOption, this.nifiTemplateImportOption, this.reusableTemplateImportOption, this.categoryUserFieldsImportOption, this.feedUserFieldsImportOption];
            this.importComponentOptions = _.indexBy(arr, 'importComponent');
        };
        /**
         * Reset the options back to their orig. state
         */
        ImportFeedController.prototype.resetImportOptions = function () {
            this.importComponentOptions = {};
            this.feedDataImportOption = this.ImportService.newFeedDataImportOption();
            this.templateDataImportOption = this.ImportService.newTemplateDataImportOption();
            this.nifiTemplateImportOption = this.ImportService.newNiFiTemplateImportOption();
            this.reusableTemplateImportOption = this.ImportService.newReusableTemplateImportOption();
            this.userDatasourcesOption = this.ImportService.newUserDatasourcesImportOption();
            this.feedUserFieldsImportOption = this.ImportService.newFeedUserFieldsImportOption();
            this.categoryUserFieldsImportOption = this.ImportService.newFeedCategoryUserFieldsImportOption();
            this.indexImportOptions();
            this.setDefaultImportOptions();
            this.additionalInputNeeded = false;
            this.disableFeedUponImport = false;
        };
        /**
         *
         * @param importOptionsArr array of importOptions
         */
        ImportFeedController.prototype.updateImportOptions = function (importOptionsArr) {
            var _this = this;
            var map = _.indexBy(importOptionsArr, 'importComponent');
            _.each(importOptionsArr, function (option) {
                if (option.userAcknowledged) {
                    option.overwriteSelectValue = "" + option.overwrite;
                }
                if (option.importComponent == ImportService_1.ImportComponentType.FEED_DATA) {
                    _this.feedDataImportOption = option;
                }
                else if (option.importComponent == ImportService_1.ImportComponentType.TEMPLATE_DATA) {
                    _this.templateDataImportOption = option;
                }
                else if (option.importComponent == ImportService_1.ImportComponentType.REUSABLE_TEMPLATE) {
                    _this.reusableTemplateImportOption = option;
                }
                else if (option.importComponent == ImportService_1.ImportComponentType.NIFI_TEMPLATE) {
                    _this.nifiTemplateImportOption = option;
                }
                else if (option.importComponent === ImportService_1.ImportComponentType.USER_DATASOURCES) {
                    _this.userDatasourcesOption = option;
                }
                else if (option.importComponent === ImportService_1.ImportComponentType.FEED_CATEGORY_USER_FIELDS) {
                    _this.categoryUserFieldsImportOption = option;
                }
                else if (option.importComponent === ImportService_1.ImportComponentType.FEED_USER_FIELDS) {
                    _this.feedUserFieldsImportOption = option;
                }
                _this.importComponentOptions[option.importComponent] = option;
            });
        };
        /**
         * Stop the upload status check,
         * @param delay wait xx millis before stopping (allows for the last status to be queried)
         */
        ImportFeedController.prototype.stopUploadStatus = function (delay) {
            var _this = this;
            var stopStatusCheck = function () {
                _this.uploadProgress = 0;
                if (angular.isDefined(_this.uploadStatusCheck)) {
                    _this.$interval.cancel(_this.uploadStatusCheck);
                    _this.uploadStatusCheck = undefined;
                }
            };
            if (delay != undefined) {
                this.$timeout(function () {
                    stopStatusCheck();
                }, delay);
            }
            else {
                stopStatusCheck();
            }
        };
        /**
         * starts the upload status check
         */
        ImportFeedController.prototype.startUploadStatus = function () {
            var _this = this;
            this.stopUploadStatus(undefined);
            this.uploadStatusMessages = [];
            this.uploadStatusCheck = this.$interval(function () {
                //poll for status
                _this.$http.get(_this.RestUrlService.ADMIN_UPLOAD_STATUS_CHECK(_this.uploadKey)).then(function (response) {
                    if (response && response.data && response.data != null) {
                        _this.uploadStatusMessages = response.data.messages;
                        _this.uploadProgress = response.data.percentComplete;
                    }
                }, function (err) {
                    //  this.uploadStatusMessages = [];
                });
            }, 500);
        };
        ImportFeedController.$inject = ["$scope", "$http", "$interval", "$timeout", "$mdDialog", "FileUpload", "RestUrlService", "FeedCreationErrorService", "CategoriesService", "ImportService", "DatasourcesService", "$filter"];
        return ImportFeedController;
    }());
    exports.ImportFeedController = ImportFeedController;
    angular.module(moduleName).controller('ImportFeedController', ImportFeedController);
});
//# sourceMappingURL=ImportFeedController.js.map