define(["require", "exports", "../module-name", "angular", "underscore", "../../services/ImportService"], function (require, exports, module_name_1, angular, _, ImportService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ImportTemplateController = /** @class */ (function () {
        function ImportTemplateController($scope, $http, $interval, $timeout, $mdDialog, FileUpload, RestUrlService, ImportService, RegisterTemplateService) {
            this.$scope = $scope;
            this.$http = $http;
            this.$interval = $interval;
            this.$timeout = $timeout;
            this.$mdDialog = $mdDialog;
            this.FileUpload = FileUpload;
            this.RestUrlService = RestUrlService;
            this.ImportService = ImportService;
            this.RegisterTemplateService = RegisterTemplateService;
            /**
             * the angular ng-form for validity checks
             * @type {{}}
             */
            this.importTemplateForm = {};
            /**
             * The file to upload
             * @type {null}
             */
            this.templateFile = null;
            /**
             * the name of the file to upload
             * @type {null}
             */
            this.fileName = null;
            /**
             * The type of upload (either 'zip', or 'xml')
             * @type {null}
             */
            this.uploadType = null;
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
             * @type {null}
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
             * All the importOptions that will be uploaded
             * @type {{}}
             */
            this.importComponentOptions = {};
            /**
             * boolean to indicate if nifi is clustered.
             * If it is the remoteProcessGroupOption will be exposed when importing reusable templates
             * @type {boolean}
             */
            this.nifiClustered = false;
            /**
             * Flag to indicate the user needs to provide ports before uploading
             * @type {boolean}
             */
            this.remoteProcessGroupInputPortsNeeded = false;
            /**
             * Array of the Remote Input Port options
             * @type {RemoteProcessInputPort[]}
             */
            this.remoteProcessGroupInputPortNames = [];
            /**
             * Flag to indicate we need to ask the user to wire and connect the reusable flow out ports to other input ports
             * @type {boolean}
             */
            this.reusableTemplateInputPortsNeeded = false;
            /**
             * Flag to indicate a connection is needed, but unable to find any
             * @type {boolean}
             */
            this.noReusableConnectionsFound = false;
            /**
             * A map of the port names to the port Object
             * used for the connections from the outputs to input ports
             * @type {{}}
             */
            this.connectionMap = {};
            /**
             * The available options in the list of possible inputPorts to connect to
             * @type {Array} of {label: port.name, value: port.name}
             */
            this.inputPortList = [];
            /**
             * The Resulting object regurend after a user uploads
             * @type {null}
             */
            this.importResult = null;
            /**
             * The resulting succsss/failure icon
             * @type {string}
             */
            this.importResultIcon = "check_circle";
            /**
             * The reuslting color of the icon
             * @type {string}
             */
            this.importResultIconColor = "#009933";
            /**
             * A mayp of any additional errors that should be displayed
             * @type {null}
             */
            this.errorMap = null;
            /**
             * The count of errors after an upload
             * @type {number}
             */
            this.errorCount = 0;
            /**
             * Flag to indicate if the Reorder list should be shown
             * @type {boolean}
             */
            this.showReorderList = false;
            /**
             * Is this an XML file upload
             * @type {boolean}
             */
            this.xmlType = false;
            /**
             * Flag to see if we should check and use remote input ports.
             * This will be disabled until all of the Remte Input port and Remote Process Groups have been completed.
             */
            this.remoteInputPortsCheckEnabled = false;
            /**
             * Called when a user changes a import option for overwriting
             */
            this.onOverwriteSelectOptionChanged = this.ImportService.onOverwriteSelectOptionChanged;
            this.templateDataImportOption = this.ImportService.newTemplateDataImportOption();
            this.nifiTemplateImportOption = this.ImportService.newNiFiTemplateImportOption();
            this.reusableTemplateImportOption = this.ImportService.newReusableTemplateImportOption();
            this.templateConnectionInfoImportOption = this.ImportService.newTemplateConnectionInfoImportOption();
            this.remoteProcessGroupImportOption = this.ImportService.newRemoteProcessGroupImportOption();
        }
        /**
         * Initialize the controller and properties
         */
        ImportTemplateController.prototype.ngOnInit = function () {
            var _this = this;
            this.indexImportOptions();
            this.setDefaultImportOptions();
            this.setNiFiClustered();
            /**
             * Watch when the file changes
             */
            var self = this;
            this.$scope.$watch(function () {
                return _this.templateFile;
            }, function (newVal, oldValue) {
                if (newVal != null) {
                    _this.fileName = newVal.name;
                    if (_this.fileName.toLowerCase().endsWith(".xml")) {
                        _this.uploadType = 'xml';
                    }
                    else {
                        _this.uploadType = 'zip';
                    }
                }
                else {
                    _this.fileName = null;
                    _this.uploadType = null;
                }
                //reset them if changed
                if (newVal != oldValue) {
                    _this.resetImportOptions();
                }
            });
        };
        /**
         * Called when a user uploads a template
         */
        ImportTemplateController.prototype.importTemplate = function () {
            var _this = this;
            //reset some flags
            this.showReorderList = false;
            this.uploadInProgress = true;
            this.importResult = null;
            var file = this.templateFile;
            var uploadUrl = this.RestUrlService.ADMIN_IMPORT_TEMPLATE_URL;
            var successFn = function (response) {
                var responseData = response.data;
                _this.xmlType = !responseData.zipFile;
                var processGroupName = (responseData.templateResults != undefined && responseData.templateResults.processGroupEntity != undefined) ? responseData.templateResults.processGroupEntity.name : '';
                var count = 0;
                var errorMap = { "FATAL": [], "WARN": [] };
                //reassign the options back from the response data
                var importComponentOptions = responseData.importOptions.importComponentOptions;
                //map the options back to the object map
                _this.updateImportOptions(importComponentOptions);
                if (!responseData.valid || !responseData.success) {
                    //Validation Error.  Additional Input is needed by the end user
                    _this.additionalInputNeeded = true;
                    if (responseData.reusableFlowOutputPortConnectionsNeeded) {
                        _this.importResult = responseData;
                        _this.importResultIcon = "warning";
                        _this.importResultIconColor = "#FF9901";
                        _this.noReusableConnectionsFound = false;
                        _this.reusableTemplateInputPortsNeeded = true;
                        _this.message = "Additional connection information needed";
                        //show the user the list and allow them to configure and save it.
                        //add button that will make these connections
                        _this.RegisterTemplateService.fetchRegisteredReusableFeedInputPorts().then(function (inputPortsResponse) {
                            //Update connectionMap and inputPortList
                            _this.inputPortList = [];
                            if (inputPortsResponse.data) {
                                angular.forEach(inputPortsResponse.data, function (port, i) {
                                    var disabled = angular.isUndefined(port.destinationProcessGroupName) || (angular.isDefined(port.destinationProcessGroupName) && port.destinationProcessGroupName != '' && port.destinationProcessGroupName == processGroupName);
                                    _this.inputPortList.push({ label: port.name, value: port.name, description: port.destinationProcessGroupName, disabled: disabled });
                                    _this.connectionMap[port.name] = port;
                                });
                            }
                            if (_this.inputPortList.length == 0) {
                                _this.noReusableConnectionsFound = true;
                            }
                        });
                    }
                    if (responseData.remoteProcessGroupInputPortsNeeded) {
                        _this.importResult = responseData;
                        _this.importResultIcon = "warning";
                        _this.importResultIconColor = "#FF9901";
                        _this.message = "Remote input port assignments needed";
                        _this.remoteProcessGroupInputPortsNeeded = true;
                        //reset the value on the importResult that will be uploaded again
                        _this.remoteProcessGroupInputPortNames = responseData.remoteProcessGroupInputPortNames;
                        var selected = _.filter(_this.remoteProcessGroupInputPortNames, function (inputPort) {
                            return inputPort.selected;
                        });
                        _this.importResult.remoteProcessGroupInputPortNames = selected;
                    }
                }
                else {
                    _this.importResult = responseData;
                    if (responseData.templateResults.errors) {
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
                        _this.errorMap = errorMap;
                        _this.errorCount = count;
                    }
                    if (count == 0) {
                        _this.showReorderList = responseData.zipFile;
                        _this.importResultIcon = "check_circle";
                        _this.importResultIconColor = "#009933";
                        if (responseData.zipFile == true) {
                            _this.message = "Successfully imported and registered the template " + responseData.templateName;
                        }
                        else {
                            _this.message = "Successfully imported the template " + responseData.templateName + " into Nifi";
                        }
                        _this.resetImportOptions();
                    }
                    else {
                        if (responseData.success) {
                            _this.resetImportOptions();
                            _this.showReorderList = responseData.zipFile;
                            _this.message = "Successfully imported " + (responseData.zipFile == true ? "and registered " : "") + " the template " + responseData.templateName + " but some errors were found. Please review these errors";
                            _this.importResultIcon = "warning";
                            _this.importResultIconColor = "#FF9901";
                        }
                        else {
                            _this.importResultIcon = "error";
                            _this.importResultIconColor = "#FF0000";
                            _this.message = "Unable to import " + (responseData.zipFile == true ? "and register " : "") + " the template " + responseData.templateName + ".  Errors were found.  You may need to fix the template or go to Nifi to fix the Controller Services and then try to import again.";
                        }
                    }
                }
                _this.uploadInProgress = false;
                _this.stopUploadStatus(1000);
            };
            var errorFn = function (response) {
                _this.importResult = response.data;
                _this.uploadInProgress = false;
                _this.importResultIcon = "error";
                _this.importResultIconColor = "#FF0000";
                var msg = response.data.message != undefined ? response.data.message : "Unable to import the template.";
                _this.message = msg;
                _this.stopUploadStatus(1000);
            };
            //build up the options from the Map and into the array for uploading
            var importComponentOptions = this.ImportService.getImportOptionsForUpload(this.importComponentOptions);
            //generate a new upload key for status tracking
            this.uploadKey = this.ImportService.newUploadKey();
            var params = {
                uploadKey: this.uploadKey,
                importComponents: angular.toJson(importComponentOptions)
            };
            this.startUploadStatus();
            this.FileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, params);
        };
        /**
         * Stop the upload and stop the progress indicator
         * @param {number} delay  wait this amount of millis before stopping
         */
        ImportTemplateController.prototype.stopUploadStatus = function (delay) {
            var _this = this;
            var stopStatusCheck = function () {
                _this.uploadProgress = 0;
                if (angular.isDefined(_this.uploadStatusCheck)) {
                    _this.$interval.cancel(_this.uploadStatusCheck);
                    _this.uploadStatusCheck = undefined;
                }
            };
            if (delay != null && delay != undefined) {
                this.$timeout(function () {
                    stopStatusCheck();
                }, delay);
            }
            else {
                stopStatusCheck();
            }
        };
        /**
         * Start the upload
         */
        ImportTemplateController.prototype.startUploadStatus = function () {
            var _this = this;
            this.stopUploadStatus(null);
            this.uploadStatusMessages = [];
            this.uploadStatusCheck = this.$interval(function () {
                //poll for status
                _this.$http.get(_this.RestUrlService.ADMIN_UPLOAD_STATUS_CHECK(_this.uploadKey)).then(function (response) {
                    if (response && response.data && response.data != null) {
                        _this.uploadStatusMessages = response.data.messages;
                        _this.uploadProgress = response.data.percentComplete;
                    }
                }, function (err) {
                    //  self.uploadStatusMessages = [];
                });
            }, 500);
        };
        /**
         *
         * @param importOptionsArr array of importOptions
         */
        ImportTemplateController.prototype.updateImportOptions = function (importOptionsArr) {
            var _this = this;
            var map = _.indexBy(importOptionsArr, 'importComponent');
            _.each(importOptionsArr, function (option) {
                if (option.userAcknowledged) {
                    option.overwriteSelectValue = "" + option.overwrite;
                }
                if (option.importComponent == ImportService_1.ImportComponentType.TEMPLATE_DATA) {
                    _this.templateDataImportOption = option;
                }
                else if (option.importComponent == ImportService_1.ImportComponentType.REUSABLE_TEMPLATE) {
                    _this.reusableTemplateImportOption = option;
                }
                else if (option.importComponent == ImportService_1.ImportComponentType.NIFI_TEMPLATE) {
                    _this.nifiTemplateImportOption = option;
                }
                else if (option.importComponent == ImportService_1.ImportComponentType.REMOTE_INPUT_PORT) {
                    _this.remoteProcessGroupImportOption = option;
                }
                else if (option.importComponent == ImportService_1.ImportComponentType.TEMPLATE_CONNECTION_INFORMATION) {
                    _this.templateConnectionInfoImportOption = option;
                }
                _this.importComponentOptions[option.importComponent] = option;
            });
        };
        /**
         * Called when the user changes the output port connections
         * @param connection
         */
        ImportTemplateController.prototype.onReusableTemplateConnectionChange = function (connection) {
            var port = this.connectionMap[connection.inputPortDisplayName];
            connection.reusableTemplateInputPortName = port.name;
            this.importTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", true);
        };
        ;
        /**
         * If a user adds connection information connecting templates together this will get called from the UI and will them import the template with the connection information
         */
        ImportTemplateController.prototype.setReusableConnections = function () {
            //TEMPLATE_CONNECTION_INFORMATION
            //submit form again for upload
            var option = ImportService_1.ImportComponentType.TEMPLATE_CONNECTION_INFORMATION;
            this.importComponentOptions[ImportService_1.ImportComponentType[option]].connectionInfo = this.importResult.reusableTemplateConnections;
            this.importTemplate();
        };
        /**
         * Called from the UI after a user has assigned some input ports to be 'remote aware input ports'
         */
        ImportTemplateController.prototype.setRemoteProcessInputPorts = function () {
            var _this = this;
            var option = ImportService_1.ImportComponentType.REMOTE_INPUT_PORT;
            this.importComponentOptions[ImportService_1.ImportComponentType[option]].remoteProcessGroupInputPorts = this.importResult.remoteProcessGroupInputPortNames;
            var inputPortMap = {};
            _.each(this.remoteProcessGroupInputPortNames, function (port) {
                port.selected = false;
                inputPortMap[port.inputPortName] = port;
            });
            _.each(this.importResult.remoteProcessGroupInputPortNames, function (inputPort) {
                inputPort.selected = true;
                //find the matching in the complete set and mark it as selected
                var matchingPort = inputPortMap[inputPort.inputPortName];
                if (angular.isDefined(matchingPort)) {
                    matchingPort.selected = true;
                }
            });
            //warn if existing ports are not selected
            var portsToRemove = [];
            _.each(this.remoteProcessGroupInputPortNames, function (port) {
                if (port.existing && !port.selected) {
                    portsToRemove.push(port);
                }
            });
            if (portsToRemove.length > 0) {
                //Warn and confirm before importing
                var names = _.map(portsToRemove, function (port) {
                    return port.inputPortName;
                }).join(",");
                var confirm = this.$mdDialog.confirm()
                    .title('Warning You are about to delete template items.')
                    .htmlContent('The following \'remote input ports\' exist, but are not selected to be imported:<br/><br/> <b>' + names
                    + '</b>. <br/><br/>Continuing will result in these remote input ports being \ndeleted from the parent NiFi canvas. <br/><br/>Are you sure you want to continue?<br/>')
                    .ariaLabel('Removal of Input Ports detected')
                    .ok('Please do it!')
                    .cancel('Cancel and Review');
                this.$mdDialog.show(confirm).then(function () {
                    var option = ImportService_1.ImportComponentType.REMOTE_INPUT_PORT;
                    _this.importComponentOptions[ImportService_1.ImportComponentType[option]].userAcknowledged = true;
                    _this.importTemplate();
                }, function () {
                    //do nothing
                });
            }
            else {
                if (this.importResult.remoteProcessGroupInputPortNames.length == 0) {
                    var confirm = this.$mdDialog.confirm()
                        .title('No remote input ports selected')
                        .htmlContent('You have not selected any input ports to be exposed as \'remote input ports\'.<br/> Are you sure you want to continue?<br/>')
                        .ariaLabel('No Remote Input Ports Selected')
                        .ok('Please do it!')
                        .cancel('Cancel and Review');
                    this.$mdDialog.show(confirm).then(function () {
                        var option = ImportService_1.ImportComponentType.REMOTE_INPUT_PORT;
                        _this.importComponentOptions[ImportService_1.ImportComponentType[option]].userAcknowledged = true;
                        _this.importTemplate();
                    }, function () {
                        //do nothing
                    });
                }
                else {
                    var option_1 = ImportService_1.ImportComponentType.REMOTE_INPUT_PORT;
                    this.importComponentOptions[ImportService_1.ImportComponentType[option_1]].userAcknowledged = true;
                    this.importTemplate();
                }
            }
        };
        ImportTemplateController.prototype.cancelImport = function () {
            //reset and reneable import button
            this.resetImportOptions();
            this.uploadStatusMessages = [];
            this.importResult = null;
        };
        /**
         * Set the default values for the import options
         */
        ImportTemplateController.prototype.setDefaultImportOptions = function () {
            if (this.uploadType == 'zip') {
                //only if it is a zip do we continue with the niFi template
                this.templateDataImportOption.continueIfExists = false;
                this.reusableTemplateImportOption.shouldImport = true;
                this.reusableTemplateImportOption.userAcknowledged = true;
                //remote process group option
                this.remoteProcessGroupImportOption.shouldImport = true;
                this.remoteProcessGroupImportOption.userAcknowledged = true;
            }
            else {
                this.nifiTemplateImportOption.continueIfExists = false;
                this.reusableTemplateImportOption.shouldImport = true;
                this.reusableTemplateImportOption.userAcknowledged = true;
                this.remoteProcessGroupImportOption.shouldImport = true;
                this.remoteProcessGroupImportOption.userAcknowledged = false;
            }
        };
        /**
         * Determine if we are clustered and if so set the flag to show the 'remote input port' options
         */
        ImportTemplateController.prototype.setNiFiClustered = function () {
            var _this = this;
            this.nifiClustered = false;
            if (this.remoteInputPortsCheckEnabled) {
                this.$http.get(this.RestUrlService.NIFI_STATUS).then(function (response) {
                    if (response.data.clustered) {
                        _this.nifiClustered = true;
                    }
                });
            }
        };
        /**
         * Index the import options  in a map by their type
         */
        ImportTemplateController.prototype.indexImportOptions = function () {
            var arr = [this.templateDataImportOption, this.nifiTemplateImportOption, this.reusableTemplateImportOption, this.templateConnectionInfoImportOption, this.remoteProcessGroupImportOption];
            this.importComponentOptions = _.indexBy(arr, 'importComponent');
        };
        /**
         * Reset the options back to their orig. state
         */
        ImportTemplateController.prototype.resetImportOptions = function () {
            this.importComponentOptions = {};
            this.templateDataImportOption = this.ImportService.newTemplateDataImportOption();
            this.nifiTemplateImportOption = this.ImportService.newNiFiTemplateImportOption();
            this.reusableTemplateImportOption = this.ImportService.newReusableTemplateImportOption();
            this.templateConnectionInfoImportOption = this.ImportService.newTemplateConnectionInfoImportOption();
            this.remoteProcessGroupImportOption = this.ImportService.newRemoteProcessGroupImportOption();
            this.indexImportOptions();
            this.setDefaultImportOptions();
            this.additionalInputNeeded = false;
            this.reusableTemplateInputPortsNeeded = false;
            this.remoteProcessGroupInputPortsNeeded = false;
            this.inputPortList = [];
            this.connectionMap = {};
        };
        /**
         * When the controller is ready, initialize
         */
        ImportTemplateController.prototype.$onInit = function () {
            this.ngOnInit();
        };
        ImportTemplateController.$inject = ["$scope", "$http", "$interval", "$timeout", "$mdDialog", "FileUpload", "RestUrlService", "ImportService", "RegisterTemplateService"];
        return ImportTemplateController;
    }());
    exports.ImportTemplateController = ImportTemplateController;
    angular.module(module_name_1.moduleName).controller('ImportTemplateController', ImportTemplateController);
});
//# sourceMappingURL=ImportTemplateController.js.map