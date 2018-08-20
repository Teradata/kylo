import {Input, OnDestroy, OnInit} from "@angular/core";
import * as angular from "angular";
import * as _ from "underscore";

import {FeedDataTransformation} from "../../model/feed-data-transformation";
import {DatasourcesServiceStatic, TableSchema} from "../wrangler";
import {UserDatasource} from "../../model/user-datasource";
import {QueryEngine} from "../wrangler/query-engine";
import {SchemaField} from "../wrangler";
import {PreviewDataSet} from "../../catalog/datasource/preview-schema/model/preview-data-set";
import {SparkDataSet} from "../../model/spark-data-set.model";
import {flowchart} from "./flowchart/flowchart_viewmodel";

import {moduleName} from "../module-name";
import {Observable} from "rxjs/Observable";
import {VisualQueryService} from "../../services/VisualQueryService";
import "../../../codemirror-require/module"

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
export class QueryBuilderComponent implements OnDestroy, OnInit {

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
    availableDatasources: UserDatasource[] = [];

    /**
     * Model for the chart.
     */
    chartViewModel: any = {};

    /**
     * Indicates that there was an error retrieving the list of tables.
     * @type {boolean} true if there was an error or false otherwise
     */
    databaseConnectionError: boolean = false;

    /**
     * Query engine for determining capabilities.
     */
    @Input()
    engine: QueryEngine<any>;

    /**
     * Error message to be displayed.
     */
    error: string;

    /**
     * Height offset from the top of the page.
     */
    heightOffset: string = "0";

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
     * Data transformation model
     */
    @Input()
    model: FeedDataTransformation;

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

    /**
     * Step index (0-based) for this component.
     */
    stepIndex: number;

    /**
     * Step number (1-based) for this component.
     */
    stepNumber: number;

    /**
     * Controller for parent stepper component.
     */
    stepperController: any;





    /**
     * Autocomplete for the table selector.
     */
    tablesAutocomplete: any = {
        clear: this.onAutocompleteClear.bind(this),
        searchText: "",
        selectedTable: null,
        noCache: true,
        querySearch: this.onAutocompleteQuerySearch.bind(this),
        refreshCache: this.onAutocompleteRefreshCache.bind(this),
        delay: 0
    };

    /**
     * List of native data sources to exclude from the model.
     */
    private nativeDataSourceIds: string[] = [];

    private fileDataSource : UserDatasource = {id:"FILE",name:"Local File", description:"Local File",type:"File"}

    static readonly $inject = ["$scope", "$element", "$mdToast", "$mdDialog", "$document", "$timeout","Utils", "RestUrlService", "HiveService", "SideNavService", "StateService", "VisualQueryService", "FeedService",
        "DatasourcesService"]
    /**
     * Constructs a {@code BuildQueryComponent}.
     */
    constructor(private $scope: angular.IScope, $element: angular.IAugmentedJQuery, private $mdToast: angular.material.IToastService, private $mdDialog: angular.material.IDialogService,
                private $document: angular.IDocumentService, private $timeout: angular.ITimeoutService, private Utils: any, private RestUrlService: any, private HiveService: any, private SideNavService: any, private StateService: any,
                private visualQueryService: VisualQueryService, private FeedService: any, private DatasourcesService: any) {
        // Setup initializers
        this.$scope.$on("$destroy", this.ngOnDestroy.bind(this));
        this.initKeyBindings();

        // Setup environment
        this.heightOffset = $element.attr("height-offset");
        this.SideNavService.hideSideNav();
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

    /**
     * Gets the browser height offset for the element with the specified offset from the top of this component.
     */
    getBrowserHeightOffset(elementOffset: number): number {
        return parseInt(this.heightOffset) + elementOffset;
    }

    /**
     * Adds the table to the flowchart.
     */
    onAddTable() {
        this.SideNavService.hideSideNav();
        this.onTableClick(this.tablesAutocomplete.selectedTable);
        this.tablesAutocomplete.clear();
    }

    /**
     * Initialize state from services.
     */
    private init() {
        // Get the list of data sources
        Promise.all([this.engine.getNativeDataSources(), this.DatasourcesService.findAll()])
            .then(resultList => {
                this.nativeDataSourceIds = resultList[0].map((dataSource: UserDatasource): string => dataSource.id);

                const supportedDatasources = resultList[0].concat(resultList[1]).filter(this.engine.supportsDataSource);
                if (supportedDatasources.length > 0) {
                    return supportedDatasources;
                } else {
                    const supportedNames = ((supportedNameList) =>{
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
                this.availableDatasources = datasources;
                //add in the File data source
                this.availableDatasources.push(this.fileDataSource);
                if (this.model.$selectedDatasourceId == null) {
                    this.model.$selectedDatasourceId = datasources[0].id;
                }
                this.validate();
            })
            .catch((err: string) => {
                this.error = err;
            })
            .then(() => {
                this.loadingPage = false;
            });
    }


    /**
     * Initialize the key bindings.
     */
    private initKeyBindings() {
        //
        // Set to true when the ctrl key is down.
        //
        let ctrlDown = false;

        //
        // Event handler for key-down on the flowchart.
        //
        this.$document.bind('keydown', (evt: JQueryKeyEventObject) => {
            if (evt.keyCode === CTRL_KEY_CODE) {
                ctrlDown = true;
                evt.stopPropagation();
                evt.preventDefault();
            }
        });

        //
        // Event handler for key-up on the flowchart.
        //
        this.$document.bind('keyup', (evt: JQueryKeyEventObject) => {
            if (evt.keyCode === DELETE_KEY_CODE) {
                //
                // Delete key.
                //
                this.chartViewModel.deleteSelected();
                this.validate();
            }

            if (evt.keyCode == A_KEY_CODE && ctrlDown) {
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
                ctrlDown = false;

                evt.stopPropagation();
                evt.preventDefault();
            }
        });
    }

    addPreviewDataSets(){
        if(this.model.datasets && this.model.datasets.length >0){
            this.model.datasets.forEach((dataset :SparkDataSet)=> {
                let tableSchema :any = {};
                tableSchema.schemaName = dataset.id;
                tableSchema.name = dataset.id;
                tableSchema.fields = dataset.schema.map(tableColumn => {
                    let field :any= {};
                    field.name = tableColumn.name;
                    field.description = null;
                    field.nativeDataType = tableColumn.dataType;
                    field.derivedDataType = tableColumn.dataType;
                    field.dataTypeWithPrecisionAndScale = tableColumn.dataType;
                    return field;
                });
                let nodeName = tableSchema.name;

                this.addDataSetToCanvas(dataset.dataSource.id,nodeName,tableSchema, dataset);

            });



        }
    }

    /**
     * Initialize the model for the flowchart.
     */
    setupFlowChartModel() {
        // Load data model
        let chartDataModel: any;
        if (this.model.chartViewModel != null) {
            chartDataModel = this.model.chartViewModel;
        } else {
            chartDataModel = {"nodes": [], "connections": []};
        }

        // Prepare nodes
        angular.forEach(chartDataModel.nodes, (node: any) => {
            // Add utility functions
            this.prepareNode(node);

            // Determine next node ID
            this.nextNodeID = Math.max(node.id + 1, this.nextNodeID);
        });

        // Create view model
        this.chartViewModel = new flowchart.ChartViewModel(chartDataModel, this.onCreateConnectionCallback.bind(this), this.onEditConnectionCallback.bind(this),
            this.onDeleteSelectedCallback.bind(this));
    }

    onDatasourceChange(){
        this.tablesAutocomplete.searchText = '';

        if(this.model.$selectedDatasourceId == 'FILE'){
            //warn if the user has other items
            if(this.chartViewModel.nodes != null && (this.chartViewModel.nodes.length >0) ){
                //WARN if you upload a file you will lose your other data
                this.$mdDialog.show(
                    this.$mdDialog.confirm()
                        .parent($("body"))
                        .clickOutsideToClose(true)
                        .title("Upload a local file")
                        .textContent("If you switch and upload a local file you will lose your other data sources. Are you sure you want to continue?")
                        .ariaLabel("Upload local file or stay in visual editor?")
                        .ok("Continue")
                        .cancel("Cancel"))
                        .then(() => {
                            this.chartViewModel.nodes= [];
                            this.model.chartViewModel = null;
                        },() => {
                            this.model.$selectedDatasourceId = this.availableDatasources[0].id;
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
            this.model.datasourceIds = this.nativeDataSourceIds.indexOf(this.model.$selectedDatasourceId) < 0 ? [this.model.$selectedDatasourceId] : [];
            this.model.$datasources = this.DatasourcesService.filterArrayByIds(this.model.$selectedDatasourceId, this.availableDatasources);
        } else if (this.model.$selectedDatasourceId =='FILE'){
          this.isValid = angular.isDefined(this.model.sampleFile);
        } else if (this.chartViewModel.nodes != null) {
            this.isValid = (this.chartViewModel.nodes.length > 0);

            this.model.chartViewModel = this.chartViewModel.data;
            this.model.sql = this.getSQLModel();
            this.model.$selectedColumnsAndTables = this.selectedColumnsAndTables;
            this.model.datasourceIds = this.selectedDatasourceIds.filter(id => this.nativeDataSourceIds.indexOf(id) < 0);
            this.model.$datasources = this.DatasourcesService.filterArrayByIds(this.selectedDatasourceIds, this.availableDatasources);
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
            };
            if (this.chartViewModel.nodes.length > 0) {
                this.$mdDialog.show(
                    this.$mdDialog.confirm()
                        .parent($("body"))
                        .clickOutsideToClose(true)
                        .title("Switch to advanced mode")
                        .textContent("If you switch to the advanced SQL editor then you will no longer be able to return to this visual editor. Are you sure you want to continue?")
                        .ariaLabel("Switch to advanced mode or stay in visual editor?")
                        .ok("Continue")
                        .cancel("Cancel")
                ).then(goAdvanced);
            } else {
                goAdvanced();
            }
        } else {
            this.advancedMode = false;
            this.model.sql = "";
            this.advancedModeText = "Advanced Mode";
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
            angular.forEach(this.attributes, (attr: any) => {
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
            angular.forEach(this.attributes, (attr: any) => {
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
        const nodeName = table.schema + "." + table.tableName;
        this.getTableSchema(table.schema, table.tableName).then((schemaData: TableSchema) => {
            let nodeName = schemaData.schemaName + "." + schemaData.name;
            this.addDataSetToCanvas(datasourceId, nodeName,schemaData)
        });

    };


    private addDataSetToCanvas(datasourceId:string,nodeName:string,tableSchema:TableSchema, dataset?:SparkDataSet){
        //
        // Template for a new node.
        //

        const coord = this.getNewXYCoord();

        angular.forEach(tableSchema.fields,  (field: SchemaField) =>{
            field.selected = true;
            if (this.engine.useNativeDataType) {
                field.dataTypeWithPrecisionAndScale = field.nativeDataType.toLowerCase();
            }
        });
        const newNodeDataModel: any = {
            name: nodeName,
            id: this.nextNodeID++,
            datasourceId: datasourceId,
            dataset:dataset,
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
    onEditConnectionCallback(connectionViewModel: any, connectionDataModel: any, dest: any, source: any) {
        this.showConnectionDialog(false, connectionViewModel, connectionDataModel, source, dest);
    };

    /**
     * When a connection is created
     */
    onCreateConnectionCallback(connectionViewModel: any, connectionDataModel: any, dest: any, source: any, inputConnection: any, outputConnection: any) {
        // Ensure connection is unique
        let newDestID = dest.data.id;
        let newSourceID = source.data.id;

        for (let i = 0; i < this.chartViewModel.data.connections.length - 1; ++i) {
            let oldDestID = this.chartViewModel.data.connections[i].dest.nodeID;
            let oldSourceID = this.chartViewModel.data.connections[i].source.nodeID;
            if ((oldDestID === newDestID && oldSourceID === newSourceID) || (oldDestID === newSourceID && oldSourceID === newDestID)) {
                // Delete connection
                this.chartViewModel.deselectAll();
                connectionViewModel.select();
                this.chartViewModel.deleteSelected();

                // Display error message
                let alert = this.$mdDialog.alert()
                    .parent($('body'))
                    .clickOutsideToClose(true)
                    .title("Duplicate join")
                    .textContent("There is already a join between those two tables. Please edit the existing join or switch to advanced mode.")
                    .ariaLabel("joins must be unique")
                    .ok("Got it!");
                this.$mdDialog.show(alert);
                return;
            }
        }

        // Add connection
        this.showConnectionDialog(true, connectionViewModel, connectionDataModel, source, dest);
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
        this.$mdDialog.show({
            controller: 'ConnectionDialogController',
            templateUrl: 'js/feed-mgr/visual-query/build-query/connection-dialog/connection-dialog-ng1.component.html',
            parent: angular.element(document.body),
            clickOutsideToClose: false,
            fullscreen: true,
            locals: {
                isNew: isNew,
                connectionDataModel: connectionDataModel,
                source: source,
                dest: dest
            }
        })
            .then( (msg: any) => {
                if (msg === "delete" || (isNew && msg === "cancel")) {
                    connectionViewModel.select();
                    this.chartViewModel.deleteSelected();
                }
                this.validate();
            });
    };

    /**
     * callback after a user selects a file from the local file system
     */
    onFileUploaded(){
        let step = this.stepperController.getStep(this.stepIndex)
        if(step) {
            this.$timeout(() => {
                this.stepperController.selectedStepIndex = step.nextActiveStepIndex;
            },10)
        }
    }

    // -----------------
    // Angular Callbacks
    // -----------------

    /**
     * Cleanup environment when this directive is destroyed.
     */
    ngOnDestroy(): void {
        this.SideNavService.showSideNav();
        this.$document.unbind("keydown");
        this.$document.unbind("keypress");
        this.$document.unbind("keyup");
    }

    /**
     * Finish initializing after data-bound properties are initialized.
     */
    ngOnInit(): void {
        // Initialize properties dependent on data-bound properties
        this.stepNumber = this.stepIndex + 1;

        if (this.model.$selectedDatasourceId == null && this.model.datasourceIds && this.model.datasourceIds.length > 0) {
            this.model.$selectedDatasourceId = this.model.datasourceIds[0];
        }

        // Allow for SQL editing
        if (this.model.chartViewModel == null && typeof this.model.sql !== "undefined" && this.model.sql !== null && (angular.isUndefined(this.model.sampleFile) || this.model.sampleFile == null)) {
            this.advancedMode = true;
            this.advancedModeText = "Visual Mode";
        } else {
            this.advancedMode = false;
            this.advancedModeText = "Advanced Mode";
        }

        // Wait for query engine to load
        const onLoad = () => {
            // Initialize state
            this.init();

            // Setup the flowchart Model
            this.setupFlowChartModel();

            this.addPreviewDataSets();

            // Validate when the page loads
            this.validate();
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
     * Finish initializing after data-bound properties are initialized.
     */
    $onInit(): void {
        this.ngOnInit();
    }

    // ----------------------
    // Autocomplete Callbacks
    // ----------------------

    onAutocompleteClear() {
        this.tablesAutocomplete.searchText = '';
        this.tablesAutocomplete.selectedTable = null;
    }

    /**
     * Search the list of table names.
     */
    onAutocompleteQuerySearch(txt: any) {
        const tables = this.engine.searchTableNames(txt, this.model.$selectedDatasourceId);
        if (tables instanceof Promise) {
            return tables.then( (tables: any) => {
                this.databaseConnectionError = false;
                return tables;
            }, () => {
                this.databaseConnectionError = true;
                return [];
            });
        } else {
            return tables;
        }
    }

    onAutocompleteRefreshCache() {
        const successFn = () => {
            let searchText = this.tablesAutocomplete.searchText.trim();
            angular.element('#tables-auto-complete').focus().val(searchText).trigger('change')
        };
        const errorFn = () => {
        };
        this.HiveService.refreshTableCache().then(successFn, errorFn);
    }
}

angular.module(moduleName).component("thinkbigVisualQueryBuilder", {
    bindings: {
        engine: "=",
        heightOffset: "@",
        model: "=",
        stepIndex: "@"
    },
    controller: QueryBuilderComponent,
    controllerAs: "$bq",
    require: {
        stepperController: "^thinkbigStepper"
    },
    templateUrl: "js/feed-mgr/visual-query/build-query/build-query.component.html"
});
