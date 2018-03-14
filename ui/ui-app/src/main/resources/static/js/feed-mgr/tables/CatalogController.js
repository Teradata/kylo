define(["require", "exports", "angular", "underscore", "./module-name"], function (require, exports, angular, _, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var CatalogController = /** @class */ (function () {
        /**
         * Displays a list of datasources.
         */
        function CatalogController($scope, $q, DatasourcesService, StateService, AccessControlService) {
            this.$scope = $scope;
            this.$q = $q;
            this.DatasourcesService = DatasourcesService;
            this.StateService = StateService;
            this.AccessControlService = AccessControlService;
            var self = this;
            this.datasources = [DatasourcesService.getHiveDatasource()];
            self.loading = true;
            self.navigateToSchemas = function (datasource) {
                StateService.FeedManager().Table().navigateToSchemas(datasource.id);
            };
            function getDataSources() {
                var successFn = function (response) {
                    var jdbcSources = _.filter(response, function (ds) {
                        return ds['@type'] === 'JdbcDatasource';
                    });
                    self.datasources.push.apply(self.datasources, jdbcSources);
                    self.loading = false;
                };
                var errorFn = function (err) {
                    self.loading = false;
                };
                var promise = DatasourcesService.findAll();
                promise.then(successFn, errorFn);
                return promise;
            }
            $q.when(AccessControlService.hasPermission(AccessControlService.DATASOURCE_ACCESS))
                .then(function (access) {
                if (access) {
                    getDataSources();
                }
                else {
                    self.loading = false;
                }
            });
        }
        ;
        return CatalogController;
    }());
    exports.CatalogController = CatalogController;
    angular.module(module_name_1.moduleName).controller('CatalogController', ["$scope", "$q", "DatasourcesService", "StateService", "AccessControlService", CatalogController]);
});
//# sourceMappingURL=CatalogController.js.map