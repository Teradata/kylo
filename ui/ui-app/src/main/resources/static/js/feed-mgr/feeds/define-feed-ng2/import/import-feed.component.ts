import {Component, Inject, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {ImportComponentOption, ImportComponentType, ImportProperty, ImportService} from "../../../services/ImportComponentOptionTypes";
import {FileUpload} from "../../../../services/FileUploadService";
import {RestUrlConstants} from "../../../services/RestUrlConstants";
import {HttpClient} from "@angular/common/http";
import {KyloIcons} from "../../../../kylo-utils/kylo-icons";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import * as _ from "underscore"
import {MatSnackBar} from "@angular/material/snack-bar";
import {FeedNifiErrorUtil} from "../../../services/feed-nifi-error-util";
import {CategoryAutocompleteComponent} from "../shared/category-autocomplete.component";
import {DatasetPreviewStepperSavedEvent} from "../../../catalog-dataset-preview/preview-stepper/dataset-preview-stepper.component";
import {DatasetPreviewStepperDialogComponent, DatasetPreviewStepperDialogData} from "../../../catalog-dataset-preview/preview-stepper/dataset-preview-stepper-dialog.component";
import {MatDialogConfig} from "@angular/material/dialog";
import {TdDialogService} from "@covalent/core/dialogs";
import {CatalogService} from "../../../catalog/api/services/catalog.service";
import {SparkDataSet} from "../../../model/spark-data-set.model";
import {Category} from "../../../model/category/category.model";
import {DatasourcesService} from "../../../services/DatasourcesService";
import {KyloRouterService} from "../../../../services/kylo-router.service";
import {StringUtils} from "../../../../common/utils/StringUtils";
import {DataSource} from "../../../catalog/api/models/datasource";

@Component({
    selector: "import-feed",
    templateUrl: "./import-feed.component.html"
})
export class ImportFeedComponent  implements OnInit, OnDestroy{


    /**
     * The file to upload
     * @type {null}
     */
    feedFile: File = null;
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
    uploadStatusCheck: number = undefined;

    /**
     * Percent upload complete
     * @type {number}
     */
    uploadProgress: number = 0;


    errorMessage:string;

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
    importComponentOptions:{[key:string] :ImportComponentOption} = {};
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
     * Feed ImportOptions
     */
    userDataSetsOption: ImportComponentOption;


    /**
     * Any required feed fields
     */
    feedUserFieldsImportOption: ImportComponentOption;

    /**
     * Any Required Category fields
     */
    categoryUserFieldsImportOption: ImportComponentOption;

    /**
     * List of available data sources.
     * @type {Array.<JdbcDatasource>}
     */
    availableDatasources: any = [];

    catalogDataSources:DataSource[];

    /**
     * The parent form group for validation checks prior to import
     */
    formGroup:FormGroup;


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
     * Message after upload
     */
    message: string;

    public kyloIcons_Feed_importFeed = KyloIcons.Feed.importFeed;

    @ViewChild("categoryAutoComplete")
    categoryAutoComplete:CategoryAutocompleteComponent

    category:Category;

    /**
     * The number of required properties needed to complete the upload
     */
    requiredPropertyCount:number = 0;

    feedOverwriteFormControl:FormControl;
    disableUponImportFormControl:FormControl;
    templateOverwriteFormControl:FormControl;
    reusableTemplateOverwriteFormControl:FormControl;



    constructor(private http:HttpClient,private snackBar:MatSnackBar,private _dialogService:TdDialogService, private catalogService:CatalogService,
                @Inject("ImportService") private importService:ImportService,
                @Inject("FileUpload")private fileUploadService:FileUpload,
                @Inject("DatasourcesService") private datasourcesService: DatasourcesService,
                private kyloRouterService:KyloRouterService) {

        this.formGroup = new FormGroup({});
        this.feedOverwriteFormControl = new FormControl(false);
        this.disableUponImportFormControl = new FormControl(false);
        this.templateOverwriteFormControl = new FormControl(false);
        this.reusableTemplateOverwriteFormControl = new FormControl(false);

        this.formGroup.addControl("feedOverwrite",this.feedOverwriteFormControl)
        this.formGroup.addControl("disableUponImport",this.disableUponImportFormControl)
        this.formGroup.addControl("templateOverwrite",this.templateOverwriteFormControl)
        this.formGroup.addControl("reusableTemplateOverwrite",this.reusableTemplateOverwriteFormControl);

        this.initImportOptions();

        // Get the list of data sources
        this.datasourcesService.findAll()
            .then((datasources: any) => {
                this.availableDatasources = datasources;
            });

        this.fetchCatalogDataSources();
    }

    public findDatasetPropertiesFilter(importOption:ImportComponentOption): ImportProperty[]{
        if(importOption.properties == undefined){
            return [];
        }
        return  importOption.properties.filter((property:ImportProperty) => property.additionalProperties && (
            Object.keys(property.additionalProperties).indexOf("legacyTableDataSource") >=0 &&
            (property.additionalProperties["legacyTableDataSource"] == "true" ||property.additionalProperties["legacyTableDataSource"] == true)) ||
            (Object.keys(property.additionalProperties).indexOf("dataset") &&
            (property.additionalProperties["dataset"] == "true" ||property.additionalProperties["dataset"] == true)));
    }

    public findDataSourcePropertiesFilter(importOption:ImportComponentOption): ImportProperty[]{
        if(importOption.properties == undefined){
            return [];
        }
        return  importOption.properties.filter((property:ImportProperty) => property.additionalProperties &&
            (Object.keys(property.additionalProperties).indexOf("legacyQueryDataSource") >=0 &&
            (property.additionalProperties["legacyQueryDataSource"] == "true" ||property.additionalProperties["legacyQueryDataSource"] == true)) ||
            (Object.keys(property.additionalProperties).indexOf("catalogDataSource") &&
            (property.additionalProperties["catalogDataSource"] == "true" ||property.additionalProperties["dataset"] == true)));

    }

    /**
     * is the supplied option one for defining a Catalog DataSet?
     * @param {ImportComponentOption} importOption
     * @return {boolean}
     */
    private isDataSetOption(importOption:ImportComponentOption):boolean {
        return  importOption.importComponent == ImportComponentType.USER_DATA_SETS || (importOption.importComponent == ImportComponentType.USER_DATASOURCES && this.findDatasetPropertiesFilter(importOption).length >0);
    }


    private fetchCatalogDataSources(): void {
         this.catalogService.getDataSourcesForPluginIds(["hive","jdbc"]).subscribe(datasources => {
                this.catalogDataSources = []
                if(datasources && datasources.length >0){
                    this.catalogDataSources =   _(datasources).chain().sortBy( (ds:DataSource) =>{
                        return ds.title;
                    }).sortBy((ds:DataSource) =>{
                        return ds.connector.pluginId;
                    }).value()
                }
                else {
                    this.catalogDataSources = [];
                }
            });

    }

    private initImportOptions(){

        /**
         * Feed ImportOptions
         */
        this.feedDataImportOption = this.importService.newFeedDataImportOption();

        /**
         * Registered Template import options
         */
        this.templateDataImportOption = this.importService.newTemplateDataImportOption();

        /**
         * NiFi template options
         */
        this.nifiTemplateImportOption = this.importService.newNiFiTemplateImportOption();

        /**
         * Reusable template options
         */
        this.reusableTemplateImportOption = this.importService.newReusableTemplateImportOption();

        /**
         * User data sources options
         */
        this.userDatasourcesOption = this.importService.newUserDatasourcesImportOption();

        this.userDataSetsOption = this.importService.newUserDataSetsImportOption();

        this.feedUserFieldsImportOption = this.importService.newFeedUserFieldsImportOption();

        this.categoryUserFieldsImportOption = this.importService.newFeedCategoryUserFieldsImportOption();
    }

    ngOnInit(): void {
        this.indexImportOptions();
        this.setDefaultImportOptions();

        /**
        // Get the list of data sources
        this.DatasourcesService.findAll()
            .then((datasources: any) => {
                this.availableDatasources = datasources;
            });

         **/
    }
    ngOnDestroy() {

    }

    goBack(){
        this.kyloRouterService.back("feeds");
    }

    /**
     * callback after a user selects a file from the local file system
     */
    onFilesChange(file:File|FileList) {
        let fileChanged = false;
        if(file) {
            this.feedFile = (file as File);
            if(this.fileName == null || this.fileName != (file as File).name) {
                fileChanged = true;
            }
            this.fileName = (file as File).name;
        }else {
            this.fileName = null;
        }
        if(fileChanged){
            //resetOptions
            this.resetImportOptions();
            this.errorMap = {};
            this.uploadStatusMessages = []
            this.message = '';
        }

    }

    /**
     * when the user changes the category
     * @param {Category} $event
     */
    onCategorySelected($event:Category){
        this.category = $event;
    }


    /**
     * Returns the Error keys as an array
     * @return {any}
     */
    getErrorMapKeys(){
        if(this.errorMap) {
            return Object.keys(this.errorMap);
        }
        else {
            return [];
        }
    }

    /**
     * Upload the file and import the feed
     */
    importFeed(): void {
        if(this.feedFile == undefined){
            //ALERT you must select a file
            return;
        }
        //reset flags
        this.uploadInProgress = true;
        this.importResult = null;

        //get the values from the formControls and populated the input options
        this.feedDataImportOption.overwrite = this.feedOverwriteFormControl.value;
        this.disableFeedUponImport = this.disableUponImportFormControl.value;
        this.templateDataImportOption.overwrite = this.templateOverwriteFormControl.value;
        this.reusableTemplateImportOption.overwrite = this.reusableTemplateOverwriteFormControl.value;

        Object.keys(this.importComponentOptions).forEach(key => {
           let option = this.importComponentOptions[key];
           //skip over datasets since those are added/updated with the dialog
           if(option && option.importComponent != ImportComponentType.USER_DATA_SETS && option.properties){
               let nestedForm = this.formGroup.get(this.nestedFormGroupControlName(option));
               if(nestedForm) {
                   option.properties.forEach(prop => {
                       //if not dataset option then set the value.  Datasets are set via the dialog callback
                       if(!this.isDataSetOption(option) ) {
                           let control = nestedForm.get(prop.propertyKey);
                           if(control){
                               prop.propertyValue = control.value;

                           }
                       }


                   });
               }
           }
        });


        var uploadUrl = RestUrlConstants.ADMIN_IMPORT_FEED_URL;

        var successFn = (response: any) => {
            var responseData = response.data;
            //reassign the options back from the response data
            var importComponentOptions = responseData.importOptions.importComponentOptions;
            //map the options back to the object map
            this.updateImportOptions(importComponentOptions);
            //find the number of options that have properties that require user input

            if (!responseData.valid) {
                //Validation Error.  Additional Input is needed by the end user
                this.additionalInputNeeded = true;
            }
            else {
                var count = 0;
                var errorMap: any = {"FATAL": [], "WARN": []};
                this.importResult = responseData;
                if (responseData.template.controllerServiceErrors) {
                    //angular.forEach(responseData.templateResults.errors, function (processor) {
                    _.each(responseData.template.controllerServiceErrors, (processor: any) => {
                        if (processor.validationErrors) {
                            _.each(processor.validationErrors, (error: any) => {
                                let copy: any = {};
                                _.extend(copy, error);
                                _.extend(copy, processor);
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
                    count += FeedNifiErrorUtil.parseNifiFeedForErrors(responseData.nifiFeed, errorMap);
                }
                this.errorMap = errorMap;
                this.errorCount = count;

                var feedName = responseData.feedName != null ? responseData.feedName : "";

                if (count == 0) {
                    this.importResultIcon = "check_circle";
                    this.importResultIconColor = "#009933";

                    //this.message = this.$filter('translate')('views.ImportFeedController.succes') + feedName + ".";
                    this.message = "Success "+feedName;

                    this.resetImportOptions();
                    this.feedFile = null;
                    this.fileName = null;

                }
                else {
                    if (responseData.success) {
                        this.resetImportOptions();
                        this.feedFile = null;
                        this.fileName = null;

                        this.message = "Successfully imported and registered the feed " + feedName + " but some errors were found. Please review these errors";
                        this.importResultIcon = "warning";
                        this.importResultIconColor = "#FF9901";
                    }
                    else {
                        this.importResultIcon = "error";
                        this.importResultIconColor = "#FF0000";
                       // this.message = this.$filter('translate')('views.ImportFeedController.error2');
                        this.message = "error2"
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
           // var msg = this.$filter('translate')('views.ImportFeedController.error');
            let msg = "Error!! ";
            if (data.developerMessage) {
                msg += data.developerMessage;
            }
            this.message = msg;
            this.stopUploadStatus(1000);
        }

        //build up the options from the Map and into the array for uploading
        var importComponentOptions = this.importService.getImportOptionsForUpload(this.importComponentOptions);

        //generate a new upload key for status tracking
        this.uploadKey = this.importService.newUploadKey();

        let category = this.categoryAutoComplete.getCategoryValue();

        var params = {
            uploadKey: this.uploadKey,
            categorySystemName: category != undefined ? category.systemName : "",
            disableFeedUponImport: this.disableUponImportFormControl.value,
            importComponents: StringUtils.stringify(importComponentOptions)
        };


        this.startUploadStatus();
        this.fileUploadService.uploadFileToUrl(this.feedFile, uploadUrl, successFn, errorFn, params);

    }


    /**
     * Reset the options back to their orig. state
     */
    private resetImportOptions(): void {
        this.importComponentOptions = {};

        this.feedDataImportOption = this.importService.newFeedDataImportOption();

        this.templateDataImportOption = this.importService.newTemplateDataImportOption();

        this.nifiTemplateImportOption = this.importService.newNiFiTemplateImportOption();

        this.reusableTemplateImportOption = this.importService.newReusableTemplateImportOption();

        this.userDatasourcesOption = this.importService.newUserDatasourcesImportOption();

        this.userDataSetsOption = this.importService.newUserDataSetsImportOption();

        this.feedUserFieldsImportOption = this.importService.newFeedUserFieldsImportOption();

        this.categoryUserFieldsImportOption = this.importService.newFeedCategoryUserFieldsImportOption();

        this.indexImportOptions();
        this.setDefaultImportOptions();

        this.additionalInputNeeded = false;
        this.disableFeedUponImport = false;
        this.disableUponImportFormControl.setValue(false);
        var arr = [this.feedDataImportOption, this.templateDataImportOption, this.nifiTemplateImportOption, this.reusableTemplateImportOption, this.categoryUserFieldsImportOption, this.userDatasourcesOption, this.feedUserFieldsImportOption, this.userDataSetsOption];
        this.updateImportOptions(arr)

    }

    /**
     * get the name of the nested form group for the supplied import option
     * @param {ImportComponentOption} option
     * @return {string}
     */
    private nestedFormGroupControlName(option: ImportComponentOption){
        return "form_"+option.importComponent;
    }

    /**
     * get the actual form group for the import option
     * @param {ImportComponentOption} option
     * @return {AbstractControl | null}
     */
    public getNestedFormGroup(option: ImportComponentOption) {
        return this.formGroup.get(this.nestedFormGroupControlName(option));
    }

    /**
     *
     * @param importOptionsArr array of importOptions
     */
    private updateImportOptions(importOptionsArr: ImportComponentOption[]): void {
        _.each(importOptionsArr, (option: any) => {
            if (option.userAcknowledged) {
                option.overwriteSelectValue = "" + option.overwrite;
            }

            if (option.importComponent == ImportComponentType.FEED_DATA) {
                this.feedDataImportOption = option;
                this.feedOverwriteFormControl.setValue(this.feedDataImportOption.overwrite)
            } else if (option.importComponent == ImportComponentType.TEMPLATE_DATA) {
                this.templateDataImportOption = option;
                this.templateOverwriteFormControl.setValue(this.templateDataImportOption.overwrite);
            } else if (option.importComponent == ImportComponentType.REUSABLE_TEMPLATE) {
                this.reusableTemplateImportOption = option;
                this.reusableTemplateOverwriteFormControl.setValue(this.reusableTemplateImportOption.overwrite)
            } else if (option.importComponent == ImportComponentType.NIFI_TEMPLATE) {
                this.nifiTemplateImportOption = option;
            } else if (option.importComponent === ImportComponentType.USER_DATASOURCES) {
                this.userDatasourcesOption = option;
            } else if (option.importComponent === ImportComponentType.USER_DATA_SETS) {
                this.userDataSetsOption = option;
            } else if (option.importComponent === ImportComponentType.FEED_CATEGORY_USER_FIELDS) {
                this.categoryUserFieldsImportOption = option;
            } else if (option.importComponent === ImportComponentType.FEED_USER_FIELDS) {
                this.feedUserFieldsImportOption = option;
            }
            this.importComponentOptions[option.importComponent] = option;

            let formName = this.nestedFormGroupControlName(option);
            //add in a nested form group with required validation on the input
            if(this.formGroup.contains(formName)){
                this.formGroup.removeControl(formName);
            }

            if(option.importComponent === ImportComponentType.USER_DATA_SETS || option.importComponent === ImportComponentType.USER_DATASOURCES){
                //user datasets are different than the rest
                let nestedForm: FormGroup = new FormGroup({})
                this.formGroup.addControl(formName, nestedForm);
                if (option.properties && option.properties.length > 0) {
                    option.properties.forEach((prop:ImportProperty) => {
                        //control to ensure validity of the dataset
                        //add in the hidden property for the datasets
                        nestedForm.addControl(prop.propertyKey, new FormControl("", [Validators.required]));
                    });
                }
            }
            else {

                //reset and add any formControls if needed
                if (option.properties && option.properties.length > 0) {

                    let nestedForm: FormGroup = new FormGroup({})
                    this.formGroup.addControl(formName, nestedForm);
                    option.properties.forEach((prop:ImportProperty) => {

                        nestedForm.addControl(prop.propertyKey, new FormControl(prop.propertyValue, [Validators.required]));
                    });
                }
            }


          });
    }

    /**
     * Stop the upload status check,
     * @param delay wait xx millis before stopping (allows for the last status to be queried)
     */
    private stopUploadStatus(delay: any): void {

        let stopStatusCheck = () => {
            this.uploadProgress = 0;
            if (!_.isUndefined(this.uploadStatusCheck)) {
                clearInterval(this.uploadStatusCheck);
                this.uploadStatusCheck = undefined;
            }
        }

        if (delay != undefined) {
            setTimeout(() => {
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
        this.uploadStatusCheck = setInterval(() => {
            //poll for status
            this.http.get(RestUrlConstants.ADMIN_UPLOAD_STATUS_CHECK(this.uploadKey)).subscribe((response: any) => {
                if (response != null) {
                    this.uploadStatusMessages = response.messages;
                    this.uploadProgress = response.percentComplete;
                }
            }, (err: any) => {
                //  this.uploadStatusMessages = [];
            });
        }, 500);
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
        var arr = [this.feedDataImportOption, this.templateDataImportOption, this.nifiTemplateImportOption, this.reusableTemplateImportOption, this.categoryUserFieldsImportOption, this.feedUserFieldsImportOption, this.userDataSetsOption];
        this.importComponentOptions = _.indexBy(arr, 'importComponent')
    }

    public updateDataSet(importProperty:ImportProperty, option:ImportComponentOption){

            let data = new DatasetPreviewStepperDialogData(false,"Update");
            let dialogConfig:MatDialogConfig = DatasetPreviewStepperDialogComponent.DIALOG_CONFIG()
            dialogConfig.data = data;
            this._dialogService.open(DatasetPreviewStepperDialogComponent,dialogConfig)
                .afterClosed()
                .filter(value => typeof value !== "undefined").subscribe( (response:DatasetPreviewStepperSavedEvent) => {
                //add these to the canvas
                let preview = response.previews[0];
                let dataSet =preview.toSparkDataSet();
                this.catalogService.createDataSetWithTitle(dataSet).subscribe((ds:SparkDataSet) => {
                    //find or create dataset then

                    let valid = true;
                    let schemaNeeded = "";
                    //validate schema
                    if(importProperty.additionalProperties) {
                        if(importProperty.additionalProperties["schema"]){
                            const importSchema = importProperty.additionalProperties["schema"];
                            let previewSchema = preview.schema.map(col => col.name+" "+col.dataType).join(",");
                            if(importSchema != previewSchema){
                                valid = false
                                schemaNeeded = importSchema;
                            }
                        }
                    }

                    const onSuccess = () => {
                        importProperty.valid = true;
                        importProperty.displayName = ds.paths[0];
                        importProperty.componentName = ds.dataSource.title;
                        importProperty.propertyValue = ds.id;
                        //update form validity
                        let property = this.formGroup.get(this.nestedFormGroupControlName(option)).get(importProperty.propertyKey);
                        if (property) {
                            property.setValue("true");
                        }
                    };
                    if (valid) {
                        onSuccess();
                    } else {
                        this._dialogService.openConfirm({
                            title: "Schemas don't match",
                            message: "The selected dataset schema doesnt match. Please supply a schema matching: \n" + schemaNeeded,
                            acceptButton: "Ignore",
                            cancelButton: "Cancel"
                        }).afterClosed().subscribe(value => {
                            if (value) {
                                onSuccess();
                            }
                        });
                    }
                })
            });
    }
}
