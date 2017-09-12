var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/core", "./services/query-engine", "feed-mgr/visual-query/VisualQueryTable"], function (require, exports, core_1, query_engine_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require("feed-mgr/visual-query/module-name");
    /**
     * Transform Data step of the Visual Query page.
     */
    var TransformDataComponent = (function () {
        /**
         * Constructs a {@code TransformDataComponent}.
         */
        function TransformDataComponent($scope, $http, $q, $mdDialog, RestUrlService, SideNavService, uiGridConstants, FeedService, BroadcastService, StepperService, WindowUnloadService) {
            this.$scope = $scope;
            this.$http = $http;
            this.$q = $q;
            this.$mdDialog = $mdDialog;
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
                lineWrapping: false,
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
            this.ngOnInit();
        }
        TransformDataComponent.prototype.ngOnInit = function () {
            this.sql = this.model.sql;
            this.sqlModel = this.model.chartViewModel;
            this.selectedColumnsAndTables = this.model.$selectedColumnsAndTables;
            this.sampleFormulas = this.engine.sampleFormulas;
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
                        .filter(function (attr) {
                        return (attr.selected && attr.description !== null);
                    });
                }
            }
            var source = useSqlModel ? this.sqlModel : this.sql;
            // if we dont have any datasources, default it to Hive data source
            if (this.model.$datasources == null || this.model.$datasources.length == 0) {
                this.model.$datasources = [{ id: "HIVE", name: "Hive" }]; // TODO remove "HIVE"
            }
            if (angular.isArray(this.model.states) && this.model.states.length > 0) {
                this.engine.setQuery(source, this.model.$datasources);
                this.engine.setState(this.model.states);
                this.functionHistory = this.engine.getHistory();
            }
            else {
                this.engine.setQuery(source, this.model.$datasources);
            }
            // Load table data
            this.query();
        };
        /**
         * Show and hide the Function History
         */
        TransformDataComponent.prototype.toggleFunctionHistory = function () {
            this.isShowFunctionHistory = !this.isShowFunctionHistory;
            this.isShowSampleMenu = false;
        };
        ;
        /**
         * Toggle the visibility of the sample menu.
         */
        TransformDataComponent.prototype.toggleSampleMenu = function () {
            this.isShowSampleMenu = !this.isShowSampleMenu;
            this.isShowFunctionHistory = false;
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
                templateUrl: "js/feed-mgr/visual-query/profile-stats-dialog.html"
            });
        };
        ;
        //Callback when Codemirror has been loaded (reference is in the html page at:
        // ui-codemirror="{ onLoad : vm.codemirrorLoaded }"
        TransformDataComponent.prototype.codemirrorLoaded = function (_editor) {
            //assign the editor to a variable on this object for future reference
            this.codemirrorEditor = _editor;
            //Set the width,height of the editor. Code mirror needs an explicit width/height
            _editor.setSize(700, 25);
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
                var alert = self.$mdDialog.alert()
                    .parent($('body'))
                    .clickOutsideToClose(true)
                    .title("Error executing the query")
                    .textContent(message)
                    .ariaLabel("error executing the query")
                    .ok("Got it!");
                self.$mdDialog.show(alert);
                // Reset state
                self.executingQuery = false;
                self.engine.pop();
                self.functionHistory.pop();
                self.refreshGrid();
                deferred.reject(message);
            };
            var notifyCallback = function (progress) {
                self.queryProgress = progress * 100;
            };
            self.engine.transform().subscribe(notifyCallback, errorCallback, successCallback);
            return deferred.promise;
        };
        ;
        TransformDataComponent.prototype.updateGrid = function () {
            var self = this;
            //transform the result to the agGrid model
            var columns = [];
            var profile = this.engine.getProfile();
            angular.forEach(this.engine.getColumns(), function (col) {
                var delegate = self.engine.createColumnDelegate(col.dataType, self);
                var longestValue = _.find(profile, function (row) {
                    return (row.columnName === col.displayName && (row.metricType === "LONGEST_STRING" || row.metricType === "MAX"));
                });
                columns.push({
                    delegate: delegate,
                    displayName: col.displayName,
                    filters: delegate.filters,
                    headerTooltip: col.hiveColumnLabel,
                    longestValue: (angular.isDefined(longestValue) && longestValue !== null) ? longestValue.metricValue : null,
                    name: self.engine.getColumnName(col)
                });
            });
            //update the ag-grid
            this.tableColumns = columns;
            this.tableRows = this.engine.getRows();
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
            var verb;
            switch (filter.condition) {
                case this.uiGridConstants.filter.LESS_THAN:
                    formula = "filter(lessThan(" + column.field + ", \"" + StringUtils.quote(filter.term) + "\"))";
                    verb = "less than";
                    break;
                case this.uiGridConstants.filter.GREATER_THAN:
                    formula = "filter(greaterThan(" + column.field + ", \"" + StringUtils.quote(filter.term) + "\"))";
                    verb = "greater than";
                    break;
                case this.uiGridConstants.filter.EXACT:
                    formula = "filter(equal(" + column.field + ", \"" + StringUtils.quote(filter.term) + "\"))";
                    verb = "equal to";
                    break;
                case this.uiGridConstants.filter.CONTAINS:
                    var query = "%" + filter.term.replace("%", "%%") + "%";
                    formula = "filter(like(" + column.field + ", \"" + StringUtils.quote(query) + "\"))";
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
            this.pushFormula(formula, context);
            this.query();
        };
        ;
        /**
         * Appends the specified formula to the current script.
         *
         * @param {string} formula the formula
         * @param {TransformContext} context the UI context for the transformation
         */
        TransformDataComponent.prototype.pushFormula = function (formula, context) {
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
            return (typeof this.engine === "object") ? this.engine.canUndo() : false;
        };
        ;
        TransformDataComponent.prototype.canRedo = function () {
            return (typeof this.engine === "object") ? this.engine.canRedo() : false;
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
                this.FeedService.setTableFields(fields);
                this.engine.save();
                deferred.resolve(true);
            }
            else {
                var self_1 = this;
                this.query().then(function () {
                    self_1.FeedService.setTableFields(self_1.engine.getFields());
                    self_1.engine.save();
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
        return TransformDataComponent;
    }());
    __decorate([
        core_1.Input(),
        __metadata("design:type", query_engine_1.QueryEngine)
    ], TransformDataComponent.prototype, "engine", void 0);
    __decorate([
        core_1.Input(),
        __metadata("design:type", Object)
    ], TransformDataComponent.prototype, "model", void 0);
    exports.TransformDataComponent = TransformDataComponent;
    angular.module(moduleName)
        .controller('VisualQueryTransformController', ["$scope", "$http", "$q", "$mdDialog", "RestUrlService", "SideNavService", "uiGridConstants", "FeedService",
        "BroadcastService", "StepperService", "WindowUnloadService", TransformDataComponent])
        .directive('thinkbigVisualQueryTransform', function () {
        return {
            bindToController: {
                engine: "=",
                model: "=",
                stepIndex: "@"
            },
            controller: "VisualQueryTransformController",
            controllerAs: "$td",
            require: ["thinkbigVisualQueryTransform", "^thinkbigStepper"],
            restrict: "E",
            templateUrl: "js/feed-mgr/visual-query/transform-data.template.html",
            link: function ($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                //store a reference to the stepper if needed
                thisController.stepperController = controllers[1];
            }
        };
    });
});
//# sourceMappingURL=transform-data.component.js.map