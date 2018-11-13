import * as _ from 'underscore';
import { OnInit, Component } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TdDialogService } from '@covalent/core/dialogs';
import { ITdDataTableColumn, TdDataTableService } from '@covalent/core/data-table';
import { BaseFilteredPaginatedTableView } from '../../common/filtered-paginated-table-view/BaseFilteredPaginatedTableView';
import { TranslateService } from '@ngx-translate/core';
import {ObjectUtils} from '../../../lib/common/utils/object-utils';

@Component({
    templateUrl: "./jcr-query.component.html",
    selector : 'jcr-query-controller'
})
export class JcrQueryComponent extends BaseFilteredPaginatedTableView implements OnInit {
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
                private _tdDialogService : TdDialogService,
                public _dataTableService: TdDataTableService,
                private translate: TranslateService)
    {
        super(_dataTableService);
    }

    loading: boolean = false;
    queryExecuted: boolean = false;
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

    gridOptions: ITdDataTableColumn[] = [];
    tableData : any []= [];
    public indexColumns: ITdDataTableColumn[] = [
        { name: 'indexName', label: 'Name', sortable: true },
        { name: 'indexKind', label: 'Kind', sortable: true},
        { name: 'nodeType', label: 'Node Type', sortable: true },
        { name: 'propertyName', label: 'Property Name(s)', sortable: true},
        { name: 'propertyTypes', label: 'Property Type', sortable: true},
        { name: 'action', label: ''},
    ];

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
        this.showDialog(this.translate.instant('ADMIN.jcrquery.dialog.adding.title'), this.translate.instant('ADMIN.jcrquery.dialog.adding.message'));
        var successFn =(response: any)=> {
            if (response) {
                this.hideDialog();
                this.getIndexes();
                this.snackBar.open(this.translate.instant("ADMIN.jcrquery.index.add"),this.translate.instant("views.common.ok"),{duration : 3000});
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
                    this.snackBar.open(this.translate.instant("ADMIN.jcrquery.index.remove")+indexName,this.translate.instant("views.common.ok"),{duration : 3000})
                }
            }
            var errorFn =(err: any)=> {

            }

            var promise = this.http.post("/proxy/v1/metadata/debug/jcr-index/" + indexName + "/unregister",
                {},{headers: this.headers}).toPromise().then(successFn, errorFn);
        }
    }

    changePreviousQuery(){
        this.sql = this.previousQuery;
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
        this.showDialog(this.translate.instant('ADMIN.jcrquery.dialog.reindexing.title'), this.translate.instant('ADMIN.jcrquery.dialog.reindexing.message'));
        var successFn = (response: any)=> {
            this.hideDialog();
            if (response) {
                this.snackBar.open(this.translate.instant("ADMIN.jcrquery.reindex.success"),this.translate.instant("views.common.ok"),{duration : 3000});
            }
        }
        var errorFn = (err: any)=> {
            this.hideDialog();
            this.snackBar.open(this.translate.instant("ADMIN.jcrquery.reindex.error"),this.translate.instant("views.common.ok"),{duration : 3000});
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
                this.errorMessage = this.translate.instant('ADMIN.jcrquery.error.query.perform');
            }
        };
        var promise = this.http.get('/proxy/v1/metadata/debug/jcr-sql',{params:{query:sql}}).toPromise();
        promise.then(successFn, errorFn);
        return promise;
    }

    getIndexes(){
        var successFn = (response: any)=> {
            this.indexes = response;
            super.setSortBy('indexName');
            super.setDataAndColumnSchema(this.indexes,this.indexColumns);
        };
        var errorFn = (err: any)=> {
            this.indexes = [];
            this.indexesErrorMessage = this.translate.instant('ADMIN.jcrquery.index.get.error') +err
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
                label: col.name,
                tooltip: col.name,
                name: 'col_'+i,
                sortable : true
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
        this.gridOptions = columns;
        this.tableData = rows;
        this.resultSize = data.rows.length;
        this.queryExecuted = true;
        return data;
    };
}