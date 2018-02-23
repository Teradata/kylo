import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/tables/module-name');

export class TableController {


    tableSchema:any;
    selectedTabIndex:any;
    hql:any;
    schema:any;
    tableName:any;
    datasource:any;
    loading:any;

    constructor(private $scope:any,private $transition$:any,private $http:any
        ,private RestUrlService:any){

        var self = this;
        this.tableSchema =null;

        self.selectedTabIndex = 0;
        self.hql = '';

        var init = function(){
            var schema = $transition$.params().schema;
            self.schema = schema;
            self.tableName = $transition$.params().tableName;
            self.datasource = $transition$.params().datasource;

            getTable(self.schema,self.tableName);
        };


        $scope.$watch(function(){
            return self.selectedTabIndex;
        },function(newVal:any){
        });



        function getTable(schema:any, table:any) {
            if (self.datasource.isHive) {
                getHiveTable(schema, table);
            } else {
                getNonHiveTable(schema, table);
            }
        }

        var successFn = function (response:any) {
            self.tableSchema = response.data;
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
        init();
    };

 
    
    
    
}

angular.module(moduleName).controller('TableController',["$scope","$transition$","$http","RestUrlService",TableController]);
