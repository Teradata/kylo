import * as angular from 'angular';
import * as _ from "underscore";
import {ImportComponentType} from "../../services/ImportService";
import {Import} from "../../services/ImportComponentOptionTypes";
import {Common} from "../../../common/CommonTypes";
import ImportComponentOption = Import.ImportComponentOption;
import ImportService = Import.ImportService;
import Map = Common.Map;

const moduleName = require('feed-mgr/feeds/define-feed/module-name');

export class ImportFeedController {

    /**
     * The file to upload
     * @type {null}
     */
    feedFile: any = null;
    /**
     * the name of the file to upload
     * @type {string}
     */
    fileName: string = null;

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
     * @type {string}
     */
    uploadKey: string = null;
    /**
     * Status of the current upload
     * @type {Array}
     */
    uploadStatusMessages: string[] = [];

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
     * flag to force the feed to be DISABLED upon importing
     * @type {boolean}
     */
    disableFeedUponImport: boolean = false;
    /**
     * All the importOptions that will be uploaded
     * @type {{}}
     */
    importComponentOptions: Map<ImportComponentOption> = {};
    /**
     * Feed ImportOptions
     */
    feedDataImportOption: ImportComponentOption;
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
     * User data sources options
     */
    userDatasourcesOption: ImportComponentOption;

    /**
     * Any required feed fields
     */
    feedUserFieldsImportOption: ImportComponentOption;

    /**
     * Any Required Category fields
     */
    categoryUserFieldsImportOption: ImportComponentOption;

    /**
     * The angular Form for validation
     * @type {{}}
     */
    importFeedForm: any = {};
    /**
     * List of available data sources.
     * @type {Array.<JdbcDatasource>}
     */
    availableDatasources: any = [];

    importResult: any;
    /**
     * The icon for the result
     */
    importResultIcon: string = "check_circle";

    /**
     * the color of the icon
     */
    importResultIconColor: string = "#009933";
    /**
     * Any additional errors
     */
    errorMap: any = null;
    /**
     * the number of errors after an import
     */
    errorCount: number = 0;


    /**
     * The category model for autocomplete
     * @type {{category: {}}}
     */
    categoryModel: any = {
        category: {}
    }

    /**
     * When the category auto complete changes
     */
    categorySearchText: string;

    /**
     * Message after upload
     */
    message: string;


    static $inject = ["$scope", "$http", "$interval", "$timeout", "$mdDialog", "FileUpload", "RestUrlService", "FeedCreationErrorService", "CategoriesService", "ImportService", "DatasourcesService", "$filter"];

    constructor(private $scope: any, private $http: angular.IHttpService, private $interval: angular.IIntervalService, private $timeout: angular.ITimeoutService
        , private $mdDialog: angular.material.IDialogService, private FileUpload: any, private RestUrlService: any
        , private FeedCreationErrorService: any, private categoriesService: any
        , private ImportService: ImportService, private DatasourcesService: any, private $filter: angular.IFilterService) {


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

    $onInit(): void {
        this.ngOnInit();
    }

    /**
     * Initialize the Controller
     */
    ngOnInit(): void {
        this.indexImportOptions();
        this.setDefaultImportOptions();

        this.$scope.$watch(() => {
            return this.feedFile;
        }, (newVal: any, oldValue: any) => {
            if (newVal != null) {
                this.fileName = newVal.name;
            }
            else {
                this.fileName = null;
            }
            //reset them if changed
            if (newVal != oldValue) {
                this.resetImportOptions();
            }
        })

        // Get the list of data sources
        this.DatasourcesService.findAll()
            .then((datasources: any) => {
                this.availableDatasources = datasources;
            });
    }


    /**
     * Called when a user changes a import option for overwriting
     */
    onOverwriteSelectOptionChanged = this.ImportService.onOverwriteSelectOptionChanged;


    importFeed(): void {
        //reset flags
        this.uploadInProgress = true;
        this.importResult = null;

        var file = this.feedFile;
        var uploadUrl = this.RestUrlService.ADMIN_IMPORT_FEED_URL;

        var successFn = (response: any) => {
            var responseData = response.data;
            //reassign the options back from the response data
            var importComponentOptions = responseData.importOptions.importComponentOptions;
            //map the options back to the object map
            this.updateImportOptions(importComponentOptions);

            if (!responseData.valid) {
                //Validation Error.  Additional Input is needed by the end user
                this.additionalInputNeeded = true;
            }
            else {
                var count = 0;
                var errorMap: any = {"FATAL": [], "WARN": []};
                this.importResult = responseData;
                //if(responseData.templateResults.errors) {
                if (responseData.template.controllerServiceErrors) {
                    //angular.forEach(responseData.templateResults.errors, function (processor) {
                    angular.forEach(responseData.template.controllerServiceErrors, (processor: any) => {
                        if (processor.validationErrors) {
                            angular.forEach(processor.validationErrors, function (error: any) {
                                let copy: any = {};
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
                    this.errorMap["FATAL"].push({message: "Unable to import feed"});
                }
                if (responseData.nifiFeed && responseData.nifiFeed) {
                    count += this.FeedCreationErrorService.parseNifiFeedErrors(responseData.nifiFeed, errorMap);
                }
                this.errorMap = errorMap;
                this.errorCount = count;

                var feedName = responseData.feedName != null ? responseData.feedName : "";

                if (count == 0) {
                    this.importResultIcon = "check_circle";
                    this.importResultIconColor = "#009933";

                    this.message = this.$filter('translate')('views.ImportFeedController.succes') + feedName + ".";

                    this.resetImportOptions();

                }
                else {
                    if (responseData.success) {
                        this.resetImportOptions();

                        this.message = "Successfully imported and registered the feed " + feedName + " but some errors were found. Please review these errors";
                        this.importResultIcon = "warning";
                        this.importResultIconColor = "#FF9901";
                    }
                    else {
                        this.importResultIcon = "error";
                        this.importResultIconColor = "#FF0000";
                        this.message = this.$filter('translate')('views.ImportFeedController.error2');
                    }
                }
            }

            this.uploadInProgress = false;
            this.stopUploadStatus(1000);
        }
        var errorFn = (response: any) => {
            var data = response.data
            //reset the flags
            this.importResult = {};
            this.uploadInProgress = false;

            //set error indicators and messages
            this.importResultIcon = "error";
            this.importResultIconColor = "#FF0000";
            var msg = this.$filter('translate')('views.ImportFeedController.error');
            if (data.developerMessage) {
                msg += data.developerMessage;
            }
            this.message = msg;
            this.stopUploadStatus(1000);
        }


        if (angular.isDefined(this.categorySearchText) && this.categorySearchText != null && this.categorySearchText != "" && this.categoryModel.category.systemName == null) {
            //error the category has text in it, but not selected
            //attempt to get it
            let category = this.categoriesService.findCategoryByName(this.categorySearchText);
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

    }

    categorySearchTextChanged(text: any): void {
    }

    categorySelectedItemChange(item: any) {
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
    }


    /**
     * Set the default values for the import options
     */
    private setDefaultImportOptions(): void {
        this.nifiTemplateImportOption.continueIfExists = true;
        //  this.reusableTemplateImportOption.userAcknowledged = false;
        //it is assumed with a feed you will be importing the template with the feed
        this.templateDataImportOption.userAcknowledged = true;
        this.feedDataImportOption.continueIfExists = false;
    }

    private indexImportOptions(): void {
        var arr = [this.feedDataImportOption, this.templateDataImportOption, this.nifiTemplateImportOption, this.reusableTemplateImportOption, this.categoryUserFieldsImportOption, this.feedUserFieldsImportOption];
        this.importComponentOptions = _.indexBy(arr, 'importComponent')
    }


    /**
     * Reset the options back to their orig. state
     */
    private resetImportOptions(): void {
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

    }

    /**
     *
     * @param importOptionsArr array of importOptions
     */
    private updateImportOptions(importOptionsArr: ImportComponentOption[]): void {
        var map = _.indexBy(importOptionsArr, 'importComponent');
        _.each(importOptionsArr, (option: any) => {
            if (option.userAcknowledged) {
                option.overwriteSelectValue = "" + option.overwrite;
            }

            if (option.importComponent == ImportComponentType.FEED_DATA) {
                this.feedDataImportOption = option;
            } else if (option.importComponent == ImportComponentType.TEMPLATE_DATA) {
                this.templateDataImportOption = option;
            } else if (option.importComponent == ImportComponentType.REUSABLE_TEMPLATE) {
                this.reusableTemplateImportOption = option;
            } else if (option.importComponent == ImportComponentType.NIFI_TEMPLATE) {
                this.nifiTemplateImportOption = option;
            } else if (option.importComponent === ImportComponentType.USER_DATASOURCES) {
                this.userDatasourcesOption = option;
            } else if (option.importComponent === ImportComponentType.FEED_CATEGORY_USER_FIELDS) {
                this.categoryUserFieldsImportOption = option;
            } else if (option.importComponent === ImportComponentType.FEED_USER_FIELDS) {
                this.feedUserFieldsImportOption = option;
            }
            this.importComponentOptions[option.importComponent] = option;
        });
    }

    /**
     * Stop the upload status check,
     * @param delay wait xx millis before stopping (allows for the last status to be queried)
     */
    private stopUploadStatus(delay: any): void {

        let stopStatusCheck = () => {
            this.uploadProgress = 0;
            if (angular.isDefined(this.uploadStatusCheck)) {
                this.$interval.cancel(this.uploadStatusCheck);
                this.uploadStatusCheck = undefined;
            }
        }

        if (delay != undefined) {
            this.$timeout(() => {
                stopStatusCheck();
            }, delay)
        }
        else {
            stopStatusCheck();
        }

    }

    /**
     * starts the upload status check
     */
    private startUploadStatus(): void {
        this.stopUploadStatus(undefined);
        this.uploadStatusMessages = [];
        this.uploadStatusCheck = this.$interval(() => {
            //poll for status
            this.$http.get(this.RestUrlService.ADMIN_UPLOAD_STATUS_CHECK(this.uploadKey)).then((response: angular.IHttpResponse<any>) => {
                if (response && response.data && response.data != null) {
                    this.uploadStatusMessages = response.data.messages;
                    this.uploadProgress = response.data.percentComplete;
                }
            }, (err: any) => {
                //  this.uploadStatusMessages = [];
            });
        }, 500);
    }


}


angular.module(moduleName).controller('ImportFeedController', ImportFeedController);

