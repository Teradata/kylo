import {HttpClient} from "@angular/common/http";
import {Component, EventEmitter, Inject, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {TdDialogService} from "@covalent/core/dialogs";
import {Observable, ObservableInput} from "rxjs/Observable";
import {debounceTime} from "rxjs/operators/debounceTime";
import {switchMap} from "rxjs/operators/switchMap";
import {Subscription} from "rxjs/Subscription";

import {CloneUtil} from "../../../common/utils/clone-util";
import {StateService} from "../../../services/StateService";
import {UserDatasource} from "../../model/user-datasource";
import {VisualQuerySaveService} from "../services/save.service";
import {SaveRequest, SaveResponseStatus} from "../wrangler/api/rest-model";
import {QueryEngine} from "../wrangler/query-engine";
import {SaveOptionsComponent} from "./save-options.component";
import {DatasourcesService, JdbcDatasource, TableReference} from '../../services/DatasourcesServiceIntrefaces';
import {DataSource} from "../../catalog/api/models/datasource";
import {CatalogService} from "../../catalog/api/services/catalog.service";
import * as _ from "underscore";

export enum SaveMode { INITIAL, SAVING, SAVED}

@Component({
    selector: "thinkbig-visual-query-store",
    styleUrls: ["./store.component.scss"],
    templateUrl: "./store.component.html"
})
export class VisualQueryStoreComponent implements OnDestroy, OnInit {

    /**
     * Query engine
     */
    @Input()
    engine: QueryEngine<any>;

    /**
     * Transformation model
     */
    @Input()
    model: any;

    /**
     * Connected to 'Back' button
     */
    @Output()
    back = new EventEmitter<void>();

    /**
     * Indicates if there is an error connecting to the data source
     */
    connectionError: boolean;

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
     * Additional options for the output format.
     */
    properties: any[] = [];

    propertiesForm = new FormGroup({});

    /**
     * Subscription to notification removal
     */
    removeSubscription: Subscription;

    /**
     * Subscription to save response
     */
    saveSubscription: Subscription;

    saveMode: SaveMode = SaveMode.INITIAL;

    tableNameControl = new FormControl(null, [Validators.required, Validators.minLength(1)]);

    tableNameList: Observable<TableReference[]>;

    catalogSQLDataSources: DataSource[] = [];

    /**
     * Output configuration
     */
    target: SaveRequest = {};

    constructor(private $http: HttpClient, @Inject("DatasourcesService") private DatasourcesService: DatasourcesService, @Inject("RestUrlService") private RestUrlService: any,
                private VisualQuerySaveService: VisualQuerySaveService, private $mdDialog: TdDialogService, @Inject("StateService") private stateService: StateService,
                private catalogService:CatalogService) {
        // Listen for notification removals
        this.removeSubscription = this.VisualQuerySaveService.subscribeRemove((event) => {
            if (event.id === this.downloadId) {
                this.downloadId = null;
                this.downloadUrl = null;
            }
        });

        // Listen for table name changes
        this.tableNameControl.valueChanges.subscribe(text => this.target.tableName = text);
        this.tableNameList = this.tableNameControl.valueChanges.pipe(
            debounceTime(100),
            switchMap(text => this.findTables(text))
        );
    };

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
        this.target.jdbc;
        this.target.catalogDatasource = new DataSource();
        const kyloSourcesPromise = Promise.all([this.engine.getNativeDataSources(), this.DatasourcesService.findAll()])
            .then(resultList => {
                this.kyloDataSources = resultList[0].concat(resultList[1]);
                if (this.model.$selectedDatasourceId) {
                   let jdbcSource = this.kyloDataSources.find(datasource => datasource.id === this.model.$selectedDatasourceId)
                    if(jdbcSource != undefined) {
                        this.target.jdbc = (jdbcSource as JdbcDatasource);
                    }
                }
            });


       const catalogDataSourceObservable = this.catalogService.getDataSourcesForPluginIds(["hive","jdbc"]);
       const catalogDataSourcePromise = catalogDataSourceObservable.toPromise();
        catalogDataSourceObservable.subscribe(datasources => {
            if(datasources && datasources.length >0){
                this.catalogSQLDataSources =   _(datasources).chain().sortBy( (ds:DataSource) =>{
                    return ds.title;
                }).sortBy((ds:DataSource) =>{
                    return ds.connector.pluginId;
                }).value()
            }
            else {
                this.catalogSQLDataSources = [];
            }
        });


        // Get list of Spark data sources
        const sparkSourcesPromise = this.$http.get<string[]>(this.RestUrlService.SPARK_SHELL_SERVICE_URL + "/data-sources").toPromise()
            .then(response => {
                this.downloadFormats = response["downloads"].sort();
                this.tableFormats = response["tables"].sort();
            });

        // Wait for completion
        Promise.all([kyloSourcesPromise, sparkSourcesPromise,catalogDataSourcePromise])
            .then(() => this.loading = false, () => this.error = "Invalid response from server.");

        this.destination = "DOWNLOAD";

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
    findTables(name: any): ObservableInput<TableReference[]> {
        if (this.target.jdbc) {
            const tables = this.engine.searchTableNames(name, this.target.jdbc.id);
            if (tables instanceof Promise) {
                return tables.then(response => {
                    this.connectionError = false;
                    return response;
                }, () => {
                    this.connectionError = true;
                    return [];
                });
            } else {
                this.connectionError = false;
                return Observable.of(tables);
            }
        }
        else if(this.target.catalogDatasource){

         return   this.catalogService.listTables(this.target.catalogDatasource.id,name)

        }
        else {
            return Observable.of([])
        }
    }

    /**
     * Should the Results card be shown, or the one showing the Download options
     * @return {boolean}
     */
    showSaveResultsCard(): boolean {
        return this.saveMode == SaveMode.SAVING || this.saveMode == SaveMode.SAVED;
    }

    /**
     * Is a save for a file download in progress
     * @return {boolean}
     */
    isSavingFile(): boolean {
        return this.saveMode == SaveMode.SAVING && this.destination === "DOWNLOAD";
    }

    /**
     * is a save for a table export in progress
     * @return {boolean}
     */
    isSavingTable(): boolean {
        return this.saveMode == SaveMode.SAVING && this.destination === "TABLE";
    }

    /**
     * is a save for a table export complete
     * @return {boolean}
     */
    isSavedTable(): boolean {
        return this.saveMode == SaveMode.SAVED && this.destination === "TABLE";
    }

    /**
     * have we successfully saved either to a file or table
     * @return {boolean}
     */
    isSaved(): boolean {
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
        this.back.emit(null);
    }

    /**
     * Reset download options
     * @private
     */
    _reset(): void {
        this.saveMode = SaveMode.INITIAL;
        this.downloadUrl = null;
        this.error = null;
        this.loading = false;
        this.propertiesForm = new FormGroup({});
    }

    /**
     * Exit the Visual Query and go to the Feeds list
     */
    exit(): void {
        this.stateService.navigateToHome();
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
    showOptionsInfo(): void {
        this.$mdDialog.open(SaveOptionsComponent, {panelClass: "full-screen-dialog"});
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
            request = CloneUtil.deepCopy(this.target);
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
                        if (this.saveMode == SaveMode.SAVING) {
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

    trackDataSource(index: number, dataSource: UserDatasource) {
        return dataSource.id;
    }

    /**
     * Gets the target output options by parsing the properties object.
     */
    private getOptions(): { [k: string]: string } {
        if(this.target.options) {
            let options = CloneUtil.deepCopy(this.target.options);
            this.properties.forEach(property => options[property.systemName] = property.value);
            return options;
        }
        else {
            return {};
        }

    }
}
