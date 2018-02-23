define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/tables/module-name');
    var TableController = /** @class */ (function () {
        function TableController($scope, $transition$, $http, RestUrlService) {
            this.$scope = $scope;
            this.$transition$ = $transition$;
            this.$http = $http;
            this.RestUrlService = RestUrlService;
            var self = this;
            this.tableSchema = null;
            self.selectedTabIndex = 0;
            self.hql = '';
            var init = function () {
                var schema = $transition$.params().schema;
                self.schema = schema;
                self.tableName = $transition$.params().tableName;
                self.datasource = $transition$.params().datasource;
                getTable(self.schema, self.tableName);
            };
            $scope.$watch(function () {
                return self.selectedTabIndex;
            }, function (newVal) {
            });
            function getTable(schema, table) {
                if (self.datasource.isHive) {
                    getHiveTable(schema, table);
                }
                else {
                    getNonHiveTable(schema, table);
                }
            }
            var successFn = function (response) {
                self.tableSchema = response.data;
                self.loading = false;
            };
            var errorFn = function (err) {
                self.loading = false;
            };
            function getNonHiveTable(schema, table) {
                var params = { schema: schema };
                var promise = $http.get(RestUrlService.GET_DATASOURCES_URL + "/" + self.datasource.id + "/tables/" + table, { params: params });
                promise.then(successFn, errorFn);
                return promise;
            }
            function getHiveTable(schema, table) {
                var promise = $http.get(RestUrlService.HIVE_SERVICE_URL + "/schemas/" + schema + "/tables/" + table);
                promise.then(successFn, errorFn);
                return promise;
            }
            init();
        }
        ;
        return TableController;
    }());
    exports.TableController = TableController;
    angular.module(moduleName).controller('TableController', ["$scope", "$transition$", "$http", "RestUrlService", TableController]);
});
//# sourceMappingURL=TableController.js.map