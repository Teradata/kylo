import {Component, EventEmitter, Inject, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges, ViewChild, ViewContainerRef} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";
import {MatStepper} from "@angular/material/stepper";
import {TdDialogService} from "@covalent/core/dialogs";
import {TdLoadingService} from "@covalent/core/loading";
import "rxjs/add/observable/from";
import 'rxjs/add/observable/fromPromise';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/debounceTime';
import "rxjs/add/operator/filter";
import 'rxjs/add/operator/map';
import "rxjs/add/operator/switchMap";
import 'rxjs/add/operator/toPromise';
import {map} from 'rxjs/operators';
import {Observable} from "rxjs/Observable";
import {ISubscription} from "rxjs/Subscription";
import * as _ from "underscore";

import SideNavService from "../../../services/SideNavService";
import {FeedDataTransformation} from "../../model/feed-data-transformation";
import {SparkDataSet} from "../../model/spark-data-set.model";
import {UserDatasource} from "../../model/user-datasource";
import {DatasourcesService} from "../../services/DatasourcesService";
import {HiveService} from "../../services/HiveService";
import {VisualQueryService} from "../../services/VisualQueryService";
import {DatasourcesServiceStatic, SchemaField, TableSchema} from "../wrangler";
import {QueryEngine} from "../wrangler/query-engine";
import {ConnectionDialog, ConnectionDialogConfig, ConnectionDialogResponse, ConnectionDialogResponseStatus} from "./connection-dialog/connection-dialog.component";
import {FlowChartComponent} from "./flow-chart/flow-chart.component";
import {FlowChart} from "./flow-chart/model/flow-chart.model";
import {DatasetPreviewStepperDialogComponent, DatasetPreviewStepperDialogData} from "../../catalog-dataset-preview/preview-stepper/dataset-preview-stepper-dialog.component";
import {MatDialogConfig} from "@angular/material/dialog";
import {DatasetPreviewStepperSavedEvent} from "../../catalog-dataset-preview/preview-stepper/dataset-preview-stepper.component";
import {Subject} from "rxjs/Subject";
import {CatalogService} from "../../catalog/api/services/catalog.service";

/**
 * Code for the delete key.
 */
const DELETE_KEY_CODE = 46;

/**
 * Code for control key.
 */
const CTRL_KEY_CODE = 17;

/**
 * Code for A key.
 */
const A_KEY_CODE = 65;

/**
 * Code for esc key.
 */
const ESC_KEY_CODE = 27;

/**
 * Displays the Build Query step of the Visual Query page.
 *
 * There are two modes for how the user may build their query:
 *
 * - Visual Mode - (default) A {@code QueryEngine} is used to retrieve a list of tables and the schema is displayed in a flow chart. The nodes in the flow chart can be connected to create joins
 * between tables.
 *
 * - Advanced Mode - A textarea is provided for the user to input their query.
 */
@Component({
    selector: 'build-query-ng2',
    styleUrls: ["js/feed-mgr/visual-query/build-query/build-query-ng2.component.css"],
    templateUrl: "js/feed-mgr/visual-query/build-query/build-query-ng2.component.html",
    host: {
        '(document:keyup)': '_keyup($event)',
        '(document:keydown)': '_keydown($event)',
    }
})
export class BuildQueryComponent implements OnDestroy, OnChanges, OnInit {

    /**
     * Query engine for determining capabilities.
     */
    @Input()
    engine: QueryEngine<any>;

    /**
     * Data transformation model
     */
    @Input()
    model: FeedDataTransformation;

    /**
     * Flag to show the datasource drop down.
     * when used in the feed stepper this will be false and the sources will pull from the previewCollectionService
     * when used in the Visual Query this will be true
     */
    @Input()
    showDatasources?: boolean = true;

    @Input()
    showCancel?:boolean = true;

    @Input()
    stepper: MatStepper;

    /**
     * The form for the page
     */
    @Input()
    form: FormGroup;

    /**
     * Event emitted to return to the previous step
     */
    @Output()
    back = new EventEmitter<void>();

    /**
     * Event emitted to advance to the next step
     */
    @Output()
    next = new EventEmitter<void>();

    /**
     * Indicates if the UI is in advanced mode
     */
    advancedMode: boolean = false;

    /**
     * Text indicating which node to switch to
     */
    advancedModeText: string;

    /**
     * List of data sources to display.
     */
    allDatasources: UserDatasource[] = [];

    /**
     * List of data sources to display.
     */
    availableDatasources: UserDatasource[] = [];

    availableSQLDatasources: UserDatasource[] = [];

    /**
     * Model for the chart.
     */
    chartViewModel: any = {data: {nodes: []}, nodes: [], connections: []}

    /**
     * Indicates that there was an error retrieving the list of tables.
     * @type {boolean} true if there was an error or false otherwise
     */
    databaseConnectionError: boolean = false;

    /**
     * Error message to be displayed.
     */
    error: string;

    /**
     * Height offset from the top of the page.
     */
    @Input()
    heightOffset: number;

    /**
     * Indicates if the model is valid.
     */
    isValid: boolean = false;

    /**
     * Indicates that the page is being loaded.
     */
    loadingPage: boolean = true;

    /**
     * Indicates that a table schema is being loaded.
     */
    loadingSchema: boolean = false;

    /**
     * Next node id.
     */
    nextNodeID = 10;

    /**
     * List of the data sources used in model.
     * @type {Array.<string>}
     */
    selectedDatasourceIds: string[] = [];

    /**
     * holds the metadata about each column and table that is used to build the SQL str in the getSQLModel() method
     */
    selectedColumnsAndTables: any = [];

    @ViewChild("flowChart")
    flowChart: FlowChartComponent;

    /**
     * Aysnc autocomplete list of tables
     */
    public filteredTables: Observable<DatasourcesServiceStatic.TableReference[]>;

    /**
     * List of native data sources to exclude from the model.
     */
    private nativeDataSourceIds: string[] = [];

    private fileDataSource: UserDatasource = {id: "FILE", name: "Local File", description: "Local File", type: "File"}

    /**
     * flag to indicate the ctrl key is pressed
     */
    ctrlDown: boolean;

    //callbacks
    onCreateConnectionSubscription: ISubscription;
    onEditConnectionSubscription: ISubscription;
    onDeleteConnectionSubscription: ISubscription;

    /**
     * Constructs a {@code BuildQueryComponent}.
     *    private hiveService: HiveService, private sideNavService: SideNavService,
     private visualQueryService: VisualQueryService, private datasourcesService: DatasourcesService,
     */
    constructor(private _dialogService: TdDialogService,
                private viewContainerRef: ViewContainerRef,
                private _loadingService: TdLoadingService,
                @Inject("HiveService") private hiveService: HiveService,
                @Inject("SideNavService") private sideNavService: SideNavService,
                @Inject("VisualQueryService") private visualQueryService: VisualQueryService,
                @Inject("DatasourcesService") private datasourcesService: DatasourcesService,
                private catalogService:CatalogService) {
        // Setup environment
        //this.heightOffset = $element.attr("height-offset");
        this.sideNavService.hideSideNav();
    }

    private initFormComponents() {
        if (this.form == undefined) {
            this.form = new FormGroup({});
        }

        if (this.showDatasources) {
            let datasource = new FormControl();
            this.form.addControl("datasource", datasource);
            datasource.valueChanges.subscribe((datasourceId: string) => {
                this.model.$selectedDatasourceId = datasourceId;
                this.onDatasourceChange();
            });


            let tableAutocomplete = new FormControl();
            this.form.addControl("tableAutocomplete", tableAutocomplete);

            this.filteredTables = tableAutocomplete.valueChanges.debounceTime(100).switchMap(text => this.onAutocompleteQuerySearch(text));
        }
    }

    /**
     * Function for the Autocomplete to display the name of the table object matched
     * @param {TableReference} table
     * @return {string | undefined}
     */
    tableAutocompleteDisplay(table?: DatasourcesServiceStatic.TableReference): string | undefined {
        return table ? table.fullName : undefined;
    }

    /**
     * Get or set the SQL for the advanced mode.
     */
    advancedModeSql(sql: string = null) {
        if (sql !== null) {
            this.model.sql = sql;
            this.validate();
        }
        return this.model.sql;
    }

    /**
     * Indicates if the active datasource can be changed.
     */
    canChangeDatasource(): boolean {
        return (this.error == null && (this.engine.allowMultipleDataSources || this.selectedDatasourceIds.length === 0));
    }

    selectedTable() : string {
        return this.form.contains('tableAutocomplete') ? this.form.get('tableAutocomplete').value : undefined;
    }

    /**
     * Adds the table to the FlowChart.
     */
    onAddTable() {
        this.sideNavService.hideSideNav();
        let table = this.selectedTable();
        if (table) {
            this.onTableClick(table);
            this.form.get('tableAutocomplete').reset('');
        }
    }

    /**
     * Initialize state from services.
     */
    private init() :Observable<UserDatasource[]> {
        let datasources$ = new Subject<UserDatasource[]>();

        // Get the list of data sources
        Promise.all([this.engine.getNativeDataSources(), this.datasourcesService.findAll()])
            .then(resultList => {
                this.nativeDataSourceIds = resultList[0].map((dataSource: UserDatasource): string => dataSource.id);

                const supportedDatasources = resultList[0].concat(resultList[1]).filter(this.engine.supportsDataSource);
                if (supportedDatasources.length > 0) {
                    return supportedDatasources;
                } else {
                    const supportedNames = ((supportedNameList) => {
                        if (supportedNameList.length === 0) {
                            return "";
                        } else if (supportedNameList.length === 1) {
                            return `Please create a ${supportedNameList[0]} data source and try again.`;
                        } else {
                            return `Please create one of the following data sources and try again: ${supportedNameList.join(", ")}`;
                        }
                    })(this.engine.getSupportedDataSourceNames());
                    throw new Error("No supported data sources were found. " + supportedNames);
                }
            })
            .then((datasources: UserDatasource[]) => {
                this.updateAvailableDatasources(datasources);
                this.allDatasources = datasources;

                if (this.model.$selectedDatasourceId == null) {
                    this.model.$selectedDatasourceId = datasources[0].id;
                    this.form.get("datasource").setValue(this.model.$selectedDatasourceId);
                }
                this.validate();
                datasources$.next(datasources);
            })
            .catch((err: any) => {
                console.error(err);
                this.error = err;
                datasources$.error(err);
            })
            .then(() => {
                this.loadingPage = false;
            });
        return datasources$.asObservable();
    }

    private updateAvailableDatasources(datasources?:UserDatasource[]){
        if(datasources ) {
            this.availableDatasources = datasources;
        }
/*
        let fileIndex = this.availableDatasources.indexOf(this.fileDataSource);
        if(this.advancedMode == true){
                if(fileIndex >=0) {
                    //remove the file datasource for adv. mode
                    this.availableDatasources.splice(fileIndex, 1);
                }
         }
            else {
            if(fileIndex <0) {
                //add in the File data source
                this.availableDatasources.push(this.fileDataSource);
            }
        }
        */
    }

    private _keydown(evt: KeyboardEvent) {
        if (evt.keyCode === CTRL_KEY_CODE) {
            this.ctrlDown = true;
            evt.stopPropagation();
            evt.preventDefault();
        }
    }

    private _keyup(evt: KeyboardEvent) {
        if (evt.keyCode === DELETE_KEY_CODE) {
            //
            // Delete key.
            //
            this.chartViewModel.deleteSelected();
            this.validate();
        }

        if (evt.keyCode == A_KEY_CODE && this.ctrlDown) {
            //
            // Ctrl + A
            //
            this.chartViewModel.selectAll();
        }

        if (evt.keyCode == ESC_KEY_CODE) {
            // Escape.
            this.chartViewModel.deselectAll();
        }

        if (evt.keyCode === CTRL_KEY_CODE) {
            this.ctrlDown = false;

            evt.stopPropagation();
            evt.preventDefault();
        }
    }

    addPreviewDataSets() {
        if (this.model.datasets && this.model.datasets.length > 0) {
            this.addSparkDataSets(this.model.datasets)
        }
    }
    isDataSetUserDataSource(dataSet:SparkDataSet){
        if(dataSet == undefined){
            return false;
        }
        return this.allDatasources.find(ds => ds.id == dataSet.dataSource.id) != undefined;
    }

    ensureDataSetId(dataset:SparkDataSet) :Observable<SparkDataSet>{
        if(dataset.id == undefined){
         return this.catalogService.createDataSet(dataset).pipe(map((ds:SparkDataSet) => {
             dataset.id = ds.id;
             return dataset;
         }))
        }
        else {
            return Observable.of(dataset);
        }

    }
    ensureDataSetIds(datasets:SparkDataSet[]) :Observable<SparkDataSet>[]{
        return datasets.filter(dataset => typeof dataset.preview !== "undefined")
            .map(dataset => this.ensureDataSetId(dataset))

    }

    addSparkDataSets(datasets:SparkDataSet[]) {
        if(datasets && datasets.length >0) {

            Observable.forkJoin(this.ensureDataSetIds(datasets)).subscribe((dataSets:SparkDataSet[]) => {
                dataSets.forEach((dataset: SparkDataSet) => {
                    let tableSchema: any = {};

                    tableSchema.schemaName = dataset.getSchemaName();
                    tableSchema.tableName = dataset.getTableName();
                    tableSchema.name = dataset.getTableName();
                    tableSchema.fields = dataset.schema.map(tableColumn => {
                        let field: any = {};
                        field.name = tableColumn.name;
                        field.description = null;
                        field.nativeDataType = tableColumn.dataType;
                        field.derivedDataType = tableColumn.dataType;
                        field.dataTypeWithPrecisionAndScale = tableColumn.dataType;
                        return field;
                    });
                    let nodeName = dataset.getDisplayIdentifier()
                    this.addDataSetToCanvas(dataset.dataSource.id, nodeName, tableSchema, dataset);

                });
            })

        }
    }

    /**
     * Initialize the model for the FlowChart.
     */
    setupFlowChartModel() {
        // Load data model
        let chartDataModel: FlowChart.ChartDataModel;
        if (this.model.chartViewModel != null) {
            chartDataModel = this.model.chartViewModel;
        } else {
            chartDataModel = new FlowChart.ChartDataModel();
        }

        // Prepare nodes
        _.each(chartDataModel.nodes, (node: any) => {
            // Add utility functions
            this.prepareNode(node);

            // Determine next node ID
            this.nextNodeID = Math.max(node.id + 1, this.nextNodeID);
        });

        // Create view model
        this.chartViewModel = new FlowChart.ChartViewModel(chartDataModel);

        this.onCreateConnectionSubscription = this.chartViewModel.onCreateConnection$.subscribe(this.onCreateConnectionCallback.bind(this));
        this.onEditConnectionSubscription = this.chartViewModel.onEditConnection$.subscribe(this.onEditConnectionCallback.bind(this));
        this.onDeleteConnectionSubscription = this.chartViewModel.onDeleteSelected$.subscribe(this.onDeleteSelectedCallback.bind(this))
    }

    onDatasourceChange() {
        //clear the autocomplete
        this.form.get('tableAutocomplete').reset('');

        if (this.model.$selectedDatasourceId == 'FILE') {
            //warn if the user has other items
            if (this.chartViewModel.nodes != null && (this.chartViewModel.nodes.length > 0)) {
                //WARN if you upload a file you will lose your other data

                this._dialogService.openConfirm({
                    message: 'If you switch and upload a local file you will lose your other data sources. Are you sure you want to continue?',
                    disableClose: true,
                    viewContainerRef: this.viewContainerRef, //OPTIONAL
                    title: 'Upload a local file', //OPTIONAL, hides if not provided
                    cancelButton: 'Cancel', //OPTIONAL, defaults to 'CANCEL'
                    acceptButton: 'Continue', //OPTIONAL, defaults to 'ACCEPT'
                    width: '500px', //OPTIONAL, defaults to 400px
                }).afterClosed().subscribe((accept: boolean) => {
                    if (accept) {
                        this.chartViewModel.nodes = [];
                        this.model.chartViewModel = null;
                    } else {
                        this.model.$selectedDatasourceId = this.availableDatasources[0].id;
                    }
                });

            }
        }
        else {
            this.model.sampleFile = null;
            this.engine.setSampleFile(null);
        }
    }

    /**
     * Called after a user Adds a table to fetch the Columns and datatypes.
     * @param schema - the schema name
     * @param table - the table name
     */
    private getTableSchema(schema: string, table: string): Promise<TableSchema> {
        return this.engine.getTableSchema(schema, table, this.model.$selectedDatasourceId)
            .then((tableSchema: TableSchema) => {
                this.loadingSchema = false;
                return tableSchema;
            });
    }

    /**
     * Validate the canvas.
     * If there is at least one table defined, it is valid
     * TODO enhance to check if there are any tables without connections
     */
    private validate() {
        if (this.advancedMode) {
            let sql = this.advancedModeSql();
            this.isValid = (typeof(sql) !== "undefined" && sql.length > 0);

            this.model.$selectedColumnsAndTables = null;
            this.model.chartViewModel = null;
            this.model.datasourceIds = this.nativeDataSourceIds.indexOf(this.model.$selectedDatasourceId.toUpperCase()) < 0 ? [this.model.$selectedDatasourceId] : [];
            this.model.$datasources = this.datasourcesService.filterArrayByIds(this.model.$selectedDatasourceId, this.availableDatasources);
        } else if (this.model.$selectedDatasourceId == 'FILE') {
            this.isValid = this.model.sampleFile != undefined;
        } else if (this.chartViewModel.nodes != null && this.chartViewModel.nodes.length > 0) {
            this.isValid = true;

            this.model.chartViewModel = this.chartViewModel.data;
            this.model.sql = this.getSQLModel();
            this.model.$selectedColumnsAndTables = this.selectedColumnsAndTables;
            this.model.datasourceIds = this.selectedDatasourceIds.filter(id => this.nativeDataSourceIds.indexOf(id.toUpperCase()) < 0);
            this.model.$datasources = this.datasourcesService.filterArrayByIds(this.selectedDatasourceIds, this.availableDatasources);
        } else {
            this.isValid = false;
        }
    }

    private getNewXYCoord() {
        let coord = {x: 20, y: 20};
        //attempt to align it on the top
        if (this.chartViewModel.data.nodes.length > 0) {
            //constants
            let yThreshold = 150;
            let tableWidth = 250;

            //reduce the set to just show those in the top row
            let tables = _.filter(this.chartViewModel.data.nodes, (table: any) => {
                return table.y <= yThreshold;
            });
            //sort by x then y (underscore sort is reverse thinking)
            tables = _.chain(tables).sortBy('y').sortBy('x').value();
            let lastX = coord.x;
            _.some(tables, (table: any) => {
                //if this table is within the top row
                //move over to find the next X position on the top row that is open
                if (table.x < lastX + tableWidth) {
                    lastX = table.x + table.width;
                }
                else {
                    //break out
                    return true;
                }

            });
            if (lastX > 20) {
                //add padding
                lastX += 20;
            }
            coord.x = lastX;

        }
        return coord;
    }

    /**
     * Turn on SQL mode.
     */
    toggleAdvancedMode() {
        if (this.advancedMode === false) {

            let goAdvanced = () => {
                this.advancedMode = true;
                this.advancedModeText = "Visual Mode";
                this.updateAvailableDatasources();
            };
            if (this.chartViewModel.nodes.length > 0) {
                this._dialogService.openConfirm({
                    message: 'If you switch to the advanced SQL editor then you will no longer be able to return to this visual editor. Are you sure you want to continue?',
                    disableClose: true,
                    viewContainerRef: this.viewContainerRef, //OPTIONAL
                    title: 'Switch to advanced mode', //OPTIONAL, hides if not provided
                    cancelButton: 'Cancel', //OPTIONAL, defaults to 'CANCEL'
                    acceptButton: 'Continue', //OPTIONAL, defaults to 'ACCEPT'
                    width: '500px', //OPTIONAL, defaults to 400px
                }).afterClosed().subscribe((accept: boolean) => {
                    if (accept) {
                        goAdvanced();
                    } else {
                        //nada
                    }
                });


            } else {
                goAdvanced();
            }
        } else {
            this.advancedMode = false;
            this.model.sql = "";
            this.advancedModeText = "Advanced Mode";
            this.updateAvailableDatasources();
        }

    };

    /**
     * Adds utility functions to a node data model.
     *
     * @param node - the node data model
     */
    prepareNode(node: any) {
        const self = this;
        /**
         * Indicates if all of the attributes are selected.
         *
         * @returns {@code true} if all attributes are selected, or {@code false} otherwise
         */
        node.nodeAttributes.hasAllSelected = function (): boolean {
            return _.every(this.attributes, function (attr: any) {
                return attr.selected
            });
        };

        /**
         * Selects the specified attribute.
         *
         * @param attr - the attribute to be selected
         */
        node.nodeAttributes.select = function (attr: any): void {
            attr.selected = true;
            this.selected.push(attr);
            self.validate();
        };

        /**
         * Selects all attributes.
         */
        node.nodeAttributes.selectAll = function (): void {
            let selected: any = [];
            _.each(this.attributes, (attr: any) => {
                attr.selected = true;
                selected.push(attr);
            });
            this.selected = selected;
            self.validate();
        };

        /**
         * Deselects the specified attribute.
         *
         * @param attr - the attribute to be deselected
         */
        node.nodeAttributes.deselect = function (attr: any): void {
            attr.selected = false;
            let idx = this.selected.indexOf(attr);
            if (idx > -1) {
                this.selected.splice(idx, 1);
            }
            self.validate();
        };

        /**
         * Deselects all attributes.
         */
        node.nodeAttributes.deselectAll = function (): void {
            _.each(this.attributes, (attr: any) => {
                attr.selected = false;
            });
            this.selected = [];
            self.validate();
        };
    };

    //
    // Add a new node to the chart.
    //
    onTableClick(table: any) {

        //get attributes for table
        const datasourceId = this.model.$selectedDatasourceId;
        this.getTableSchema(table.schema, table.tableName).then((schemaData: TableSchema) => {
            let nodeName = schemaData.schemaName + "." + schemaData.name;
            this.addDataSetToCanvas(datasourceId, nodeName, schemaData)
        });

    };

    private addDataSetToCanvas(datasourceId: string, nodeName: string, tableSchema: TableSchema, dataset?: SparkDataSet) {
        //
        // Template for a new node.
        //


        const coord = this.getNewXYCoord();

        _.each(tableSchema.fields, (field: SchemaField) => {
            field.selected = true;
            if (this.engine.useNativeDataType) {
                field.dataTypeWithPrecisionAndScale = field.nativeDataType.toLowerCase();
            }
        });
        const newNodeDataModel: any = {
            name: nodeName,
            id: this.nextNodeID++,
            datasourceId: datasourceId,
            dataset: dataset,
            datasetMatchesUserDataSource: dataset == undefined || this.isDataSetUserDataSource(dataset),
            x: coord.x,
            y: coord.y,
            nodeAttributes: {
                attributes: tableSchema.fields,
                reference: [tableSchema.schemaName, tableSchema.name],
                selected: []
            },
            connectors: {
                top: {},
                bottom: {},
                left: {},
                right: {}
            },
            inputConnectors: [
                {
                    name: ""
                }
            ],
            outputConnectors: [
                {
                    name: ""
                }
            ]
        };
        //ensure the dataset is part of the model
        if(dataset){
            if(_.isUndefined(this.model.datasets) || this.model.datasets === null){
                this.model.datasets = [];
            }
            if(this.model.datasets.find(ds => ds.id == dataset.id) == undefined){
                this.model.datasets.push(dataset);
            }
        }
        this.prepareNode(newNodeDataModel);
        this.chartViewModel.addNode(newNodeDataModel);
        this.validate();
    }

    /**
     * Parses the tables on the canvas and returns a SQL string, along with populating the self.selectedColumnsAndTables array of objects.
     *
     * @returns the SQL string or null if multiple data sources are used
     */
    getSQLModel(): string | null {
        let builder = VisualQueryService.sqlBuilder(this.chartViewModel.data, this.engine.sqlDialect);
        let sql = builder.build();

        this.selectedColumnsAndTables = builder.getSelectedColumnsAndTables();
        this.selectedDatasourceIds = builder.getDatasourceIds();
        return sql;
    }

    /**
     * When a connection is edited
     */
    onEditConnectionCallback(response: FlowChart.ConnectionCallbackResponse) {
        this.showConnectionDialog(false, response.connectionViewModel, response.connectionDataModel, response.src, response.dest);
    };

    /**
     * When a connection is created
     */
    onCreateConnectionCallback(response: FlowChart.ConnectionCallbackResponse) {
        // Ensure connection is unique
        let newDestID = response.dest.data.id;
        let newSourceID = response.src.data.id;

        for (let i = 0; i < this.chartViewModel.data.connections.length - 1; ++i) {
            let oldDestID = this.chartViewModel.data.connections[i].dest.nodeID;
            let oldSourceID = this.chartViewModel.data.connections[i].source.nodeID;
            if ((oldDestID === newDestID && oldSourceID === newSourceID) || (oldDestID === newSourceID && oldSourceID === newDestID)) {
                // Delete connection
                this.chartViewModel.deselectAll();
                response.connectionViewModel.select();
                this.chartViewModel.deleteSelected();

                // Display error message
                this._dialogService.openAlert({
                    message: 'There is already a join between those two tables. Please edit the existing join or switch to advanced mode.',
                    viewContainerRef: this.viewContainerRef,
                    title: 'Duplicate join',
                    width: '500px',
                });


                return;
            }
        }

        // Add connection
        this.showConnectionDialog(true, response.connectionViewModel, response.connectionDataModel, response.src, response.dest);
        this.validate();
    };

    /**
     * Called when the current selection is deleted.
     */
    onDeleteSelectedCallback() {
        this.validate();
    };

    showConnectionDialog(isNew: any, connectionViewModel: any, connectionDataModel: any, source: any, dest: any) {
        this.chartViewModel.deselectAll();

        let config: ConnectionDialogConfig = {
            isNew: isNew,
            connectionViewModel: connectionViewModel,
            connectionDataModel: connectionDataModel,
            source: source,
            dest: dest
        };

        return this._dialogService.open(ConnectionDialog, {data: config})
            .afterClosed().subscribe((response: ConnectionDialogResponse) => {
                if(response) {
                    if (response.status == ConnectionDialogResponseStatus.DELETE || isNew && response.status == ConnectionDialogResponseStatus.CANCEL) {
                        connectionViewModel.select();
                        this.chartViewModel.deleteSelected();
                    }
                    else if (response.status == ConnectionDialogResponseStatus.SAVE) {
                        // connectionDataModel = response.connectionDataModel;
                        let viewConnection = this.chartViewModel.findConnection(response.id);
                        viewConnection.data.joinType = response.joinType;
                        viewConnection.data.name = response.connectionName;
                        viewConnection.data.joinKeys.sourceKey = response.source;
                        viewConnection.data.joinKeys.destKey = response.dest;
                    }
                    this.validate()
                }
            })
    }

    /**
     * callback after a user selects a file from the local file system
     */
    onFileUploaded() {
        this.next.emit();
    }

    // -----------------
    // Angular Callbacks
    // -----------------

    /**
     * Cleanup environment when this directive is destroyed.
     */
    ngOnDestroy(): void {
        //cancel subscriptions

        if (this.onCreateConnectionSubscription) {
            this.onCreateConnectionSubscription.unsubscribe();
        }
        if (this.onEditConnectionSubscription) {
            this.onEditConnectionSubscription.unsubscribe();
        }
        if (this.onDeleteConnectionSubscription) {
            this.onDeleteConnectionSubscription.unsubscribe();
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.model && !changes.model.firstChange) {
            // Setup the flowchart Model
            this.setupFlowChartModel();
            this.addPreviewDataSets();

            // Validate when the page loads
            this.validate();
        }
    }

    /**
     * Finish initializing after data-bound properties are initialized.
     */
    ngOnInit(): void {
        //init the form objects
        this.initFormComponents();


        if (this.model.$selectedDatasourceId == null && this.model.datasourceIds && this.model.datasourceIds.length > 0) {
            this.model.$selectedDatasourceId = this.model.datasourceIds[0];
        }

        // Allow for SQL editing
        if (this.model.chartViewModel == null && typeof this.model.sql !== "undefined" && this.model.sql !== null && (_.isUndefined(this.model.sampleFile) || this.model.sampleFile == null)) {
            this.advancedMode = true;
            this.advancedModeText = "Visual Mode";
        } else {
            this.advancedMode = false;
            this.advancedModeText = "Advanced Mode";
        }

        // Wait for query engine to load
        const onLoad = () => {
            // Initialize state
            this.init().subscribe( (datasources:UserDatasource[]) => {
                // Setup the flowchart Model
                this.setupFlowChartModel();

                this.addPreviewDataSets();

                // Validate when the page loads
                this.validate();
            });


        };

        if (this.engine instanceof Promise) {
            this.engine.then(queryEngine => {
                this.engine = queryEngine;
                onLoad();
            });
        } else {
            onLoad();
        }
    }

    /**
     * Search the list of table names.
     */
    onAutocompleteQuerySearch(txt: any): Promise<DatasourcesServiceStatic.TableReference[]> {
        let promise: Promise<DatasourcesServiceStatic.TableReference[]> = null;
        const tables = this.engine.searchTableNames(txt, this.model.$selectedDatasourceId);
        if (tables instanceof Promise) {
            promise = tables.then((tables: any) => {
                this.databaseConnectionError = false;
                return tables;
            }, () => {
                this.databaseConnectionError = true;
                return [];
            });
        }
        else {
            this.databaseConnectionError = false;
            promise = Observable.of(tables).toPromise();
        }
        return promise;

    }

    onAutocompleteRefreshCache() {
        this.hiveService.refreshTableCache();
    }

    openCatalogBrowser(){
        let data = new DatasetPreviewStepperDialogData(true,"Add");
        let dialogConfig:MatDialogConfig = DatasetPreviewStepperDialogComponent.DIALOG_CONFIG()
        dialogConfig.data = data;
        this._dialogService.open(DatasetPreviewStepperDialogComponent,dialogConfig)
            .afterClosed()
            .filter(value => typeof value !== "undefined").subscribe( (response:DatasetPreviewStepperSavedEvent) => {
                //add these to the canvas
            let sparkDataSets =response.previews.map(ds => ds.toSparkDataSet())
            this.addSparkDataSets(sparkDataSets);
            });
    }
}
