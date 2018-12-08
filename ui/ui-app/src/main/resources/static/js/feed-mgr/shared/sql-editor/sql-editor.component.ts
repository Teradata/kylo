import {Component, Inject, OnDestroy, OnInit, ViewChild, ViewContainerRef, Input, EventEmitter, OnChanges, SimpleChanges, Output} from "@angular/core";
import {TdDialogService} from "@covalent/core/dialogs";
import {FattableComponent, FattableOptions} from "../fattable/fattable.component";
import {HiveService} from "../../services/HiveService";
import {CodemirrorComponent} from "ng2-codemirror";
import {TdLoadingService} from "@covalent/core/loading";
import {DatasourcesService} from "../../services/DatasourcesService";
import {FormControl, FormGroup} from "@angular/forms";
import {TranslateService} from "@ngx-translate/core";
declare const CodeMirror: any;

import { FormsModule } from '@angular/forms';

@Component( {
    selector:"sql-editor",
    templateUrl:"./sql-editor.component.html"
})
export class SqlEditorComponent implements OnInit, OnDestroy{

    /**
     * The datasource to use for the editor
     */
    @Input()
    datasourceId?:any;

    @Input()
    defaultSchemaName?:string =null;

    @Input()
    defaultTableName?:string =null;

    @Input()
    allowFullscreen?:boolean = true

    @Input()
    allowExecuteQuery:boolean;

    @Input()
    allowDatabaseBrowse:boolean;

    /**
     * the sql
     */
    @Input()
    sql?:string ='';

    @Input()
    form?:FormGroup;

    /**
     * number of px high to render the editor
     */
    @Input()
        height:number = 500;

    /**
     * Indicates if query execution failed
     */
    @Input()
    queryExecutionFailure:boolean;

    @Output()
    sqlChange = new EventEmitter<string>();

    /**
     * resolved datasource from the passed in datasourceId if available
     */
    datasource:any;

    browseDatabaseNameControl:FormControl;

    browseDatabasePlaceholder:string = "Database"

    browseTableNameControl:FormControl;

    browseTableNamePlaceholder:string = "Table";


    /**
     * are we executing the query
     */
    executingQuery:boolean;

    /**
     * resulting data from the query before applying it to the grid
     */
    private queryResults:any;

    /**
     * options to be applied to the preview
     */
    previewTableOptions:FattableOptions = null;

    loadingDatasourceSchemas:boolean;

    databaseMetadata:any = {};

    databaseNames:string[];

    databaseTableNames:any[];

    /**
     * expose the Object.keys method to the ui
     *
     * @type {(o: {}) => string[]}
     */
    objectKeys = Object.keys;

    codeMirrorConfig: any = {
        onLoad: this.codeMirrorLoaded.bind(this)
    };

    codeMirrorOptions:any = {
        lineWrapping: true,
        indentWithTabs: true,
        smartIndent: true,
        lineNumbers: true,
        matchBrackets: true,
        autofocus: true,
        extraKeys: { 'Ctrl-Space': 'autocomplete' },
        hint: CodeMirror["hint"] != undefined? CodeMirror["hint"].sql : undefined,
        hintOptions: {
            tables: {}
        },
        mode: 'text/x-sql'
    };



    @ViewChild(CodemirrorComponent)
    codeMirrorView: CodemirrorComponent;


    @ViewChild(FattableComponent)
    queryPreview: FattableComponent;


    constructor(private _dialogService: TdDialogService, private _translateService:TranslateService,   private viewContainerRef: ViewContainerRef,  @Inject("HiveService") private hiveService: HiveService,@Inject("DatasourcesService") private datasourcesService: DatasourcesService) {


    }
    onSqlChange(value:any){
        if(typeof value == "string") {
            //remove trailing semi-colon in SQL
            value = value.replace(/;$/, "");
            this.sqlChange.emit(value);
        }
    }

    overrideQueryFailureCheck() {
        this.sqlChange.emit(this.sql);
    }


    ngOnInit(){
        this.browseDatabasePlaceholder = this._translateService.instant('"views.hql-editor.Database')
        this.browseTableNamePlaceholder = this._translateService.instant('"views.hql-editor.Table')
        if(this.form == undefined) {
            this.form = new FormGroup({})
        }
        this.browseDatabaseNameControl = new FormControl();
        this.form.addControl("browseDatabaseName",this.browseDatabaseNameControl)
        this.browseTableNameControl = new FormControl();
        this.form.addControl("browseTableName",this.browseTableNameControl)
        this.setupCodeMirror();
    }

    ngAfterViewInit(): void {
        this.codeMirrorLoaded(this.codeMirrorView.instance);
    }


    ngOnDestroy() {

    }


    private codeMirrorLoaded(editor:any){
        //any code mirror init here
    }





    //Not tested yet


    private setupCodeMirror(){

        if (this.datasource && this.datasource.isHive) {
            this.codeMirrorOptions.mode = 'text/x-hive';
        } else {
            this.codeMirrorOptions.mode = 'text/x-sql';
        }

        if (this.defaultSchemaName != null && this.defaultTableName != null) {
            // TODO Change to a deferred to provide the SQL and execute the query when the query text becomes available.
            if (!this.sql) {
                if (this.datasource.isHive) {
                    this.sql = "SELECT * FROM " + this.quote(this.defaultSchemaName) + "." + this.quote(this.defaultTableName) + " LIMIT 20";
                    if (this.allowExecuteQuery) {
                        this.query();
                    }
                } else {
                    this.datasourcesService.getPreviewSql(this.datasourceId, this.defaultSchemaName, this.defaultTableName, 20)
                        .then(function (response: string) {
                            this.sql = response;
                            if (this.allowExecuteQuery) {
                                this.query();
                            }
                        });
                }
            }
        }
       // this.codeMirrorView.focus()


    }

    private quote(expression: any) {
        if (this.datasource.isHive) {
            return "`" + expression + "`";
        } else {
            return expression;
        }
    }
    query(){
        this.executingQuery = true;
        var successFn = (tableData: any) =>{
            var result = this.queryResults = this.hiveService.transformQueryResultsToUiGridModel(tableData);
            this.previewTableOptions = new FattableOptions({
                headers: result.columns,
                rows: result.rows
            });
            this.queryPreview.setupTable();
            this.executingQuery = false;
        };
        var errorFn = (err: any) => {
            this.executingQuery = false;
        };
        var promise;
        if (this.datasource.isHive) {
            promise = this.hiveService.queryResult(this.sql);
        } else {
            promise = this.datasourcesService.query(this.datasourceId, this.sql);
        }
        return promise.then(successFn, errorFn);
    }

    fullscreen(){

    }

    browseTable() {
        this.executingQuery = true;
        let browseDatabaseName = this.browseDatabaseNameControl.value;
        let browseTableName = this.browseTableNameControl.value;
        return this.hiveService.browseTable(browseDatabaseName, browseTableName, null)
            .then((tableData: any) =>{
                this.executingQuery = false;
                this.queryResults = this.hiveService.transformQueryResultsToUiGridModel(tableData);
            }, (err: any) => {
                this.executingQuery = false;
                this._dialogService.openAlert({
                    message: 'There was an error browsing the table',
                    viewContainerRef: this.viewContainerRef,
                    title: 'Cannot browse the table',
                    width: '500px',
                });
            });
    }

    onDatabaseChange(){
        let dbName = this.form.get('browseDatabaseName').value;
        let tableData = this.databaseMetadata[dbName] || [];
        this.databaseTableNames =Object.keys(tableData);
    }





    getTable(schema:string, table:string){
        this.loadingDatasourceSchemas = true;
        let successFn = (response:any) =>{
            if(response && response.data.hintOptions && response.data.hintOptions.tables) {
                this.codeMirrorOptions.hintOptions.tables = response.data.hintOptions.tables;
            }
            this.loadingDatasourceSchemas = false;
            this.databaseMetadata = response.data.databaseMetadata;
            this.databaseNames = response.data.databaseNames;
        };
        var errorFn = (err:any) => {
            this.loadingDatasourceSchemas = false;
            //self.metadataMessage = metadataErrorMessage
        };
        var promise = this.hiveService.getTablesAndColumns();
        promise.then(successFn, errorFn);
        return promise;
    }

    private getDatasource(datasourceId: any) {
        this.executingQuery = true;
        var successFn = (response: any) => {
            this.datasource = response;
            this.executingQuery = false;
        };
        var errorFn = (err: any) =>{
            this.executingQuery = false;
        };
        return this.datasourcesService.findById(datasourceId).then(successFn, errorFn);
    }




}