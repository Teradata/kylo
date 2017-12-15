define(['angular',"feed-mgr/templates/module-name"], function (angular,moduleName) {

    var controller = function ($scope, $http,$interval, $timeout,$mdDialog, FileUpload, RestUrlService, ImportService,RegisterTemplateService) {

        /**
         * reference to the controller
         * @type {controller}
         */
        var self = this;

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
         * Connection information options to connect out ports to other input ports
         */
        this.templateConnectionInfoImportOption = ImportService.newTemplateConnectionInfoImportOption();

        /**
         * Called when a user changes a import option for overwriting
         */
        this.onOverwriteSelectOptionChanged = ImportService.onOverwriteSelectOptionChanged;

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

        self.importResult = null;
        self.importResultIcon = "check_circle";
        self.importResultIconColor = "#009933";

        self.errorMap = null;
        self.errorCount = 0;

        self.showReorderList = false;

        self.xmlType = false;



        this.importTemplate = function () {
            self.showReorderList = false;
            self.uploadInProgress = true;
            self.importResult = null;
            var file = self.templateFile;
            var uploadUrl = RestUrlService.ADMIN_IMPORT_TEMPLATE_URL;
            var successFn = function (response) {
                var responseData = response.data;
                self.xmlFile = !responseData.zipFile;

                var processGroupName = (responseData.templateResults != undefined && responseData.templateResults.processGroupEntity != undefined) ? responseData.templateResults.processGroupEntity.name : ''

                var count = 0;
                var errorMap = {"FATAL": [], "WARN": []};

            /*
               if(responseData.importOptions.properties){
                    _.each(responseData.importOptions.properties,function(prop){
                        var inputName = prop.processorName.split(' ').join('_')+prop.propertyKey.split(' ').join('_');
                        prop.inputName = inputName.toLowerCase();
                    });
                }
                */


                //reassign the options back from the response data
                var importComponentOptions = responseData.importOptions.importComponentOptions;
                //map the options back to the object map
                updateImportOptions(importComponentOptions);

                if(!responseData.valid  || !responseData.success){
                    //Validation Error.  Additional Input is needed by the end user
                    self.additionalInputNeeded = true;
                    if(responseData.reusableFlowOutputPortConnectionsNeeded) {
                        self.importResult = responseData;
                        self.importResultIcon = "warning";
                        self.importResultIconColor = "#FF9901";
                        self.noReusableConnectionsFound = false;
                        self.reusableTemplateInputPortsNeeded = true;
                        self.message = "Additional connection information needed";
                        //show the user the list and allow them to configure and save it.

                        //add button that will make these connections
                        RegisterTemplateService.fetchRegisteredReusableFeedInputPorts().then(function (inputPortsResponse) {
                            //Update connectionMap and inputPortList
                            self.inputPortList = [];
                            if (inputPortsResponse.data) {
                                angular.forEach(inputPortsResponse.data, function (port, i) {
                                    var disabled = angular.isDefined(port.destinationProcessGroupName) && port.destinationProcessGroupName != '' && port.destinationProcessGroupName ==processGroupName
                                    self.inputPortList.push({label: port.name, value: port.name, description:port.destinationProcessGroupName, disabled:disabled});
                                    self.connectionMap[port.name] = port;
                                });
                            }
                            if(self.inputPortList.length ==0){
                                self.noReusableConnectionsFound = true;
                            }

                        });
                    }


                }
                else {

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

                    if (count == 0) {
                        self.showReorderList = responseData.zipFile;
                        self.importResultIcon = "check_circle";
                        self.importResultIconColor = "#009933";
                        if (responseData.zipFile == true) {
                            self.message = "Successfully imported and registered the template " + responseData.templateName;
                        }
                        else {
                            self.message = "Successfully imported the template " + responseData.templateName + " into Nifi"
                        }
                        resetImportOptions();
                    }
                    else {
                        if (responseData.success) {
                            resetImportOptions();
                            self.showReorderList = responseData.zipFile;
                            self.message = "Successfully imported " + (responseData.zipFile == true ? "and registered " : "") + " the template " + responseData.templateName + " but some errors were found. Please review these errors";
                            self.importResultIcon = "warning";
                            self.importResultIconColor = "#FF9901";
                        }
                        else {
                            self.importResultIcon = "error";
                            self.importResultIconColor = "#FF0000";
                            self.message = "Unable to import " + (responseData.zipFile == true ? "and register " : "") + " the template " + responseData.templateName + ".  Errors were found.  You may need to fix the template or go to Nifi to fix the Controller Services and then try to import again.";
                        }
                    }


                }
                self.uploadInProgress = false;
                stopUploadStatus(1000);


            }
            var errorFn = function (response) {
                self.importResult = response.data;
                self.uploadInProgress = false;
                self.importResultIcon = "error";
                self.importResultIconColor = "#FF0000";
                var msg = response.data.message != undefined ? response.data.message : "Unable to import the template.";
                self.message = msg;

                stopUploadStatus(1000);
            }

            //build up the options from the Map and into the array for uploading
            var importComponentOptions = ImportService.getImportOptionsForUpload(self.importComponentOptions);

            //generate a new upload key for status tracking
            self.uploadKey = ImportService.newUploadKey();

            var params = {
                uploadKey : self.uploadKey,
                importComponents:angular.toJson(importComponentOptions)
            };


            startUploadStatus();

            FileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, params);
        };

        /**
         * Watch when the file changs
         */
        $scope.$watch(function () {
            return self.templateFile;
        }, function (newVal, oldValue) {
            if (newVal != null) {
                self.fileName = newVal.name;
              if(self.fileName.toLowerCase().endsWith(".xml")){
                  self.uploadType = 'xml';
              }
              else {
                  self.uploadType = 'zip'
              }
            }
            else {
                self.fileName = null;
               self.uploadType = null;
            }
            //reset them if changed
            if(newVal != oldValue){
                resetImportOptions();
            }

        });

        /**
         * Called when the user changes the output port connections
         * @param connection
         */
        self.onReusableTemplateConnectionChange = function (connection) {
            var port = self.connectionMap[connection.inputPortDisplayName];
            connection.reusableTemplateInputPortName = port.name;
            self.importTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", true);
        };

        self.setReusableConnections = function(){
            //TEMPLATE_CONNECTION_INFORMATION
            //submit form again for upload
            self.importComponentOptions[ImportService.importComponentTypes.TEMPLATE_CONNECTION_INFORMATION].connectionInfo = self.importResult.reusableTemplateConnections;
            self.importTemplate();

        }


        self.cancelImport = function(){
            //reset and reneable import button
            resetImportOptions();
            self.uploadStatusMessages =[];
            self.importResult = null;
        }



        /**
         * Set the default values for the import options
         */
        function setDefaultImportOptions(){
           if(self.uploadType == 'zip') {
               //only if it is a zip do we continue with the niFi template
               self.templateDataImportOption.continueIfExists = false;
               self.reusableTemplateImportOption.shouldImport = true;
               self.reusableTemplateImportOption.userAcknowledged = true;
           }
           else {
               self.nifiTemplateImportOption.continueIfExists = false;
               self.reusableTemplateImportOption.shouldImport = true;
               self.reusableTemplateImportOption.userAcknowledged = true;
           }


        }

        /**
         * Initialize the Controller
         */
        function init() {
            indexImportOptions();
            setDefaultImportOptions();
        }

        function indexImportOptions(){
            var arr = [self.templateDataImportOption,self.nifiTemplateImportOption,self.reusableTemplateImportOption];
            self.importComponentOptions = _.indexBy(arr,'importComponent')
        }

        /**
         * Reset the options back to their orig. state
         */
        function resetImportOptions(){
            self.importComponentOptions = {};

            self.templateDataImportOption = ImportService.newTemplateDataImportOption();

            self.nifiTemplateImportOption = ImportService.newNiFiTemplateImportOption();

            self.reusableTemplateImportOption = ImportService.newReusableTemplateImportOption();

            self.templateConnectionInfoImportOption = ImportService.newTemplateConnectionInfoImportOption();

            indexImportOptions();
            setDefaultImportOptions();

            self.additionalInputNeeded = false;

            self.reusableTemplateInputPortsNeeded = false;
            self.inputPortList = [];
            self.connectionMap = {};

        }



        init();
        /**
         *
         * @param importOptionsArr array of importOptions
         */
        function updateImportOptions(importOptionsArr){
            var map = _.indexBy(importOptionsArr,'importComponent');
            _.each(importOptionsArr, function(option) {
                if(option.userAcknowledged){
                    option.overwriteSelectValue = ""+option.overwrite;
                }

                if(option.importComponent == ImportService.importComponentTypes.TEMPLATE_DATA){
                    self.templateDataImportOption= option;
                }
                else if(option.importComponent == ImportService.importComponentTypes.REUSABLE_TEMPLATE){
                    self.reusableTemplateImportOption= option;
                }
                else if(option.importComponent == ImportService.importComponentTypes.NIFI_TEMPLATE){
                    self.nifiTemplateImportOption= option;
                }
                else if(option.importComponent == ImportService.importComponentTypes.TEMPLATE_CONNECTION_INFORMATION){
                    self.templateConnectionInfoImportOption= option;
                }
                self.importComponentOptions[option.importComponent] = option;
            });
        }



        /**
         * Stop the upload status check,
         * @param delay wait xx millis before stopping (allows for the last status to be queried)
         */
        function stopUploadStatus(delay){

            function stopStatusCheck(){
                self.uploadProgress = 0;
                if (angular.isDefined(self.uploadStatusCheck)) {
                    $interval.cancel(self.uploadStatusCheck);
                    self.uploadStatusCheck = undefined;
                }
            }

            if(delay != undefined) {
                $timeout(function(){
                    stopStatusCheck();
                },delay)
            }
            else {
                stopStatusCheck();
            }

        }

        /**
         * starts the upload status check
         */
        function startUploadStatus(){
            stopUploadStatus();
            self.uploadStatusMessages =[];
            self.uploadStatusCheck = $interval(function() {
                //poll for status
                $http.get(RestUrlService.ADMIN_UPLOAD_STATUS_CHECK(self.uploadKey)).then(function(response) {
                    if(response && response.data && response.data != null) {
                        self.uploadStatusMessages = response.data.messages;
                        self.uploadProgress = response.data.percentComplete;
                    }
                }, function(err){
                    //  self.uploadStatusMessages = [];
                });
            },500);
        }



    };

    angular.module(moduleName).controller('ImportTemplateController', ["$scope","$http","$interval","$timeout","$mdDialog","FileUpload","RestUrlService","ImportService","RegisterTemplateService",controller]);


});

