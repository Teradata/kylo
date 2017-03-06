define(['angular',"feed-mgr/visual-query/module-name"], function (angular,moduleName) {

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

    /** Prefix for table aliases */
    var TABLE_PREFIX = "tbl";

    var controller = function($scope, $log, $http, $mdToast, $mdDialog, $document, Utils, RestUrlService, HiveService, SideNavService, StateService, VisualQueryService, FeedService) {

        var self = this;
        this.model = VisualQueryService.model;
        this.isValid = false;
        this.stepNumber = parseInt(this.stepIndex) + 1;
        this.stepperController = null;

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
            searchTextChange: function(text) {

            },
            selectedItemChange: function(table) {

            },
            querySearch: function(txt) {
                return HiveService.queryTablesSearch(txt);

            }
        };

        this.onAddTable = function() {
            SideNavService.hideSideNav();
            self.onTableClick(self.tablesAutocomplete.selectedTable);
            self.tablesAutocomplete.clear();
        };

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

        /**
         * Called after a user Adds a table to fetch the Columns and datatypes
         * @param schema
         * @param table
         * @param callback
         * @returns {HttpPromise}
         */
        function getTableSchema(schema, table, callback) {
            var successFn = function(response) {
                callback(response.data);
            };
            var errorFn = function(err) {
                self.loading = false;
            };
            var promise = $http.get(RestUrlService.HIVE_SERVICE_URL + "/schemas/" + schema + "/tables/" + table);
            promise.then(successFn, errorFn);
            return promise;
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
            } else if (typeof(self.chartViewModel.nodes) !== "undefined") {
                self.isValid = (self.chartViewModel.nodes.length > 0);

                self.model.visualQueryModel = self.chartViewModel.data;
                var sql = getSQLModel();
                self.model.visualQuerySql = sql;
                self.model.selectedColumnsAndTables = self.selectedColumnsAndTables;

                var feedModel = FeedService.createFeedModel;
                feedModel.dataTransformation.chartViewModel = angular.copy(self.chartViewModel.data);
                feedModel.dataTransformation.sql = sql;
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
         * turn on and off sql mode
         * TODO more work needs to be done to get it working with the tables
         *
         */
        this.toggleAdvancedMode = function() {

            if (self.advancedMode == false) {
                //todo alert user you cannot go back to drag/drop
                self.advancedMode = true;
                self.advancedModeText = 'Visual Mode'
            }
            else {
                self.advancedMode = false;
                self.model.visualQuerySql = '';
                self.advancedModeText = 'Advanced Mode';
                //TODO reset the canvas model
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
                    x: coord.x,
                    y: coord.y,
                    nodeAttributes: {
                        attributes: schemaData.fields,
                        selected: [],
                        sql: "`" + StringUtils.quoteSql(table.schema) + "`.`" + StringUtils.quoteSql(table.tableName) + "`"
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
         * Adds joins for the specified table to a SQL statement.
         *
         * @param {TableInfo} tableInfo the table to search for joins
         * @param {TableJoinMap} graph the table join map
         * @param {string[]} fromTables the list of tables to include in the FROM clause
         * @param {string[]} joinClauses the list of JOIN clauses
         */
        function addTableJoins(tableInfo, graph, fromTables, joinClauses) {
            // Add JOIN clauses for tables connected to this one
            var edges = [];
            var srcID = tableInfo.data.id;
            graph[srcID].seen = true;

            angular.forEach(graph[srcID].edges, function(connection, dstID) {
                if (connection !== null) {
                    joinClauses.push(getJoinSQL(tableInfo.data, graph[dstID].data, connection, graph));
                    edges.push(dstID);
                    graph[srcID].edges[dstID] = null;
                    graph[dstID].edges[srcID] = null;
                }
            });

            // Add table to FROM clause if it's the root of a JOIN tree
            if (edges.length !== 0 && fromTables !== null) {
                fromTables.push(tableInfo.data.nodeAttributes.sql + " " + TABLE_PREFIX + tableInfo.data.id);
            }

            // Add JOIN clauses for tables connected to child nodes
            angular.forEach(edges, function(nodeID) {
                addTableJoins(graph[nodeID], graph, null, joinClauses);
            });
        }

        /**
         * A map of node IDs to the node model and connections.
         *
         * @typedef {Object.<number, TableInfo>} TableJoinMap
         */

        /**
         * A dictionary with the node model and connections.
         *
         * @typedef {{data: Object, edges: Object.<number, Object>, seen: boolean}} TableInfo
         */

        /**
         * Creates a map indicating how tables may be joined. The key is the node ID and the value is a dictionary containing the node model and the connections for the joins.
         *
         * @returns {TableJoinMap} the table join map
         */
        function createTableJoinMap() {
            var map = {};

            // Add every node to the map
            angular.forEach(self.chartViewModel.data.nodes, function(node) {
                map[node.id] = {data: node, edges: {}, seen: false};
            });

            // Add edges to the map
            angular.forEach(self.chartViewModel.data.connections, function(connection) {
                map[connection.source.nodeID].edges[connection.dest.nodeID] = connection;
                map[connection.dest.nodeID].edges[connection.source.nodeID] = connection;
            });

            return map;
        }

        /**
         * Generates a list of possible aliases for the specified column.
         *
         * @param tableName the name of the table
         * @param columnName the name of the column
         * @returns {string[]} the list of aliases
         */
        function getColumnAliases(tableName, columnName) {
            var aliases = [];
            if (columnName !== "processing_dttm") {
                aliases.push(columnName);
            }
            aliases.push(tableName.replace(/.*\./, "") + "_" + columnName, tableName.replace(".", "_") + "_" + columnName);
            return aliases;
        }

        /**
         * Generates the SQL for joining two tables. The destination table will be added to the SQL statement as part of the JOIN clause.
         *
         * @param {Object} src the node for the source table
         * @param {Object} dst the node for the destination table
         * @param {Object} connection the join description
         * @param {TableJoinMap} graph the table join map
         * @return {string} the JOIN statement
         */
        function getJoinSQL(src, dst, connection, graph) {
            // Use default text if missing join keys
            if (typeof(connection.joinKeys.destKey) === "undefined" || typeof(connection.joinKeys.sourceKey) === "undefined") {
                return "JOIN " + dst.nodeAttributes.sql + " " + TABLE_PREFIX + dst.id;
            }

            // Create JOIN clause
            graph[dst.id].seen = true;

            var sql = connection.joinType + " " + dst.nodeAttributes.sql + " " + TABLE_PREFIX + dst.id + " ON " + TABLE_PREFIX + dst.id + ".`";
            sql += StringUtils.quoteSql((connection.source.nodeID === src.id) ? connection.joinKeys.sourceKey : connection.joinKeys.destKey);
            sql += "` = " + TABLE_PREFIX + src.id + ".`";
            sql += StringUtils.quoteSql((connection.source.nodeID === src.id) ? connection.joinKeys.destKey : connection.joinKeys.sourceKey);
            sql += "`";

            // Add conditions for 'seen' tables
            var conditions = _.values(graph[dst.id].edges)
                    // Filter for tables already added in the SQL query
                    .filter(function(edge) {
                        return (edge != null && edge.source.nodeID != src.id && graph[edge.source.nodeID].seen && edge.dest.nodeID != src.id && graph[edge.dest.nodeID].seen);
                    })
                    // Build join condition
                    .map(function(edge) {
                        var condition = TABLE_PREFIX + edge.source.nodeID + ".`" + StringUtils.quoteSql(edge.joinKeys.sourceKey) + "` = ";
                        condition += TABLE_PREFIX + edge.dest.nodeID + ".`" + StringUtils.quoteSql(edge.joinKeys.destKey) + "`";

                        // Remove join from graph
                        graph[edge.source.nodeID].edges[edge.dest.nodeID] = null;
                        graph[edge.dest.nodeID].edges[edge.source.nodeID] = null;

                        return condition;
                    });
            if (conditions.length > 0) {
                sql += " AND " + conditions.join(" AND ");
            }

            return sql;
        }

        /**
         * Parses the tables on the canvas and returns a SQL string, along with populating the self.selectedColumnsAndTables array of objects.
         *
         * @returns {string} the SQL string
         */
        function getSQLModel() {
            // Check and reset state
            self.selectedColumnsAndTables = [];

            if (self.chartViewModel.data.nodes.length === 0) {
                return "";
            }

            // Determine a unique alias for each column
            var aliasCount = {};

            angular.forEach(self.chartViewModel.data.nodes, function(node) {
                angular.forEach(node.nodeAttributes.attributes, function(attr) {
                    if (attr.selected) {
                        angular.forEach(getColumnAliases(node.name, attr.name), function(alias) {
                            aliasCount[alias] = (typeof(aliasCount[alias]) !== "undefined") ? aliasCount[alias] + 1 : 1;
                        });
                    }
                });
            });

            // Build FROM and JOIN clauses
            var fromTables = [];
            var graph = createTableJoinMap();
            var joinClauses = [];

            angular.forEach(graph, function(node) {
                if (_.size(node.edges) === 0) {
                    fromTables.push(node.data.nodeAttributes.sql + " " + TABLE_PREFIX + node.data.id);
                }
                else {
                    addTableJoins(node, graph, fromTables, joinClauses);
                }
            });

            // Build SELECT statement
            var select = "";

            angular.forEach(self.chartViewModel.data.nodes, function(node) {
                var table = TABLE_PREFIX + node.id;
                angular.forEach(node.nodeAttributes.attributes, function(attr) {
                    if (attr.selected) {
                        // Determine column alias
                        var alias = _.find(getColumnAliases(node.name, attr.name), function(name){ return (aliasCount[name] === 1) });
                        if (typeof(alias) === "undefined") {
                            var i = 0;
                            do {
                                ++i;
                                alias = attr.name + "_" + i;
                            } while (aliasCount[alias] > 0);
                            aliasCount[alias] = 1;
                        }

                        // Add column to clause
                        select += (select.length === 0) ? "SELECT " : ", ";
                        select += table + ".`" + StringUtils.quoteSql(attr.name) + "`";
                        if (alias !== attr.name) {
                            select += " AS `" + StringUtils.quoteSql(alias) + "`";
                        }
                        self.selectedColumnsAndTables.push({
                            column: attr.name,
                            alias: TABLE_PREFIX + node.id, tableName: node.name,
                            tableColumn: attr.name, dataType: attr.dataType
                        });
                    }
                });
            });

            // Return SQL
            var sql = "";

            angular.forEach(fromTables, function(table) {
                sql += (sql.length === 0) ? select + " FROM " : ", ";
                sql += table;
            });
            angular.forEach(joinClauses, function(join) {
                sql += " " + join;
            });

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

        //setup the flowchart Model
        setupFlowChartModel();

        //validate when the page loads
        validate();
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


    angular.module(moduleName).controller('VisualQueryBuilderController', ["$scope","$log","$http","$mdToast","$mdDialog","$document","Utils","RestUrlService","HiveService","SideNavService","StateService","VisualQueryService","FeedService",controller]);
    angular.module(moduleName).directive('thinkbigVisualQueryBuilder', directive);

    angular.module(moduleName).controller('ConnectionDialog', ["$scope","$mdDialog","isNew","connectionDataModel","source","dest",ConnectionDialog]);





});

