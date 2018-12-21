import {AfterViewInit, Component, EventEmitter, Inject, Input, OnChanges, OnInit, Output, SimpleChanges, TemplateRef, ViewChild, ViewContainerRef, ViewEncapsulation} from "@angular/core";
import {TdDialogService} from "@covalent/core/dialogs";
import {CodemirrorComponent} from "ng2-codemirror";
import {Subject} from "rxjs/Subject";
import * as _ from "underscore";

import StepperService from '../../../common/stepper/StepperService';
import {CloneUtil} from "../../../common/utils/clone-util";
import {BroadcastService} from '../../../services/broadcast-service';
import {WindowUnloadService} from "../../../services/WindowUnloadService";
import {DefaultFeedDataTransformation, FeedDataTransformation} from "../../model/feed-data-transformation";
import {Feed} from "../../model/feed/feed.model";
import {TableColumnDefinition} from "../../model/TableColumnDefinition"
import {DomainType, DomainTypesService} from "../../services/DomainTypesService";
import {FeedService} from '../../services/FeedService';
import {
    ApplyDomainTypesDialogComponent,
    ApplyDomainTypesResponse,
    ApplyDomainTypesResponseStatus,
    ApplyDomainTypesRow
} from "../../shared/domain-type/apply-domain-types/apply-domain-types-dialog.component";
import {ColumnProfile, ColumnProfileHelper} from "../wrangler/api/column-profile";
import {ColumnController} from "../wrangler/column-controller";
import {ChainedOperation, ColumnDelegate, DataCategory} from "../wrangler/column-delegate";
import {ProfileOutputRow, ScriptState} from "../wrangler/index";
import {TransformValidationResult} from "../wrangler/model/transform-validation-result";
import {PageSpec, QueryEngine} from "../wrangler/query-engine";
import {AnalyzeColumnDialog} from "./main-dialogs/analyze-column-dialog";
import {VisualQueryProfileStatsController} from "./main-dialogs/VisualQueryProfileStats";
import {WranglerDataService} from "./services/wrangler-data.service";
import {VisualQueryTable} from "./visual-query-table/visual-query-table.component";
import {QuickColumnsDialog, QuickColumnsDialogData} from "./main-dialogs/quick-columns-dialog";
import {ColumnUtil} from "../wrangler/core/column-util";
import {ColumnItem, SchemaLayoutDialog, SchemaLayoutDialogData} from "./main-dialogs/schema-layout-dialog";
import {QuickCleanDialog, QuickCleanDialogData} from "./main-dialogs/quick-clean-dialog";
import {SampleDialog, SampleDialogData} from "./main-dialogs/sample-dialog";
import {TableFieldPolicy} from "../../model/TableFieldPolicy";
import {StringUtils} from "../../../common/utils/StringUtils";
import {DatasetPreviewStepperDialogComponent, DatasetPreviewStepperDialogData} from "../../catalog-dataset-preview/preview-stepper/dataset-preview-stepper-dialog.component";
import {MatDialogConfig} from "@angular/material";
import {DatasetPreviewStepperSavedEvent} from "../../catalog-dataset-preview/preview-stepper/dataset-preview-stepper.component";
import {JoinData, JoinPreviewStepperStep} from "./dataset-join-dialog/join-preview-stepper-step";
import {JoinPreviewStepData} from "./dataset-join-dialog/join-preview-step-data";
import {ColumnRef, ResTarget, SqlBuilderUtil, VisualQueryService} from "../../services/VisualQueryService";
import {DATASET_PROVIDER, SparkQueryParser} from "../services/spark/spark-query-parser";
import {CatalogService} from "../../catalog/api/services/catalog.service";
import {SparkDataSet} from "../../model/spark-data-set.model";
import {SparkConstants} from "../services/spark/spark-constants";
import {JoinDataset} from "../wrangler/model/join-dataset.model";
import {ObjectUtils} from "../../../../lib/common/utils/object-utils";
import {InlineJoinScriptBuilder} from "./dataset-join-dialog/inline-join-script-builder";

declare const CodeMirror: any;

export class WranglerColumn {

    dataType: string = null;
    delegate: ColumnDelegate = null;
    displayName: string = null;
    domainTypeId: string = null;
    filters: any[] = null;
    headerTooltip: string = null;
    longestValue: any = null;
    name: string = null;

    public constructor(init?: Partial<WranglerColumn>) {
        Object.assign(this, init);
    }
}

/**
 * Transform Data step of the Visual Query page.
 */
@Component({
    encapsulation: ViewEncapsulation.None,
    selector: "thinkbig-visual-query-transform",
    styleUrls: ["./transform-data.component.scss"],
    templateUrl: "./transform-data.component.html"
})
export class TransformDataComponent implements AfterViewInit, ColumnController, OnChanges, OnInit {

    /**
     * Indicates if this is the active step
     */
    @Input()
    active: boolean = true;

    /**
     * Query engine for transforming data.
     */
    @Input()
    engine: QueryEngine<any>;

    /**
     * Feed model
     */
    @Input()
    feedModel: Feed;

    /**
     * Data transformation model
     */
    @Input()
    model: FeedDataTransformation;

    /**
     * Label for the "NEXT" button
     */
    @Input()
    nextButton: string;

    /**
     * Should NEXT button be disabled?
     * (true if feed is being saved)
     */
    @Input()
    disableNextButton: boolean;

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
     * Event emitted to indicate query failure
     */
    @Output()
    queryFailure = new EventEmitter<void>();

    //Flag to determine if we can move on to the next step
    isValid: boolean = false;

    //The SQL String from the previous step
    sql: string;
    //The sql model passed over from the previous step
    sqlModel: any;
    //The array of columns with their respective Table, schema and alias passed over from the previous step
    //{column:'name', alias:'alias',tableName:'table name',tableColumn:'alias_name',dataType:'dataType'}
    selectedColumnsAndTables: any;
    //Function History
    functionHistory: object[] = [];

    //The current formula string
    currentFormula: string = '';

    //flag to indicate if the Hive data is available
    hiveDataLoaded: boolean = false;
    //flag to indicate codemirror is ready
    codemirroLoaded: boolean = false;
    //the codemirror editor
    codemirrorEditor: any = null;
    //the tern server reference
    ternServer: any = null;

    //Flag to show/hide function history panel
    isShowFunctionHistory: boolean = false;

    // Flag to show/hide sample menu
    isShowSampleMenu: boolean = false;

    //noinspection JSUnusedGlobalSymbols
    /**
     * Columns for the results table.
     * @type {Array.<Object>}
     */
    tableColumns: WranglerColumn[];

    validationResults: TransformValidationResult[][];

    //noinspection JSUnusedGlobalSymbols
    /**
     * Configuration for the results table.
     * @type {Object}
     */
    tableOptions = {
        headerFont: "500 13px Roboto, 'Helvetica Neue', sans-serif",
        rowFont: "regular 13px Roboto, 'Helvetica Neue', sans-serif"
    };

    //noinspection JSUnusedGlobalSymbols
    /**
     * Rows for the results table.
     * @type {Array.<Object>}
     */
    tableRows: string[][];

    /**
     * History state token for client to track state changes
     */
    tableState: number;

    /**
     * Last page requested
     */
    currentPage = PageSpec.defaultPage();

    /**
     * Rows analyzed by the server
     */
    actualRows: number = null;

    /**
     * Cols analyzed by the server
     */
    actualCols: number = null;

    //Code Mirror options.  Tern Server requires it be in javascript mode
    codeMirrorConfig: object = {
        onLoad: this.codemirrorLoaded.bind(this)
    };
    codemirrorOptions: object = {
        lineWrapping: true,
        indentWithTabs: false,
        smartIndent: false,
        lineNumbers: false,
        matchBrackets: false,
        mode: 'javascript',
        scrollbarStyle: null
    };

    // Progress of transformation from 0 to 100
    queryProgress: number = 0;

    executingQuery: boolean = false;

    /*
    Active query will be followed by an immediate query
     */
    chainedOperation: ChainedOperation = new ChainedOperation();

    gridApi: any;

    @Input()
    stepIndex: any;

    /**
     * Method for limiting the number of results.
     */
    sampleMethod: string = "LIMIT";

    /**
     * List of sample formulas.
     */
    sampleFormulas: { name: string, formula: string }[] = [];

    /**
     * List of available domain types.
     */
    domainTypes: DomainType[] = [];

    /**
     * List of field policies for current transformation.
     */
    @Input()
    fieldPolicies: any[];

    /**
     * Height offset from the top of the page.
     */
    @Input()
    heightOffset: number;

    @Input()
    warnWhenLeaving?: boolean = true;

    /**
     * Validation results for current transformation.
     */
    tableValidation: TransformValidationResult[][];

    /**
     * Whether we've completed initial load?
     * @type {boolean}
     */
    isLoaded: boolean = false;

    /**
     * Keeps track of running executions
     */
    executionStack: Promise<any>[] = [];

    @ViewChild(CodemirrorComponent)
    codeMirrorView: CodemirrorComponent;

    @ViewChild(VisualQueryTable)
    visualQueryTable: VisualQueryTable;

    @ViewChild("joinDataSetStep")
    joinDatasetPreviewStepComponent:JoinPreviewStepperStep;

    /**
     * Constructs a {@code TransformDataComponent}.
     */
    constructor(private $mdDialog: TdDialogService, @Inject("DomainTypesService") private domainTypesService: DomainTypesService,
                @Inject("RestUrlService") private RestUrlService: any, @Inject("SideNavService") private SideNavService: any, @Inject("uiGridConstants") private uiGridConstants: any,
                @Inject("FeedService") private feedService: FeedService, @Inject("BroadcastService") private broadcastService: BroadcastService,
                @Inject("StepperService") private stepperService: StepperService, @Inject("WindowUnloadService") private WindowUnloadService: WindowUnloadService,
                @Inject("VisualQueryService") private visualQueryService: VisualQueryService,
                private wranglerDataService: WranglerDataService,
                private catalogService:CatalogService,
                private viewContainerRef: ViewContainerRef
                ) {
        //Hide the left side nav bar
        this.SideNavService.hideSideNav();
    }

    ngAfterViewInit(): void {
        this.codemirrorLoaded(this.codeMirrorView.instance);
    }

    ngOnInit(): void {
        if (this.warnWhenLeaving) {
            // Display prompt on window unload
            this.WindowUnloadService.setText("You will lose any unsaved changes. Are you sure you want to continue?");

        }
        //convert the model to a default model
        this.model = ObjectUtils.getAs(this.model,DefaultFeedDataTransformation)

        this.sql = this.model.sql;
        this.sqlModel = this.model.chartViewModel;
        this.selectedColumnsAndTables = this.model.$selectedColumnsAndTables;
        //reset sample file changed flag
        this.model.sampleFileChanged = false;

        // Select source model
        let useSqlModel = false;
        if (this.sqlModel !== null && typeof this.sqlModel === "object") {
            // Check for non-Hive datasource
            if (Array.isArray(this.model.datasourceIds) && this.model.datasourceIds.length > 0) {
                useSqlModel = (this.model.datasourceIds.length > 1 || (typeof this.model.datasourceIds[0] !== "undefined" && this.model.datasourceIds[0] !== "HIVE"));  // TODO remove "HIVE"
            }
            if (this.model.datasets && this.model.datasets.length > 0) {
                //use the node model if we are using anything but Hive.
                //only Hive is able to use the adv SQL model

                useSqlModel = this.model.datasets.find(ds => {
                    return ds.dataSource.connector.pluginId != "hive";
                }) != undefined;
            }
            if (!useSqlModel) {
                useSqlModel = _.chain(this.sqlModel.nodes)
                    .map(_.property("nodeAttributes"))
                    .map(_.property("attributes"))
                    .flatten(true)
                    .some(function (attr: any) {
                        return (attr.selected && attr.description !== null);
                    })
                    .value();
            }
        }

        let source = useSqlModel ? this.sqlModel : this.sql;

        // if we dont have any datasources, default it to Hive data source
        if (this.model.$datasources == null || this.model.$datasources.length == 0) {
            this.model.$datasources = [{id: "HIVE", name: "Hive"}];  // TODO remove "HIVE"
        }

        // Wait for query engine to load
        const onLoad = () => {
            this.hiveDataLoaded = false;
            let domainTypesLoaded = false;
            this.sampleFormulas = this.engine.sampleFormulas;

            if (this.model.datasets) {
                this.engine.setDatasets(this.model.datasets)
            }
            if (Array.isArray(this.model.states) && this.model.states.length > 0) {
                this.engine.setQuery(source, this.model.$datasources, this.model.$catalogDataSources);
                this.engine.setState(this.model.states);
                this.functionHistory = this.engine.getHistory();
            } else {
                this.engine.setQuery(source, this.model.$datasources, this.model.$catalogDataSources);
                this.functionHistory = this.engine.getHistory();
            }

            // Provide access to table for fetching pages
            this.wranglerDataService.asyncQuery = this.queryOrGetState.bind(this);

            // Watch for changes to field policies
            if (this.fieldPolicies == null) {
                this.fieldPolicies = [];
            }
            this.engine.setFieldPolicies(this.fieldPolicies);

            // Fetch domain types
            this.domainTypesService.findAll()
                .then((domainTypes: any) => {
                    this.domainTypes = domainTypes;
                    domainTypesLoaded = true;
                });

            // Indicate ready
            this.updateTableState();
            this.tableColumns = [];

            // Initial load will trigger query from the table model.
            if (this.isLoaded) {
                this.query().catch((result) => {
                    console.debug("Error executing query!");
                });
            }
            this.isLoaded = true;
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

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.active && (!changes.active.firstChange || changes.active.currentValue)) {
            if (typeof this.model === "object" && this.sql !== this.model.sql) {
                this.isValid = false;
                this.sql = null;
            }
            this.onStepChange();
        } else if (changes.fieldPolicies) {
            this.fieldPolicies.forEach(policy => {
                if (policy.name == null) {
                    policy.name = policy.fieldName;
                }
                if (policy.fieldName !== policy.name) {
                    policy.fieldName = policy.name;
                }
                if (policy.feedFieldName !== policy.name) {
                    policy.feedFieldName = policy.name;
                }
            });
            this.engine.setFieldPolicies(this.fieldPolicies);
        }
    }

    /**
     * Detects and applies domain types to all columns.
     */
    applyDomainTypes(cols: WranglerColumn[], rows: string[][]): void {
        // Detect domain types
        var flgChanged: boolean = false;
        var domainTypes: DomainType[] = [];
        var fields: WranglerColumn[] = [];
        var colIndexes: number[] = [];
        let fieldPolicies = this.engine.getFieldPolicies();
        cols.forEach((field: WranglerColumn, index: number) => {
            var domainType = this.domainTypesService.detectDomainType({
                name: field.name, sampleValues: rows.map((value) => {
                    return value[index];
                })
            }, this.domainTypes);
            if (domainType !== null) {
                domainTypes.push(domainType);
                fields.push(field);
                colIndexes.push(index);
            }
        });


        // Get user confirmation for domain type changes to field data types
        if (fields.length > 0) {
            this.$mdDialog.open(ApplyDomainTypesDialogComponent, {data: {domainTypes: domainTypes, fields: fields}, panelClass: "full-screen-dialog", disableClose:true, width:"80%"})
                .afterClosed().subscribe((response: ApplyDomainTypesResponse) => {
                    if(response.status ==ApplyDomainTypesResponseStatus.APPLY ) {
                        let selected = response.appliedRows;
                        selected.forEach((selection: ApplyDomainTypesRow) => {
                            var fieldIndex = fields.findIndex((element: WranglerColumn) => {
                                return element.name === selection.name;
                            });
                            this.setDomainType(colIndexes[fieldIndex], domainTypes[fieldIndex].id);
                            flgChanged = true;
                        });
                        if (flgChanged) {
                            this.resample();
                        }
                    }
            }, () => {
                // ignore cancel
            });
        }
    }

    /**
     * Join the dataframe to another dataset
     */
    inlineJoin(){
        let data = new DatasetPreviewStepperDialogData(false,"Add");
        data.additionalSteps = [];
        let step =  this.joinDatasetPreviewStepComponent;
        //update the data
        step.data.cols = this.engine.getColumns();
        data.additionalSteps.push(step);
        step.setAdditionalStepIndex(data.additionalSteps.length-1);
        let dialogConfig:MatDialogConfig = DatasetPreviewStepperDialogComponent.DIALOG_CONFIG()
        dialogConfig.data = data;
        dialogConfig.viewContainerRef = this.viewContainerRef;
        this.$mdDialog.open(DatasetPreviewStepperDialogComponent,dialogConfig)
            .afterClosed()
            .filter(value => typeof value !== "undefined").subscribe( (response:DatasetPreviewStepperSavedEvent) => {
            let joinData = response.data as JoinData;
               //convert to spark dataset
            let dataset = joinData.joinDataSet.toSparkDataSet();
               //ensure the dataset is registered
            this.catalogService.ensureDataSetId(dataset).subscribe((ds:SparkDataSet) => {
                //join
                joinData.ds = ds;
                let joinScriptBuilder = new InlineJoinScriptBuilder(this.visualQueryService,this.model,this.engine.getColumns(),joinData);
                let joinDataSet:JoinDataset = joinScriptBuilder.build();

                // the formula is empty since its crafted from the "joinData" that is passed to the context
                let formula = "";
                //if successful register and add it to the list
                if(this.model.datasets == undefined){
                    this.model.datasets = [];
                }
                this.model.datasets.push(ds);


                this.pushFormula(formula, {formula: formula, icon: 'link', name: 'Join '+joinData.joinDataSet.displayKey+" on "+joinDataSet.dfField+ "= "+joinDataSet.joinField, joinDataSet:joinDataSet}, true, true)
                    .then( () => {

                    }, () => {
                        //remove it
                        delete this.model.inlineJoinDataSets[joinDataSet.datasetId];
                    })

            })

        });
    }

    /**
     * Show and hide the Function History
     */
    toggleFunctionHistory() {
        this.isShowFunctionHistory = !this.isShowFunctionHistory;
    };

    /**
     * Toggle the visibility of the sample menu.
     */
    toggleSampleMenu() {
        this.isShowSampleMenu = !this.isShowSampleMenu;
    };

    /**
     * Displays the column statistics dialog.
     */
    showProfileDialog() {
        this.$mdDialog.open(VisualQueryProfileStatsController, {data: CloneUtil.deepCopy(this.engine.getProfile()), panelClass: "full-screen-dialog"});
    };

    //Callback when Codemirror has been loaded (reference is in the html page at:
    // ui-codemirror="{ onLoad : vm.codemirrorLoaded }"
    codemirrorLoaded(_editor: any) {
        //assign the editor to a variable on this object for future reference
        this.codemirrorEditor = _editor;
        //Set the width,height of the editor. Code mirror needs an explicit width/height
        _editor.setSize(585, 25);
        _editor.on("focus", () => _editor.setSize(585, "auto"));
        _editor.on("blur", () => _editor.setSize(585, 25));

        //disable users ability to add new lines.  The Formula bar is only 1 line
        _editor.on("beforeChange", (instance: any, change: any) => {
            let newtext = change.text.join("").replace(/\n/g, ""); // remove ALL \n !
            change.update(change.from, change.to, [newtext]);
            return true;
        });

        //hide the scrollbar
        _editor.on("change", (instance: any, change: any) => {
            //$(".CodeMirror-hscrollbar").css('display', 'none');
        });
        //set the flag to be loaded and then call out to update Autocomplete options
        this.codemirroLoaded = true;
        this.updateCodeMirrorAutoComplete();
    };

    /**
     * Creates a Tern server.
     */
    private createTernServer() {
        this.engine.getTernjsDefinitions().then((response: any) => {
            this.engine.setFunctionDefs(response);

            this.ternServer = new CodeMirror.TernServer({defs: [response]});
            this.ternServer.server.addDefs(this.engine.getColumnDefs());

            const _editor = this.codemirrorEditor;
            _editor.setOption("extraKeys", {
                "Ctrl-Space": (cm: any) => {
                    this.ternServer.complete(cm);
                },
                "Ctrl-I": (cm: any) => {
                    this.ternServer.showType(cm);
                },
                "Ctrl-O": (cm: any) => {
                    this.ternServer.showDocs(cm);
                },
                "Alt-.": (cm: any) => {
                    this.ternServer.jumpToDef(cm);
                },
                "Alt-,": (cm: any) => {
                    this.ternServer.jumpBack(cm);
                },
                "Ctrl-Q": (cm: any) => {
                    this.ternServer.rename(cm);
                },
                "Ctrl-.": (cm: any) => {
                    this.ternServer.selectName(cm);
                },
                "Tab": () => {
                    this.selectNextTabStop();
                }
            });
            _editor.on("blur", () => {
                this.ternServer.hideDoc();
            });
            _editor.on("cursorActivity", this.showHint.bind(this));
            _editor.on("focus", this.showHint.bind(this));
            _editor.focus();
        });
    }

    /**
     * Setup the CodeMirror and Tern Server autocomplete. This will only execute when both Hive and Code Mirror are fully
     * initialized.
     */
    updateCodeMirrorAutoComplete() {
        if (this.codemirroLoaded && this.hiveDataLoaded) {
            if (this.ternServer === null) {
                this.createTernServer();
            } else {
                const defs = this.engine.getColumnDefs();
                this.ternServer.server.deleteDefs(defs["!name"]);
                this.ternServer.server.addDefs(defs);
            }
        }
    };

    /**
     * Makes an asynchronous request to get the list of completions available at the cursor.
     *
     * @param {CodeMirror|CodeMirror.Doc} cm the code mirror instance
     * @param {Function} callback the callback function
     */
    getHint(cm: any, callback: any): void {
        this.ternServer.getHint(cm, (data: any) => {
            // Complete function calls so arg hints can be displayed
            CodeMirror.on(data, "pick", (completion: any) => {
                if (completion.data.type.substr(0, 3) === "fn(") {
                    let cursor = cm.getCursor();
                    cm.replaceRange("(", cursor, cursor, "complete");
                }
            });

            // Hide when the only completion exactly matches the current text
            if (data.list.length === 1 && (data.to.ch - data.from.ch) === data.list[0].text.length) {
                data.list = [];
            }

            // Display hints
            callback(data);
        });
    };

    /**
     * Shows either argument hints or identifier hints depending on the context.
     *
     * @param {CodeMirror|CodeMirror.Doc} cm the code mirror instance
     */
    showHint(cm: any) {
        // Show args if in a function
        let cursor = cm.getCursor();
        let token = cm.getTokenAt(cursor);
        let lexer = token.state.lexical;

        if (lexer.info === "call" && token.type !== "variable") {
            this.ternServer.updateArgHints(cm);
        } else {
            this.ternServer.hideDoc();
        }

        // Show completions if available
        const self = this;

        function hint(cm: any, callback: any) {
            self.getHint(cm, callback);
        }

        (hint as any).async = true;

        if (cursor.ch === 0 || token.type === "variable" || (token.string === "." && (lexer.type === "stat" || lexer.type === ")"))) {
            cm.showHint({
                completeSingle: false,
                hint: hint
            });
        }
    };

    /**
     * Display error dialog
     */
    showError(message: string): void {
        this.$mdDialog.openAlert({
            closeButton: "Ok",
            message: message,
            title: "Transform Exception"
        });
    }

    /**
     * Executes query if state changed, otherwise returns the current state
     */
    queryOrGetState(pageSpec: PageSpec): Promise<ScriptState<any>> {
        const deferred = new Subject<ScriptState<any>>();
        if (pageSpec.equals(this.currentPage)) {
            setTimeout(() => {
                // Fetch the state or join with the existing execution
                if (this.executionStack.length > 0) {
                    var promise = this.executionStack[this.executionStack.length - 1];
                    promise.then(() => {
                        deferred.next(this.engine.getState());
                        deferred.complete();
                    });
                } else {
                    deferred.next(this.engine.getState());
                    deferred.complete();
                }

            }, 10);
        } else {
            this.query(true, pageSpec).then(() => {
                this.currentPage = pageSpec;
                deferred.next(this.engine.getState());
                deferred.complete();
            }).catch((reason) => {
                deferred.error(reason);
            });
        }
        return deferred.toPromise();
    }

    /**
     * Query Hive using the query from the previous step. Set the Grids rows and columns.
     *
     * @return {Promise} a promise for when the query completes
     */
    query(refresh: boolean = true, pageSpec ?: PageSpec, doValidate: boolean = true, doProfile: boolean = false): Promise<any> {
        const deferred = new Subject<any>();

        const promise = deferred.toPromise();
        this.executionStack.push(promise);

        //flag to indicate query is running
        this.setExecutingQuery(true);
        this.setQueryProgress(50);

        // Query Spark shell service
        let didUpdateColumns = false;

        const successCallback = () => {
            //mark the query as finished
            this.setQueryProgress(100);
            this.setExecutingQuery(false);

            this.isValid = true;

            //store the result for use in the commands
            if (refresh) {
                // Clear previous filters
                if (typeof(this.gridApi) !== "undefined") {
                    this.gridApi.core.clearAllFilters();
                }
                this.updateGrid();
            }
            this.removeExecution(promise);
            deferred.complete();
        };
        const errorCallback = (message: string) => {
            console.debug("Query error!");
            this.queryFailure.emit();
            this.setExecutingQuery(false);
            this.resetAllProgress();

            // Reset state
            if (this.engine.canUndo()) {
                this.onUndo();
            }
            this.removeExecution(promise);
            this.hiveDataLoaded = false;
            deferred.error(message);
            this.back.emit();
        };
        const notifyCallback = (progress: number) => {
            //self.setQueryProgress(progress * 100);
            /*
            if (self.engine.getColumns() !== null && !didUpdateColumns && self.ternServer !== null) {
                didUpdateColumns = true;
            }
            */
        };

        this.engine.transform(pageSpec, doValidate, doProfile).subscribe(notifyCallback, errorCallback, successCallback);
        return promise;
    };


    /**
     * Attempt to extract the error string from the verbose message
     */
    private cleanError(message: string): string {
        if (message != null && message.startsWith("AnalysisException: ")) {
            let idx = message.indexOf(";;");
            if (idx > -1) {
                message = message.substr(19, 1).toUpperCase() + message.substr(20, idx - 20);
            }
        }
        return message;
    }

    private removeExecution(promise: Promise<any>): void {
        var idx = this.executionStack.indexOf(promise);
        this.executionStack.splice(idx, 1);
    }

    private updateGrid() {

        //transform the result to the agGrid model
        let columns: WranglerColumn[] = [];
        let fieldPolicies = this.engine.getFieldPolicies();
        let profile = this.engine.getProfile();

        const engineColumns = this.engine.getColumns();
        if (engineColumns != null) {
            engineColumns.forEach((col, index) => {
                const delegate = this.engine.createColumnDelegate(col.dataType, this, col);
                const fieldPolicy = (fieldPolicies != null && col.index < fieldPolicies.length) ? fieldPolicies[index] : null;
                const longestValue = _.find(profile, (row: any) => {
                    return (row.columnName === col.displayName && (row.metricType === "LONGEST_STRING" || row.metricType === "MAX"))
                });

                columns.push(new WranglerColumn({
                    dataType: col.dataType,
                    delegate: delegate,
                    displayName: col.displayName,
                    domainTypeId: fieldPolicy ? fieldPolicy.domainTypeId : null,
                    filters: delegate.filters,
                    headerTooltip: col.hiveColumnLabel,
                    longestValue: (typeof longestValue !== "undefined" && longestValue !== null) ? longestValue.metricValue : null,
                    name: this.engine.getColumnName(col)
                }));
            });
        }

        //update the ag-grid
        this.updateTableState();
        this.actualCols = this.engine.getActualCols();
        this.actualRows = this.engine.getActualRows();
        this.tableRows = this.engine.getRows();
        this.tableColumns = columns;
        this.tableValidation = this.engine.getValidationResults();

        //mark the flag to indicate Hive is loaded
        // Only apply domain types on initial resultset
        if (!this.hiveDataLoaded) {
            this.applyDomainTypes(this.tableColumns, this.tableRows);
            this.hiveDataLoaded = true;
        }

        this.updateSortIcon();
        this.updateCodeMirrorAutoComplete();
        this.checkWarnDuplicateColumnName();

    }

    checkWarnDuplicateColumnName(): void {
        let cols = this.tableColumns.map((v: WranglerColumn) => {
            return v.displayName;
        }).slice().sort();
        var duplicateCols = [];
        for (var i = 0; i < cols.length - 1; i++) {
            if (cols[i + 1] == cols[i]) {
                duplicateCols.push(cols[i]);
            }
        }
        if (duplicateCols.length > 0) {
            this.onUndo();
            this.$mdDialog.openAlert({
                closeButton: "Ok",
                message: "The last operation created duplicate columns for: " + duplicateCols.join(",") + ". Please try again and alias the new column.",
                title: "Warning"
            });
        }
    }

    addColumnSort(direction: string, column: any, query?: boolean): Promise<any> {
        let formula;

        let directionLower = (typeof direction !== "undefined") ? direction.toLowerCase() : '';
        let icon = ''
        if (directionLower == 'asc') {
            formula = "sort(asc(\"" + column.headerTooltip + "\"))";
            icon = 'arrow_drop_up';
        }
        else if (directionLower == 'desc') {
            formula = "sort(desc(\"" + column.headerTooltip + "\"))";
            icon = 'arrow_drop_down';
        }
        if (formula) {
            this.wranglerDataService.sortDirection_ = <"desc" | "asc">  directionLower;
            this.wranglerDataService.sortIndex_ = column.index;

            let name = "Sort by " + column.displayName + " " + directionLower;
            return this.pushFormula(formula, {formula: formula, icon: icon, name: name, sort: {direction: directionLower, columnIndex: column.index, columnName: column.field}}, query);
        }
        else {
            return new Promise<any>(resolve => resolve({}));
        }
    }

    /**
     * Add formula for a column filter.
     *
     * @param {Object} filter the filter
     * @param {ui.grid.GridColumn} column the column
     */
    addColumnFilter(filter: any, column: any, query ?: boolean): Promise<any> {
        // Generate formula for filter
        let formula;
        let safeTerm = (column.delegate.dataCategory === DataCategory.NUMERIC) ? filter.term : "'" + StringUtils.quote(filter.term) + "'";
        let verb;

        switch (filter.condition) {
            case this.uiGridConstants.filter.LESS_THAN:
                formula = "filter(lessThan(" + column.field + ", " + safeTerm + "))";
                verb = "less than";
                break;

            case this.uiGridConstants.filter.GREATER_THAN:
                formula = "filter(greaterThan(" + column.field + ", " + safeTerm + "))";
                verb = "greater than";
                break;

            case this.uiGridConstants.filter.EXACT:
                formula = "filter(equal(" + column.field + ", " + safeTerm + "))";
                verb = "equal to";
                break;

            case this.uiGridConstants.filter.CONTAINS:
                const query = "%" + filter.term.replace("%", "%%") + "%";
                formula = "filter(like(" + column.field + ", '" + StringUtils.quote(query) + "'))";
                verb = "containing";
                break;

            default:
                throw new Error("Unsupported filter condition: " + filter.condition);
        }

        // Add formula
        let name = "Find " + column.displayName + " " + verb + " " + filter.term;

        if (typeof query === "undefined") {
            query = false;
        }

        return this.pushFormula(formula, {formula: formula, icon: filter.icon, name: name}, query);
    };

    /**
     * Adds the specified formula to the current script and refreshes the table data.
     *
     * @param {string} formula the formula
     * @param {TransformContext} context the UI context for the transformation
     */
    addFunction(formula: any, context: any): Promise<any> {
        return this.pushFormula(formula, context, true);
    };


    /**
     * Appends the specified formula to the current script.
     *
     * @param {string} formula - the formula
     */
    pushFormulaToEngine(formula: any, context: any): boolean {
        let self = this;
        // Covert to a syntax tree
        this.ternServer.server.addFile("[doc]", formula);
        let file = this.ternServer.server.findFile("[doc]");

        // Add to the Spark script
        try {
            this.engine.push(file.ast, context);
            return true;
        } catch (e) {
            let msg: string = e.message;
            if (msg != null) {
                msg = self.engine.decodeError(msg);
            }
            self.displayError("Error in formula", msg)
            console.log(e);
            return false;
        }
    };

    displayError(title: string, msg: string): void {
        this.$mdDialog.openAlert({
            title: title,
            message: msg,
            ariaLabel: msg,
            closeButton: "Ok"
        });
    }

    /**
     * Appends the specified formula to the current script.
     *
     * @param {string} formula - the formula
     * @param {TransformContext} context - the UI context for the transformation
     * @param {boolean} doQuery - true to immediately execute the query
     * @param {boolean} refreshGrid - true to refresh grid
     */
    pushFormula(formula: any, context: any, doQuery: boolean = false, refreshGrid: boolean = true): Promise<{}> {
        const self = this;
        self.currentPage = PageSpec.defaultPage();
        return new Promise<{}>((resolve, reject) => {
            setTimeout(function () {
                if (self.pushFormulaToEngine(formula, context)) {
                    // Add to function history
                    self.functionHistory.push(context);

                    if (doQuery || self.engine.getRows() === null) {
                        return self.query(refreshGrid, self.currentPage).catch(reason => reject(reason)).then(() => resolve());
                    }
                }
                // Formula couldn't parse
                self.resetAllProgress();
                return reject();
            }, 10);
        });
    };

    /**
     * Executes a formula, gathers the value and removes it from the histroy stack
     * @param {string} formula the formula to exexecute
     * @param {string} numRecords number of records to query (0 = all)
     * @param {number} colIdx the column index of the result to extract
     * @returns {Promise<ColumnProfile>}
     */
    extractFormulaResult(formula: string, numRecords: number, colIdx:number = 0): Promise<any> {
        const self = this;
        self.pushFormulaToEngine(formula, {});
        let priorLimit: number = self.engine.limit;
        let priorSample: number = self.engine.sample;
        self.engine.sample = 1.0;
        self.engine.limit = numRecords;
        let page = PageSpec.emptyPage();
        page.numRows = page.numCols = colIdx+1;
        page.firstRow = 0;
        return self.query(false, page).then(function () {
            let result = self.engine.getRows();
            self.engine.limit = priorLimit;
            self.engine.sample = priorSample;
            self.engine.pop();
            return result[0][colIdx];
        }).catch(() => {
            self.engine.limit = priorLimit;
            self.engine.sample = priorSample;
        })
    }

    extractColumnStatistics(fieldName: string): Promise<ColumnProfile> {
        const self = this;
        self.pushFormulaToEngine(`select(${fieldName})`, {});
        return self.query(false, PageSpec.emptyPage(), true, true).then(function () {
            let profileStats = self.engine.getProfile();
            self.engine.pop();
            return new ColumnProfile(fieldName, profileStats);
        });
    }

    /**
     * Generates and displays a categorical histogram
     *
     * @return {Promise} a promise for when the query completes
     */
    showAnalyzeColumn(fieldName: string): any {
        this.pushFormulaToEngine(`select(${fieldName})`, {});
        this.query(false, PageSpec.emptyPage(), true, true).then(() => {
            let profileStats = new ColumnProfile(fieldName, this.engine.getProfile());
            this.engine.pop();
            this.$mdDialog.open(AnalyzeColumnDialog, {data: {profileStats: profileStats, fieldName: fieldName}, width: "800px"});
        });
    };

   /**
     * Generates and displays a categorical histogram
     *
     * @return {Promise} a promise for when the query completes
     */
    showQuickColumnAnalysis(): any {

        let fieldNames : string[] = [];
        _.each(this.engine.getColumns(),  (item:any) => {
            fieldNames.push(ColumnUtil.getColumnFieldName(item));
        });

        this.pushFormulaToEngine(`select(${fieldNames.join(',')})`, {});
        this.query(false, PageSpec.emptyPage(), true, true).then(() => {
            let profileRows : ProfileOutputRow[] = this.engine.getProfile();
            let profiles : ColumnProfile[] = ColumnProfileHelper.createColumnProfiles(profileRows);
            this.engine.pop();
            let dialogData = new QuickColumnsDialogData(profiles);
            return  this.$mdDialog.open(QuickColumnsDialog,{data:dialogData, panelClass: "full-screen-dialog",height:'100%',width:'350px',position:{top:'0',right:'0'}});
        });
    };

    /**
     * Show dialog for applying global cleansing operations
     */
    showQuickClean() : void {

        let dialogData = new QuickCleanDialogData(this.engine.getColumns());

        this.$mdDialog.open(QuickCleanDialog,{data:dialogData, panelClass: "full-screen-dialog",height:'100%',width:'350px',position:{top:'0',right:'0'}}).afterClosed()
            .subscribe((script: String) => {
                if (script != null) {
                    this.pushFormula(script, {formula: script, icon: 'blur_linear', name: 'Quick clean'}, true);
                }
            });

    }
    /**
     * Shows columns dialog
     */
    showSchemaLayout(): void {

         let dialogData = new SchemaLayoutDialogData(this.engine.getColumns());

         this.$mdDialog.open(SchemaLayoutDialog,{data:dialogData, panelClass: "full-screen-dialog",height:'100%',width:'350px',position:{top:'0',right:'0'}}).afterClosed()
             .subscribe((columns: ColumnItem[]) => {
                    if (columns != null) {
                        let formulaFields : string[] = [];
                        columns.forEach((col:ColumnItem) => {
                            if (!col.deleted) {
                                let castClause = ((col.newType != col.origType) ? `.cast("${col.newType}")` : '');
                                formulaFields.push(`${col.origName}${castClause}.as("${col.newName}")`);
                            }
                        });
                        const formula = `select(${formulaFields.join(",")}`;
                        this.pushFormula(formula, {formula: formula, icon: 'reorder', name: 'Modify schema'}, true);
                    }
         });

    };

    /**
     * Shows samples dialog
     */
    showSampleDialog(): void {

        let dialogData = new SampleDialogData(this.engine.method, this.engine.reqLimit, this.engine.sample);
        let self = this;
        this.$mdDialog.open(SampleDialog, {data: dialogData, panelClass: "full-screen-dialog", height: '100%', width: '350px', position: {top: '0', right: '0'}}).afterClosed()
            .subscribe((result: SampleDialogData) => {
                if (result != null) {
                    this.engine.method = result.method;
                    this.engine.reqLimit = result.limit;

                    // Fetch the ratio based on the total rows in the dataset against the requested limit
                    if (result.method == 'rndnum') {

                        self.extractFormulaResult(`groupBy("1").count()`, 0, 1).then((value: any) => {
                            let rowCount = value;
                            let ratio = (result.limit > rowCount ? 1.0 : result.limit / rowCount);
                            self.engine.sample = ratio;
                            self.engine.limit = 0;
                            self.resample();
                        });

                    } else {
                        self.engine.limit = result.limit;
                        self.engine.sample = result.ratio;
                        self.resample();
                    }
                }

            });
    }

    /**
     * Sets the formula in the function bar to the specified value.
     *
     * @param {string} formula the formula
     */
    setFormula(formula: any) {
        this.currentFormula = formula;
        this.codemirrorEditor.setValue(formula);
        setTimeout(() => this.codemirrorEditor.focus(), 200);  // wait for menu to close
        this.selectNextTabStop();
    };

    /**
     * Selects the next uppercase word in the formula bar.
     */
    selectNextTabStop() {
        let match = /\b[A-Z]{2,}\b/.exec(this.currentFormula);
        if (match !== null) {
            this.codemirrorEditor.setSelection(new CodeMirror.Pos(0, match.index), new CodeMirror.Pos(0, match.index + match[0].length));
        } else {
            this.codemirrorEditor.setCursor(0, this.currentFormula.length);
        }
    };

    /**
     * Called when the user clicks Add on the function bar
     */
    onAddFunction = () => {
        this.addFunction(this.currentFormula, {formula: this.currentFormula, icon: "code", name: this.currentFormula});
    };

    /**
     * Update state counter so clients know that state (function stack) has changed
     */
    updateTableState(): void {
        // Update state variable to indicate to client we are in a new state
        this.tableState = this.engine.getState().tableState;
    }

    updateSortIcon(): void {
        let columnSort = this.engine.getState().sort;
        if (columnSort != null) {
            this.wranglerDataService.sortDirection_ = <"desc" | "asc">  columnSort.direction;
            this.wranglerDataService.sortIndex_ = columnSort.columnIndex;
        }
        else {
            this.wranglerDataService.sortDirection_ = null;
            this.wranglerDataService.sortIndex_ = null;
        }
    }

    /**
     * Refreshes the grid. Used after undo and redo.
     */
    refreshGrid() {

        this.updateTableState();
        let columns = this.engine.getColumns();
        let rows = this.engine.getRows();

        if (columns === null || rows === null) {
            this.query().catch((result) => {
                console.debug("Error executing query!");
            });
        } else {
            this.updateGrid();
        }
    };

    /**
     * Refreshes the table content.
     */
    resample() {
        let fieldNames : string[] = [];
        _.each(this.engine.getColumns(),  (item:any) => {
            fieldNames.push(ColumnUtil.getColumnFieldName(item));
        });

        this.pushFormulaToEngine(`select(${fieldNames.join(',')})`, {});
        this.query().catch((result) => {
            console.debug("Error executing query!");
        });
    }

    //noinspection JSUnusedGlobalSymbols
    onUndo() {
        this.engine.undo();
        this.functionHistory.pop();
        this.refreshGrid();
    };

    //noinspection JSUnusedGlobalSymbols
    onRedo() {
        let func = this.engine.redo();
        this.functionHistory.push(func);
        this.refreshGrid();
    };

    canUndo() {
        return (this.engine && this.engine.canUndo) ? this.engine.canUndo() : false;
    };

    canRedo() {
        return (this.engine && this.engine.canRedo) ? this.engine.canRedo() : false;
    };

    isUsingSampleFile() {
        //TODO reference "FILE" as a constant  or a method ... model.isFileDataSource()
        return this.model.$selectedDatasourceId == "FILE"
    }

    removeItem(index: number): void {
        this.engine.remove(index + 1);
        this.functionHistory = this.engine.getHistory();
    }

    /**
     * Remove step item from history
     * @param {number} index
     */
    toggleItem(index: number): void {
        // Adjust for difference between visible history and actual history which contains the 1st query
        this.engine.toggle(index + 1);
        this.functionHistory = this.engine.getHistory();
        let self = this;
        this.query().catch(reason => {
            // reverse impact
            self.engine.restoreLastKnownState();
            this.query().catch((result) => {
                console.debug("Error executing query!");
            });
            this.functionHistory = this.engine.getHistory();
        });
    }

    isSampleFileChanged() {
        return this.isUsingSampleFile() && this.model.sampleFileChanged;
    }

    /**
     * Update the feed model when changing from this transform step to a different step
     */
    private onStepChange() {
        const localFileChanged = this.isSampleFileChanged();

        if (!this.active) {
            this.saveToFeedModel().then(() => {
                // notify those that the data is loaded/updated
                this.broadcastService.notify('DATA_TRANSFORM_SCHEMA_LOADED', 'SCHEMA_LOADED');
            }, () => {
                this.broadcastService.notify('DATA_TRANSFORM_SCHEMA_LOADED', 'SCHEMA_LOADED');
            });
        } else if (this.sql == null || localFileChanged) {
            this.ngOnInit();
            this.model.sampleFileChanged = false;
        }
    }

    /**
     * Saves the current transformation to the feed model.
     *
     * @returns {Promise} signals when the save is complete
     */
    private saveToFeedModel() {
        // Add unsaved filters

        // Check if updates are necessary
        let feedModel = (this.feedModel != null) ? this.feedModel : new Feed();//this.feedService.createFeedModel;
        let newScript = this.engine.getFeedScript();
        if (newScript === feedModel.dataTransformation.dataTransformScript) {
            return new Promise((resolve) => {
                resolve(true);
            });
        }


        // Populate Feed Model from the Visual Query Model
        feedModel.dataTransformation.dataTransformScript = newScript;
        feedModel.dataTransformation.states = this.engine.save();

        feedModel.table.existingTableName = "";
        feedModel.table.method = "EXISTING_TABLE";
        feedModel.table.sourceTableSchema.name = "";

        // Get list of fields
        return new Promise(resolve => {
            let fields = this.engine.getFields();

            if (fields !== null) {
                feedModel.table.setTableFields(fields,this.engine.getFieldPolicies());
            let valid =   feedModel.validateSchemaDidNotChange();
            if(!valid)
                feedModel.table.syncTableFieldPolicyNames()
                this.engine.save();
                resolve(true);
            } else {
                this.query().then(() => {
                    feedModel.table.setTableFields(fields,this.engine.getFieldPolicies());
                    feedModel.validateSchemaDidNotChange();
                    feedModel.table.syncTableFieldPolicyNames()
                    this.engine.save();
                    resolve(true);
                }).catch((result) => {
                    console.debug("Error executing query!");
                });
            }
        });
    }

    /**
     * Sets the domain type for the specified field.
     *
     * @param columnIndex - the field index
     * @param domainTypeId - the domain type id
     */
    setDomainType(columnIndex: number, domainTypeId: string) {
        const domainType: any = (domainTypeId != null) ? this.domainTypes.find(domainType => domainType.id === domainTypeId) : {fieldPolicy: {standardization: null, validation: null}};
        if (domainType) {
            const fieldPolicies = (this.engine.getFieldPolicies() !== null) ? this.engine.getFieldPolicies() : [];
            this.fieldPolicies = this.engine.getColumns().map((column, index) => {
                let fieldPolicy: any;
                if (index < fieldPolicies.length) {
                    fieldPolicy = fieldPolicies[index];
                } else {
                    fieldPolicy = TableFieldPolicy.forName(column.hiveColumnLabel);
                    fieldPolicy.fieldName = column.hiveColumnLabel;
                    fieldPolicy.feedFieldName = column.hiveColumnLabel;
                }

                if (index === columnIndex) {
                    //TODO MOVE OUT TO COMMON UTIL
                    this.feedService.setDomainTypeForField(new TableColumnDefinition(), fieldPolicy, domainType);
                }
                return fieldPolicy;
            });
            this.engine.setFieldPolicies(this.fieldPolicies);
        }
    }

    /**
     * Set the query progress
     * @param {number} progress
     */
    setQueryProgress(progress: number) {
        if (!this.chainedOperation) {
            this.queryProgress = progress;
        } else {
            this.queryProgress = this.chainedOperation.fracComplete(progress);
        }
    }

    /**
     * Set whether the query is actively running
     * @param {boolean} query
     */
    setExecutingQuery(query: boolean) {
        if ((query == true && !this.executingQuery) || (!this.chainedOperation || this.chainedOperation.isLastStep())) {
            this.executingQuery = query;
        }
        // Reset chained operation to default
        if (this.executingQuery == false) {
            this.resetAllProgress();
        }
    }

    /**
     * Indicates the active query will be followed by another in quick succession
     * @param {ChainedOperation} chainedOperation
     */
    setChainedQuery(chainedOp: ChainedOperation) {
        this.chainedOperation = chainedOp;
    }

    /**
     * Resets all progress to non-running
     */
    resetAllProgress() {
        this.chainedOperation = new ChainedOperation();
        this.queryProgress = 0;
        this.executingQuery = false;
    }

    goForward() {
        this.saveToFeedModel().then(() => this.next.emit());
    }
}
