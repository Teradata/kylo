import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "./module-name";

export class TableController {


    tableSchema:any;
    selectedTabIndex:any;
    hql:any;
    schema:any;
    tableName:any;
    datasource:any;
    loading:any;
    datasourceId:any;
    cardTitle:any;

    constructor(private $scope:any,private $transition$:any,private $http:any,private $filter:any
        ,private RestUrlService:any,private DatasourcesService:any){

        var self = this;
        this.tableSchema =null;

        self.selectedTabIndex = 0;
        self.hql = '';
        self.schema = $transition$.params().schema;
        self.tableName = $transition$.params().tableName;
        self.datasourceId = $transition$.params().datasource;

        var init = function(){
            getTable(self.schema, self.tableName);
        };


        $scope.$watch(function(){
            return self.selectedTabIndex;
        },function(newVal:any){
        });



        function getTable(schema:any, table:any) {
            self.loading = true;
            if (self.datasource.isHive) {
                getHiveTable(schema, table);
            } else {
                getNonHiveTable(schema, table);
            }
        }

        var successFn = function (response:any) {
            self.tableSchema = response.data;
            self.cardTitle = self.schema;
            self.loading = false;
        };
        var errorFn = function (err:any) {
            self.loading = false;

        };

        function getNonHiveTable(schema:any, table:any){
            var params = {schema: schema};
            var promise = $http.get(RestUrlService.GET_DATASOURCES_URL + "/" + self.datasource.id + "/tables/" + table, {params: params});
            promise.then(successFn, errorFn);
            return promise;
        }

        function getHiveTable(schema:any,table:any){
            var promise = $http.get(RestUrlService.HIVE_SERVICE_URL+"/schemas/"+schema+"/tables/"+table);
            promise.then(successFn, errorFn);
            return promise;
        }

        function getDatasource(datasourceId:any) {
            self.loading = true;
            var successFn = function (response:any) {
                self.datasource = response;
                self.loading = false;
            };
            var errorFn = function (err:any) {
                self.loading = false;
            };
            return DatasourcesService.findById(datasourceId).then(successFn, errorFn);
        }

        getDatasource(self.datasourceId).then(init);
    };

 
    
    
    
}

angular.module(moduleName).controller('TableController',["$scope","$transition$","$http","$filter","RestUrlService", "DatasourcesService",TableController]);
