import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "./module-name";

export class CatalogController {


    datasources:any;
    loading:any;
    navigateToSchemas:any;
    /**
     * Displays a list of datasources.
     */
    constructor(private $scope:any, private $q:any, private DatasourcesService:any, private StateService:any, private AccessControlService:any) {
        var self = this;
        this.datasources = [DatasourcesService.getHiveDatasource()];

        self.loading = true;

        self.navigateToSchemas = function (datasource:any) {
            StateService.FeedManager().Table().navigateToSchemas(datasource.id);
        };

        function getDataSources() {
            var successFn = function (response:any) {
                var jdbcSources = _.filter(response, function(ds) {
                    return ds['@type'] === 'JdbcDatasource';
                });
                self.datasources.push.apply(self.datasources, jdbcSources);
                self.loading = false;
            };
            var errorFn = function (err:any) {
                self.loading = false;
            };

            var promise = DatasourcesService.findAll();
            promise.then(successFn, errorFn);
            return promise;
        }

        $q.when(AccessControlService.hasPermission(AccessControlService.DATASOURCE_ACCESS))
            .then(function (access:any) {
                if (access) {
                    getDataSources();
                } else {
                    self.loading = false;
                }
            });
    };

}
angular.module(moduleName).controller('CatalogController', ["$scope", "$q", "DatasourcesService", "StateService", "AccessControlService", CatalogController]);

