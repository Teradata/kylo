import {AfterViewInit, Component, ElementRef, EventEmitter, Inject, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild, ViewEncapsulation} from "@angular/core";
import {TdDialogService} from "@covalent/core/dialogs";
import {CodemirrorComponent} from "ng2-codemirror";
import {Subject} from "rxjs/Subject";
import * as _ from "underscore";

import StepperService from '../../../common/stepper/StepperService';
import {CloneUtil} from "../../../common/utils/clone-util";
import BroadcastService from '../../../services/broadcast-service';
import {WindowUnloadService} from "../../../services/WindowUnloadService";
import {FeedDataTransformation} from "../../model/feed-data-transformation";
import {Feed} from "../../model/feed/feed.model";
import {TableColumnDefinition} from "../../model/TableColumnDefinition"
import {DomainType, DomainTypesService} from "../../services/DomainTypesService";
import {FeedService} from '../../services/FeedService';
import {ApplyTableDomainTypesDialog} from "../../shared/apply-domain-type/apply-table-domain-types.component";
import {ColumnProfile, ColumnProfileHelper} from "../wrangler/api/column-profile";
import {ColumnController} from "../wrangler/column-controller";
import {ChainedOperation, ColumnDelegate, DataCategory} from "../wrangler/column-delegate";
import {ProfileOutputRow, ScriptState, StringUtils} from "../wrangler/index";
import {TransformValidationResult} from "../wrangler/model/transform-validation-result";
import {PageSpec, QueryEngine} from "../wrangler/query-engine";
import {AnalyzeColumnDialog} from "./profile-stats/analyze-column-dialog";
import VisualQueryProfileStatsController from "./profile-stats/VisualQueryProfileStats";
import {WranglerDataService} from "./services/wrangler-data.service";
import {VisualQueryTable} from "./visual-query-table/visual-query-table.component";
import {QuickColumnsDialog, QuickColumnsDialogData} from "./profile-stats/quick-columns-dialog";
import {ColumnUtil} from "../wrangler/core/column-util";
import {ColumnItem, SchemaLayoutDialog, SchemaLayoutDialogData} from "./profile-stats/schema-layout-dialog";

declare const CodeMirror: any;

// import {moduleName} from "../module-name";

export class WranglerColumn {

    dataType: string = null;
    delegate: ColumnDelegate = null;
    displayName: string = null;
    domainTypeId: string = null;
    filters: any[] = null;
    headerTooltip: string = null;
    longestValue: string = null;
    name: string = null;
}

/**
 * Transform Data step of the Visual Query page.
 */
@Component({
    encapsulation: ViewEncapsulation.None,
    selector: "thinkbig-visual-query-transform",
    styleUrls: ["js/feed-mgr/visual-query/transform-data/transform-data.component.css"],
    templateUrl: "js/feed-mgr/visual-query/transform-data/transform-data.component.html"
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
     * Event emitted to return to the previous step
     */
    @Output()
    back = new EventEmitter<void>();

    /**
     * Event emitted to advance to the next step
     */
    @Output()
    next = new EventEmitter<void>();

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
    heightOffset: string = "0";

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

    /**
     * Constructs a {@code TransformDataComponent}.
     */
    constructor($element: ElementRef, private $mdDialog: TdDialogService, @Inject("DomainTypesService") private domainTypesService: DomainTypesService,
                @Inject("RestUrlService") private RestUrlService: any, @Inject("SideNavService") private SideNavService: any, @Inject("uiGridConstants") private uiGridConstants: any,
                @Inject("FeedService") private feedService: FeedService, @Inject("BroadcastService") private broadcastService: BroadcastService,
                @Inject("StepperService") private stepperService: StepperService, @Inject("WindowUnloadService") private WindowUnloadService: WindowUnloadService,
                private wranglerDataService: WranglerDataService) {
        //Hide the left side nav bar
        this.SideNavService.hideSideNav();


        // Get height offset attribute
        this.heightOffset = $element.nativeElement.getAttribute("height-offset");
    }

    ngAfterViewInit(): void {
        this.codemirrorLoaded(this.codeMirrorView.instance);
    }

    ngOnInit(): void {
        if (this.warnWhenLeaving) {
            // Display prompt on window unload
            this.WindowUnloadService.setText("You will lose any unsaved changes. Are you sure you want to continue?");

        }

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
            let domainTypesLoaded = false;
            this.sampleFormulas = this.engine.sampleFormulas;

            if (this.model.datasets) {
                this.engine.setDatasets(this.model.datasets)
            }

            if (Array.isArray(this.model.states) && this.model.states.length > 0) {
                this.engine.setQuery(source, this.model.$datasources);
                this.engine.setState(this.model.states);
                this.functionHistory = this.engine.getHistory();
            } else {
                this.engine.setQuery(source, this.model.$datasources);
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
                this.query();
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
            this.$mdDialog.open(ApplyTableDomainTypesDialog, {data: {domainTypes: domainTypes, fields: fields}, panelClass: "full-screen-dialog"})
                .afterClosed().subscribe((selected: any) => {
                selected.forEach((selection: any) => {
                    var fieldIndex = fields.findIndex((element: WranglerColumn) => {
                        return element.name === selection.name;
                    });
                    this.setDomainType(colIndexes[fieldIndex], domainTypes[fieldIndex].id);
                    flgChanged = true;
                });
                if (flgChanged) {
                    // Need to supply a formula
                    const formula = `withColumn("${fields[0].name}", ${fields[0].name})`
                    this.pushFormula(formula, {formula: formula, icon: 'functions', name: 'Change domain type'}, true);
                }
            }, () => {
                // ignore cancel
            });
        }
    }

    /**
     * Gets the browser height offset for the element with the specified offset from the top of this component.
     */
    getBrowserHeightOffset(elementOffset: number): number {
        return (this.heightOffset != null ? parseInt(this.heightOffset) : 0) + elementOffset;
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
            closeButton: "Got it!",
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
            this.setExecutingQuery(false);
            this.resetAllProgress();
            this.showError(this.cleanError(message));

            // Reset state
            this.onUndo();
            this.removeExecution(promise);
            deferred.error(message);
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

                columns.push({
                    dataType: col.dataType,
                    delegate: delegate,
                    displayName: col.displayName,
                    domainTypeId: fieldPolicy ? fieldPolicy.domainTypeId : null,
                    filters: delegate.filters,
                    headerTooltip: col.hiveColumnLabel,
                    longestValue: (typeof longestValue !== "undefined" && longestValue !== null) ? longestValue.metricValue : null,
                    name: this.engine.getColumnName(col)
                });
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
     * Executes a formula, gathers the value and reverses
     * @param {string} fieldName
     * @param {string} formula
     * @returns {Promise<ColumnProfile>}
     */
    extractFormulaResult(formula: string, sample: number): Promise<any> {
        const self = this;
        self.pushFormulaToEngine(formula, {});
        let limit: number = self.engine.limit;
        self.engine.limit = sample;
        let page = PageSpec.emptyPage();
        page.numRows = page.numCols = 1;
        page.firstRow = 0;
        return self.query(false, page).then(function () {
            let result = self.engine.getRows();
            self.engine.limit = limit;
            self.engine.pop();
            return result[0][0];
        }).catch(() => {
            self.engine.limit = limit;
        });
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
     * Sets the formula in the function bar to the specified value.
     *
     * @param {string} formula the formula
     */
    setFormula(formula: any) {
        this.currentFormula = formula;
        this.codemirrorEditor.setValue(formula);
        this.codemirrorEditor.focus();
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
            this.query();
        } else {
            this.updateGrid();
        }
    };

    /**
     * Refreshes the table content.
     */
    resample() {
        this.query();
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
            this.query();
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
        let feedModel = (this.feedModel != null) ? this.feedModel : this.feedService.createFeedModel;
        let newScript = this.engine.getFeedScript();
        if (newScript === feedModel.dataTransformation.dataTransformScript) {
            return new Promise((resolve, reject) => reject(true));
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
                this.feedService.setTableFields(fields, this.engine.getFieldPolicies());
                this.feedService.syncTableFieldPolicyNames();
                this.engine.save();
                resolve(true);
            } else {
                this.query().then(() => {
                    this.feedService.setTableFields(this.engine.getFields(), this.engine.getFieldPolicies());
                    this.feedService.syncTableFieldPolicyNames();
                    this.engine.save();
                    resolve(true);
                });
            }
        });
    }

    /**
     * Reset the sample or limit value when the sample method changes.
     */
    onSampleMethodChange() {
        if (this.sampleMethod === "SAMPLE") {
            this.engine.sample = 0.1;
        } else if (this.sampleMethod === "LIMIT") {
            this.engine.limit = 1000;
        }
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
                    fieldPolicy = this.feedService.newTableFieldPolicy(column.hiveColumnLabel);
                    fieldPolicy.fieldName = column.hiveColumnLabel;
                    fieldPolicy.feedFieldName = column.hiveColumnLabel;
                }

                if (index === columnIndex) {
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
