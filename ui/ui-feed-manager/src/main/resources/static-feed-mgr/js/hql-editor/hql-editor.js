(function() {

    var directive = function() {
        return {
            restrict: "EA",
            bindToController: {
                mode: '@',
                scrollResults: '=?',
                allowExecuteQuery: '=?',
                allowDatabaseBrowse: '=?',
                allowFullscreen: '=?',
                defaultSchemaName: '@',
                defaultTableName: '@'
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/hql-editor/hql-editor.html',
            controller: "HqlEditorController",
            require: "ngModel",
            link: function($scope, element, attrs, ngModel) {
                ngModel.$render = function() {
                    if (ngModel.$viewValue != '') {
                        $scope.vm.sql = ngModel.$viewValue;
                    }
                };
                $scope.$watch("vm.sql", function() {
                    ngModel.$setViewValue($scope.vm.sql);
                });
            }
        };
    };

    var controller = function($scope, $element, $stateParams, $mdDialog, $mdToast, $http, RestUrlService, StateService, HiveService) {

        var self = this;
        var init = function() {
            getTable();
            if (self.defaultSchemaName == undefined) {
                self.defaultSchemaName = null;
            }
            if (self.defaultTableName == undefined) {
                self.defaultTableName = null;
            }
            if (self.allowFullscreen == undefined) {
                self.allowFullscreen = true;
            }

            if (self.allowExecuteQuery == undefined) {
                self.allowExecuteQuery = false;
            }
            if (self.allowDatabaseBrowse == undefined) {
                self.allowDatabaseBrowse = false;
            }
            if (self.defaultSchemaName != null && self.defaultTableName != null) {
                self.sql = 'SELECT * FROM ' + self.defaultSchemaName + "." + self.defaultTableName + " LIMIT 20";
                if (self.allowExecuteQuery) {
                    self.query();
                }
            }
        };
        this.loadingHiveSchemas = false;

        this.codemirrorOptions = {
            lineWrapping: true,
            indentWithTabs: true,
            smartIndent: true,
            lineNumbers: true,
            matchBrackets: true,
            autofocus: true,
            extraKeys: {'Ctrl-Space': 'autocomplete'},
            hint: CodeMirror.hint.sql,
            hintOptions: {
                tables: {}
            },
            mode: 'text/x-hive'
        };

        this.databaseMetadata = {};
        this.browseDatabaseName = null;
        this.browseTableName = null;
        this.databaseNames = [];
        this.browseResults = null;

        function getTable(schema, table) {
            self.loadingHiveSchemas = true;
            var successFn = function(response) {
                var tableColumns = response.data;

                //store metadata in 3 objects and figure out what to expose to the editor
                var databaseNames = [];
                var databaseGroup = {};  //Group data by {Database: { table: [fields]} }
                var databaseTableGroup = {}  //Group data by {database.Table: [fields] }
                var tablesObj = {};  //Group data by {table:[fields] } /// could loose data if tablename matches the same table name in a different database;
                //TODO need to figure out how to expose the database names to the codemirror editor

                angular.forEach(tableColumns, function(row) {
                    var db = row.databaseName;
                    var dbTable = row.databaseName + "." + row.tableName;
                    if (databaseGroup[db] == undefined) {
                        databaseGroup[db] = {};
                        databaseNames.push(db);
                    }
                    var tableObj = databaseGroup[db];
                    if (tableObj[row.tableName] == undefined) {
                        tableObj[row.tableName] = [];
                    }

                    if (tablesObj[row.tableName] == undefined) {
                        tablesObj[row.tableName] = [];
                    }
                    var tablesArr = tablesObj[row.tableName]

                    var tableFields = tableObj[row.tableName];
                    if (databaseTableGroup[dbTable] == undefined) {
                        databaseTableGroup[dbTable] = [];
                    }
                    var databaseTableGroupObj = databaseTableGroup[dbTable];

                    //now populate the tableFields and databaseTableGroupObj with the field Name
                    tableFields.push(row.columnName);
                    databaseTableGroupObj.push(row.columnName);
                    tablesArr.push(row.columnName);

                });
                self.codemirrorOptions.hintOptions = {tables: databaseTableGroup};
                self.loadingHiveSchemas = false;
                self.databaseMetadata = databaseGroup;
                self.databaseNames = databaseNames;
            };
            var errorFn = function(err) {
                self.loadingHiveSchemas = false;
                $mdDialog.show(
                        $mdDialog.alert()
                                .parent(angular.element(document.querySelector('#hqlEditorContainer')))
                                .clickOutsideToClose(true)
                                .title('Cannot access Thinkbig Hive Jetty App Server')
                                .textContent('Ensure the thinkbig-hive App.java is Running (In the thinkbig-hive project, right click on App.java -> Run and then refresh this page) ')
                                .ariaLabel('Cannot access Thinkbig Hive Jetty App Server')
                                .ok('Got it!')
                        //.targetEvent(ev)
                );
            };
            var promise = $http.get(RestUrlService.HIVE_SERVICE_URL + "/table-columns");
            promise.then(successFn, errorFn);
            return promise;
        }

        this.query = function() {
            this.executingQuery = true;
            return HiveService.queryResult(this.sql).then(function(tableData) {
                self.executingQuery = false;
                var result = self.queryResults = HiveService.transformQueryResultsToUiGridModel(tableData);
                self.gridOptions.columnDefs = result.columns;
                self.gridOptions.data = result.rows;
            });
        };

        //Setup initial grid options
        this.gridOptions = {
            columnDefs: [],
            data: null,
            enableColumnResizing: true,
            enableGridMenu: true,
            flatEntityAccess: true
        };

        this.fullscreen = function() {
            $mdDialog.show({
                controller: 'HqlFullScreenEditorController',
                controllerAs: 'vm',
                templateUrl: 'js/hql-editor/hql-editor-fullscreen.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    hql: self.sql,
                    defaultSchemaName: self.defaultSchemaName,
                    defaultTableName: self.defaultTableName,
                    allowExecuteQuery: self.allowExecuteQuery,
                    allowDatabaseBrowse: self.allowDatabaseBrowse,
                    mode: self.mode
                }
            }).then(function(msg) {

            }, function() {

            });
        };

        this.browseTable = function() {
            self.executingQuery = true;
            return HiveService.browseTable(this.browseDatabaseName, this.browseTableName, null).then(function(tableData) {
                self.executingQuery = false;
                self.queryResults = HiveService.transformQueryResultsToUiGridModel(tableData);
            }, function(err) {
                self.executingQuery = false;
                $mdDialog.show(
                        $mdDialog.alert()
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

        init();
    };

    angular.module(MODULE_FEED_MGR).controller('HqlEditorController', controller);
    angular.module(MODULE_FEED_MGR).directive('thinkbigHqlEditor', directive);
}());

(function() {

    var controller = function ($scope, $mdDialog, hql, defaultSchemaName, defaultTableName, allowExecuteQuery, allowDatabaseBrowse, mode) {

        var self = this;
        this.hql = hql;
        this.defaultSchemaName = defaultSchemaName;
        this.defaultTableName = defaultTableName;
        this.allowExecuteQuery = allowExecuteQuery;
        this.allowDatabaseBrowse = allowDatabaseBrowse;
        this.mode = mode;

        $scope.cancel = function($event) {
            $mdDialog.hide();
        };

    };
    angular.module(MODULE_FEED_MGR).controller('HqlFullScreenEditorController', controller);

}());
