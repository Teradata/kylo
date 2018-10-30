import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "./module-name";
import { Transition } from '@uirouter/core';
import { DatasourcesService } from '../services/DatasourcesService';

export class TableController {


    tableSchema:any = null;
    selectedTabIndex:number = 0;
    hql:string = '';
    schema:any;
    tableName:any;
    datasource:any;
    loading:any;
    datasourceId:any;
    cardTitle:any;
    $transition$:Transition;

    $onInit() {
        this.ngOnInit();
    }
    ngOnInit() {
        this.schema = this.$transition$.params().schema;
        this.tableName = this.$transition$.params().tableName;
        this.datasourceId = this.$transition$.params().datasource;
    }

    static readonly $inject = ["$scope","$http","$filter","RestUrlService", "DatasourcesService"];

    constructor(private $scope:IScope,private $http:angular.IHttpService,private $filter:angular.IFilterService
        ,private RestUrlService:any,private datasourcesService:DatasourcesService){

        $scope.$watch( () => {
            return this.selectedTabIndex;
        },(newVal:any) =>{
        });

        this.getDatasource(this.datasourceId).then(this.init);
    };
    
    init = () =>{
        this.getTable(this.schema, this.tableName);
    };

    successFn = (response:any) =>{
        this.tableSchema = response.data;
        this.cardTitle = this.schema;
        this.loading = false;
    };
    errorFn = (err:any) => {
        this.loading = false;

    };
    getTable(schema:any, table:any) {
        this.loading = true;
        if (this.datasource.isHive) {
            this.getHiveTable(schema, table);
        } else {
            this.getNonHiveTable(schema, table);
        }
    }

    getNonHiveTable(schema:any, table:any){
        var params = {schema: schema};
        var promise = this.$http.get(this.RestUrlService.GET_DATASOURCES_URL + "/" + this.datasource.id + "/tables/" + table, {params: params});
        promise.then(this.successFn, this.errorFn);
        return promise;
    }

    getHiveTable(schema:any,table:any){
        var promise = this.$http.get(this.RestUrlService.HIVE_SERVICE_URL+"/schemas/"+schema+"/tables/"+table);
        promise.then(this.successFn, this.errorFn);
        return promise;
    }

    getDatasource(datasourceId:any) {
        this.loading = true;
        var successFn =  (response:any) => {
            this.datasource = response;
            this.loading = false;
        };
        var errorFn = (err:any) => {
            this.loading = false;
        };
        return this.datasourcesService.findById(datasourceId).then(successFn, errorFn);
    }
}

angular.module(moduleName).component('tableController',{
    controllerAs: 'vm',
    controller :TableController,
    templateUrl: './table.html',
    bindings : {
        $transition$ : '<'
    }
});
