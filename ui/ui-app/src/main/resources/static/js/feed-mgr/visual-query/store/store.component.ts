import {Input, OnDestroy, OnInit} from "@angular/core";
import * as angular from "angular";
import {Subscription} from "rxjs/Subscription";

import {UserDatasource} from "../../model/user-datasource";
import {DatasourcesServiceStatic} from "../../services/DatasourcesService.typings";
import {VisualQuerySaveService} from "../services/save.service";
import {SaveRequest, SaveResponseStatus} from "../wrangler/api/rest-model";
import {QueryEngine} from "../wrangler/query-engine";
import DatasourcesService = DatasourcesServiceStatic.DatasourcesService;
import JdbcDatasource = DatasourcesServiceStatic.JdbcDatasource;
import TableReference = DatasourcesServiceStatic.TableReference;


export enum SaveMode { INITIAL, SAVING, SAVED}

export class VisualQueryStoreComponent implements OnDestroy, OnInit {

    stepperController : any;

    /**
     * Target destination type. Either DOWNLOAD or TABLE.
     */
    destination: string;

    /**
     * Identifier for download
     */
    downloadId: string;

    /**
     * Url to download results
     */
    downloadUrl: string;

    /**
     * Query engine
     */
    @Input()
    engine: QueryEngine<any>;

    /**
     * Error message
     */
    error: string;

    /**
     * Spark data source names
     */
    downloadFormats: string[];

    /**
     * Spark table source names
     */
    tableFormats: string[];

    /**
     * HTML form
     */
    form: any;

    /**
     * Indicates if the properties are valid.
     */
    isValid: boolean;

    /**
     * List of Kylo data sources
     */
    kyloDataSources: UserDatasource[] = [];

    /**
     * Indicates the page is loading
     */
    loading = true;

    /**
     * Transformation model
     */
    @Input()
    model: any;

    /**
     * Additional options for the output format.
     */
    properties: any[];

    /**
     * Subscription to notification removal
     */
    removeSubscription: Subscription;

    /**
     * Subscription to save response
     */
    saveSubscription: Subscription;

    /**
     * Index of this step
     */
    stepIndex: string;

    saveMode: SaveMode = SaveMode.INITIAL;

    /**
     * Output configuration
     */
    target: SaveRequest = {};

    static readonly $inject: string[] = ["$http", "DatasourcesService", "RestUrlService", "VisualQuerySaveService", "$mdDialog"];

    constructor(private $http: angular.IHttpService, private DatasourcesService: DatasourcesService, private RestUrlService: any, private VisualQuerySaveService: VisualQuerySaveService, private $mdDialog: angular.material.IDialogService) {
        // Listen for notification removals
        this.removeSubscription = this.VisualQuerySaveService.subscribeRemove((event) => {
            if (event.id === this.downloadId) {
                this.downloadId = null;
                this.downloadUrl = null;
            }
        });
    };

    $onDestroy(): void {
        this.ngOnDestroy();
    }

    $onInit(): void {
        this.ngOnInit();
    }

    /**
     * Release resources when component is destroyed.
     */
    ngOnDestroy(): void {
        if (this.removeSubscription) {
            this.removeSubscription.unsubscribe();
        }
        if (this.saveSubscription) {
            this.saveSubscription.unsubscribe();
        }
    }

    /**
     * Initialize resources when component is initialized.
     */
    ngOnInit(): void {
        // Get list of Kylo data sources
        const kyloSourcesPromise = Promise.all([this.engine.getNativeDataSources(), this.DatasourcesService.findAll()])
            .then(resultList => {
                this.kyloDataSources = resultList[0].concat(resultList[1]);
                if (this.model.$selectedDatasourceId) {
                    this.target.jdbc = this.kyloDataSources.find(datasource => datasource.id === this.model.$selectedDatasourceId) as JdbcDatasource;
                }
            });

        // Get list of Spark data sources
        const sparkSourcesPromise = this.$http.get<string[]>(this.RestUrlService.SPARK_SHELL_SERVICE_URL + "/data-sources")
            .then(response => {
                this.downloadFormats = response.data["downloads"].sort();
                this.tableFormats = response.data["tables"].sort();
            });

        // Wait for completion
        Promise.all([kyloSourcesPromise, sparkSourcesPromise])
            .then(() => this.loading = false, () => this.error = "Invalid response from server.");
    }

    /**
     * Downloads the saved results.
     */
    download(): void {
        window.open(this.downloadUrl, "_blank");

        // Clear download buttons
        this.VisualQuerySaveService.removeNotification(this.downloadId);
        this.downloadId = null;
        this.downloadUrl = null;
    }

    /**
     * Find tables matching the specified name.
     */
    findTables(name: any): TableReference[] | Promise<TableReference[]> {
        let tables: TableReference[] | Promise<TableReference[]> = [];

        if (this.target.jdbc) {
            tables = this.engine.searchTableNames(name, this.target.jdbc.id);
            if (tables instanceof Promise) {
                tables = tables.then(response => {
                    this.form.datasource.$setValidity("connectionError", true);
                    return response;
                }, () => {
                    this.form.datasource.$setValidity("connectionError", false);
                    return [];
                });
            } else {
                this.form.datasource.$setValidity("connectionError", true);
            }
        }

        return tables;
    }

    /**
     * Should the Results card be shown, or the one showing the Download options
     * @return {boolean}
     */
    showSaveResultsCard() : boolean {
        return this.saveMode == SaveMode.SAVING || this.saveMode == SaveMode.SAVED;
    }

    /**
     * Is a save for a file download in progress
     * @return {boolean}
     */
    isSavingFile():boolean {
        return this.saveMode == SaveMode.SAVING && this.destination === "DOWNLOAD";
    }

    /**
     * is a save for a table export in progress
     * @return {boolean}
     */
    isSavingTable():boolean {
        return this.saveMode == SaveMode.SAVING && this.destination === "TABLE";
    }

    /**
     * is a save for a table export complete
     * @return {boolean}
     */
    isSavedTable():boolean {
        return this.saveMode == SaveMode.SAVED && this.destination === "TABLE";
    }

    /**
     * have we successfully saved either to a file or table
     * @return {boolean}
     */
    isSaved():boolean {
        return this.saveMode == SaveMode.SAVED;
    }

    /**
     * Navigate back from the saved results card and show the download options
     */
    downloadAgainAs(): void {
        this._reset();
    }

    /**
     * Navigate back and take the user from the Save/store screen to the Transform table screen
     */
    modifyTransformation(): void {
        this._reset();
        let prevStep : any = this.stepperController.previousActiveStep(parseInt(this.stepIndex));
        if(angular.isDefined(prevStep)){
            this.stepperController.selectedStepIndex = prevStep.index;
        }
    }

    /**
     * Reset download options
     * @private
     */
    _reset():void {
        this.saveMode = SaveMode.INITIAL;
        this.downloadUrl = null;
        this.error = null;
        this.loading = false;
    }

    /**
     * Exit the Visual Query and go to the Feeds list
     */
    exit(): void {
        this.stepperController.cancelStepper();
    }

    /**
     * Reset options when format changes.
     */
    onFormatChange(): void {
        this.target.options = {};
    }

    /**
     * Show options info dialog for different data formats for download.
     */
    showOptionsInfo () : void {
        this.$mdDialog.show({
            clickOutsideToClose: true,
            controller: class {
                static readonly $inject = ["$mdDialog"];

                constructor(private $mdDialog: angular.material.IDialogService) {
                }

                /**
                 * Hides this dialog.
                 */
                hide() {
                    this.$mdDialog.hide();
                }
            },
            controllerAs: "dialog",
            parent: angular.element(document.body),
            templateUrl: "js/feed-mgr/visual-query/store/save.options.dialog.html"
        });
    };



    /**
     * Saves the results.
     */
    save(): void {
        this.saveMode = SaveMode.SAVING;
        // Remove current subscription
        if (this.saveSubscription) {
            this.saveSubscription.unsubscribe();
            this.saveSubscription = null;
        }

        // Build request
        let request: SaveRequest;

        if (this.destination === "DOWNLOAD") {
            request = {
                format: this.target.format,
                options: this.getOptions()
            };
        } else {
            request = angular.copy(this.target);
            request.options = this.getOptions();
        }

        // Save transformation
        this.downloadUrl = null;
        this.error = null;
        this.loading = true;
        this.saveSubscription = this.VisualQuerySaveService.save(request, this.engine)
            .subscribe(response => {
                    this.loading = false;
                    if (response.status === SaveResponseStatus.SUCCESS && this.destination === "DOWNLOAD") {
                        this.downloadId = response.id;
                        this.downloadUrl = response.location;
                        //reset the save mode if its Saving
                        if(this.saveMode == SaveMode.SAVING) {
                            this.saveMode = SaveMode.SAVED;
                        }
                    }
                },
                response => {
                    this.error = response.message;
                    this.loading = false;
                    this.saveMode = SaveMode.INITIAL;
                },
                () => this.loading = false);
    }

    /**
     * Gets the target output options by parsing the properties object.
     */
    private getOptions(): { [k: string]: string } {
        let options = angular.copy(this.target.options);
        this.properties.forEach(property => options[property.systemName] = property.value);
        return options;
    }
}

angular.module(require("feed-mgr/visual-query/module-name"))
    .component("thinkbigVisualQueryStore", {
        bindings: {
            engine: "=",
            model: "=",
            stepIndex: "@"
        },
        controller: VisualQueryStoreComponent,
        controllerAs: "$st",
        require: {
            stepperController: "^thinkbigStepper"
        },
        templateUrl: "js/feed-mgr/visual-query/store/store.component.html"
    });
