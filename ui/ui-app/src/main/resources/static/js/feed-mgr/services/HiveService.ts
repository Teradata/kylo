import * as _ from "underscore";
import * as moment from 'moment';
import { RestUrlConstants } from './RestUrlConstants';
import { HttpClient, HttpParams } from '@angular/common/http';
import 'rxjs/add/operator/timeout';
import { Injectable } from '@angular/core';
import { TdDialogService } from '@covalent/core/dialogs';

@Injectable()
export class HiveService {

    loading : boolean = true;
    loadingHiveSchemas : boolean = true;
    constructor(private http: HttpClient, private _dialogService: TdDialogService) {

    }
    refreshTableCache() {
        return this.http.get(RestUrlConstants.HIVE_SERVICE_URL + "/refreshUserHiveAccessCache");
    };
    queryTablesSearch(query: any) {
        return this.getTables(null,query);
    };
    parseTableResponse(response: any) {
        var schemaTables = {};
        var allTables: any = [];
        if (response) {
            response.forEach((table: any) => {
                var schema = table.substr(0, table.indexOf("."));
                var tableName = table.substr(table.indexOf(".") + 1);
                if (schemaTables[schema] == undefined) {
                    schemaTables[schema] = [];
                }
                allTables.push({ schema: schema, tableName: tableName, fullName: table, fullNameLower: table.toLowerCase() });
            })
        }
        return allTables;
    };
    getTables(schema: any, table: any) {
        var successFn = (response: any) => {
            return this.parseTableResponse(response);
        };
        var errorFn = (err: any) => {
            this.loading = false;
        };
        let params = new HttpParams();
        // Begin assigning parameters
        params = params.append("table", table);
        if(schema !== undefined && schema !== null ) {
            params = params.append("schema", schema);
        }
        // params = params.append("refreshCache", this.refreshCache.toString()); //never used so commented


        var promise = this.http.get(RestUrlConstants.HIVE_SERVICE_URL + "/tables", {params : params}).toPromise();
        promise.then(successFn, errorFn);
        return promise;
    };
    getTablesAndColumns() {
        return this.http.get(RestUrlConstants.HIVE_SERVICE_URL + "/table-columns").timeout(3000).toPromise();
    };
    queryResult(query: any) {
        if (this.query != null) {
            var successFn = (response: any) => {
                var tableData = response;
                return tableData;
            }
            var errorFn = (err: any) => {
                this.loadingHiveSchemas = false;
                this._dialogService.openAlert({
                    message: 'Error querying the data for ' + query,
                    title: 'Error executing the Query',
                    closeButton: 'Got it!',
                    ariaLabel : 'Error browsing the data'
                });
            }
            var config = { params: { query: query } };
            var promise = this.http.get(RestUrlConstants.HIVE_SERVICE_URL + "/query-result", config).toPromise();
            promise.then(successFn, errorFn);
            return promise;
        }
    };
    query(query: any) {
        if (this.query != null) {
            var successFn = (response: any) => {
                var tableData = response;
                return tableData;

            }
            var errorFn = (err: any) => {
                this.loadingHiveSchemas = false;
                this._dialogService.openAlert({
                    message: 'Error querying the data for ' + query,
                    title: 'Error executing the Query',
                    closeButton: 'Got it!',
                    ariaLabel : 'Error browsing the data'
                });
            }
            var config = { params: { query: query } };
            var promise = this.http.get(RestUrlConstants.HIVE_SERVICE_URL + "/query", config).toPromise();
            promise.then(successFn, errorFn);
            return promise;
        }
    };
    browseTable(schema: any, table: any, whereCondition: any) {
        if (schema != null && table != null) {
            var successFn = (response: any) => {
                var tableData = response;
                return tableData;
            }
            var errorFn = (err: any) => {
                this.loadingHiveSchemas = false;
                this._dialogService.openAlert({
                    message: 'Error Browsing the data ',
                    title: 'Cannot browse the table',
                    closeButton: 'Got it!',
                    ariaLabel : 'Error browsing the data'
                });
            }
            var promise = this.http.get(RestUrlConstants.HIVE_SERVICE_URL + "/browse/" + schema + "/" + table, { params: { where: whereCondition } }).toPromise();
            promise.then(successFn, errorFn);
            return promise;
        }
    };
    getColumnNamesForRow(row: any) {
        var columns: any = [];
        var displayColumns: any = [];
        row.forEach((val: any, name: any) => {
            var displayName = name;
            if (name.indexOf('.') >= 0) {
                displayName = name.substring(name.indexOf('.') + 1);
            }
            displayColumns.push(displayName)
            columns.push(name);
        });
        return columns;

    };
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
    };

    getColumnNamesForQueryResult(queryResult: any) {
        if (queryResult != null) {
            if (queryResult.data) {
                queryResult = queryResult.data;
            }
            var row = queryResult[0];
            return this.getColumnNamesForRow(row);
        }
        return null;
    };
    orderColumns(columns: any, comparator: any) {
        columns.sort(comparator);
    };
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
            rows[0].forEach((val: any, name: any) => {
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
    };
    transformQueryResultsToUiGridModel(queryResult: any, hideColumns: any, transformFn: any) {
        var data: any = {};
        var rows: any = queryResult.data.rows;
        var columns: any = [];
        var fields: any = [];
        var displayColumns: any = [];
        if (queryResult && queryResult.data) {
            queryResult.data.columns.forEach((col: any, idx: any) => {
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
            rows.forEach((row: any, i: any) => {
                transformFn(row, fields, displayColumns);
            });

        }

        data.columns = columns;
        data.rows = rows;
        data.queryResultColumns = queryResult.data.columns;
        return data;
    };
    transformResultsToUiGridModel(results: any, hideColumns: any, transformFn: any) {
        var data: any = {};
        var rows: any = results.data;
        var columns: any = [];
        var fields: any = [];
        var displayColumns: any = [];
        if (rows && rows.length) {
            rows[0].forEach((val: any, name: any) => {
                var displayName = name;
                if (name.indexOf('.') >= 0) {
                    displayName = name.substring(name.indexOf('.') + 1);
                }
                if (hideColumns && (_.contains(hideColumns, displayName) || _.contains(hideColumns, name))) {

                }
                else {
                    displayColumns.push(displayName)
                    columns.push({ displayName: displayName, minWidth: 150, name: name });
                    fields.push(name);
                }
            });
        }
        if (transformFn != null) {
            rows.forEach((row: any, i: any) => {
                transformFn(row, fields, displayColumns);
            });
        }
        data.columns = columns;
        data.rows = rows;
        return data;
    };
    transformResultsToAgGridModel(results: any, hideColumns: any, transformFn: any) {
        var data: any = {};
        var rows = results.data;
        var columns: any = [];
        var fields: any = [];
        var displayColumns: any = [];
        if (rows && rows.length) {
            rows[0].forEach((val: any, name: any) => {
                var displayName = name;
                if (name.indexOf('.') >= 0) {
                    displayName = name.substring(name.indexOf('.') + 1);
                }
                if (hideColumns && (_.contains(hideColumns, displayName) || _.contains(hideColumns, name))) {

                }
                else {
                    displayColumns.push(displayName)
                    columns.push({ headerName: displayName, width: 100, field: name });
                    fields.push(name);
                }
            });
        }
        if (transformFn != null) {
            rows.forEach((row: any, i: any) => {
                transformFn(row, fields, displayColumns);
            });

        }
        data.columns = columns;
        data.rows = rows;
        return data;
    };
    transformResults2(results: any, hideColumns: any, transformFn: any) {
        var data: any = {};
        var rows: any = results.data;
        var columns: any = [];
        var displayColumns: any = [];
        var pivotedData = [];
        rows.forEach((row: any, i: any) => {
            if (columns.length == 0) {
                rows.forEach((val: any, name: any) => {
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
            displayColumns.forEach((displayColumn: any, i: any) => {
                row[displayColumn] = row[columns[i]];
            });
        });

        data.columns = columns;
        data.displayColumns = displayColumns;
        data.rows = rows;
        data.pivotData = null;
        return data;
    };
}