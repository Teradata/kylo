import {moduleName} from "../module-name";;
import * as angular from 'angular';
import * as _ from "underscore";

export default class CodeMirrorService{

 constructor (private $q: any){
 }
 
transformToCodeMirrorData(response: any) {
    return this.populateCodeMirrorTablesAndColumns(response.data);
}
 
populateCodeMirrorTablesAndColumns(tableColumns: any){
            var codeMirrorData: any = {};
            //store metadata in 3 objects and figure out what to expose to the editor
            var databaseNames: any = [];
            var databaseGroup: any = {};  //Group data by {Database: { table: [fields]} }
            var databaseTableGroup: any = {};  //Group data by {database.Table: [fields] }
            var tablesObj: any = {};  //Group data by {table:[fields] } /// could loose data if tablename matches the same table name in a different database;
            //TODO need to figure out   how to expose the database names to the codemirror editor

            tableColumns.forEach((row: any) => {
                var db = row.databaseName;
                var dbTable = row.databaseName + "." + row.tableName;
                if (databaseGroup[db] === undefined) {
                    databaseGroup[db] = {};
                    databaseNames.push(db);
                }
                var tableObj = databaseGroup[db];
                if (tableObj[row.tableName] === undefined) {
                    tableObj[row.tableName] = [];
                }

                if (tablesObj[row.tableName] === undefined) {
                    tablesObj[row.tableName] = [];
                }
                var tablesArr = tablesObj[row.tableName];

                var tableFields = tableObj[row.tableName];
                if (databaseTableGroup[dbTable] === undefined) {
                    databaseTableGroup[dbTable] = [];
                }
                var databaseTableGroupObj = databaseTableGroup[dbTable];

                //now populate the tableFields and databaseTableGroupObj with the field Name
                tableFields.push(row.columnName);
                databaseTableGroupObj.push(row.columnName);
                tablesArr.push(row.columnName);

            });
            codeMirrorData.hintOptions = {tables: databaseTableGroup};
            codeMirrorData.databaseMetadata = databaseGroup;
            codeMirrorData.databaseNames = databaseNames;
            return codeMirrorData;
        }

     
}

 angular.module(moduleName).factory('CodeMirrorService', ["$q", 
            ($q:any) => new CodeMirrorService($q)
        ]);
 
