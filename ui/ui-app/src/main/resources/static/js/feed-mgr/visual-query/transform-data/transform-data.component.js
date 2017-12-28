var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/core", "angular", "jquery", "underscore", "../wrangler/column-delegate", "../wrangler/query-engine"], function (require, exports, core_1, angular, $, _, column_delegate_1, query_engine_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require("feed-mgr/visual-query/module-name");
    /**
     * Transform Data step of the Visual Query page.
     */
    var TransformDataComponent = /** @class */ (function () {
        /**
         * Constructs a {@code TransformDataComponent}.
         */
        function TransformDataComponent($scope, $element, $q, $mdDialog, domainTypesService, RestUrlService, SideNavService, uiGridConstants, FeedService, BroadcastService, StepperService, WindowUnloadService) {
            this.$scope = $scope;
            this.$q = $q;
            this.$mdDialog = $mdDialog;
            this.domainTypesService = domainTypesService;
            this.RestUrlService = RestUrlService;
            this.uiGridConstants = uiGridConstants;
            this.FeedService = FeedService;
            this.BroadcastService = BroadcastService;
            //Flag to determine if we can move on to the next step
            this.isValid = false;
            //Function History
            this.functionHistory = [];
            //The current formula string
            this.currentFormula = '';
            //flag to indicate if the Hive data is available
            this.hiveDataLoaded = false;
            //flag to indicate codemirror is ready
            this.codemirroLoaded = false;
            //the codemirror editor
            this.codemirrorEditor = null;
            //the tern server reference
            this.ternServer = null;
            //Flag to show/hide function history panel
            this.isShowFunctionHistory = false;
            // Flag to show/hide sample menu
            this.isShowSampleMenu = false;
            //noinspection JSUnusedGlobalSymbols
            /**
             * Columns for the results table.
             * @type {Array.<Object>}
             */
            this.tableColumns = [];
            //noinspection JSUnusedGlobalSymbols
            /**
             * Configuration for the results table.
             * @type {Object}
             */
            this.tableOptions = {
                headerFont: "700 12px Roboto, 'Helvetica Neue', sans-serif",
                rowFont: "400 14px Roboto, 'Helvetica Neue', sans-serif"
            };
            //noinspection JSUnusedGlobalSymbols
            /**
             * Rows for the results table.
             * @type {Array.<Object>}
             */
            this.tableRows = [];
            this.executingQuery = false;
            //Code Mirror options.  Tern Server requires it be in javascript mode
            this.codeMirrorConfig = {
                onLoad: this.codemirrorLoaded.bind(this)
            };
            this.codemirrorOptions = {
                lineWrapping: true,
                indentWithTabs: false,
                smartIndent: false,
                lineNumbers: false,
                matchBrackets: false,
                mode: 'javascript',
                scrollbarStyle: null
            };
            // Progress of transformation from 0 to 100
            this.queryProgress = 0;
            /**
             * Method for limiting the number of results.
             */
            this.sampleMethod = "LIMIT";
            /**
             * List of sample formulas.
             */
            this.sampleFormulas = [];
            /**
             * List of available domain types.
             */
            this.domainTypes = [];
            /**
             * Height offset from the top of the page.
             */
            this.heightOffset = "0";
            /**
             * Called when the user clicks Add on the function bar
             */
            this.onAddFunction = function () {
                this.addFunction(this.currentFormula, { formula: this.currentFormula, icon: "code", name: this.currentFormula });
            };
            //Listen for when the next step is active
            BroadcastService.subscribe($scope, StepperService.STEP_CHANGED_EVENT, this.onStepChange.bind(this));
            //Hide the left side nav bar
            SideNavService.hideSideNav();
            // Display prompt on window unload
            WindowUnloadService.setText("You will lose any unsaved changes. Are you sure you want to continue?");
            // Get height offset attribute
            this.heightOffset = $element.attr("height-offset");
            // Invalidate when SQL changes
            var self = this;
            $scope.$watch(function () {
                return (typeof self.model === "object") ? self.model.sql : null;
            }, function () {
                if (typeof self.model === "object" && self.sql !== self.model.sql) {
                    self.isValid = false;
                    self.sql = null;
                }
            });
        }
        TransformDataComponent.prototype.$onInit = function () {
            this.ngOnInit();
        };
        TransformDataComponent.prototype.ngOnInit = function () {
            var _this = this;
            this.sql = this.model.sql;
            this.sqlModel = this.model.chartViewModel;
            this.selectedColumnsAndTables = this.model.$selectedColumnsAndTables;
            // Select source model
            var useSqlModel = false;
            if (angular.isObject(this.sqlModel)) {
                // Check for non-Hive datasource
                if (angular.isArray(this.model.datasourceIds) && this.model.datasourceIds.length > 0) {
                    useSqlModel = (this.model.datasourceIds.length > 1 || (angular.isDefined(this.model.datasourceIds[0]) && this.model.datasourceIds[0] !== "HIVE")); // TODO remove "HIVE"
                }
                if (!useSqlModel) {
                    useSqlModel = _.chain(this.sqlModel.nodes)
                        .map(_.property("nodeAttributes"))
                        .map(_.property("attributes"))
                        .flatten(true)
                        .some(function (attr) {
                        return (attr.selected && attr.description !== null);
                    })
                        .value();
                }
            }
            var source = useSqlModel ? this.sqlModel : this.sql;
            // if we dont have any datasources, default it to Hive data source
            if (this.model.$datasources == null || this.model.$datasources.length == 0) {
                this.model.$datasources = [{ id: "HIVE", name: "Hive" }]; // TODO remove "HIVE"
            }
            // Wait for query engine to load
            var onLoad = function () {
                var domainTypesLoaded = false;
                _this.sampleFormulas = _this.engine.sampleFormulas;
                if (angular.isArray(_this.model.states) && _this.model.states.length > 0) {
                    _this.engine.setQuery(source, _this.model.$datasources);
                    _this.engine.setState(_this.model.states);
                    _this.functionHistory = _this.engine.getHistory();
                }
                else {
                    _this.engine.setQuery(source, _this.model.$datasources);
                }
                // Watch for changes to field policies
                if (_this.fieldPolicies == null) {
                    _this.fieldPolicies = [];
                }
                _this.engine.setFieldPolicies(_this.fieldPolicies);
                _this.$scope.$watch(function () { return _this.fieldPolicies; }, function () {
                    _this.fieldPolicies.forEach(function (policy) {
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
                    _this.engine.setFieldPolicies(_this.fieldPolicies);
                    if (domainTypesLoaded) {
                        _this.query();
                    }
                }, true);
                // Fetch domain types
                _this.domainTypesService.findAll()
                    .then(function (domainTypes) {
                    _this.domainTypes = domainTypes;
                    domainTypesLoaded = true;
                    // Load table data
                    _this.query();
                });
            };
            if (this.engine instanceof Promise) {
                this.engine.then(function (queryEngine) {
                    _this.engine = queryEngine;
                    onLoad();
                });
            }
            else {
                onLoad();
            }
        };
        /**
         * Gets the browser height offset for the element with the specified offset from the top of this component.
         */
        TransformDataComponent.prototype.getBrowserHeightOffset = function (elementOffset) {
            return parseInt(this.heightOffset) + elementOffset;
        };
        /**
         * Show and hide the Function History
         */
        TransformDataComponent.prototype.toggleFunctionHistory = function () {
            this.isShowFunctionHistory = !this.isShowFunctionHistory;
        };
        ;
        /**
         * Toggle the visibility of the sample menu.
         */
        TransformDataComponent.prototype.toggleSampleMenu = function () {
            this.isShowSampleMenu = !this.isShowSampleMenu;
        };
        ;
        /**
         * Displays the column statistics dialog.
         */
        TransformDataComponent.prototype.showProfileDialog = function () {
            var self = this;
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
        ;
        //Callback when Codemirror has been loaded (reference is in the html page at:
        // ui-codemirror="{ onLoad : vm.codemirrorLoaded }"
        TransformDataComponent.prototype.codemirrorLoaded = function (_editor) {
            //assign the editor to a variable on this object for future reference
            this.codemirrorEditor = _editor;
            //Set the width,height of the editor. Code mirror needs an explicit width/height
            _editor.setSize(585, 25);
            _editor.on("focus", function () { return _editor.setSize(585, "auto"); });
            _editor.on("blur", function () { return _editor.setSize(585, 25); });
            //disable users ability to add new lines.  The Formula bar is only 1 line
            _editor.on("beforeChange", function (instance, change) {
                var newtext = change.text.join("").replace(/\n/g, ""); // remove ALL \n !
                change.update(change.from, change.to, [newtext]);
                return true;
            });
            //hide the scrollbar
            _editor.on("change", function (instance, change) {
                //$(".CodeMirror-hscrollbar").css('display', 'none');
            });
            //set the flag to be loaded and then call out to update Autocomplete options
            this.codemirroLoaded = true;
            this.updateCodeMirrorAutoComplete();
        };
        ;
        /**
         * Creates a Tern server.
         */
        TransformDataComponent.prototype.createTernServer = function () {
            var self = this;
            this.engine.getTernjsDefinitions().then(function (response) {
                self.engine.setFunctionDefs(response);
                self.ternServer = new CodeMirror.TernServer({ defs: [response] });
                self.ternServer.server.addDefs(self.engine.getColumnDefs());
                var _editor = self.codemirrorEditor;
                _editor.setOption("extraKeys", {
                    "Ctrl-Space": function (cm) {
                        self.ternServer.complete(cm);
                    },
                    "Ctrl-I": function (cm) {
                        self.ternServer.showType(cm);
                    },
                    "Ctrl-O": function (cm) {
                        self.ternServer.showDocs(cm);
                    },
                    "Alt-.": function (cm) {
                        self.ternServer.jumpToDef(cm);
                    },
                    "Alt-,": function (cm) {
                        self.ternServer.jumpBack(cm);
                    },
                    "Ctrl-Q": function (cm) {
                        self.ternServer.rename(cm);
                    },
                    "Ctrl-.": function (cm) {
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
        };
        /**
         * Setup the CodeMirror and Tern Server autocomplete. This will only execute when both Hive and Code Mirror are fully
         * initialized.
         */
        TransformDataComponent.prototype.updateCodeMirrorAutoComplete = function () {
            if (this.codemirroLoaded && this.hiveDataLoaded) {
                if (this.ternServer === null) {
                    this.createTernServer();
                }
                else {
                    var defs = this.engine.getColumnDefs();
                    this.ternServer.server.deleteDefs(defs["!name"]);
                    this.ternServer.server.addDefs(defs);
                }
            }
        };
        ;
        /**
         * Makes an asynchronous request to get the list of completions available at the cursor.
         *
         * @param {CodeMirror|CodeMirror.Doc} cm the code mirror instance
         * @param {Function} callback the callback function
         */
        TransformDataComponent.prototype.getHint = function (cm, callback) {
            this.ternServer.getHint(cm, function (data) {
                // Complete function calls so arg hints can be displayed
                CodeMirror.on(data, "pick", function (completion) {
                    if (completion.data.type.substr(0, 3) === "fn(") {
                        var cursor = cm.getCursor();
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
        ;
        /**
         * Shows either argument hints or identifier hints depending on the context.
         *
         * @param {CodeMirror|CodeMirror.Doc} cm the code mirror instance
         */
        TransformDataComponent.prototype.showHint = function (cm) {
            // Show args if in a function
            var cursor = cm.getCursor();
            var token = cm.getTokenAt(cursor);
            var lexer = token.state.lexical;
            if (lexer.info === "call" && token.type !== "variable") {
                this.ternServer.updateArgHints(cm);
            }
            else {
                this.ternServer.hideDoc();
            }
            // Show completions if available
            var self = this;
            function hint(cm, callback) {
                self.getHint(cm, callback);
            }
            hint.async = true;
            if (cursor.ch === 0 || token.type === "variable" || (token.string === "." && (lexer.type === "stat" || lexer.type === ")"))) {
                cm.showHint({
                    completeSingle: false,
                    hint: hint
                });
            }
        };
        ;
        /**
         * Query Hive using the query from the previous step. Set the Grids rows and columns.
         *
         * @return {Promise} a promise for when the query completes
         */
        TransformDataComponent.prototype.query = function () {
            var self = this;
            var deferred = this.$q.defer();
            //flag to indicate query is running
            this.executingQuery = true;
            this.queryProgress = 0;
            // Query Spark shell service
            var didUpdateColumns = false;
            var successCallback = function () {
                //mark the query as finished
                self.executingQuery = false;
                // Clear previous filters
                if (typeof (self.gridApi) !== "undefined") {
                    self.gridApi.core.clearAllFilters();
                }
                //mark the flag to indicate Hive is loaded
                self.hiveDataLoaded = true;
                self.isValid = true;
                //store the result for use in the commands
                self.updateGrid();
                deferred.resolve();
            };
            var errorCallback = function (message) {
                // Display error message
                self.$mdDialog.show({
                    clickOutsideToClose: true,
                    controller: (_a = /** @class */ (function () {
                            function class_1($mdDialog) {
                                this.$mdDialog = $mdDialog;
                                /**
                                 * Additional details about the error.
                                 */
                                this.detailMessage = message;
                                /**
                                 * Indicates that the detail message should be shown.
                                 */
                                this.showDetail = false;
                            }
                            /**
                             * Hides this dialog.
                             */
                            class_1.prototype.hide = function () {
                                this.$mdDialog.hide();
                            };
                            return class_1;
                        }()),
                        _a.$inject = ["$mdDialog"],
                        _a),
                    controllerAs: "dialog",
                    parent: angular.element("body"),
                    template: "\n                  <md-dialog arial-label=\"error executing the query\" style=\"max-width: 640px;\">\n                    <md-dialog-content class=\"md-dialog-content\" role=\"document\" tabIndex=\"-1\">\n                      <h2 class=\"md-title\">Error executing the query</h2>\n                      <p>There was a problem executing the query.</p>\n                      <md-button ng-if=\"!dialog.showDetail\" ng-click=\"dialog.showDetail = true\" style=\"margin: 0; padding: 0;\">Show more</md-button>\n                      <p ng-if=\"dialog.showDetail\">{{ dialog.detailMessage }}</p>\n                    </md-dialog-content>\n                    <md-dialog-actions>\n                      <md-button ng-click=\"dialog.hide()\" class=\"md-primary md-confirm-button\" md-autofocus=\"true\">Got it!</md-button>\n                    </md-dialog-actions>\n                  </md-dialog>\n                "
                });
                // Reset state
                self.executingQuery = false;
                self.engine.pop();
                self.functionHistory.pop();
                self.refreshGrid();
                deferred.reject(message);
                var _a;
            };
            var notifyCallback = function (progress) {
                self.queryProgress = progress * 100;
                if (self.engine.getColumns() !== null && !didUpdateColumns && self.ternServer !== null) {
                    didUpdateColumns = true;
                    self.updateGrid();
                }
            };
            self.engine.transform().subscribe(notifyCallback, errorCallback, successCallback);
            return deferred.promise;
        };
        ;
        TransformDataComponent.prototype.updateGrid = function () {
            var self = this;
            //transform the result to the agGrid model
            var columns = [];
            var fieldPolicies = this.engine.getFieldPolicies();
            var profile = this.engine.getProfile();
            angular.forEach(this.engine.getColumns(), function (col, index) {
                var delegate = self.engine.createColumnDelegate(col.dataType, self);
                var fieldPolicy = (col.index < fieldPolicies.length) ? fieldPolicies[index] : null;
                var longestValue = _.find(profile, function (row) {
                    return (row.columnName === col.displayName && (row.metricType === "LONGEST_STRING" || row.metricType === "MAX"));
                });
                columns.push({
                    dataType: col.dataType,
                    delegate: delegate,
                    displayName: col.displayName,
                    domainTypeId: fieldPolicy ? fieldPolicy.domainTypeId : null,
                    filters: delegate.filters,
                    headerTooltip: col.hiveColumnLabel,
                    longestValue: (angular.isDefined(longestValue) && longestValue !== null) ? longestValue.metricValue : null,
                    name: self.engine.getColumnName(col)
                });
            });
            //update the ag-grid
            this.tableColumns = columns;
            this.tableRows = this.engine.getRows();
            this.tableValidation = this.engine.getValidationResults();
            this.updateCodeMirrorAutoComplete();
        };
        /**
         * Adds formulas for column filters.
         */
        TransformDataComponent.prototype.addFilters = function () {
            var self = this;
            angular.forEach(self.tableColumns, function (column) {
                angular.forEach(column.filters, function (filter) {
                    if (filter.term) {
                        self.addColumnFilter(filter, column);
                    }
                });
            });
        };
        ;
        /**
         * Add formula for a column filter.
         *
         * @param {Object} filter the filter
         * @param {ui.grid.GridColumn} column the column
         */
        TransformDataComponent.prototype.addColumnFilter = function (filter, column) {
            // Generate formula for filter
            var formula;
            var safeTerm = (column.delegate.dataCategory === column_delegate_1.DataCategory.NUMERIC) ? filter.term : "'" + StringUtils.quote(filter.term) + "'";
            var verb;
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
                    var query = "%" + filter.term.replace("%", "%%") + "%";
                    formula = "filter(like(" + column.field + ", '" + StringUtils.quote(query) + "'))";
                    verb = "containing";
                    break;
                default:
                    throw new Error("Unsupported filter condition: " + filter.condition);
            }
            // Add formula
            var name = "Find " + column.displayName + " " + verb + " " + filter.term;
            this.pushFormula(formula, { formula: formula, icon: filter.icon, name: name });
        };
        ;
        /**
         * Adds the specified formula to the current script and refreshes the table data.
         *
         * @param {string} formula the formula
         * @param {TransformContext} context the UI context for the transformation
         */
        TransformDataComponent.prototype.addFunction = function (formula, context) {
            this.addFilters();
            this.pushFormula(formula, context, true);
        };
        ;
        /**
         * Appends the specified formula to the current script.
         *
         * @param {string} formula - the formula
         * @param {TransformContext} context - the UI context for the transformation
         * @param {boolean} doQuery - true to immediately execute the query
         */
        TransformDataComponent.prototype.pushFormula = function (formula, context, doQuery) {
            if (doQuery === void 0) { doQuery = false; }
            // Covert to a syntax tree
            this.ternServer.server.addFile("[doc]", formula);
            var file = this.ternServer.server.findFile("[doc]");
            // Add to the Spark script
            try {
                this.engine.push(file.ast, context);
            }
            catch (e) {
                var alert_1 = this.$mdDialog.alert()
                    .parent($('body'))
                    .clickOutsideToClose(true)
                    .title("Error executing the query")
                    .textContent(e.message)
                    .ariaLabel("error executing the query")
                    .ok("Got it!");
                this.$mdDialog.show(alert_1);
                console.log(e);
                return;
            }
            // Add to function history
            this.functionHistory.push(context);
            if (doQuery || this.engine.getRows() === null) {
                this.query();
            }
        };
        ;
        /**
         * Sets the formula in the function bar to the specified value.
         *
         * @param {string} formula the formula
         */
        TransformDataComponent.prototype.setFormula = function (formula) {
            this.currentFormula = formula;
            this.codemirrorEditor.setValue(formula);
            this.codemirrorEditor.focus();
            this.selectNextTabStop();
        };
        ;
        /**
         * Selects the next uppercase word in the formula bar.
         */
        TransformDataComponent.prototype.selectNextTabStop = function () {
            var match = /\b[A-Z]{2,}\b/.exec(this.currentFormula);
            if (match !== null) {
                this.codemirrorEditor.setSelection(new CodeMirror.Pos(0, match.index), new CodeMirror.Pos(0, match.index + match[0].length));
            }
            else {
                this.codemirrorEditor.setCursor(0, this.currentFormula.length);
            }
        };
        ;
        /**
         * Refreshes the grid. Used after undo and redo.
         */
        TransformDataComponent.prototype.refreshGrid = function () {
            var columns = this.engine.getColumns();
            var rows = this.engine.getRows();
            if (columns === null || rows === null) {
                this.query();
            }
            else {
                this.updateGrid();
            }
        };
        ;
        /**
         * Refreshes the table content.
         */
        TransformDataComponent.prototype.resample = function () {
            this.addFilters();
            this.query();
        };
        //noinspection JSUnusedGlobalSymbols
        TransformDataComponent.prototype.onUndo = function () {
            this.engine.undo();
            this.functionHistory.pop();
            this.refreshGrid();
        };
        ;
        //noinspection JSUnusedGlobalSymbols
        TransformDataComponent.prototype.onRedo = function () {
            var func = this.engine.redo();
            this.functionHistory.push(func);
            this.refreshGrid();
        };
        ;
        TransformDataComponent.prototype.canUndo = function () {
            return (this.engine && this.engine.canUndo) ? this.engine.canUndo() : false;
        };
        ;
        TransformDataComponent.prototype.canRedo = function () {
            return (this.engine && this.engine.canRedo) ? this.engine.canRedo() : false;
        };
        ;
        /**
         * Update the feed model when changing from this transform step to a different step
         */
        TransformDataComponent.prototype.onStepChange = function (event, changedSteps) {
            var self = this;
            var thisIndex = parseInt(this.stepIndex);
            if (changedSteps.oldStep === thisIndex) {
                this.saveToFeedModel().then(function () {
                    // notify those that the data is loaded/updated
                    self.BroadcastService.notify('DATA_TRANSFORM_SCHEMA_LOADED', 'SCHEMA_LOADED');
                });
            }
            else if (changedSteps.newStep === thisIndex && this.sql == null) {
                this.ngOnInit();
            }
        };
        /**
         * Saves the current transformation to the feed model.
         *
         * @returns {Promise} signals when the save is complete
         */
        TransformDataComponent.prototype.saveToFeedModel = function () {
            var _this = this;
            // Add unsaved filters
            this.addFilters();
            // Check if updates are necessary
            var feedModel = this.FeedService.createFeedModel;
            var newScript = this.engine.getFeedScript();
            if (newScript === feedModel.dataTransformation.dataTransformScript) {
                var result = this.$q.defer();
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
            var deferred = this.$q.defer();
            var fields = this.engine.getFields();
            if (fields !== null) {
                this.FeedService.setTableFields(fields, this.engine.getFieldPolicies());
                this.FeedService.syncTableFieldPolicyNames();
                this.engine.save();
                deferred.resolve(true);
            }
            else {
                this.query().then(function () {
                    _this.FeedService.setTableFields(_this.engine.getFields(), _this.engine.getFieldPolicies());
                    _this.FeedService.syncTableFieldPolicyNames();
                    _this.engine.save();
                    deferred.resolve(true);
                });
            }
            return deferred.promise;
        };
        /**
         * Reset the sample or limit value when the sample method changes.
         */
        TransformDataComponent.prototype.onSampleMethodChange = function () {
            if (this.sampleMethod === "SAMPLE") {
                this.engine.sample(0.1);
            }
            else if (this.sampleMethod === "LIMIT") {
                this.engine.limit(1000);
            }
        };
        /**
         * Sets the domain type for the specified field.
         *
         * @param columnIndex - the field index
         * @param domainTypeId - the domain type id
         */
        TransformDataComponent.prototype.setDomainType = function (columnIndex, domainTypeId) {
            var _this = this;
            var domainType = (domainTypeId != null) ? this.domainTypes.find(function (domainType) { return domainType.id === domainTypeId; }) : { fieldPolicy: { standardization: null, validation: null } };
            if (domainType) {
                var fieldPolicies_1 = (this.engine.getFieldPolicies() !== null) ? this.engine.getFieldPolicies() : [];
                this.fieldPolicies = this.engine.getColumns().map(function (column, index) {
                    var fieldPolicy;
                    if (index < fieldPolicies_1.length) {
                        fieldPolicy = fieldPolicies_1[index];
                    }
                    else {
                        fieldPolicy = _this.FeedService.newTableFieldPolicy(column.hiveColumnLabel);
                        fieldPolicy.fieldName = column.hiveColumnLabel;
                        fieldPolicy.feedFieldName = column.hiveColumnLabel;
                    }
                    if (index === columnIndex) {
                        _this.FeedService.setDomainTypeForField({}, fieldPolicy, domainType);
                    }
                    return fieldPolicy;
                });
            }
        };
        __decorate([
            core_1.Input(),
            __metadata("design:type", query_engine_1.QueryEngine)
        ], TransformDataComponent.prototype, "engine", void 0);
        __decorate([
            core_1.Input(),
            __metadata("design:type", Object)
        ], TransformDataComponent.prototype, "model", void 0);
        return TransformDataComponent;
    }());
    exports.TransformDataComponent = TransformDataComponent;
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
});
//# sourceMappingURL=transform-data.component.js.map