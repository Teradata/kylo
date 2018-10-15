import * as _ from 'underscore';
import { OnInit, Component } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TdDialogService } from '@covalent/core/dialogs';
import { ObjectUtils } from '../../common/utils/object-utils';

@Component({
    templateUrl: "js/admin/jcr/jcr-query.html",
    selector : 'jcr-query-controller'
})
export class JcrQueryComponent implements OnInit {
    sql: string = "";
    headers = new HttpHeaders({'Content-Type': 'application/json; charset=UTF-8'});
    ngOnInit(){
           this.sql = 'SELECT fs.[jcr:title], fd.[tba:state], c.[tba:systemName] \n'
                       + 'FROM [tba:feed] as e \n'
                       + 'JOIN [tba:feedSummary] AS fs ON ISCHILDNODE(fs, e) \n'
                       + 'JOIN [tba:feedData] AS fd ON ISCHILDNODE(fd, e) \n'
                       +'JOIN [tba:categoryDetails] AS cd ON ISCHILDNODE(e, cd) \n'
                       + 'JOIN [tba:category] as c on ISCHILDNODE(cd,c)';
            this.getIndexes();
        }
    constructor(private http: HttpClient, 
            private snackBar: MatSnackBar,
            private _tdDialogService : TdDialogService)
            {
             }

        loading: boolean = false;
        errorMessage: string = null;
        queryTime: any = null;
        indexesErrorMessage: string = "";
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
                if (response) {
                    this.hideDialog();
                    this.getIndexes();
                    this.snackBar.open('Added the index','OK',{duration : 3000});
                }
            }
            var errorFn = (err: any)=> {
                this.hideDialog();
            }

            var indexCopy = _.extend({},this.index);
            var promise = this.http.post('/proxy/v1/metadata/debug/jcr-index/register',
                            ObjectUtils.toJson(indexCopy),{headers : this.headers}).toPromise().then(successFn, errorFn)
        }

        unregisterIndex(indexName: any){

            if(ObjectUtils.isDefined(indexName)) {
                var successFn = (response: any)=> {
                    if (response) {
                       this.getIndexes();
                       this.snackBar.open('Removed the index '+indexName,'OK',{duration : 3000})
                    }
                }
                var errorFn =(err: any)=> {

                }

                var promise = this.http.post("/proxy/v1/metadata/debug/jcr-index/" + indexName + "/unregister", 
                              {},{headers: this.headers}).toPromise().then(successFn, errorFn);
            }
        }

        changePreviousQuery(){
           this.sql= this.previousQuery;
        }

        showDialog(title: string,message: string){
            this._tdDialogService.openAlert({
                message : message,
                disableClose : true,
                title : title,
                ariaLabel : title
            });
        }

        hideDialog(){
            this._tdDialogService.closeAll();
        }

        reindex(){
                this.showDialog("Reindexing", "Reindexing. Please wait...");
                var successFn = (response: any)=> {
                    this.hideDialog();
                    if (response) {
                        this.snackBar.open('Successfully reindexed','',{duration : 3000});
                    }
                }
                var errorFn = (err: any)=> {
                    this.hideDialog();
                    this.snackBar.open('Error reindexing ','',{duration : 3000});
                }

                var promise = this.http.post("/proxy/v1/metadata/debug/jcr-index/reindex",{},{headers : this.headers})
                                            .toPromise().then(successFn, errorFn);
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
                this.transformResults(response);
            };
            var errorFn = (err: any)=> {
                this.resultSize = 0;
                this.loading = false;
                if(err && err.developerMessage){
                    this.errorMessage = err.developerMessage;
                }
                else {
                    this.errorMessage = 'Error performing query ';
                }
            };
            var promise = this.http.get('/proxy/v1/metadata/debug/jcr-sql',{params:{query:sql}}).toPromise();
            promise.then(successFn, errorFn);
            return promise;
        }

        getIndexes(){
            var successFn = (response: any)=> {
                this.indexes = response;
            };
            var errorFn = (err: any)=> {
                this.indexes = [];
                this.indexesErrorMessage = 'Error getting indexes '+err
            };
            var promise = this.http.get('/proxy/v1/metadata/debug/jcr-index').toPromise();
            promise.then(successFn, errorFn);
            return promise;
        }


        transformResults(result: any) {
           var data: any = {};
           var rows: any = [];
           var columns: any = [];
           this.queryTime = result.queryTime;
           this.explainPlan = result.explainPlan;
           _.forEach(result.columns,(col : any,i : number)=>{
                        columns.push({
                            displayName: col.name,
                            headerTooltip: col.name,
                            minWidth: 150,
                            name: 'col_'+i
                        });
            });

            _.forEach(result.rows,(row: any,i: any)=>{

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