import * as angular from 'angular';
import * as _ from "underscore";
import * as moment from 'moment';
import {RestUrlConstants} from "./RestUrlConstants";

//const moduleName = require('../module-name');


export class HiveService {
    loading: boolean = false;
    refreshCache: boolean;
    loadingHiveSchemas: boolean;

    static $inject = ["$q", "$http", "$mdDialog"];

    constructor(private $q: angular.IQService, private $http: angular.IHttpService, private $mdDialog: angular.material.IDialogService) {
    }

    refreshTableCache() {
        return this.$http.get(RestUrlConstants.HIVE_SERVICE_URL + "/refreshUserHiveAccessCache");
    }

    queryTablesSearch(query: any) {
        var self = this;
        var deferred = this.$q.defer();
        self.getTables(null, query).then(function (response: any) {
            deferred.resolve(self.parseTableResponse(response.data));
        });
        return deferred.promise;
    }

    parseTableResponse(response: any) {
        var schemaTables = {};
        var allTables: any = [];
        if (response) {
            angular.forEach(response, function (table: any) {
                var schema = table.substr(0, table.indexOf("."));
                var tableName = table.substr(table.indexOf(".") + 1);
                if (schemaTables[schema] == undefined) {
                    schemaTables[schema] = [];
                }
                allTables.push({schema: schema, tableName: tableName, fullName: table, fullNameLower: table.toLowerCase()});
            })
        }
        return allTables;
    }

    getTables(schema: any, table: any) {
        var self = this;
        var successFn = function (response: any) {
            return self.parseTableResponse(response.data);
        };
        var errorFn = function (err: any) {
            self.loading = false;
        };
        var params = {schema: schema, table: table, refreshCache: self.refreshCache};
        var promise = this.$http.get(RestUrlConstants.HIVE_SERVICE_URL + "/tables", {params: params});
        promise.then(successFn, errorFn);
        return promise;
    }

    getTablesAndColumns() {
        return this.$http.get(RestUrlConstants.HIVE_SERVICE_URL + "/table-columns", {timeout: 30000});
    }

    queryResult(query: any) {
        var self = this;
        if (this.query != null) {
            var successFn = function (response: any) {
                var tableData = response.data;
                return tableData;

            }
            var errorFn = function (err: any) {
                self.loadingHiveSchemas = false;
                this.$mdDialog.show(
                    this.$mdDialog.alert()
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
            var promise = this.$http.get(RestUrlConstants.HIVE_SERVICE_URL + "/query-result", config);
            promise.then(successFn, errorFn);
            return promise;
        }
    }

    query(query: any) {
        var self = this;
        if (this.query != null) {
            var successFn = function (response: any) {
                var tableData = response.data;
                return tableData;

            }
            var errorFn = function (err: any) {
                self.loadingHiveSchemas = false;
                this.$mdDialog.show(
                    this.$mdDialog.alert()
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

            var promise = this.$http.get(RestUrlConstants.HIVE_SERVICE_URL + "/query", config);
            promise.then(successFn, errorFn);
            return promise;
        }
    }

    browseTable(schema: any, table: any, whereCondition: any) {
        var self = this;
        if (schema != null && table != null) {
            var successFn = function (response: any) {
                var tableData = response.data;
                return tableData;

            }
            var errorFn = function (err: any) {
                self.loadingHiveSchemas = false;
                this.$mdDialog.show(
                    this.$mdDialog.alert()
                        .parent(angular.element(document.querySelector('body')))
                        .clickOutsideToClose(true)
                        .title('Cannot browse the table')
                        .textContent('Error Browsing the data ')
                        .ariaLabel('Error browsing the data')
                        .ok('Got it!')
                    //.targetEvent(ev)
                );
            }
            var promise = this.$http.get(RestUrlConstants.HIVE_SERVICE_URL + "/browse/" + schema + "/" + table, {params: {where: whereCondition}});
            promise.then(successFn, errorFn);
            return promise;
        }
    }

    getColumnNamesForRow(row: any) {
        var columns: any = [];
        var displayColumns: any = [];
        angular.forEach(row, function (val: any, name: any) {
            var displayName = name;
            if (name.indexOf('.') >= 0) {
                displayName = name.substring(name.indexOf('.') + 1);
            }
            displayColumns.push(displayName)
            columns.push(name);
        });
        return columns;

    }

    getUTCTime(dateStr: any) {
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
            return moment(parseInt(dateStr)).toDate();// TODO GREG
        }
    }

    getColumnNamesForQueryResult(queryResult: any) {
        var self = this;
        if (queryResult != null) {
            if (queryResult.data) {
                queryResult = queryResult.data;
            }
            var row = queryResult[0];
            return self.getColumnNamesForRow(row);
        }
        return null;

    }

    orderColumns(columns: any, comparator: any) {
        columns.sort(comparator);
    }

    /**
     * TODO ORDERING OF COLUMNS IS NOT PRESERVED!
     * @param results
     * @param hideColumns
     * @returns {{}}
     */
    transformResults(results: any, hideColumns: any) {
        var data: any = {};
        var rows: any = results.data;
        var columns: any = [];
        var displayColumns: any = [];
        var pivotedData: any = [];
        if (rows && rows.length) {
            angular.forEach(rows[0], function (val: any, name: any) {
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
    }

    transformQueryResultsToUiGridModel(queryResult: any, hideColumns?: any, transformFn?: any) {
        var data: any = {};
        var rows: any = queryResult.data.rows;
        var columns: any = [];
        var fields: any = [];
        var displayColumns: any = [];
        if (queryResult && queryResult.data) {
            angular.forEach(queryResult.data.columns, function (col: any, idx: any) {
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
            angular.forEach(rows, function (row: any, i: any) {
                transformFn(row, fields, displayColumns);
            });

        }

        data.columns = columns;
        data.rows = rows;
        data.queryResultColumns = queryResult.data.columns;
        return data;
    }

    transformResultsToUiGridModel(results: any, hideColumns: any, transformFn: any) {
        var data: any = {};
        var rows: any = results.data;
        var columns: any = [];
        var fields: any = [];
        var displayColumns: any = [];
        if (rows && rows.length) {
            angular.forEach(rows[0], function (val: any, name: any) {
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
            angular.forEach(rows, function (row: any, i: any) {
                transformFn(row, fields, displayColumns);
            });

        }

        data.columns = columns;
        data.rows = rows;
        return data;
    }

    transformResultsToAgGridModel(results: any, hideColumns: any, transformFn: any) {
        var data: any = {};
        var rows = results.data;
        var columns: any = [];
        var fields: any = [];
        var displayColumns: any = [];
        if (rows && rows.length) {
            angular.forEach(rows[0], function (val: any, name: any) {
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
            angular.forEach(rows, function (row: any, i: any) {
                transformFn(row, fields, displayColumns);
            });

        }

        data.columns = columns;
        data.rows = rows;
        return data;
    }

    transformResults2(results: any, hideColumns: any, transformFn: any) {
        var data: any = {};
        var rows: any = results.data;
        var columns: any = [];
        var displayColumns: any = [];
        var pivotedData = [];
        angular.forEach(rows, function (row: any, i: any) {

            if (columns.length == 0) {
                angular.forEach(row, function (val: any, name: any) {
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
            angular.forEach(displayColumns, function (displayColumn: any, i: any) {
                row[displayColumn] = row[columns[i]];
            });
        });

        data.columns = columns;
        data.displayColumns = displayColumns;
        data.rows = rows;
        data.pivotData = null;
        return data;
    }


}
