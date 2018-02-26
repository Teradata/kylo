define(['angular','services/module-name'], function (angular,moduleName) {
    angular.module(moduleName).factory('HiveService', ["$q","$http","$mdDialog","$timeout","RestUrlService",function ($q, $http, $mdDialog,$timeout, RestUrlService) {

        var data = {
            refreshTableCache: function () {
                return $http.get(RestUrlService.HIVE_SERVICE_URL + "/refreshUserHiveAccessCache");
            },
            queryTablesSearch: function (query) {
                var self = this;
                var deferred = $q.defer();
                self.getTables(null, query).then(function (response) {
                    deferred.resolve(self.parseTableResponse(response.data));
                });
                return deferred.promise;
            },
            parseTableResponse: function (response) {
                var schemaTables = {};
                var allTables = [];
                if (response) {
                    angular.forEach(response, function (table) {
                        var schema = table.substr(0, table.indexOf("."));
                        var tableName = table.substr(table.indexOf(".") + 1);
                        if (schemaTables[schema] == undefined) {
                            schemaTables[schema] = [];
                        }
                        allTables.push({schema: schema, tableName: tableName, fullName: table, fullNameLower: table.toLowerCase()});
                    })
                }
                return allTables;
            },
            getTables: function (schema, table) {
                var self = this;
                var successFn = function (response) {
                    return self.parseTableResponse(response.data);
                };
                var errorFn = function (err) {
                    self.loading = false;
                };
                var params = {schema: schema, table: table, refreshCache: self.refreshCache};
                var promise = $http.get(RestUrlService.HIVE_SERVICE_URL + "/tables", {params: params});
                promise.then(successFn, errorFn);
                return promise;
            },
            getTablesAndColumns: function () {
                return $http.get(RestUrlService.HIVE_SERVICE_URL + "/table-columns", {timeout: 30000});
            },
            queryResult: function (query) {
                var self = this;
                if (this.query != null) {
                    var successFn = function (response) {
                        var tableData = response.data;
                        return tableData;

                    }
                    var errorFn = function (err) {
                        self.loadingHiveSchemas = false;
                        $mdDialog.show(
                            $mdDialog.alert()
                                .parent(angular.element(document.querySelector('body')))
                                .clickOutsideToClose(true)
                                .title('Error executing the Query')
                                .textContent('Error querying the data for ' + query)
                                .ariaLabel('Error browsing the data')
                                .ok('Got it!')
                            //.targetEvent(ev)
                        );
                    }

                    var config = {params: {query: query}};
                    var promise = $http.get(RestUrlService.HIVE_SERVICE_URL + "/query-result", config);
                    promise.then(successFn, errorFn);
                    return promise;
                }
            },
            query: function (query) {
                var self = this;
                if (this.query != null) {
                    var successFn = function (response) {
                        var tableData = response.data;
                        return tableData;

                    }
                    var errorFn = function (err) {
                        self.loadingHiveSchemas = false;
                        $mdDialog.show(
                            $mdDialog.alert()
                                .parent(angular.element(document.querySelector('body')))
                                .clickOutsideToClose(true)
                                .title('Error executing the Query')
                                .textContent('Error querying the data for ' + query)
                                .ariaLabel('Error browsing the data')
                                .ok('Got it!')
                            //.targetEvent(ev)
                        );
                    }

                    var config = {params: {query: query}};

                    var promise = $http.get(RestUrlService.HIVE_SERVICE_URL + "/query", config);
                    promise.then(successFn, errorFn);
                    return promise;
                }
            },
            browseTable: function (schema, table, whereCondition) {
                var self = this;
                if (schema != null && table != null) {
                    var successFn = function (response) {
                        var tableData = response.data;
                        return tableData;

                    }
                    var errorFn = function (err) {
                        self.loadingHiveSchemas = false;
                        $mdDialog.show(
                            $mdDialog.alert()
                                .parent(angular.element(document.querySelector('body')))
                                .clickOutsideToClose(true)
                                .title('Cannot browse the table')
                                .textContent('Error Browsing the data ')
                                .ariaLabel('Error browsing the data')
                                .ok('Got it!')
                            //.targetEvent(ev)
                        );
                    }
                    var promise = $http.get(RestUrlService.HIVE_SERVICE_URL + "/browse/" + schema + "/" + table, {params: {where: whereCondition}});
                    promise.then(successFn, errorFn);
                    return promise;
                }
            },
            getColumnNamesForRow: function (row) {
                var columns = [];
                var displayColumns = [];
                angular.forEach(row, function (val, name) {
                    var displayName = name;
                    if (name.indexOf('.') >= 0) {
                        displayName = name.substring(name.indexOf('.') + 1);
                    }
                    displayColumns.push(displayName)
                    columns.push(name);
                });
                return columns;

            },
            getUTCTime: function (dateStr) {
                //If the date is 14 chars long then it is in the format of yyyyMMddHHMMSS
                //otherwise its in millis
                if (dateStr.length == 14) {
                    //20160222070231
                    //20160322224705
                    var year = parseInt(dateStr.substring(0, 4));
                    var month = parseInt(dateStr.substring(4, 6));
                    var day = parseInt(dateStr.substring(6, 8));
                    var hrs = parseInt(dateStr.substring(8, 10));
                    var min = parseInt(dateStr.substring(10, 12));
                    var sec = parseInt(dateStr.substring(12, 14));
                    return Date.UTC(year, (month - 1), day, hrs, min, sec);
                }
                else {
                    //string is timestamp in millis UTC format
                    return new moment(parseInt(dateStr)).toDate();
                }
            },

            getColumnNamesForQueryResult: function (queryResult) {
                var self = this;
                if (queryResult != null) {
                    if (queryResult.data) {
                        queryResult = queryResult.data;
                    }
                    var row = queryResult[0];
                    return self.getColumnNamesForRow(row);
                }
                return null;

            },
            orderColumns: function (columns, comparator) {
                columns.sort(comparator);
            },
            /**
             * TODO ORDERING OF COLUMNS IS NOT PRESERVED!
             * @param results
             * @param hideColumns
             * @returns {{}}
             */
            transformResults: function (results, hideColumns) {
                var data = {};
                var rows = results.data;
                var columns = [];
                var displayColumns = [];
                var pivotedData = [];
                if (rows && rows.length) {
                    angular.forEach(rows[0], function (val, name) {
                        var displayName = name;
                        if (name.indexOf('.') >= 0) {
                            displayName = name.substring(name.indexOf('.') + 1);
                        }
                        if (hideColumns && (_.contains(hideColumns, displayName) || _.contains(hideColumns, name))) {

                        }
                        else {
                            displayColumns.push(displayName)
                            columns.push(name);
                        }
                    });
                }

                data.columns = columns;
                data.displayColumns = displayColumns;
                data.rows = rows;
                data.pivotData = null;
                return data;
            },
            transformQueryResultsToUiGridModel: function (queryResult, hideColumns, transformFn) {
                var data = {};
                var rows = queryResult.data.rows;
                var columns = [];
                var fields = [];
                var displayColumns = [];
                if (queryResult && queryResult.data) {
                    angular.forEach(queryResult.data.columns, function (col, idx) {
                        var displayName = col.displayName;

                        if (hideColumns && (_.contains(hideColumns, displayName) || _.contains(hideColumns, name))) {

                        }
                        else {
                            displayColumns.push(displayName)
                            columns.push({
                                displayName: displayName,
                                headerTooltip: col.hiveColumnLabel,
                                minWidth: 150,
                                name: col.displayName,
                                queryResultColumn: col
                            });
                            fields.push(col.field);
                        }
                    });
                }

                if (transformFn != null) {
                    angular.forEach(rows, function (row, i) {
                        transformFn(row, fields, displayColumns);
                    });

                }

                data.columns = columns;
                data.rows = rows;
                data.queryResultColumns = queryResult.data.columns;
                return data;
            },
            transformResultsToUiGridModel: function (results, hideColumns, transformFn) {
                var data = {};
                var rows = results.data;
                var columns = [];
                var fields = [];
                var displayColumns = [];
                if (rows && rows.length) {
                    angular.forEach(rows[0], function (val, name) {
                        var displayName = name;
                        if (name.indexOf('.') >= 0) {
                            displayName = name.substring(name.indexOf('.') + 1);
                        }
                        if (hideColumns && (_.contains(hideColumns, displayName) || _.contains(hideColumns, name))) {

                        }
                        else {
                            displayColumns.push(displayName)
                            columns.push({displayName: displayName, minWidth: 150, name: name});
                            fields.push(name);
                        }
                    });
                }

                if (transformFn != null) {
                    angular.forEach(rows, function (row, i) {
                        transformFn(row, fields, displayColumns);
                    });

                }

                data.columns = columns;
                data.rows = rows;
                return data;
            },
            transformResultsToAgGridModel: function (results, hideColumns, transformFn) {
                var data = {};
                var rows = results.data;
                var columns = [];
                var fields = [];
                var displayColumns = [];
                if (rows && rows.length) {
                    angular.forEach(rows[0], function (val, name) {
                        var displayName = name;
                        if (name.indexOf('.') >= 0) {
                            displayName = name.substring(name.indexOf('.') + 1);
                        }
                        if (hideColumns && (_.contains(hideColumns, displayName) || _.contains(hideColumns, name))) {

                        }
                        else {
                            displayColumns.push(displayName)
                            columns.push({headerName: displayName, width: 100, field: name});
                            fields.push(name);
                        }
                    });
                }

                if (transformFn != null) {
                    angular.forEach(rows, function (row, i) {
                        transformFn(row, fields, displayColumns);
                    });

                }

                data.columns = columns;
                data.rows = rows;
                return data;
            },
            transformResults2: function (results, hideColumns, transformFn) {
                var data = {};
                var rows = results.data;
                var columns = [];
                var displayColumns = [];
                var pivotedData = [];
                angular.forEach(rows, function (row, i) {

                    if (columns.length == 0) {
                        angular.forEach(row, function (val, name) {
                            var displayName = name;
                            if (name.indexOf('.') >= 0) {
                                displayName = name.substring(name.indexOf('.') + 1);
                            }
                            if (hideColumns && (_.contains(hideColumns, displayName) || _.contains(hideColumns, name))) {

                            }
                            else {
                                displayColumns.push(displayName)
                                columns.push(name);
                            }
                        });
                    }
                    if (transformFn != null) {

                        transformFn(row, columns, displayColumns);

                    }
                    angular.forEach(displayColumns, function (displayColumn, i) {
                        row[displayColumn] = row[columns[i]];
                    });
                });

                data.columns = columns;
                data.displayColumns = displayColumns;
                data.rows = rows;
                data.pivotData = null;
                return data;
            }

        };

        return data;

    }]);
});
