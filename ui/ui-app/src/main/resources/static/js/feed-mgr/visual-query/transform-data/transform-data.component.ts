import {Input, OnInit} from "@angular/core";
import * as angular from "angular";
import * as $ from "jquery";
import * as _ from "underscore";

import {WindowUnloadService} from "../../../services/WindowUnloadService";
import {FeedDataTransformation} from "../../model/feed-data-transformation";
import {DomainType, DomainTypesService} from "../../services/DomainTypesService";
import {DataCategory} from "../wrangler/column-delegate";
import {TransformValidationResult} from "../wrangler/model/transform-validation-result";
import {QueryEngine} from "../wrangler/query-engine";

declare const CodeMirror: any;

const moduleName: string = require("feed-mgr/visual-query/module-name");

/**
 * Transform Data step of the Visual Query page.
 */
export class TransformDataComponent implements OnInit {

    /**
     * Query engine for transforming data.
     */
    @Input()
    engine: QueryEngine<any>;

    /**
     * Data transformation model
     */
    @Input()
    model: FeedDataTransformation;

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
    tableColumns: any = [];

    //noinspection JSUnusedGlobalSymbols
    /**
     * Configuration for the results table.
     * @type {Object}
     */
    tableOptions = {
        headerFont: "700 12px Roboto, 'Helvetica Neue', sans-serif",
        rowFont: "400 14px Roboto, 'Helvetica Neue', sans-serif"
    };

    //noinspection JSUnusedGlobalSymbols
    /**
     * Rows for the results table.
     * @type {Array.<Object>}
     */
    tableRows: object[] = [];

    executingQuery: boolean = false;
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

    gridApi: any;

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
    fieldPolicies: any[];

    /**
     * Height offset from the top of the page.
     */
    heightOffset: string = "0";

    /**
     * Validation results for current transformation.
     */
    tableValidation: TransformValidationResult[][];

    /**
     * Constructs a {@code TransformDataComponent}.
     */
    constructor(private $scope: angular.IScope, $element: angular.IAugmentedJQuery, private $q: angular.IQService, private $mdDialog: angular.material.IDialogService,
                private domainTypesService: DomainTypesService, private RestUrlService: any, SideNavService: any, private uiGridConstants: any, private FeedService: any, private BroadcastService: any,
                StepperService: any, WindowUnloadService: WindowUnloadService) {
        //Listen for when the next step is active
        BroadcastService.subscribe($scope, StepperService.STEP_CHANGED_EVENT, this.onStepChange.bind(this));

        //Hide the left side nav bar
        SideNavService.hideSideNav();

        // Display prompt on window unload
        WindowUnloadService.setText("You will lose any unsaved changes. Are you sure you want to continue?");

        // Get height offset attribute
        this.heightOffset = $element.attr("height-offset");

        // Invalidate when SQL changes
        const self = this;
        $scope.$watch(
            function () {
                return (typeof self.model === "object") ? self.model.sql : null;
            },
            function () {
                if (typeof self.model === "object" && self.sql !== self.model.sql) {
                    self.isValid = false;
                    self.sql = null;
                }
            }
        );
    }

    $onInit(): void {
        this.ngOnInit();
    }

    ngOnInit(): void {
        this.sql = this.model.sql;
        this.sqlModel = this.model.chartViewModel;
        this.selectedColumnsAndTables = this.model.$selectedColumnsAndTables;

        // Select source model
        let useSqlModel = false;
        if (angular.isObject(this.sqlModel)) {
            // Check for non-Hive datasource
            if (angular.isArray(this.model.datasourceIds) && this.model.datasourceIds.length > 0) {
                useSqlModel = (this.model.datasourceIds.length > 1 || (angular.isDefined(this.model.datasourceIds[0]) && this.model.datasourceIds[0] !== "HIVE"));  // TODO remove "HIVE"
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

            if (angular.isArray(this.model.states) && this.model.states.length > 0) {
                this.engine.setQuery(source, this.model.$datasources);
                this.engine.setState(this.model.states);
                this.functionHistory = this.engine.getHistory();
            } else {
                this.engine.setQuery(source, this.model.$datasources);
            }

            // Watch for changes to field policies
            if (this.fieldPolicies == null) {
                this.fieldPolicies = [];
            }
            this.engine.setFieldPolicies(this.fieldPolicies);

            this.$scope.$watch(() => this.fieldPolicies, () => {
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
                if (domainTypesLoaded) {
                    this.query();
                }
            }, true);

            // Fetch domain types
            this.domainTypesService.findAll()
                .then(domainTypes => {
                    this.domainTypes = domainTypes;
                    domainTypesLoaded = true;

                    // Load table data
                    this.query();
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
     * Gets the browser height offset for the element with the specified offset from the top of this component.
     */
    getBrowserHeightOffset(elementOffset: number): number {
        return parseInt(this.heightOffset) + elementOffset;
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
        const self = this;
        this.$mdDialog.show({
            clickOutsideToClose: false,
            controller: "VisualQueryProfileStatsController",
            fullscreen: true,
            locals: {
                profile: self.engine.getProfile()
            },
            parent: angular.element(document.body),
            templateUrl: "js/feed-mgr/visual-query/transform-data/profile-stats/profile-stats-dialog.html"
        });
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
        _editor.on("beforeChange", function (instance: any, change: any) {
            let newtext = change.text.join("").replace(/\n/g, ""); // remove ALL \n !
            change.update(change.from, change.to, [newtext]);
            return true;
        });

        //hide the scrollbar
        _editor.on("change", function (instance: any, change: any) {
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
        const self = this;
        this.engine.getTernjsDefinitions().then(function (response: any) {
            self.engine.setFunctionDefs(response);

            self.ternServer = new CodeMirror.TernServer({defs: [response]});
            self.ternServer.server.addDefs(self.engine.getColumnDefs());

            const _editor = self.codemirrorEditor;
            _editor.setOption("extraKeys", {
                "Ctrl-Space": function (cm: any) {
                    self.ternServer.complete(cm);
                },
                "Ctrl-I": function (cm: any) {
                    self.ternServer.showType(cm);
                },
                "Ctrl-O": function (cm: any) {
                    self.ternServer.showDocs(cm);
                },
                "Alt-.": function (cm: any) {
                    self.ternServer.jumpToDef(cm);
                },
                "Alt-,": function (cm: any) {
                    self.ternServer.jumpBack(cm);
                },
                "Ctrl-Q": function (cm: any) {
                    self.ternServer.rename(cm);
                },
                "Ctrl-.": function (cm: any) {
                    self.ternServer.selectName(cm);
                },
                "Tab": function () {
                    self.selectNextTabStop();
                }
            });
            _editor.on("blur", function () {
                self.ternServer.hideDoc();
            });
            _editor.on("cursorActivity", self.showHint.bind(self));
            _editor.on("focus", self.showHint.bind(self));
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
        this.ternServer.getHint(cm, function (data: any) {
            // Complete function calls so arg hints can be displayed
            CodeMirror.on(data, "pick", function (completion: any) {
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
     * Query Hive using the query from the previous step. Set the Grids rows and columns.
     *
     * @return {Promise} a promise for when the query completes
     */
    query() {
        const self = this;
        const deferred = this.$q.defer();

        //flag to indicate query is running
        this.executingQuery = true;
        this.queryProgress = 0;

        // Query Spark shell service
        let didUpdateColumns = false;

        const successCallback = function () {
            //mark the query as finished
            self.executingQuery = false;

            // Clear previous filters
            if (typeof(self.gridApi) !== "undefined") {
                self.gridApi.core.clearAllFilters();
            }

            //mark the flag to indicate Hive is loaded
            self.hiveDataLoaded = true;
            self.isValid = true;

            //store the result for use in the commands
            self.updateGrid();

            deferred.resolve();
        };
        const errorCallback = function (message: string) {
            // Display error message
            self.$mdDialog.show({
                clickOutsideToClose: true,
                controller: class {

                    /**
                     * Additional details about the error.
                     */
                    detailMessage = message;

                    /**
                     * Indicates that the detail message should be shown.
                     */
                    showDetail = false;

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
                parent: angular.element("body"),
                template: `
                  <md-dialog arial-label="error executing the query" style="max-width: 640px;">
                    <md-dialog-content class="md-dialog-content" role="document" tabIndex="-1">
                      <h2 class="md-title">Error executing the query</h2>
                      <p>There was a problem executing the query.</p>
                      <md-button ng-if="!dialog.showDetail" ng-click="dialog.showDetail = true" style="margin: 0; padding: 0;">Show more</md-button>
                      <p ng-if="dialog.showDetail">{{ dialog.detailMessage }}</p>
                    </md-dialog-content>
                    <md-dialog-actions>
                      <md-button ng-click="dialog.hide()" class="md-primary md-confirm-button" md-autofocus="true">Got it!</md-button>
                    </md-dialog-actions>
                  </md-dialog>
                `
            });

            // Reset state
            self.executingQuery = false;
            self.engine.pop();
            self.functionHistory.pop();
            self.refreshGrid();

            deferred.reject(message);
        };
        const notifyCallback = function (progress: number) {
            self.queryProgress = progress * 100;
            if (self.engine.getColumns() !== null && !didUpdateColumns && self.ternServer !== null) {
                didUpdateColumns = true;
                self.updateGrid();
            }
        };

        self.engine.transform().subscribe(notifyCallback, errorCallback, successCallback);
        return deferred.promise;
    };

    private updateGrid() {
        const self = this;

        //transform the result to the agGrid model
        let columns: any = [];
        let fieldPolicies = this.engine.getFieldPolicies();
        let profile = this.engine.getProfile();

        angular.forEach(this.engine.getColumns(), function (col, index) {
            const delegate = self.engine.createColumnDelegate(col.dataType, self);
            const fieldPolicy = (col.index < fieldPolicies.length) ? fieldPolicies[index] : null;
            const longestValue = _.find(profile, function (row: any) {
                return (row.columnName === col.displayName && (row.metricType === "LONGEST_STRING" || row.metricType === "MAX"))
            });

            columns.push({
                dataType: col.dataType,
                delegate: delegate,
                displayName: col.displayName,
                domainTypeId: fieldPolicy ? fieldPolicy.domainTypeId : null,
                filters: delegate.filters,
                headerTooltip: col.hiveColumnLabel,
                longestValue: ( angular.isDefined(longestValue) && longestValue !== null) ? longestValue.metricValue : null,
                name: self.engine.getColumnName(col)
            });
        });

        //update the ag-grid
        this.tableColumns = columns;
        this.tableRows = this.engine.getRows();
        this.tableValidation = this.engine.getValidationResults();

        this.updateCodeMirrorAutoComplete();
    }

    /**
     * Adds formulas for column filters.
     */
    addFilters() {
        const self = this;
        angular.forEach(self.tableColumns, function (column) {
            angular.forEach(column.filters, function (filter) {
                if (filter.term) {
                    self.addColumnFilter(filter, column);
                }
            });
        });
    };

    /**
     * Add formula for a column filter.
     *
     * @param {Object} filter the filter
     * @param {ui.grid.GridColumn} column the column
     */
    addColumnFilter(filter: any, column: any) {
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
        this.pushFormula(formula, {formula: formula, icon: filter.icon, name: name});
    };

    /**
     * Adds the specified formula to the current script and refreshes the table data.
     *
     * @param {string} formula the formula
     * @param {TransformContext} context the UI context for the transformation
     */
    addFunction(formula: any, context: any) {
        this.addFilters();
        this.pushFormula(formula, context, true);
    };

    /**
     * Appends the specified formula to the current script.
     *
     * @param {string} formula - the formula
     * @param {TransformContext} context - the UI context for the transformation
     * @param {boolean} doQuery - true to immediately execute the query
     */
    pushFormula(formula: any, context: any, doQuery: boolean = false) {
        // Covert to a syntax tree
        this.ternServer.server.addFile("[doc]", formula);
        let file = this.ternServer.server.findFile("[doc]");

        // Add to the Spark script
        try {
            this.engine.push(file.ast, context);
        } catch (e) {
            let alert = this.$mdDialog.alert()
                .parent($('body'))
                .clickOutsideToClose(true)
                .title("Error executing the query")
                .textContent(e.message)
                .ariaLabel("error executing the query")
                .ok("Got it!");
            this.$mdDialog.show(alert);
            console.log(e);
            return;
        }

        // Add to function history
        this.functionHistory.push(context);

        if (doQuery || this.engine.getRows() === null) {
            this.query();
        }
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
    onAddFunction = function () {
        this.addFunction(this.currentFormula, {formula: this.currentFormula, icon: "code", name: this.currentFormula});
    };

    /**
     * Refreshes the grid. Used after undo and redo.
     */
    refreshGrid() {
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
        this.addFilters();
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

    /**
     * Update the feed model when changing from this transform step to a different step
     */
    private onStepChange(event: string, changedSteps: { newStep: number, oldStep: number }) {
        const self = this;
        const thisIndex = parseInt(this.stepIndex);
        if (changedSteps.oldStep === thisIndex) {
            this.saveToFeedModel().then(function () {
                // notify those that the data is loaded/updated
                self.BroadcastService.notify('DATA_TRANSFORM_SCHEMA_LOADED', 'SCHEMA_LOADED');
            });
        } else if (changedSteps.newStep === thisIndex && this.sql == null) {
            this.ngOnInit();
        }
    }

    /**
     * Saves the current transformation to the feed model.
     *
     * @returns {Promise} signals when the save is complete
     */
    private saveToFeedModel() {
        // Add unsaved filters
        this.addFilters();

        // Check if updates are necessary
        let feedModel = this.FeedService.createFeedModel;
        let newScript = this.engine.getFeedScript();
        if (newScript === feedModel.dataTransformation.dataTransformScript) {
            let result = this.$q.defer();
            result.reject(true);
            return result.promise;
        }

        // Populate Feed Model from the Visual Query Model
        feedModel.dataTransformation.dataTransformScript = newScript;
        feedModel.dataTransformation.states = this.engine.save();

        feedModel.table.existingTableName = "";
        feedModel.table.method = "EXISTING_TABLE";
        feedModel.table.sourceTableSchema.name = "";

        // Get list of fields
        let deferred = this.$q.defer();
        let fields = this.engine.getFields();

        if (fields !== null) {
            this.FeedService.setTableFields(fields, this.engine.getFieldPolicies());
            this.FeedService.syncTableFieldPolicyNames();
            this.engine.save();
            deferred.resolve(true);
        } else {
            this.query().then(() => {
                this.FeedService.setTableFields(this.engine.getFields(), this.engine.getFieldPolicies());
                this.FeedService.syncTableFieldPolicyNames();
                this.engine.save();
                deferred.resolve(true);
            });
        }

        return deferred.promise;
    }

    /**
     * Reset the sample or limit value when the sample method changes.
     */
    onSampleMethodChange() {
        if (this.sampleMethod === "SAMPLE") {
            this.engine.sample(0.1);
        } else if (this.sampleMethod === "LIMIT") {
            this.engine.limit(1000);
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
                    fieldPolicy = this.FeedService.newTableFieldPolicy(column.hiveColumnLabel);
                    fieldPolicy.fieldName = column.hiveColumnLabel;
                    fieldPolicy.feedFieldName = column.hiveColumnLabel;
                }

                if (index === columnIndex) {
                    this.FeedService.setDomainTypeForField({}, fieldPolicy, domainType);
                }

                return fieldPolicy;
            });
        }
    }
}

angular.module(moduleName).component("thinkbigVisualQueryTransform", {
    bindings: {
        engine: "=",
        fieldPolicies: "<?",
        heightOffset: "@",
        model: "=",
        stepIndex: "@"
    },
    controller: ["$scope", "$element", "$q", "$mdDialog", "DomainTypesService", "RestUrlService", "SideNavService", "uiGridConstants", "FeedService", "BroadcastService", "StepperService",
        "WindowUnloadService", TransformDataComponent],
    controllerAs: "$td",
    require: {
        stepperController: "^thinkbigStepper"
    },
    templateUrl: "js/feed-mgr/visual-query/transform-data/transform-data.component.html"
});
