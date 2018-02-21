define(['angular', 'feed-mgr/tables/module-name'], function (angular, moduleName) {

    /**
     * Displays a list of datasources.
     */
    var CatalogController = function ($scope, $q, DatasourcesService, StateService, AccessControlService) {
        var self = this;
        this.datasources = [DatasourcesService.getHiveDatasource()];

        self.loading = true;

        self.navigateToSchemas = function (datasource) {
            StateService.FeedManager().Table().navigateToSchemas(datasource.id);
        };

        function getDataSources() {
            var successFn = function (response) {
                var jdbcSources = _.filter(response, function(ds) {
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
                } else {
                    self.loading = false;
                }
            });
    };

    angular.module(moduleName).controller('CatalogController', ["$scope", "$q", "DatasourcesService", "StateService", "AccessControlService", CatalogController]);
});

