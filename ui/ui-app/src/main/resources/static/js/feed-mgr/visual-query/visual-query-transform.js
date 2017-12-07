define(["angular", "feed-mgr/visual-query/module-name", "feed-mgr/visual-query/VisualQueryTable"], function (angular, moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            require: ['thinkbigVisualQueryTransform', '^thinkbigStepper'],
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/feed-mgr/visual-query/visual-query-transform.html',
            controller: "VisualQueryTransformController",
            link: function ($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                //store a reference to the stepper if needed
                thisController.stepperController = controllers[1];
            }

        };
    };

    var controller = function ($scope, $log, $http, $q, $mdDialog, $mdToast, RestUrlService, VisualQueryService, HiveService, SideNavService, SparkShellService, VisualQueryColumnDelegate,
                               uiGridConstants, FeedService, BroadcastService, StepperService, WindowUnloadService) {
        var self = this;
        //The model passed in from the previous step
        this.model = VisualQueryService.model;
        //Flag to determine if we can move on to the next step
        this.isValid = false;
        //The SQL String from the previous step
        this.sql = this.model.visualQuerySql;
        //The sql model passed over from the previous step
        this.sqlModel = this.model.visualQueryModel;
        //The array of columns with their respective Table, schema and alias passed over from the previous step
        //{column:'name', alias:'alias',tableName:'table name',tableColumn:'alias_name',dataType:'dataType'}
        this.selectedColumnsAndTables = this.model.selectedColumnsAndTables;
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

        /**
         * Translates expressions into Spark code.
         * @type {SparkShellService}
         */
        this.sparkShellService = function () {
            var model = FeedService.createFeedModel.dataTransformation;

            // Select source model
            var useSqlModel = false;
            if (angular.isObject(self.sqlModel)) {
                // Check for non-Hive datasource
                if (angular.isArray(model.datasourceIds) && model.datasourceIds.length > 0) {
                    useSqlModel = (model.datasourceIds.length > 1 || (angular.isDefined(model.datasourceIds[0]) && model.datasourceIds[0].id !== VisualQueryService.HIVE_DATASOURCE));
                }
                if (!useSqlModel) {
                    useSqlModel = _.chain(self.sqlModel.nodes)
                        .map(_.property("nodeAttributes"))
                        .map(_.property("attributes"))
                        .flatten(true)
                        .filter(function (attr) {
                            return (attr.selected && attr.description !== null);
                        });
                }
            }

            var source = useSqlModel ? self.sqlModel : self.sql;

            // if we dont have any datasources, default it to Hive data source
            if(model.datasources == null || model.datasources.length == 0){
                model.datasources = [{id:VisualQueryService.HIVE_DATASOURCE,name:"Hive"}];
            }
            if (angular.isArray(model.states) && model.states.length > 0) {
                var service = new SparkShellService(source, model.states, model.datasources);
                self.functionHistory = service.getHistory();
                return service;
            } else {
                return new SparkShellService(source, null, model.datasources);
            }
        }();

        this.executingQuery = false;
        //Code Mirror options.  Tern Server requires it be in javascript mode
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
         * Show and hide the Function History
         */
        this.toggleFunctionHistory = function () {
            self.isShowFunctionHistory = !self.isShowFunctionHistory;
            self.isShowSampleMenu = false;
        };

        /**
         * Toggle the visibility of the sample menu.
         */
        this.toggleSampleMenu = function () {
            self.isShowSampleMenu = !self.isShowSampleMenu;
            self.isShowFunctionHistory = false;
        };

        /**
         * Displays the column statistics dialog.
         */
        this.showProfileDialog = function () {
            $mdDialog.show({
                clickOutsideToClose: false,
                controller: "VisualQueryProfileStatsController",
                fullscreen: true,
                locals: {
                    profile: self.sparkShellService.getProfile()
                },
                parent: angular.element(document.body),
                templateUrl: "js/feed-mgr/visual-query/profile-stats-dialog.html"
            });
        };

        //Callback when Codemirror has been loaded (reference is in the html page at:
        // ui-codemirror="{ onLoad : vm.codemirrorLoaded }"
        this.codemirrorLoaded = function (_editor) {
            //assign the editor to a variable on this object for future reference
            self.codemirrorEditor = _editor;
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
            self.codemirroLoaded = true;
            self.updateCodeMirrorAutoComplete();
        };

        /**
         * Creates a Tern server.
         */
        function createTernServer() {
            $http.get(RestUrlService.UI_BASE_URL + "/spark-functions").then(function (response) {
                self.sparkShellService.setFunctionDefs(response.data);

                self.ternServer = new CodeMirror.TernServer({defs: [response.data]});
                self.ternServer.server.addDefs(self.sparkShellService.getColumnDefs());

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
                _editor.on("cursorActivity", self.showHint);
                _editor.on("focus", self.showHint);
                _editor.focus();
            });
        }

        /**
         * Setup the CodeMirror and Tern Server autocomplete. This will only execute when both Hive and Code Mirror are fully
         * initialized.
         */
        this.updateCodeMirrorAutoComplete = function () {
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
        this.getHint = function (cm, callback) {
            self.ternServer.getHint(cm, function (data) {
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
        this.getHint.async = true;

        /**
         * Shows either argument hints or identifier hints depending on the context.
         *
         * @param {CodeMirror|CodeMirror.Doc} cm the code mirror instance
         */
        this.showHint = function (cm) {
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
            if (cursor.ch === 0 || token.type === "variable" || (token.string === "." && (lexer.type === "stat" || lexer.type === ")"))) {
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
        this.query = function () {
            //flag to indicate query is running
            this.executingQuery = true;
            this.queryProgress = 0;

            // Query Spark shell service
            var successCallback = function () {
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
                updateGrid();
            };
            var errorCallback = function (message) {
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
            var notifyCallback = function (progress) {
                self.queryProgress = progress * 100;
            };

            return self.sparkShellService.transform().then(successCallback, errorCallback, notifyCallback);
        };

        function updateGrid() {
            //transform the result to the agGrid model
            var columns = [];
            var profile = self.sparkShellService.getProfile();

            angular.forEach(self.sparkShellService.getColumns(), function (col) {
                var delegate = new VisualQueryColumnDelegate(col.dataType, self);
                var longestValue = _.find(profile, function (row) {
                    return (row.columnName === col.displayName && (row.metricType === "LONGEST_STRING" || row.metricType === "MAX"))
                });

                columns.push({
                    delegate: delegate,
                    displayName: col.displayName,
                    filters: delegate.filters,
                    headerTooltip: col.hiveColumnLabel,
                    longestValue: ( angular.isDefined(longestValue) && longestValue !== null) ? longestValue.metricValue : null,
                    name: col.displayName
                });
            });

            //update the ag-grid
            self.tableColumns = columns;
            self.tableRows = self.sparkShellService.getRows();

            self.updateCodeMirrorAutoComplete();
        }

        /**
         * Adds formulas for column filters.
         */
        this.addFilters = function () {
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
        this.addColumnFilter = function (filter, column) {
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
        this.addFunction = function (formula, context) {
            self.addFilters();
            self.pushFormula(formula, context);
            self.query();
        };

        /**
         * Appends the specified formula to the current script.
         *
         * @param {string} formula the formula
         * @param {TransformContext} context the UI context for the transformation
         */
        this.pushFormula = function (formula, context) {
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
         * Sets the formula in the function bar to the specified value.
         *
         * @param {string} formula the formula
         */
        this.setFormula = function (formula) {
            self.currentFormula = formula;
            self.codemirrorEditor.setValue(formula);
            self.codemirrorEditor.focus();
            self.selectNextTabStop();
        };

        /**
         * Selects the next uppercase word in the formula bar.
         */
        this.selectNextTabStop = function () {
            var match = /\b[A-Z]{2,}\b/.exec(self.currentFormula);
            if (match !== null) {
                self.codemirrorEditor.setSelection(new CodeMirror.Pos(0, match.index), new CodeMirror.Pos(0, match.index + match[0].length));
            } else {
                self.codemirrorEditor.setCursor(0, self.currentFormula.length);
            }
        };

        /**
         * Called when the user clicks Add on the function bar
         */
        this.onAddFunction = function () {
            self.addFunction(self.currentFormula, {formula: self.currentFormula, icon: "code", name: self.currentFormula});
        };

        /**
         * Refreshes the grid. Used after undo and redo.
         */
        this.refreshGrid = function () {
            var columns = self.sparkShellService.getColumns();
            var rows = self.sparkShellService.getRows();

            if (columns === null || rows === null) {
                self.query();
            } else {
                updateGrid();
            }
        };

        //noinspection JSUnusedGlobalSymbols
        this.onUndo = function () {
            self.sparkShellService.undo();
            self.functionHistory.pop();
            this.refreshGrid();
        };

        //noinspection JSUnusedGlobalSymbols
        this.onRedo = function () {
            var func = this.sparkShellService.redo();
            self.functionHistory.push(func);
            this.refreshGrid();
        };

        this.canUndo = function () {
            return this.sparkShellService.canUndo();
        };
        this.canRedo = function () {
            return this.sparkShellService.canRedo();
        };

        //Listen for when the next step is active
        BroadcastService.subscribe($scope, StepperService.STEP_CHANGED_EVENT, updateModel);

        /**
         * Update the feed model when changing from this transform step to a different step
         * @param event
         * @param changedSteps
         */
        function updateModel(event, changedSteps) {
            var thisIndex = parseInt(self.stepIndex);
            if (changedSteps.oldStep === thisIndex) {
                saveToFeedModel().then(function () {
                    // notify those that the data is loaded/updated
                    BroadcastService.notify('DATA_TRANSFORM_SCHEMA_LOADED', 'SCHEMA_LOADED');
                });
            } else if (changedSteps.newStep === thisIndex && self.sql == null) {
                var functionDefs = self.sparkShellService.getFunctionDefs();

                self.sql = self.model.visualQuerySql;
                self.sparkShellService = new SparkShellService(self.sql, null, FeedService.createFeedModel.dataTransformation.datasources);
                self.sparkShellService.setFunctionDefs(functionDefs);
                self.query();
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

            // Ensure correct table method
            var feedModel = FeedService.createFeedModel;
            feedModel.table.method = "EXISTING_TABLE";

            // Check if updates are necessary
            var newScript = self.sparkShellService.getFeedScript();
            if (newScript === feedModel.dataTransformation.dataTransformScript) {
                var result = $q.defer();
                result.reject(true);
                return result.promise;
            }

            // Populate Feed Model from the Visual Query Model
            feedModel.dataTransformation.dataTransformScript = newScript;
            feedModel.dataTransformation.states = self.sparkShellService.save();

            feedModel.table.existingTableName = "";
            feedModel.table.sourceTableSchema.name = "";

            // Get list of fields
            var deferred = $q.defer();
            var fields = self.sparkShellService.getFields();

            if (fields !== null) {
                FeedService.setTableFields(fields);
                self.sparkShellService.save();
                deferred.resolve(true);
            } else {
                self.query().then(function () {
                    FeedService.setTableFields(self.sparkShellService.getFields());
                    self.sparkShellService.save();
                    deferred.resolve(true);
                });
            }

            return deferred.promise;
        }

        //Hide the left side nav bar
        SideNavService.hideSideNav();

        // Display prompt on window unload
        WindowUnloadService.setText("You will lose any unsaved changes. Are you sure you want to continue?");

        // Load table data
        this.query();

        // Invalidate when SQL changes
        $scope.$watch(
            function () {
                return self.model.visualQuerySql;
            },
            function () {
                if (self.sql !== self.model.visualQuerySql) {
                    self.isValid = false;
                    self.sql = null;
                }
            }
        );
    };

    angular.module(moduleName).controller('VisualQueryTransformController', ["$scope", "$log", "$http", "$q", "$mdDialog", "$mdToast", "RestUrlService", "VisualQueryService", "HiveService",
                                                                             "SideNavService", "SparkShellService", "VisualQueryColumnDelegate", "uiGridConstants", "FeedService", "BroadcastService",
                                                                             "StepperService", "WindowUnloadService", controller]);
    angular.module(moduleName).directive('thinkbigVisualQueryTransform', directive);
});
