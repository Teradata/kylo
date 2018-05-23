import {moduleName} from "../module-name";
import * as angular from "angular";

import * as _ from "underscore";
import {ImportComponentType} from "../../services/ImportService";
import {Import} from "../../services/ImportComponentOptionTypes";
import {Common} from "../../../common/CommonTypes";
import {OnInit} from "@angular/core";
import ImportComponentOption = Import.ImportComponentOption;
import RemoteProcessInputPort = Import.RemoteProcessInputPort;
import ImportTemplateResult = Import.ImportTemplateResult;
import InputPortListItem = Import.InputPortListItem;
import ImportService = Import.ImportService;
import Map = Common.Map;

export class ImportTemplateController implements ng.IController, OnInit {

    /**
     * the angular ng-form for validity checks
     * @type {{}}
     */
    importTemplateForm = {};

    /**
     * The file to upload
     * @type {null}
     */
    templateFile: any = null;
    /**
     * the name of the file to upload
     * @type {null}
     */
    fileName: string = null;

    /**
     * The type of upload (either 'zip', or 'xml')
     * @type {null}
     */
    uploadType: string = null;

    /**
     * flag if uploading
     * @type {boolean}
     */
    uploadInProgress: boolean = false;

    /**
     * Flag to indicate the upload produced validation errors
     * @type {boolean}
     */
    validationErrors: boolean = false;

    /**
     * unique key to track upload status
     * @type {null}
     */
    uploadKey: string = null;

    /**
     * Status of the current upload
     * @type {Array}
     */
    uploadStatusMessages: any = [];

    /**
     * handle on the $interval object to cancel later
     * @type {null}
     */
    uploadStatusCheck: angular.IPromise<any> = undefined;

    /**
     * Percent upload complete
     * @type {number}
     */
    uploadProgress: number = 0;

    /**
     * Flag to indicate additional properties exist and a header show be shown for template options
     */
    additionalInputNeeded: boolean = false;

    /**
     * All the importOptions that will be uploaded
     * @type {{}}
     */
    importComponentOptions: Map<ImportComponentOption> = {};

    /**
     * Registered Template import options
     */
    templateDataImportOption: ImportComponentOption;

    /**
     * NiFi template options
     */
    nifiTemplateImportOption: ImportComponentOption;

    /**
     * Reusable template options
     */
    reusableTemplateImportOption: ImportComponentOption;

    /**
     * Connection information options to connect out ports to other input ports
     */
    templateConnectionInfoImportOption: ImportComponentOption;

    /**
     * Option to indicate what ports should be pushed up and created as root level input ports for remote process groups
     */
    remoteProcessGroupImportOption: ImportComponentOption;


    /**
     * Flag to indicate the user needs to provide ports before uploading
     * @type {boolean}
     */
    remoteProcessGroupInputPortsNeeded: boolean = false


    /**
     * Array of the Remote Input Port options
     * @type {RemoteProcessInputPort[]}
     */
    remoteProcessGroupInputPortNames: RemoteProcessInputPort[] = [];

    /**
     * Flag to indicate we need to ask the user to wire and connect the reusable flow out ports to other input ports
     * @type {boolean}
     */
    reusableTemplateInputPortsNeeded: boolean = false;

    /**
     * Flag to indicate a connection is needed, but unable to find any
     * @type {boolean}
     */
    noReusableConnectionsFound: boolean = false;

    /**
     * A map of the port names to the port Object
     * used for the connections from the outputs to input ports
     * @type {{}}
     */
    connectionMap: Map<any> = {}

    /**
     * The available options in the list of possible inputPorts to connect to
     * @type {Array} of {label: port.name, value: port.name}
     */
    inputPortList: InputPortListItem[] = [];

    /**
     * The Resulting object regurend after a user uploads
     * @type {null}
     */
    importResult: ImportTemplateResult = null;
    /**
     * The resulting succsss/failure icon
     * @type {string}
     */
    importResultIcon: string = "check_circle";

    /**
     * The reuslting color of the icon
     * @type {string}
     */
    importResultIconColor: string = "#009933";

    /**
     * A mayp of any additional errors that should be displayed
     * @type {null}
     */
    errorMap: any = null;
    /**
     * The count of errors after an upload
     * @type {number}
     */
    errorCount: number = 0;

    /**
     * Flag to indicate if the Reorder list should be shown
     * @type {boolean}
     */
    showReorderList: boolean = false;

    /**
     * Is this an XML file upload
     * @type {boolean}
     */
    xmlType: boolean = false;

    /**
     * General message to be displayed after upload
     */
    message: string;

    /**
     * Flag to see if we should check and use remote input ports.
     * This will be disabled until all of the Remte Input port and Remote Process Groups have been completed.
     */
    remoteProcessGroupAware :boolean = false;

    static $inject = ["$scope", "$http", "$interval", "$timeout", "$mdDialog", "FileUpload", "RestUrlService", "ImportService", "RegisterTemplateService"];

    constructor(private $scope: angular.IScope, private $http: angular.IHttpService, private $interval: angular.IIntervalService, private $timeout: angular.ITimeoutService
        , private $mdDialog: angular.material.IDialogService, private FileUpload: any, private RestUrlService: any, private ImportService: ImportService
        , private RegisterTemplateService: any) {


        this.templateDataImportOption = this.ImportService.newTemplateDataImportOption();

        this.nifiTemplateImportOption = this.ImportService.newNiFiTemplateImportOption();

        this.reusableTemplateImportOption = this.ImportService.newReusableTemplateImportOption();

        this.templateConnectionInfoImportOption = this.ImportService.newTemplateConnectionInfoImportOption();

        this.remoteProcessGroupImportOption = this.ImportService.newRemoteProcessGroupImportOption();


    }

    /**
     * Initialize the controller and properties
     */
    ngOnInit() {

        this.indexImportOptions();
        this.setDefaultImportOptions();
        this.checkRemoteProcessGroupAware();
        /**
         * Watch when the file changes
         */
        var self = this;
        this.$scope.$watch(() => {
            return this.templateFile;
        }, (newVal: any, oldValue: any) => {
            if (newVal != null) {
                this.fileName = newVal.name;
                if (this.fileName.toLowerCase().endsWith(".xml")) {
                    this.uploadType = 'xml';
                }
                else {
                    this.uploadType = 'zip'
                }
            }
            else {
                this.fileName = null;
                this.uploadType = null;
            }
            //reset them if changed
            if (newVal != oldValue) {
                this.resetImportOptions();
            }

        });
    }

    /**
     * Called when a user changes a import option for overwriting
     */
    onOverwriteSelectOptionChanged = this.ImportService.onOverwriteSelectOptionChanged;


    /**
     * Called when a user uploads a template
     */
    importTemplate() {
        //reset some flags
        this.showReorderList = false;
        this.uploadInProgress = true;
        this.importResult = null;

        let file = this.templateFile;
        let uploadUrl = this.RestUrlService.ADMIN_IMPORT_TEMPLATE_URL;

        let successFn = (response: angular.IHttpResponse<ImportTemplateResult>) => {
            var responseData = response.data;
            this.xmlType = !responseData.zipFile;

            var processGroupName = (responseData.templateResults != undefined && responseData.templateResults.processGroupEntity != undefined) ? responseData.templateResults.processGroupEntity.name : ''

            /**
             * Count or errors after this upload
             * @type {number}
             */
            let count = 0;
            /**
             * Map of errors by type after this upload
             * @type {{FATAL: any[]; WARN: any[]}}
             */
            let errorMap: any = {"FATAL": [], "WARN": []};

            //reassign the options back from the response data
            let importComponentOptions = responseData.importOptions.importComponentOptions;
            //map the options back to the object map
            this.updateImportOptions(importComponentOptions);

            this.importResult = responseData;

            if (!responseData.valid || !responseData.success) {
                //Validation Error.  Additional Input is needed by the end user
                this.additionalInputNeeded = true;
                this.importResultIcon = "error";
                this.importResultIconColor = "#FF0000";
                this.message = "Unable to import the template";
                if (responseData.reusableFlowOutputPortConnectionsNeeded) {
                    this.importResultIcon = "warning";
                    this.importResultIconColor = "#FF9901";
                    this.noReusableConnectionsFound = false;
                    this.reusableTemplateInputPortsNeeded = true;
                    this.message = "Additional connection information needed";
                    //show the user the list and allow them to configure and save it.

                    //add button that will make these connections
                    this.RegisterTemplateService.fetchRegisteredReusableFeedInputPorts().then((inputPortsResponse: any) => {
                        //Update connectionMap and inputPortList
                        this.inputPortList = [];
                        if (inputPortsResponse.data) {
                            angular.forEach(inputPortsResponse.data, (port, i) => {
                                var disabled = angular.isUndefined(port.destinationProcessGroupName) || (angular.isDefined(port.destinationProcessGroupName) && port.destinationProcessGroupName != '' && port.destinationProcessGroupName == processGroupName);
                                this.inputPortList.push({label: port.name, value: port.name, description: port.destinationProcessGroupName, disabled: disabled});
                                this.connectionMap[port.name] = port;
                            });
                        }
                        if (this.inputPortList.length == 0) {
                            this.noReusableConnectionsFound = true;
                        }

                    });
                }
                if (responseData.remoteProcessGroupInputPortsNeeded) {
                    this.importResultIcon = "warning";
                    this.importResultIconColor = "#FF9901";
                    this.message = "Remote input port assignments needed";
                    this.remoteProcessGroupInputPortsNeeded = true;
                    //reset the value on the importResult that will be uploaded again
                    this.remoteProcessGroupInputPortNames = responseData.remoteProcessGroupInputPortNames;

                    var selected = _.filter(this.remoteProcessGroupInputPortNames, (inputPort: RemoteProcessInputPort) => {
                        return inputPort.selected;
                    })
                    this.importResult.remoteProcessGroupInputPortNames = selected;
                }


            }


                if (responseData.templateResults.errors) {
                    angular.forEach(responseData.templateResults.errors, (processor) => {
                        if (processor.validationErrors) {
                            angular.forEach(processor.validationErrors, (error: any) => {
                                var copy: any = {};
                                angular.extend(copy, error);
                                angular.extend(copy, processor);
                                copy.validationErrors = null;
                                errorMap[error.severity].push(copy);
                                count++;
                            });
                        }
                    });

                    this.errorMap = errorMap;
                    this.errorCount = count;
                }
                if(!this.additionalInputNeeded) {
                    if (count == 0) {
                        this.showReorderList = responseData.zipFile;
                        this.importResultIcon = "check_circle";
                        this.importResultIconColor = "#009933";
                        if (responseData.zipFile == true) {
                            this.message = "Successfully imported and registered the template " + responseData.templateName;
                        }
                        else {
                            this.message = "Successfully imported the template " + responseData.templateName + " into Nifi"
                        }
                        this.resetImportOptions();
                    }
                    else {
                        if (responseData.success) {
                            this.resetImportOptions();
                            this.showReorderList = responseData.zipFile;
                            this.message = "Successfully imported " + (responseData.zipFile == true ? "and registered " : "") + " the template " + responseData.templateName + " but some errors were found. Please review these errors";
                            this.importResultIcon = "warning";
                            this.importResultIconColor = "#FF9901";
                        }
                        else {
                            this.importResultIcon = "error";
                            this.importResultIconColor = "#FF0000";
                            this.message = "Unable to import " + (responseData.zipFile == true ? "and register " : "") + " the template " + responseData.templateName + ".  Errors were found.  You may need to fix the template or go to Nifi to fix the Controller Services and then try to import again.";
                        }
                    }
                }


            this.uploadInProgress = false;
            this.stopUploadStatus(1000);


        }
        let errorFn = (response: angular.IHttpResponse<any>) => {
            this.importResult = response.data;
            this.uploadInProgress = false;
            this.importResultIcon = "error";
            this.importResultIconColor = "#FF0000";
            var msg = response.data.message != undefined ? response.data.message : "Unable to import the template.";
            this.message = msg;

            this.stopUploadStatus(1000);
        }

        //build up the options from the Map and into the array for uploading
        var importComponentOptions = this.ImportService.getImportOptionsForUpload(this.importComponentOptions);

        //generate a new upload key for status tracking
        this.uploadKey = this.ImportService.newUploadKey();

        var params = {
            uploadKey: this.uploadKey,
            importComponents: angular.toJson(importComponentOptions)
        };

        this.additionalInputNeeded = false;
        this.startUploadStatus();

        this.FileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, params);
    }

    /**
     * Stop the upload and stop the progress indicator
     * @param {number} delay  wait this amount of millis before stopping
     */
    stopUploadStatus(delay: number) {

        let stopStatusCheck = () => {
            this.uploadProgress = 0;
            if (angular.isDefined(this.uploadStatusCheck)) {
                this.$interval.cancel(this.uploadStatusCheck);
                this.uploadStatusCheck = undefined;
            }
        }

        if (delay != null && delay != undefined) {
            this.$timeout(() => {
                stopStatusCheck();
            }, delay)
        }
        else {
            stopStatusCheck();
        }

    }

    /**
     * Start the upload
     */
    startUploadStatus() {
        this.stopUploadStatus(null);
        this.uploadStatusMessages = [];
        this.uploadStatusCheck = this.$interval(() => {
            //poll for status
            this.$http.get(this.RestUrlService.ADMIN_UPLOAD_STATUS_CHECK(this.uploadKey)).then((response: angular.IHttpResponse<any>) => {
                if (response && response.data && response.data != null) {
                    this.uploadStatusMessages = response.data.messages;
                    this.uploadProgress = response.data.percentComplete;
                }
            }, (err: any) => {
                //  self.uploadStatusMessages = [];
            });
        }, 500);
    }


    /**
     *
     * @param importOptionsArr array of importOptions
     */
    updateImportOptions(importOptionsArr: ImportComponentOption[]): void {
        var map = _.indexBy(importOptionsArr, 'importComponent');

        _.each(importOptionsArr, (option: any) => {
            if (option.userAcknowledged) {
                option.overwriteSelectValue = "" + option.overwrite;
            }

            if (option.importComponent == ImportComponentType.TEMPLATE_DATA) {
                this.templateDataImportOption = option;
            }
            else if (option.importComponent == ImportComponentType.REUSABLE_TEMPLATE) {
                this.reusableTemplateImportOption = option;
            }
            else if (option.importComponent == ImportComponentType.NIFI_TEMPLATE) {
                this.nifiTemplateImportOption = option;
            }
            else if (option.importComponent == ImportComponentType.REMOTE_INPUT_PORT) {
                this.remoteProcessGroupImportOption = option;
            }
            else if (option.importComponent == ImportComponentType.TEMPLATE_CONNECTION_INFORMATION) {
                this.templateConnectionInfoImportOption = option;
            }
            this.importComponentOptions[option.importComponent] = option;
        });
    }


    /**
     * Called when the user changes the output port connections
     * @param connection
     */
    onReusableTemplateConnectionChange(connection: any) {
        var port = this.connectionMap[connection.inputPortDisplayName];
        connection.reusableTemplateInputPortName = port.name;
        this.importTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", true);
    };

    /**
     * If a user adds connection information connecting templates together this will get called from the UI and will them import the template with the connection information
     */
    setReusableConnections() {
        //TEMPLATE_CONNECTION_INFORMATION
        //submit form again for upload
        let option = ImportComponentType.TEMPLATE_CONNECTION_INFORMATION
        this.importComponentOptions[ImportComponentType[option]].connectionInfo = this.importResult.reusableTemplateConnections;
        this.importTemplate();

    }


    /**
     * Called from the UI after a user has assigned some input ports to be 'remote aware input ports'
     */
    setRemoteProcessInputPorts(): void {
        let option = ImportComponentType.REMOTE_INPUT_PORT
        this.importComponentOptions[ImportComponentType[option]].remoteProcessGroupInputPorts = this.importResult.remoteProcessGroupInputPortNames;

        var inputPortMap = {};
        _.each(this.remoteProcessGroupInputPortNames, (port) => {
            port.selected = false;
            inputPortMap[port.inputPortName] = port;
        });

        _.each(this.importResult.remoteProcessGroupInputPortNames, (inputPort) => {
            inputPort.selected = true;
            //find the matching in the complete set and mark it as selected
            var matchingPort = inputPortMap[inputPort.inputPortName];
            if (angular.isDefined(matchingPort)) {
                matchingPort.selected = true;
            }
        });

        //warn if existing ports are not selected
        let portsToRemove: RemoteProcessInputPort[] = [];
        _.each(this.remoteProcessGroupInputPortNames, (port) => {
            if (port.existing && !port.selected) {
                portsToRemove.push(port);
            }
        });
        if (portsToRemove.length > 0) {
            //Warn and confirm before importing
            var names = _.map(portsToRemove, (port) => {
                return port.inputPortName
            }).join(",");

            var confirm = this.$mdDialog.confirm()
                .title('Warning You are about to delete template items.')
                .htmlContent('The following \'remote input ports\' exist, but are not selected to be imported:<br/><br/> <b>' + names
                    + '</b>. <br/><br/>Continuing will result in these remote input ports being \ndeleted from the parent NiFi canvas. <br/><br/>Are you sure you want to continue?<br/>')
                .ariaLabel('Removal of Input Ports detected')
                .ok('Please do it!')
                .cancel('Cancel and Review');

            this.$mdDialog.show(confirm).then(() => {
                let option = ImportComponentType.REMOTE_INPUT_PORT
                this.importComponentOptions[ImportComponentType[option]].userAcknowledged = true;
                this.importTemplate();
            }, () => {
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

                this.$mdDialog.show(confirm).then(() => {
                    let option = ImportComponentType.REMOTE_INPUT_PORT
                    this.importComponentOptions[ImportComponentType[option]].userAcknowledged = true;
                    this.importTemplate();
                }, () => {
                    //do nothing
                });
            }
            else {
                let option = ImportComponentType.REMOTE_INPUT_PORT
                this.importComponentOptions[ImportComponentType[option]].userAcknowledged = true;
                this.importTemplate();
            }
        }
    }


    cancelImport() {
        //reset and reneable import button
        this.resetImportOptions();
        this.uploadStatusMessages = [];
        this.importResult = null;
    }


    /**
     * Set the default values for the import options
     */
    setDefaultImportOptions() {
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
            this.reusableTemplateImportOption.shouldImport = false;
            this.reusableTemplateImportOption.userAcknowledged = true;
            this.remoteProcessGroupImportOption.shouldImport = false;
            this.remoteProcessGroupImportOption.userAcknowledged = false;
        }


    }

    /**
     * Determine if we are clustered and if so set the flag to show the 'remote input port' options
     */
   private checkRemoteProcessGroupAware(): void {
            this.$http.get(this.RestUrlService.REMOTE_PROCESS_GROUP_AWARE).then((response: angular.IHttpResponse<any>) => {
                    this.remoteProcessGroupAware = response.data.remoteProcessGroupAware;
            });
    }


    /**
     * Index the import options  in a map by their type
     */
    indexImportOptions() {
        var arr = [this.templateDataImportOption, this.nifiTemplateImportOption, this.reusableTemplateImportOption, this.templateConnectionInfoImportOption, this.remoteProcessGroupImportOption];
        this.importComponentOptions = _.indexBy(arr, 'importComponent');
    }

    /**
     * Reset the options back to their orig. state
     */
    resetImportOptions() {
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

    }


    /**
     * When the controller is ready, initialize
     */
    $onInit(): void {
        this.ngOnInit();
    }


}

angular.module(moduleName).controller('ImportTemplateController', ImportTemplateController);
