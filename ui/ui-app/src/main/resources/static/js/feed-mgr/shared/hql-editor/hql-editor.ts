import * as angular from 'angular';
import * as _ from "underscore";
import "pascalprecht.translate";
// const CodeMirror = require('angular-ui-codemirror');
import {moduleName} from "../../module-name";;

declare const CodeMirror: any;

export class HqlEditorController {


    defaultSchemaName: any;
    defaultTableName: any;
    allowFullscreen: any;
    allowExecuteQuery: any;
    allowDatabaseBrowse: any;
    sql: any;
    loadingHiveSchemas: boolean = false;
    metadataMessage: string = "";
    codemirrorOptions: any;
    databaseMetadata: any ={};
    browseDatabaseName: any = null;
    browseTableName: any = null;
    databaseNames: any[] = [];
    browseResults: any = null;
    gridOptions: any;
    executingQuery: any;
    queryResults: any;
    mode: any;
    editor: any;
    datasource: any;
    datasourceId: any;
    tableId: any;
    ngModel: angular.INgModelController;
    scrollResults: any;
    
    ngOnInit(): void {
        var metadataLoadedMessage = this.$filter('translate')('views.hql-editor.UseCTRL');
        var metadataLoadingMessage = this.$filter('translate')('views.hql-editor.LoadingTable');
        var metadataErrorMessage = this.$filter('translate')('views.hql-editor.UnableTlt');
        this.ngModel.$render = () => {
            if (this.ngModel.$viewValue != '') {
                this.$scope.vm.sql = this.ngModel.$viewValue;
            }
        };
        this.$scope.$watch("vm.sql", () => {
            this.ngModel.$setViewValue(this.$scope.vm.sql);
        });
        if (angular.isDefined(this.datasourceId)) {
            this.getDatasource(this.datasourceId).then(this.init());
        }
    }

    $onInit(): void {
        this.ngOnInit();
    }

    static readonly $inject = ["$scope", "$element", "$mdDialog", "$mdToast", "$http",
        "$filter", "$q", "RestUrlService", "StateService", "HiveService", "DatasourcesService",
        "CodeMirrorService", "FattableService"];
    constructor(private $scope: IScope, private $element: any, private $mdDialog: angular.material.IDialogService, private $mdToast: angular.material.IToastService, private $http: angular.IHttpService
        , private $filter: angular.IFilterService, private $q: angular.IQService, private RestUrlService: any, private StateService: any, private HiveService: any, private DatasourcesService: any
        , private CodeMirrorService: any, private FattableService: any) {

            this.codemirrorOptions = {
            lineWrapping: true,
            indentWithTabs: true,
            smartIndent: true,
            lineNumbers: true,
            matchBrackets: true,
            autofocus: true,
            extraKeys: { 'Ctrl-Space': 'autocomplete' },
            hint: CodeMirror["hint"].sql,
            hintOptions: {
                tables: {}
            },
            mode: this.datasource && this.datasource.isHive ? 'text/x-hive' : 'text/x-sql'
        };

       
        //constructor ends here
    };
    codemirrorLoaded =  (_editor: any) => {
        this.editor = _editor;
    };
    init = () => {
        // getTable();
        if (this.defaultSchemaName == undefined) {
            this.defaultSchemaName = null;
        }
        if (this.defaultTableName == undefined) {
            this.defaultTableName = null;
        }
        if (this.allowFullscreen == undefined) {
            this.allowFullscreen = true;
        }

        if (this.allowExecuteQuery == undefined) {
            this.allowExecuteQuery = false;
        }
        if (this.allowDatabaseBrowse == undefined) {
            this.allowDatabaseBrowse = false;
        }
        if (this.defaultSchemaName != null && this.defaultTableName != null) {
            // TODO Change to a deferred to provide the SQL and execute the query when the query text becomes available.
            if (!this.sql) {
                if (this.datasource.isHive) {
                    this.sql = "SELECT * FROM " + this.quote(this.defaultSchemaName) + "." + this.quote(this.defaultTableName) + " LIMIT 20";
                    if (this.allowExecuteQuery) {
                        this.query();
                    }
                } else {
                    this.DatasourcesService.getPreviewSql(this.datasourceId, this.defaultSchemaName, this.defaultTableName, 20)
                        .then(function (response: string) {
                            this.sql = response;
                            if (this.allowExecuteQuery) {
                                this.query();
                            }
                        });
                }
            }
        }
        this.editor.setValue(this.sql);
        this.editor.focus();
    };
    quote = (expression: any) => {
        if (this.datasource.isHive) {
            return "`" + expression + "`";
        } else {
            return expression;
        }
    }
    query = () => {
        this.executingQuery = true;
        var successFn = (tableData: any) =>{
            var result = this.queryResults = this.HiveService.transformQueryResultsToUiGridModel(tableData);
            this.FattableService.setupTable({
                tableContainerId: this.tableId,
                headers: result.columns,
                rows: result.rows
            });
            this.executingQuery = false;
        };
        var errorFn = (err: any) => {
            this.executingQuery = false;
        };
        var promise;
        if (this.datasource.isHive) {
            promise = this.HiveService.queryResult(this.sql);
        } else {
            promise = this.DatasourcesService.query(this.datasourceId, this.sql);
        }
        return promise.then(successFn, errorFn);
    };
    fullscreen = () => {
        this.$mdDialog.show({
            controller: 'HqlFullScreenEditorController',
            controllerAs: 'vm',
            templateUrl: './hql-editor-fullscreen.html',
            parent: angular.element(document.body),
            clickOutsideToClose: false,
            fullscreen: true,
            locals: {
                hql: this.sql,
                defaultSchemaName: this.defaultSchemaName,
                defaultTableName: this.defaultTableName,
                allowExecuteQuery: this.allowExecuteQuery,
                allowDatabaseBrowse: this.allowDatabaseBrowse,
                datasourceId: this.datasourceId,
                tableId: this.tableId
            }
        }).then((msg: any) => {

        }, () => {

        });
    };
    browseTable = () => {
        this.executingQuery = true;
        return this.HiveService.browseTable(this.browseDatabaseName, this.browseTableName, null)
               .then((tableData: any) =>{
            this.executingQuery = false;
            this.queryResults = this.HiveService.transformQueryResultsToUiGridModel(tableData);
        }, (err: any) => {
            this.executingQuery = false;
            this.$mdDialog.show(
                this.$mdDialog.alert()
                    .parent(angular.element(document.querySelector('#hqlEditorContainer')))
                    .clickOutsideToClose(true)
                    .title('Cannot browse the table')
                    .textContent('Error Browsing the data ')
                    .ariaLabel('Error browsing the data')
                    .ok('Got it!')
                //.targetEvent(ev)
            );
        });
    };
    getDatasource(datasourceId: any) {
        this.executingQuery = true;
        var successFn = (response: any) => {
            this.datasource = response;
            this.executingQuery = false;
        };
        var errorFn = (err: any) =>{
            this.executingQuery = false;
        };
        return this.DatasourcesService.findById(datasourceId).then(successFn, errorFn);
    }


}

angular.module(moduleName).component('thinkbigHqlEditor', {
    bindings: {
        scrollResults: '=?',
        allowExecuteQuery: '=?',
        allowDatabaseBrowse: '=?',
        allowFullscreen: '=?',
        defaultSchemaName: '@',
        defaultTableName: '@',
        datasourceId: '@',
        tableId: '@',
    },
    controllerAs: 'vm',
    templateUrl: './hql-editor.html',
    controller: HqlEditorController,
    require: {
        ngModel: "?ngModel"
    },
});


class HqlFullScreenEditorController {

    // static readonly $inject = ["$scope", "$mdDialog", "hql", "defaultSchemaName", "defaultTableName",
    //     "allowExecuteQuery", "allowDatabaseBrowse", "datasourceId", "tableId"];
    constructor(private $scope: any, private $mdDialog: angular.material.IDialogService, private hql: any, private defaultSchemaName: any, private defaultTableName: any
        , private allowExecuteQuery: any, private allowDatabaseBrowse: any, private datasourceId: any, private tableId: any) {
        $scope.cancel = ($event: any) => {
            this.$mdDialog.hide();
        };
    }
}
angular.module(moduleName).controller('HqlFullScreenEditorController', ["$scope","$mdDialog","hql","defaultSchemaName","defaultTableName","allowExecuteQuery","allowDatabaseBrowse","datasourceId", "tableId", HqlFullScreenEditorController]);

