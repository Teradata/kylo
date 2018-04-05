define(["require", "exports", "angular", "../module-name", "underscore"], function (require, exports, angular, module_name_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var JcrQueryController = /** @class */ (function () {
        function JcrQueryController($scope, $http, $mdDialog, $mdToast, AccessControlService) {
            this.$scope = $scope;
            this.$http = $http;
            this.$mdDialog = $mdDialog;
            this.$mdToast = $mdToast;
            this.AccessControlService = AccessControlService;
            this.sql = "";
            this.loading = false;
            this.errorMessage = null;
            this.queryTime = null;
            this.indexesErrorMessage = "";
            this.codemirrorOptions = {
                lineWrapping: true,
                indentWithTabs: true,
                smartIndent: true,
                lineNumbers: true,
                matchBrackets: true,
                autofocus: true,
                mode: 'text/x-hive'
            };
            this.gridOptions = {
                columnDefs: [],
                data: null,
                enableColumnResizing: true,
                enableGridMenu: true,
                flatEntityAccess: true
            };
            this.resultSize = 0;
            this.indexes = [];
            this.index = {
                indexName: '',
                nodeType: '',
                propertyName: '',
                propertyType: ''
            };
            this.indexTable = {
                currentPage: 1,
                rowsPerPage: 5
            };
            this.previousQueries = [];
            this.previousQuery = '';
            this.explainPlan = null;
            this.propertyTypes = [{ name: "String", value: 1 },
                { name: "Binary", value: 2 },
                { name: "Long", value: 3 },
                { name: "Double", value: 4 },
                { name: "Date", value: 5 },
                { name: "Boolean", value: 6 },
                { name: "Name", value: 7 },
                { name: "Path", value: 8 },
                { name: "Reference", value: 9 },
                { name: "Weak Referebce", value: 10 },
                { name: "URI", value: 11 },
                { name: "Decimal", value: 12 }];
            this.indexKinds = ["VALUE", "ENUMERATED_VALUE", "UNIQUE_VALUE", "TEXT", "NODE_TYPE"];
            this.ngOnInit();
        }
        JcrQueryController.prototype.ngOnInit = function () {
            this.sql = 'SELECT fs.[jcr:title], fd.[tba:state], c.[tba:systemName] \n'
                + 'FROM [tba:feed] as e \n'
                + 'JOIN [tba:feedSummary] AS fs ON ISCHILDNODE(fs, e) \n'
                + 'JOIN [tba:feedData] AS fd ON ISCHILDNODE(fd, e) \n'
                + 'JOIN [tba:categoryDetails] AS cd ON ISCHILDNODE(e, cd) \n'
                + 'JOIN [tba:category] as c on ISCHILDNODE(cd,c)';
            this.getIndexes();
        };
        JcrQueryController.prototype.executeQuery = function () {
            this.query();
        };
        JcrQueryController.prototype.registerIndex = function () {
            var _this = this;
            this.showDialog("Adding Index", "Adding index. Please wait...");
            var successFn = function (response) {
                if (response.data) {
                    _this.hideDialog();
                    _this.getIndexes();
                    _this.$mdToast.show(_this.$mdToast.simple()
                        .textContent('Added the index')
                        .hideDelay(3000));
                }
            };
            var errorFn = function (err) {
                _this.hideDialog();
            };
            var indexCopy = angular.extend({}, this.index);
            var promise = this.$http({
                url: "/proxy/v1/metadata/debug/jcr-index/register",
                method: "POST",
                data: angular.toJson(indexCopy),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            }).then(successFn, errorFn);
        };
        JcrQueryController.prototype.unregisterIndex = function (indexName) {
            var _this = this;
            if (angular.isDefined(indexName)) {
                var successFn = function (response) {
                    if (response.data) {
                        _this.getIndexes();
                        _this.$mdToast.show(_this.$mdToast.simple()
                            .textContent('Removed the index ' + indexName)
                            .hideDelay(3000));
                    }
                };
                var errorFn = function (err) {
                };
                var promise = this.$http({
                    url: "/proxy/v1/metadata/debug/jcr-index/" + indexName + "/unregister",
                    method: "POST",
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    }
                }).then(successFn, errorFn);
            }
        };
        JcrQueryController.prototype.changePreviousQuery = function () {
            this.sql = this.previousQuery;
        };
        JcrQueryController.prototype.showDialog = function (title, message) {
            this.$mdDialog.show(this.$mdDialog.alert()
                .parent(angular.element(document.body))
                .clickOutsideToClose(false)
                .title(title)
                .textContent(message)
                .ariaLabel(title));
        };
        JcrQueryController.prototype.hideDialog = function () {
            this.$mdDialog.hide();
        };
        JcrQueryController.prototype.reindex = function () {
            var _this = this;
            this.showDialog("Reindexing", "Reindexing. Please wait...");
            var successFn = function (response) {
                _this.hideDialog();
                if (response.data) {
                    _this.$mdToast.show(_this.$mdToast.simple()
                        .textContent('Successfully reindexed')
                        .hideDelay(3000));
                }
            };
            var errorFn = function (err) {
                _this.hideDialog();
                _this.$mdToast.show(_this.$mdToast.simple()
                    .textContent('Error reindexing ')
                    .hideDelay(3000));
            };
            var promise = this.$http({
                url: "/proxy/v1/metadata/debug/jcr-index/reindex",
                method: "POST",
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            }).then(successFn, errorFn);
        };
        JcrQueryController.prototype.query = function () {
            var _this = this;
            this.loading = true;
            this.errorMessage = null;
            this.explainPlan = null;
            var sql = this.sql;
            var successFn = function (response) {
                if (_.indexOf(_this.previousQueries, sql) == -1) {
                    _this.previousQueries.push(sql);
                }
                _this.loading = false;
                _this.transformResults(response.data);
            };
            var errorFn = function (err) {
                _this.resultSize = 0;
                _this.loading = false;
                if (err && err.data && err.data.developerMessage) {
                    _this.errorMessage = err.data.developerMessage;
                }
                else {
                    _this.errorMessage = 'Error performing query ';
                }
            };
            var promise = this.$http.get('/proxy/v1/metadata/debug/jcr-sql', { params: { query: sql } });
            promise.then(successFn, errorFn);
            return promise;
        };
        JcrQueryController.prototype.getIndexes = function () {
            var _this = this;
            var successFn = function (response) {
                _this.indexes = response.data;
            };
            var errorFn = function (err) {
                _this.indexes = [];
                _this.indexesErrorMessage = 'Error getting indexes ' + err;
            };
            var promise = this.$http.get('/proxy/v1/metadata/debug/jcr-index');
            promise.then(successFn, errorFn);
            return promise;
        };
        JcrQueryController.prototype.transformResults = function (result) {
            var data = {};
            var rows = [];
            var columns = [];
            this.queryTime = result.queryTime;
            this.explainPlan = result.explainPlan;
            angular.forEach(result.columns, function (col, i) {
                columns.push({
                    displayName: col.name,
                    headerTooltip: col.name,
                    minWidth: 150,
                    name: 'col_' + i
                });
            });
            angular.forEach(result.rows, function (row, i) {
                var rowObj = {};
                _.each(row.columnValues, function (col, i) {
                    rowObj['col_' + i] = col.value;
                });
                rows.push(rowObj);
            });
            data.columns = columns;
            data.rows = rows;
            this.gridOptions.columnDefs = columns;
            this.gridOptions.data = rows;
            this.resultSize = data.rows.length;
            return data;
        };
        ;
        JcrQueryController.$inject = ["$scope", "$http", "$mdDialog", "$mdToast", "AccessControlService"];
        return JcrQueryController;
    }());
    exports.JcrQueryController = JcrQueryController;
    angular.module(module_name_1.moduleName)
        .component("jcrQueryController", {
        controller: JcrQueryController,
        controllerAs: "vm",
        templateUrl: "js/admin/jcr/jcr-query.html"
    });
});
//.controller("JcrQueryController", [JcrQueryController]);
//# sourceMappingURL=JcrQueryController.js.map