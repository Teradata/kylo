(function() {

    var directive = function() {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            require: ['thinkbigVisualQueryTransform', '^thinkbigStepper'],
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/visual-query/visual-query-transform.html',
            controller: "VisualQueryTransformController",
            link: function($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                //store a reference to the stepper if needed
                thisController.stepperController = stepperController;
            }

        };
    };

    var controller = function($scope, $log, $http, $q, $mdDialog, $mdToast, RestUrlService, VisualQueryService, HiveService,
                              TableDataFunctions, SideNavService, SparkShellService, VisualQueryColumnDelegate, uiGridConstants, FeedService, BroadcastService,StepperService) {
        var self = this;
        //The model passed in from the previous step
        this.model = VisualQueryService.model;
        //Flag to determine if we can move on to the next step
        this.isValid = true;
        //The SQL String from the previous step
        this.sql = this.model.visualQuerySql;
        //The sql model passed over from the previous step
        this.sqlModel = this.model.visualQueryModel;
        //The array of columns with their respective Table, schema and alias passed over from the previous step
        //{column:'name', alias:'alias',tableName:'table name',tableColumn:'alias_name',dataType:'dataType'}
        this.selectedColumnsAndTables = this.model.selectedColumnsAndTables;
        //The query result transformed to the ag-grid model  (@see HiveService.transformToAgGrid
        this.tableData = {columns: [], rows: []};
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

        //Function Command Holder
        //@see TableDataFunctions.js
        this.functionCommandHolder = null;

        //Flag to show/hide function history panel
        this.isShowFunctionHistory = false;

        // Flag to show/hide sample menu
        this.isShowSampleMenu = false;

        //Setup initial grid options
        this.gridOptions = {
            columnDefs: [],
            data: null,
            enableColumnResizing: true,
            enableFiltering: true,
            flatEntityAccess: true,
            onRegisterApi: function(grid) {
                self.gridApi = grid;
                grid.colMovable.on.columnPositionChanged($scope, angular.bind(self, self.onColumnMove));
            }
        };

        // Translates expressions into Spark code
        this.sparkShellService = new SparkShellService(this.sql);

        //Code Mirror options.  Tern Server requires it be in javascript mode
        this.codemirrorOptions = {
            lineWrapping: false,
            indentWithTabs: false,
            smartIndent: false,
            lineNumbers: false,
            matchBrackets: false,
            autofocus: true,
            mode: 'javascript',
            scrollbarStyle: null
        };

        /**
         * Show and hide the Funciton History
         */
        this.toggleFunctionHistory = function() {
            self.isShowFunctionHistory = !self.isShowFunctionHistory;
            self.isShowSampleMenu = false;
        };

        /**
         * Toggle the visibility of the sample menu.
         */
        this.toggleSampleMenu = function() {
            self.isShowSampleMenu = !self.isShowSampleMenu;
            self.isShowFunctionHistory = false;
        };

        //Callback when Codemirror has been loaded (reference is in the html page at:
        // ui-codemirror="{ onLoad : vm.codemirrorLoaded }"
        this.codemirrorLoaded = function(_editor) {
            //assign the editor to a variable on this object for future reference
            self.codemirrorEditor = _editor;
            //Set the width,height of the editor. Code mirror needs an explicit width/height
            _editor.setSize(700, 25);

            //disable users ability to add new lines.  The Formula bar is only 1 line
            _editor.on("beforeChange", function(instance, change) {
                var newtext = change.text.join("").replace(/\n/g, ""); // remove ALL \n !
                change.update(change.from, change.to, [newtext]);
                return true;
            });

            //hide the scrollbar
            _editor.on("change", function(instance, change) {
                //$(".CodeMirror-hscrollbar").css('display', 'none');
            });
            //set the flag to be loaded and then call out to update Autocomplete options
            self.codemirroLoaded = true;
            self.updateCodeMirrorAutoComplete();
        };

        /**
         * Creates a Tern server.
         */
        function createTernServer() {
            $http.get('js/vendor/tern/defs/tableFunctions.json').success(function(code) {
                self.sparkShellService.setFunctionDefs(code);

                self.ternServer = new CodeMirror.TernServer({defs: [code]});
                self.ternServer.server.addDefs(self.sparkShellService.getColumnDefs());

                var _editor = self.codemirrorEditor;
                _editor.setOption("extraKeys", {
                    "Ctrl-Space": function(cm) {
                        self.ternServer.complete(cm);
                    },
                    "Ctrl-I": function(cm) {
                        self.ternServer.showType(cm);
                    },
                    "Ctrl-O": function(cm) {
                        self.ternServer.showDocs(cm);
                    },
                    "Alt-.": function(cm) {
                        self.ternServer.jumpToDef(cm);
                    },
                    "Alt-,": function(cm) {
                        self.ternServer.jumpBack(cm);
                    },
                    "Ctrl-Q": function(cm) {
                        self.ternServer.rename(cm);
                    },
                    "Ctrl-.": function(cm) {
                        self.ternServer.selectName(cm);
                    }
                });
                _editor.on("blur", function() {
                    self.ternServer.hideDoc();
                });
                _editor.on("cursorActivity", self.showHint);
                _editor.on("focus", self.showHint);
            });
        }

        /**
         * Setup the CodeMirror and Tern Server autocomplete. This will only execute when both Hive and Code Mirror are fully
         * initialized.
         */
        this.updateCodeMirrorAutoComplete = function() {
            if (self.codemirroLoaded && self.hiveDataLoaded) {
                if (self.ternServer === null) {
                    createTernServer();
                } else {
                    var defs = self.sparkShellService.getColumnDefs();
                    self.ternServer.server.deleteDefs(defs["!name"]);
                    self.ternServer.server.addDefs(defs);
                }
            }
        };

        /**
         * Makes an asynchronous request to get the list of completions available at the cursor.
         *
         * @param {CodeMirror|CodeMirror.Doc} cm the code mirror instance
         * @param {Function} callback the callback function
         */
        this.getHint = function(cm, callback) {
            self.ternServer.getHint(cm, function(data) {
                // Complete function calls so arg hints can be displayed
                CodeMirror.on(data, "pick", function(completion) {
                    if (completion.data.type.substr(0, 3) === "fn(") {
                        var cursor = cm.getCursor();
                        cm.replaceRange("(", cursor, cursor, "complete");
                    }
                });

                // Display hints
                callback(data);
            });
        };
        this.getHint.async = true;

        /**
         * Shows either argument hints or identifier hints depending on the context.
         *
         * @param {CodeMirror|CodeMirror.Doc} cm the code mirror instance
         */
        this.showHint = function(cm) {
            // Show args if in a function
            var cursor = cm.getCursor();
            var token = cm.getTokenAt(cursor);
            var lexer = token.state.lexical;

            if (lexer.info === "call" && token.type !== "variable") {
                self.ternServer.updateArgHints(cm);
            } else {
                self.ternServer.hideDoc();
            }

            // Show completions if available
            if (cursor.ch === 0 || token.type === "variable" || (token.string === "." && lexer.type === "stat")) {
                cm.showHint({
                    completeSingle: false,
                    hint: self.getHint
                });
            }
        };

        /**
         * Query Hive using the query from the previous step. Set the Grids rows and columns.
         * 
         * @return {Promise} a promise for when the query completes
         */
        this.query = function() {
            //flag to indicate query is running
            this.executingQuery = true;

            // Query Spark shell service
            var successCallback = function() {
                //mark the query as finished
                self.executingQuery = false;

                // Clear previous filters
                if (typeof(self.gridApi) !== "undefined") {
                    self.gridApi.core.clearAllFilters();
                }

                //mark the flag to indicate Hive is loaded
                self.hiveDataLoaded = true;

                //store the result for use in the commands
                self.tableData = {rows: self.sparkShellService.getRows(), columns: self.sparkShellService.getColumns()};
                updateGrid(self.tableData);

                //Initialize the Command function holder
                self.functionCommandHolder = TableDataFunctions.newCommandHolder(self.tableData);
            };
            var errorCallback = function(message) {
                // Display error message
                var alert = $mdDialog.alert()
                        .parent($('body'))
                        .clickOutsideToClose(true)
                        .title("Error executing the query")
                        .textContent(message)
                        .ariaLabel("error executing the query")
                        .ok("Got it!");
                $mdDialog.show(alert);

                // Reset state
                self.executingQuery = false;
                self.sparkShellService.pop();
                self.functionHistory.pop();
                self.refreshGrid();
            };

            return self.sparkShellService.transform().then(successCallback, errorCallback);
        };

        function updateGrid(tableData) {
            //transform the result to the agGrid model
            var columns = [];

            angular.forEach(tableData.columns, function(col) {
                var delegate = new VisualQueryColumnDelegate(col.dataType, self);
                columns.push({
                    delegate: delegate,
                    displayName: col.displayName,
                    filters: delegate.filters,
                    headerCellTemplate: "visual-query/grid-header-cell",
                    headerTooltip: col.hiveColumnLabel,
                    minWidth: 150,
                    name: col.displayName,
                    queryResultColumn: col
                });
            });

            //update the ag-grid
            self.gridApi.grid.moveColumns.orderCache = [];
            self.gridOptions.columnDefs = columns;
            self.gridOptions.data = tableData.rows;

            self.updateCodeMirrorAutoComplete();
        }

        /**
         * Adds formulas for column filters.
         */
        this.addFilters = function() {
            angular.forEach(self.gridApi.grid.columns, function(column) {
                angular.forEach(column.filters, function(filter) {
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
        this.addColumnFilter = function(filter, column) {
            // Generate formula for filter
            var formula;
            var verb;

            switch (filter.condition) {
                case uiGridConstants.filter.LESS_THAN:
                    formula = "filter(lessThan(" + column.field + ", \"" + StringUtils.quote(filter.term) + "\"))";
                    verb = "less than";
                    break;

                case uiGridConstants.filter.GREATER_THAN:
                    formula = "filter(greaterThan(" + column.field + ", \"" + StringUtils.quote(filter.term) + "\"))";
                    verb = "greater than";
                    break;

                case uiGridConstants.filter.EXACT:
                    formula = "filter(equal(" + column.field + ", \"" + StringUtils.quote(filter.term) + "\"))";
                    verb = "equal to";
                    break;

                case uiGridConstants.filter.CONTAINS:
                    var query = "%" + filter.term.replace("%", "%%") + "%";
                    formula = "filter(like(" + column.field + ", \"" + StringUtils.quote(query) + "\"))";
                    verb = "containing";
                    break;

                default:
                    throw new Error("Unsupported filter condition: " + filter.condition);
            }

            // Add formula
            var name = "Find " + column.displayName + " " + verb + " " + filter.term;
            self.pushFormula(formula, {formula: formula, icon: filter.icon, name: name});
        };

        /**
         * Adds the specified formula to the current script and refreshes the table data.
         *
         * @param {string} formula the formula
         * @param {TransformContext} context the UI context for the transformation
         */
        this.addFunction = function(formula, context) {
            var tableData = self.functionCommandHolder.executeStr(formula);
            if (tableData != null && tableData != undefined) {
                updateGrid(tableData);

                self.addFilters();
                self.pushFormula(formula, context);
                self.query();
            }
        };

        /**
         * Appends the specified formula to the current script.
         *
         * @param {string} formula the formula
         * @param {TransformContext} context the UI context for the transformation
         */
        this.pushFormula = function(formula, context) {
            // Covert to a syntax tree
            self.ternServer.server.addFile("[doc]", formula);
            var file = self.ternServer.server.findFile("[doc]");

            // Add to the Spark script
            try {
                self.sparkShellService.push(file.ast, context);
            } catch (e) {
                var alert = $mdDialog.alert()
                        .parent($('body'))
                        .clickOutsideToClose(true)
                        .title("Error executing the query")
                        .textContent(e.message)
                        .ariaLabel("error executing the query")
                        .ok("Got it!");
                $mdDialog.show(alert);
                console.log(e);
                return;
            }

            // Add to function history
            self.functionHistory.push(context);
        };

        /**
         * Called when the user clicks Add on the function bar
         */
        this.onAddFunction = function() {
            self.addFunction(self.currentFormula, {formula: self.currentFormula, icon: "code", name: self.currentFormula});
        };

        /**
         * Called when the column ordering changes.
         */
        this.onColumnMove = function() {
            var formula = "";

            angular.forEach(self.gridApi.grid.columns, function(column) {
                formula += (formula.length === 0) ? "select(" : ", ";
                formula += column.field;
            });

            formula += ")";
            self.pushFormula(formula, {formula: formula, icon: "reorder", name: "Reorder columns"});
        };

        /**
         * Refreshes the grid. Used after undo and redo.
         */
        this.refreshGrid = function() {
            var columns = self.sparkShellService.getColumns();
            var rows = self.sparkShellService.getRows();

            if (columns === null || rows === null) {
                self.query();
            }
            else {
                updateGrid({columns: self.sparkShellService.getColumns(), rows: self.sparkShellService.getRows()});
            }
        };

        this.onUndo = function() {
            self.sparkShellService.undo();
            self.functionHistory.pop();
            this.refreshGrid();
        };
        this.onRedo = function() {
            var func = this.sparkShellService.redo();
            self.functionHistory.push(func);
            this.refreshGrid();
        };

        this.canUndo = function() {
            return this.sparkShellService.canUndo();
        };
        this.canRedo = function() {
            return this.sparkShellService.canRedo();
        };

        /**
         * Drops the function in the history at the specified index.
         *
         * @param {number} index the index of the function to drop
         */
        this.dropHistory = function(index) {
            self.sparkShellService.splice(index + 1, 1);
            self.functionHistory.splice(index, 1);
            this.refreshGrid();
        };

        //Listen for when the next step is active
        BroadcastService.subscribe($scope,StepperService.ACTIVE_STEP_EVENT,onNextStepActive)

        function onNextStepActive(event,index){
            var thisIndex = parseInt(self.stepIndex);
            //if we are on the next step then call out to get the model
            if(index == thisIndex +1) {
              saveToFeedModel().then(function(){
                  BroadcastService.notify('DATA_TRANSFORM_SCHEMA_LOADED','SCHEMA_LOADED');
              });
            }
        }

        /**
         * Saves the current transformation to the feed model.
         *
         * @returns {Promise} signals when the save is complete
         */
        function saveToFeedModel() {
            // Add unsaved filters
            self.addFilters();

            // Populate Feed Model from the Visual Query Model
            var feedModel = FeedService.createFeedModel;
            feedModel.dataTransformation.dataTransformScript = self.sparkShellService.getScript();
            feedModel.dataTransformation.formulas = _.map(self.functionHistory, function(history) {
                return history.formula;
            });

            feedModel.table.existingTableName = "";
            feedModel.table.method = "EXISTING_TABLE";
            feedModel.table.sourceTableSchema.name = "";

            // Get list of fields
            var deferred = $q.defer();
            var fields = self.sparkShellService.getFields();

            if (fields !== null) {
                FeedService.setTableFields(fields);
                self.sparkShellService.saveGlobalState();
                deferred.resolve(true);
            } else {
                self.query().then(function() {
                    FeedService.setTableFields(self.sparkShellService.getFields());
                    self.sparkShellService.saveGlobalState();
                    deferred.resolve(true);
                });
            }

            return deferred.promise;
        }

        //Hide the left side nav bar
        SideNavService.hideSideNav();

        // Load table data
        if (FeedService.createFeedModel.dataTransformation.formulas.length > 0 && self.sparkShellService.loadGlobalState()) {
            self.functionHistory = self.sparkShellService.getHistory();
        } else {
            SparkShellService.clearGlobalState();
        }
        this.query();

        $scope.$on('$destroy', function() {
            //clean up code here
        });
    };

    angular.module(MODULE_FEED_MGR).controller('VisualQueryTransformController', controller);
    angular.module(MODULE_FEED_MGR)
            .directive('thinkbigVisualQueryTransform', directive);
})();
