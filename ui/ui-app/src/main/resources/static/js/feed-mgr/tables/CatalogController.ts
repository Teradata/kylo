import * as angular from 'angular';
import * as _ from "underscore";
import { moduleName } from "./module-name";
import {AccessControlService} from '../../services/AccessControlService';
import { DatasourcesService } from '../services/DatasourcesService';

export class CatalogController {


    datasources: any;
    loading: boolean = true;

    $onInit() {
        this.ngOnInit();
    }
    ngOnInit() {
        this.$q.when(this.accessControlService.hasPermission(AccessControlService.DATASOURCE_ACCESS))
            .then((access: any) => {
                if (access) {
                    this.getDataSources();
                } else {
                    this.loading = false;
                }
            });
    }

    /**
     * Displays a list of datasources.
     */

    static readonly $inject = ["$scope", "$q", "DatasourcesService", "StateService", "AccessControlService"];
    constructor(private $scope: IScope, private $q: angular.IQService, private datasourcesService: DatasourcesService, private StateService: any,
        private accessControlService: AccessControlService) {
       
            this.datasources = [this.datasourcesService.getHiveDatasource()];
    
        };
    navigateToSchemas = (datasource: any) => {
        this.StateService.FeedManager().Table().navigateToSchemas(datasource.id);
    };
    getDataSources() {
        var successFn = (response: any) => {
            var jdbcSources = _.filter(response, (ds) => {
                return ds['@type'] === 'JdbcDatasource';
            });
            this.datasources.push.apply(this.datasources, jdbcSources);
            this.loading = false;
        };
        var errorFn = (err: any) => {
            this.loading = false;
        };

        var promise = this.datasourcesService.findAll();
        promise.then(successFn, errorFn);
        return promise;
    };
}
angular.module(moduleName).component('catalogController', {
    controller: CatalogController,
    controllerAs: 'vm',
    templateUrl: './catalog.html'
});

