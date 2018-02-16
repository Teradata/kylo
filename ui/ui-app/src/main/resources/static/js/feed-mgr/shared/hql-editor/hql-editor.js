define(['angular',"feed-mgr/module-name", "pascalprecht.translate"], function (angular,moduleName) {

    var directive = function() {
        return {
            restrict: "EA",
            bindToController: {
                scrollResults: '=?',
                allowExecuteQuery: '=?',
                allowDatabaseBrowse: '=?',
                allowFullscreen: '=?',
                defaultSchemaName: '@',
                defaultTableName: '@',
                datasourceId: '@',
                tableId: '@'
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/shared/hql-editor/hql-editor.html',
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

    var controller = function($scope, $element, $mdDialog, $mdToast, $http, $filter,$q, RestUrlService, StateService, HiveService, DatasourcesService, CodeMirrorService, FattableService) {

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
                if (!self.sql) {
                    self.sql = "SELECT * FROM " + quote(self.defaultSchemaName) + "." + quote(self.defaultTableName) + " LIMIT 20";
                }
                if (self.allowExecuteQuery) {
                    self.query();
                }
            }
            self.editor.setValue(self.sql);
            self.editor.focus();
        };
        this.loadingHiveSchemas = false;
        this.metadataMessage = "";
        var metadataLoadedMessage = $filter('translate')('views.hql-editor.UseCTRL');
        var metadataLoadingMessage = $filter('translate')('views.hql-editor.LoadingTable');
        var metadataErrorMessage = $filter('translate')('views.hql-editor.UnableTlt');

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
            mode: self.datasource && self.datasource.isHive ? 'text/x-hive' : 'text/x-sql'
        };

        this.databaseMetadata = {};
        this.browseDatabaseName = null;
        this.browseTableName = null;
        this.databaseNames = [];
        this.browseResults = null;

        function quote(expression) {
            if (self.datasource.isHive) {
                return "`" + expression + "`";
            } else {
                return expression;
            }
        }

        function getTable() {
            self.loadingHiveSchemas = true;
            self.metadataMessage = metadataLoadingMessage;
            var successFn = function(data) {
                var codeMirrorData = CodeMirrorService.transformToCodeMirrorData(data);
                if(codeMirrorData && codeMirrorData.hintOptions && codeMirrorData.hintOptions.tables) {
                    self.codemirrorOptions.hintOptions.tables = codeMirrorData.hintOptions.tables;
                }
                self.loadingHiveSchemas = false;
                self.databaseMetadata = codeMirrorData.databaseMetadata;
                self.databaseNames = codeMirrorData.databaseNames;
                self.metadataMessage = metadataLoadedMessage
            };
            var errorFn = function(err) {
                self.loadingHiveSchemas = false;
                self.metadataMessage = metadataErrorMessage

            };

            var promise;

                if (angular.isDefined(self.datasource) && self.datasource.isHive) {
                    promise = HiveService.getTablesAndColumns();
                } else if(angular.isDefined(self.datasourceId)){
                    promise = DatasourcesService.getTablesAndColumns(self.datasourceId, self.defaultSchemaName);
                }
                else {
                    promise = $q.defer().promise;
                    return promise;
                }
            promise.then(successFn, errorFn);
            return promise;
        }

        this.query = function() {
            this.executingQuery = true;
            var successFn = function(tableData) {
                var result = self.queryResults = HiveService.transformQueryResultsToUiGridModel(tableData);
                FattableService.setupTable({
                    tableContainerId: self.tableId,
                    headers: result.columns,
                    rows: result.rows
                });
                self.executingQuery = false;
            };
            var errorFn = function (err) {
                self.executingQuery = false;
            };
            var promise;
            if (self.datasource.isHive) {
                promise = HiveService.queryResult(self.sql);
            } else {
                promise = DatasourcesService.query(self.datasourceId, self.sql);
            }
            return promise.then(successFn, errorFn);
        };

        this.fullscreen = function() {
            $mdDialog.show({
                controller: 'HqlFullScreenEditorController',
                controllerAs: 'vm',
                templateUrl: 'js/feed-mgr/shared/hql-editor/hql-editor-fullscreen.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    hql: self.sql,
                    defaultSchemaName: self.defaultSchemaName,
                    defaultTableName: self.defaultTableName,
                    allowExecuteQuery: self.allowExecuteQuery,
                    allowDatabaseBrowse: self.allowDatabaseBrowse,
                    datasourceId: self.datasourceId,
                    tableId: self.tableId
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

        function getDatasource(datasourceId) {
            self.executingQuery = true;
            var successFn = function (response) {
                self.datasource = response;
                self.executingQuery = false;
            };
            var errorFn = function (err) {
                self.executingQuery = false;
            };
            return DatasourcesService.findById(datasourceId).then(successFn, errorFn);
        }

        this.codemirrorLoaded = function(_editor) {
            self.editor = _editor;
        };

        if(angular.isDefined(self.datasourceId)) {
            getDatasource(self.datasourceId).then(init);
        }
        else {
          //.  init();
        }
    };

    angular.module(moduleName).controller('HqlEditorController', ["$scope","$element","$mdDialog","$mdToast","$http","$filter","$q","RestUrlService","StateService","HiveService","DatasourcesService","CodeMirrorService","FattableService", controller]);
    angular.module(moduleName).directive('thinkbigHqlEditor', directive);



    var HqlFullScreenEditorController = function ($scope, $mdDialog, hql, defaultSchemaName, defaultTableName, allowExecuteQuery, allowDatabaseBrowse, datasourceId, tableId) {

        var self = this;
        this.hql = hql;
        this.defaultSchemaName = defaultSchemaName;
        this.defaultTableName = defaultTableName;
        this.allowExecuteQuery = allowExecuteQuery;
        this.allowDatabaseBrowse = allowDatabaseBrowse;
        this.datasourceId = datasourceId;
        this.tableId = tableId;

        $scope.cancel = function($event) {
            $mdDialog.hide();
        };

    };
    angular.module(moduleName).controller('HqlFullScreenEditorController', ["$scope","$mdDialog","hql","defaultSchemaName","defaultTableName","allowExecuteQuery","allowDatabaseBrowse","datasourceId", "tableId",HqlFullScreenEditorController]);


});
