define(['angular',"feed-mgr/visual-query/module-name","feed-mgr/visual-query/module"], function (angular,moduleName) {

    var directive = function() {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            require: ['thinkbigVisualQueryBuilder', '^thinkbigStepper'],
            scope: {},
            controllerAs: '$vq',
            templateUrl: 'js/feed-mgr/visual-query/visual-query-builder.html',
            controller: "VisualQueryBuilderController",
            link: function($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                thisController.stepperController = controllers[1];
            }

        };
    };

    var controller = function($scope, $log, $http, $mdToast, $mdDialog, $document, Utils, RestUrlService, HiveService, SideNavService, StateService, VisualQueryService, FeedService,
                              DatasourcesService) {

        var self = this;
        this.model = VisualQueryService.model;
        this.isValid = false;
        this.stepNumber = parseInt(this.stepIndex) + 1;
        this.stepperController = null;

        /**
         * List of available data sources.
         * @type {Array.<JdbcDatasource>}
         */
        self.availableDatasources = [{id: VisualQueryService.HIVE_DATASOURCE, name: "Hive"}];

        /**
         * Indicates that there was an error retrieving the list of tables.
         * @type {boolean} true if there was an error or false otherwise
         */
        self.databaseConnectionError = false;

        /**
         * List of the data sources used in model.
         * @type {Array.<string>}
         */
        self.selectedDatasourceIds = [];

        SideNavService.hideSideNav();

        //Allow for SQL editing
        if (typeof(self.model.visualQueryModel) === "undefined" && typeof(self.model.visualQuerySql) !== "undefined") {
            this.advancedMode = true;
            this.advancedModeText = 'Visual Mode';
        } else {
            this.advancedMode = false;
            this.advancedModeText = 'Advanced Mode';
        }

        // holds the metadata about each column and table that is used to build the SQL str in the getSQLModel() method
        this.selectedColumnsAndTables = [];

        //Flow Chart Variables

        //
        // Code for the delete key.
        //
        var deleteKeyCode = 46;

        //
        // Code for control key.
        //
        var ctrlKeyCode = 17;

        //
        // Set to true when the ctrl key is down.
        //
        var ctrlDown = false;

        //
        // Code for A key.
        //
        var aKeyCode = 65;

        //
        // Code for esc key.
        //
        var escKeyCode = 27;

        //
        // Selects the next node id.
        //
        var nextNodeID = 10;

        var chartDataModel = {};

        this.advancedModeSql = function(opt_sql) {
            if (arguments.length === 1) {
                self.model.visualQuerySql = opt_sql;
                validate();
            }
            return self.model.visualQuerySql;
        };

        this.tablesAutocomplete = {
            clear: function() {
                this.searchText = '';
                this.selectedTable = null;
            },
            searchText: '',
            selectedTable: null,
            noCache: true,
            searchTextChange: function(text) {

            },
            selectedItemChange: function(table) {

            },
            querySearch: function(txt) {
                if (self.model.selectedDatasourceId === VisualQueryService.HIVE_DATASOURCE) {
                    return HiveService.queryTablesSearch(txt);
                } else {
                    return DatasourcesService.listTables(self.model.selectedDatasourceId, txt)
                        .then(function (tables) {
                            self.databaseConnectionError = false;
                            return tables;
                        }, function () {
                            self.databaseConnectionError = true;
                            return [];
                        });
                }
            },
            refreshCache: function(){
                HiveService.init();
                var searchText = this.searchText.trim();
                angular.element('#tables-auto-complete').focus().val(searchText).trigger('change')
            }
        };

        this.refreshAutocompleteCache = function(){
            self.tablesAutocomplete.refreshCache();
        }

        this.onAddTable = function() {
            SideNavService.hideSideNav();
            self.onTableClick(self.tablesAutocomplete.selectedTable);
            self.tablesAutocomplete.clear();
        };

        /**
         * Initialize state from services.
         */
        function init() {
            // Get the list of data sources
            DatasourcesService.findAll()
                .then(function (datasources) {
                    Array.prototype.push.apply(self.availableDatasources, datasources);
                })
                .finally(function () {
                    // Get state if in Edit Feed mode
                    var feedModel = FeedService.createFeedModel.dataTransformation;
                    if (angular.isUndefined(self.model.visualQuerySql) && angular.isString(feedModel.sql)) {
                        self.advancedMode = (angular.isUndefined(self.model.visualQuerySql) && feedModel.chartViewModel == null);
                        self.model.visualQuerySql = feedModel.sql;
                        if (angular.isArray(feedModel.datasourceIds)) {
                            self.model.selectedDatasourceId = feedModel.datasourceIds[0];
                        }
                    }

                    // Setup the flowchart Model
                    setupFlowChartModel();

                    // Validate when the page loads
                    validate();
                });
        }

        /**
         * Initialize the model for the flowchart.
         */
        function setupFlowChartModel() {
            // Load data model
            var chartDataModel;
            if (typeof(self.model.visualQueryModel) !== "undefined") {
                chartDataModel = self.model.visualQueryModel;
            } else if (FeedService.createFeedModel.dataTransformation.chartViewModel !== null) {
                chartDataModel = FeedService.createFeedModel.dataTransformation.chartViewModel;
            } else {
                chartDataModel = {"nodes": [], "connections": []};
            }

            // Prepare nodes
            angular.forEach(chartDataModel.nodes, function(node) {
                // Add utility functions
                self.prepareNode(node);

                // Determine next node ID
                nextNodeID = Math.max(node.id + 1, nextNodeID);
            });

            // Create view model
            self.chartViewModel = new flowchart.ChartViewModel(chartDataModel, self.onCreateConnectionCallback, self.onEditConnectionCallback, self.onDeleteSelectedCallback);
        }

        this.setupFlowChartModel = setupFlowChartModel;

        /**
         * Called after a user Adds a table to fetch the Columns and datatypes.
         * @param {string} schema the schema name
         * @param {string} table the table name
         * @param callback the callback function
         * @returns {HttpPromise}
         */
        function getTableSchema(schema, table, callback) {
            var promise;
            if (self.model.selectedDatasourceId === VisualQueryService.HIVE_DATASOURCE) {
                promise = $http.get(RestUrlService.HIVE_SERVICE_URL + "/schemas/" + schema + "/tables/" + table)
                    .then(function (response) {
                        return response.data;
                    });
            } else {
                promise = DatasourcesService.getTableSchema(self.model.selectedDatasourceId, table, schema);
            }

            return promise.then(callback, function () {
                self.loading = false;
            });
        }

        /**
         * Validate the canvas.
         * If there is at least one table defined, it is valid
         * TODO enhance to check if there are any tables without connections
         */
        function validate() {
            if (self.advancedMode) {
                var sql = self.advancedModeSql();
                self.isValid = (typeof(sql) !== "undefined" && sql.length > 0);

                delete self.model.selectedColumnsAndTables;
                delete self.model.visualQueryModel;

                var feedModel = FeedService.createFeedModel;
                feedModel.dataTransformation.sql = self.model.visualQuerySql;
                feedModel.dataTransformation.chartViewModel = null;
                feedModel.dataTransformation.datasourceIds = (self.model.selectedDatasourceId !== VisualQueryService.HIVE_DATASOURCE) ? [self.model.selectedDatasourceId] : [];
                feedModel.dataTransformation.datasources = DatasourcesService.filterArrayByIds(self.model.selectedDatasourceId, self.availableDatasources);
            } else if (typeof(self.chartViewModel.nodes) !== "undefined") {
                self.isValid = (self.chartViewModel.nodes.length > 0);

                self.model.visualQueryModel = self.chartViewModel.data;
                var sql = getSQLModel();
                self.model.visualQuerySql = sql;
                self.model.selectedColumnsAndTables = self.selectedColumnsAndTables;

                var feedModel = FeedService.createFeedModel;
                feedModel.dataTransformation.chartViewModel = angular.copy(self.chartViewModel.data);
                feedModel.dataTransformation.sql = sql;
                feedModel.dataTransformation.datasourceIds = self.selectedDatasourceIds.filter(function (id) { return id !== VisualQueryService.HIVE_DATASOURCE; });
                feedModel.dataTransformation.datasources = DatasourcesService.filterArrayByIds(self.selectedDatasourceIds, self.availableDatasources);
            } else {
                self.isValid = false;
            }
        }

        function getNewXYCoord() {
            var coord = {x: 20, y: 20};
            //attempt to align it on the top
            if (self.chartViewModel.data.nodes.length > 0) {
                //constants
                var yThreshold = 150;
                var tableWidth = 250;

                //reduce the set to just show those in the top row
                var tables = _.filter(self.chartViewModel.data.nodes, function(table) {
                    return table.y <= yThreshold;
                });
                //sort by x then y (underscore sort is reverse thinking)
                tables = _.chain(tables).sortBy('y').sortBy('x').value();
                var lastX = coord.x;
                _.some(tables, function(table) {
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
        this.toggleAdvancedMode = function() {
            if (self.advancedMode === false) {
                var goAdvanced = function () {
                    self.advancedMode = true;
                    self.advancedModeText = "Visual Mode";
                };
                if (self.chartViewModel.nodes.length > 0) {
                    $mdDialog.show(
                        $mdDialog.confirm()
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
                self.advancedMode = false;
                self.model.visualQuerySql = "";
                self.advancedModeText = "Advanced Mode";
            }

        };

        /**
         * Adds utility functions to a node data model.
         *
         * @param {Object} node the node data model
         */
        this.prepareNode = function(node) {
            /**
             * Indicates if all of the attributes are selected.
             *
             * @returns {boolean} {@code true} if all attributes are selected, or {@code false} otherwise
             */
            node.nodeAttributes.hasAllSelected = function() {
                return _.every(this.attributes, function(attr) {return attr.selected});
            };

            /**
             * Selects the specified attribute.
             *
             * @param {Object} attr the attribute to be selected
             */
            node.nodeAttributes.select = function(attr) {
                attr.selected = true;
                this.selected.push(attr);
                validate();
            };

            /**
             * Selects all attributes.
             */
            node.nodeAttributes.selectAll = function() {
                var selected = [];
                angular.forEach(this.attributes, function(attr) {
                    attr.selected = true;
                    selected.push(attr);
                });
                this.selected = selected;
                validate();
            };

            /**
             * Deselects the specified attribute.
             *
             * @param {Object} attr the attribute to be deselected
             */
            node.nodeAttributes.deselect = function(attr) {
                attr.selected = false;
                var idx = this.selected.indexOf(attr);
                if (idx > -1) {
                    this.selected.splice(idx, 1);
                }
                validate();
            };

            /**
             * Deselects all attributes.
             */
            node.nodeAttributes.deselectAll = function() {
                angular.forEach(this.attributes, function(attr) {
                    attr.selected = false;
                });
                this.selected = [];
                validate();
            };
        };

        //
        // Add a new node to the chart.
        //
        this.onTableClick = function(table) {

            //get attributes for table
            var datasourceId = self.model.selectedDatasourceId;
            var nodeName = table.schema + "." + table.tableName;
            getTableSchema(table.schema, table.tableName, function(schemaData) {
                //
                // Template for a new node.
                //
                var coord = getNewXYCoord();

                angular.forEach(schemaData.fields, function(attr) {
                    attr.selected = true;
                });
                var newNodeDataModel = {
                    name: nodeName,
                    id: nextNodeID++,
                    datasourceId: datasourceId,
                    x: coord.x,
                    y: coord.y,
                    nodeAttributes: {
                        attributes: schemaData.fields,
                        reference: [table.schema, table.tableName],
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
                self.prepareNode(newNodeDataModel);
                self.chartViewModel.addNode(newNodeDataModel);
                validate();
            })

        };

        //
        // Event handler for key-down on the flowchart.
        //
        $document.bind('keydown', function(evt) {
            if (evt.keyCode === ctrlKeyCode) {

                ctrlDown = true;
                evt.stopPropagation();
                evt.preventDefault();
            }
        });

        //
        // Event handler for key-up on the flowchart.
        //
        $document.bind('keyup', function(evt) {

            if (evt.keyCode === deleteKeyCode) {
                //
                // Delete key.
                //
                self.chartViewModel.deleteSelected();
                validate();
            }

            if (evt.keyCode == aKeyCode && ctrlDown) {
                //
                // Ctrl + A
                //
                self.chartViewModel.selectAll();
            }

            if (evt.keyCode == escKeyCode) {
                // Escape.
                self.chartViewModel.deselectAll();
            }

            if (evt.keyCode === ctrlKeyCode) {
                ctrlDown = false;

                evt.stopPropagation();
                evt.preventDefault();
            }
        });

        /**
         * Parses the tables on the canvas and returns a SQL string, along with populating the self.selectedColumnsAndTables array of objects.
         *
         * @returns {string|null} the SQL string or null if multiple data sources are used
         */
        function getSQLModel() {
            var builder = VisualQueryService.sqlBuilder(self.chartViewModel.data);
            var sql = builder.build();

            self.selectedColumnsAndTables = builder.getSelectedColumnsAndTables();
            self.selectedDatasourceIds = builder.getDatasourceIds();
            return sql;
        }

        this.getSQLModel = getSQLModel;

        /**
         * When a connection is edited
         * @param connectionViewModel
         * @param connectionDataModel
         * @param source
         * @param dest
         */
        this.onEditConnectionCallback = function(connectionViewModel, connectionDataModel, dest, source) {
            self.showConnectionDialog(false, connectionViewModel, connectionDataModel, source, dest);
        };

        /**
         * When a connection is created
         * @param connectionViewModel
         * @param connectionDataModel
         * @param source
         * @param dest
         * @param inputConnection
         * @param outputConnection
         */
        this.onCreateConnectionCallback = function(connectionViewModel, connectionDataModel, dest, source, inputConnection, outputConnection) {
            // Ensure connection is unique
            var newDestID = dest.data.id;
            var newSourceID = source.data.id;

            for (var i=0; i < self.chartViewModel.data.connections.length - 1; ++i) {
                var oldDestID = self.chartViewModel.data.connections[i].dest.nodeID;
                var oldSourceID = self.chartViewModel.data.connections[i].source.nodeID;
                if ((oldDestID === newDestID && oldSourceID === newSourceID) || (oldDestID === newSourceID && oldSourceID === newDestID)) {
                    // Delete connection
                    self.chartViewModel.deselectAll();
                    connectionViewModel.select();
                    self.chartViewModel.deleteSelected();

                    // Display error message
                    var alert = $mdDialog.alert()
                            .parent($('body'))
                            .clickOutsideToClose(true)
                            .title("Duplicate join")
                            .textContent("There is already a join between those two tables. Please edit the existing join or switch to advanced mode.")
                            .ariaLabel("joins must be unique")
                            .ok("Got it!");
                    $mdDialog.show(alert);
                    return;
                }
            }

            // Add connection
            self.showConnectionDialog(true, connectionViewModel, connectionDataModel, source, dest);
            validate();
        };

        /**
         * Called when the current selection is deleted.
         */
        this.onDeleteSelectedCallback = function() {
            validate();
        };

        this.showConnectionDialog = function(isNew, connectionViewModel, connectionDataModel, source, dest) {
            self.chartViewModel.deselectAll();
            $mdDialog.show({
                controller: 'ConnectionDialog',
                templateUrl: 'js/feed-mgr/visual-query/visual-query-builder-connection-dialog.html',
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
            .then(function(msg) {
                if (msg === "delete" || (isNew && msg === "cancel")) {
                    connectionViewModel.select();
                    self.chartViewModel.deleteSelected();
                }
                validate();
            });
        };

        $scope.$on('$destroy', function() {
            SideNavService.showSideNav();
            $document.unbind('keydown');
            $document.unbind('keypress');
            $document.unbind('keyup');

        });

        // Initialize state
        init();

        // Ensure Kylo Spark Shell is running
        $http.post(RestUrlService.SPARK_SHELL_SERVICE_URL + "/start");
    };


    function ConnectionDialog($scope, $mdDialog, isNew, connectionDataModel, source, dest) {

        $scope.isValid = false;
        $scope.connectionDataModel = angular.copy(connectionDataModel);
        $scope.source = angular.copy(source);
        $scope.dest = angular.copy(dest);
        $scope.joinTypes = [{name: "Inner Join", value: "INNER JOIN"}, {name: "Left Join", value: "LEFT JOIN"}, {name: "Right Join", value: "RIGHT JOIN"}];
        $scope.isNew = isNew;

        if (isNew) {
            //attempt to auto find matches
            var sourceNames = [];
            var destNames = [];
            angular.forEach(source.data.nodeAttributes.attributes, function(attr) {
                sourceNames.push(attr.name);
            });

            angular.forEach(dest.data.nodeAttributes.attributes, function(attr) {
                destNames.push(attr.name);
            });

            var matches = _.intersection(sourceNames, destNames);
            if (matches && matches.length && matches.length > 0) {
                var col = matches[0];
                if (matches.length > 1) {
                    if (matches[0] == 'id') {
                        col = matches[1];
                    }
                }
                $scope.connectionDataModel.joinKeys.sourceKey = col;
                $scope.connectionDataModel.joinKeys.destKey = col;
                $scope.connectionDataModel.joinType = "INNER JOIN"
            }
        }

        $scope.onJoinTypeChange = function() {
            //    .log('joinType changed')
        };

        $scope.hide = function() {
            $mdDialog.hide();
        };

        $scope.validate = function() {
            $scope.isValid =
                $scope.connectionDataModel.joinType != '' && $scope.connectionDataModel.joinType != null && $scope.connectionDataModel.joinKeys.sourceKey != null
                && $scope.connectionDataModel.joinKeys.destKey != null;
        };

        $scope.save = function() {

            connectionDataModel.name = $scope.connectionDataModel.name;
            connectionDataModel.joinType = $scope.connectionDataModel.joinType;
            connectionDataModel.joinKeys = $scope.connectionDataModel.joinKeys;

            $mdDialog.hide('save');
        };

        $scope.cancel = function() {
            $mdDialog.hide('cancel');
        };

        $scope.delete = function() {
            $mdDialog.hide('delete');
        };

        $scope.validate();

    }


    angular.module(moduleName).controller('VisualQueryBuilderController', ["$scope","$log","$http","$mdToast","$mdDialog","$document","Utils","RestUrlService","HiveService","SideNavService",
                                                                           "StateService","VisualQueryService","FeedService","DatasourcesService",controller]);
    angular.module(moduleName).directive('thinkbigVisualQueryBuilder', directive);

    angular.module(moduleName).controller('ConnectionDialog', ["$scope","$mdDialog","isNew","connectionDataModel","source","dest",ConnectionDialog]);
});

