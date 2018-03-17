import * as angular from 'angular';
import {moduleName} from "../module-name";
import * as _ from 'underscore';

export class JcrQueryController implements ng.IComponentController{
   // sql: string;
    sql= "";
    ngOnInit(){
           this.sql = 'SELECT fs.[jcr:title], fd.[tba:state], c.[tba:systemName] \n'
                       + 'FROM [tba:feed] as e \n'
                       + 'JOIN [tba:feedSummary] AS fs ON ISCHILDNODE(fs, e) \n'
                       + 'JOIN [tba:feedData] AS fd ON ISCHILDNODE(fd, e) \n'
                       +'JOIN [tba:categoryDetails] AS cd ON ISCHILDNODE(e, cd) \n'
                       + 'JOIN [tba:category] as c on ISCHILDNODE(cd,c)';
            this.getIndexes();
        }
constructor(private $scope: any,
            private $http: any, 
            private $mdDialog: any,
            private $mdToast: any,
            private AccessControlService:any)
            {
               this.ngOnInit();
             }

        loading: boolean = false;
        errorMessage: any = null;
        queryTime: any = null;
        indexesErrorMessage: any = "";
        codemirrorOptions: any = {
                        lineWrapping: true,
                        indentWithTabs: true,
                        smartIndent: true,
                        lineNumbers: true,
                        matchBrackets: true,
                        autofocus: true,
                        mode: 'text/x-hive'
        };
  
        gridOptions: any = {
            columnDefs: [],
            data: null,
            enableColumnResizing: true,
            enableGridMenu: true,
            flatEntityAccess: true
        };

        resultSize: any = 0;
        indexes:any = [];
        index:any ={
            indexName:'',
            nodeType:'',
            propertyName:'',
            propertyType:''
        };
        indexTable: any = {
            currentPage:1,
            rowsPerPage:5
        }
        previousQueries: any = [];
        previousQuery:any = '';

        explainPlan: any = null;


        executeQuery(){
           this.query();
        }

        propertyTypes: any[] = [{name:"String",value:1},
                                {name:"Binary",value:2},
                                {name:"Long",value:3},
                                {name:"Double",value:4},
                                {name:"Date",value:5},
                                {name:"Boolean",value:6},
                                {name:"Name",value:7},
                                {name:"Path",value:8},
                                {name:"Reference",value:9},
                                {name:"Weak Referebce",value:10},
                                {name:"URI",value:11},
                                {name:"Decimal",value:12}];

        indexKinds: any[] = ["VALUE","ENUMERATED_VALUE","UNIQUE_VALUE","TEXT","NODE_TYPE"]

        registerIndex(){
            this.showDialog("Adding Index", "Adding index. Please wait...");
            var successFn =(response: any)=> {
                if (response.data) {
                    this.hideDialog();
                    this.getIndexes();
                    this.$mdToast.show(
                        this.$mdToast.simple()
                            .textContent('Added the index')
                            .hideDelay(3000)
                    );
                }
            }
            var errorFn = (err: any)=> {
                this.hideDialog();
            }

            var indexCopy = angular.extend({},this.index);
            var promise = this.$http({
                url: "/proxy/v1/metadata/debug/jcr-index/register",
                method: "POST",
                data: angular.toJson(indexCopy),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            }).then(successFn, errorFn);
        }

        unregisterIndex(indexName: any){

            if(angular.isDefined(indexName)) {
                var successFn = (response: any)=> {
                    if (response.data) {
                       this.getIndexes();
                        this.$mdToast.show(
                            this.$mdToast.simple()
                                .textContent('Removed the index '+indexName)
                                .hideDelay(3000)
                        );
                    }
                }
                var errorFn =(err: any)=> {

                }

                var promise = this.$http({
                    url: "/proxy/v1/metadata/debug/jcr-index/" + indexName + "/unregister",
                    method: "POST",
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    }
                }).then(successFn, errorFn);
            }
        }

        changePreviousQuery(){
           this.sql= this.previousQuery;
        }

        showDialog(title: any,message: any){
            this.$mdDialog.show(
                this.$mdDialog.alert()
                    .parent(angular.element(document.body))
                    .clickOutsideToClose(false)
                    .title(title)
                    .textContent(message)
                    .ariaLabel(title)
            );
        }

        hideDialog(){
            this.$mdDialog.hide();
        }

        reindex (){
                this.showDialog("Reindexing", "Reindexing. Please wait...");



                var successFn = (response: any)=> {
                    this.hideDialog();
                    if (response.data) {
                        this.$mdToast.show(
                            this.$mdToast.simple()
                                .textContent('Successfully reindexed')
                                .hideDelay(3000)
                        );
                    }
                }
                var errorFn = (err: any)=> {
                    this.hideDialog();
                    this.$mdToast.show(
                        this.$mdToast.simple()
                            .textContent('Error reindexing ')
                            .hideDelay(3000)
                    );
                }

                var promise = this.$http({
                    url: "/proxy/v1/metadata/debug/jcr-index/reindex",
                    method: "POST",
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    }
                }).then(successFn, errorFn);
        }

        query() {
            this.loading = true;
            this.errorMessage = null;
            this.explainPlan = null;
            var sql = this.sql;
            var successFn = (response: any)=> {
                if(_.indexOf(this.previousQueries,sql) == -1) {
                    this.previousQueries.push(sql);
                }
                this.loading = false;
                this.transformResults(response.data);
            };
            var errorFn = (err: any)=> {
                this.resultSize = 0;
                this.loading = false;
                if(err && err.data && err.data.developerMessage){
                    this.errorMessage = err.data.developerMessage;
                }
                else {
                    this.errorMessage = 'Error performing query ';
                }

            };
            var promise = this.$http.get('/proxy/v1/metadata/debug/jcr-sql',{params:{query:sql}});
            promise.then(successFn, errorFn);
            return promise;
        }

        getIndexes(){
            var successFn = (response: any)=> {
                this.indexes = response.data;
            };
            var errorFn = (err: any)=> {
                this.indexes = [];
                this.indexesErrorMessage = 'Error getting indexes '+err
            };
            var promise = this.$http.get('/proxy/v1/metadata/debug/jcr-index');
            promise.then(successFn, errorFn);
            return promise;
        }


        transformResults(result: any) {

            var data: any = {};
            var rows: any = [];
            var columns: any = [];
           this.queryTime = result.queryTime;
           this.explainPlan = result.explainPlan;


            angular.forEach(result.columns,(col,i)=>{
                        columns.push({
                            displayName: col.name,
                            headerTooltip: col.name,
                            minWidth: 150,
                            name: 'col_'+i
                        });
            });

            angular.forEach(result.rows,(row: any,i: any)=>{

              var rowObj = {}
               _.each(row.columnValues,(col:any,i:any)=>{
                   rowObj['col_'+i] = col.value;
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

}

 angular.module(moduleName).controller("JcrQueryController", ["$scope", "$http","$mdDialog", "$mdToast","AccessControlService",JcrQueryController]);
