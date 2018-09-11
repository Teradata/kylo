import {Subject} from "rxjs/Subject";
import {DataSource} from "../../catalog/api/models/datasource";
import {PartialObserver} from "rxjs/Observer";
import {FileMetadataTransformResponse} from "../../catalog/datasource/preview-schema/model/file-metadata-transform-response";
import {DatabaseObject, DatabaseObjectType} from "../../catalog/datasource/tables/database-object";
import {PreviewJdbcDataSet} from "../../catalog/datasource/preview-schema/model/preview-jdbc-data-set";
import {Node} from "../../catalog/api/models/node";
import {Observable} from "rxjs/Observable";
import {PreviewDataSet} from "../../catalog/datasource/preview-schema/model/preview-data-set";
import {PreviewDataSetRequest} from "../../catalog/datasource/preview-schema/model/preview-data-set-request";
import {PreviewHiveDataSet} from "../../catalog/datasource/preview-schema/model/preview-hive-data-set";
import {BrowserObject} from "../../catalog/api/models/browser-object";
import {RemoteFile} from "../../catalog/datasource/files/remote-file";
import {PreviewSchemaService} from "../../catalog/datasource/preview-schema/service/preview-schema.service";
import {Injectable} from "@angular/core";
import {FileMetadataTransformService} from "../../catalog/datasource/preview-schema/service/file-metadata-transform.service";
import {TdDialogService} from "@covalent/core/dialogs";
import {MatDialogConfig} from "@angular/material/dialog";
import {ReplaySubject} from "rxjs/ReplaySubject";


export enum DataSetType {
    FILE=1,HIVE=2,JDBC=3
}

export class DataSourceChangedEvent {
    constructor(public dataSource:DataSource,public params:any){}
}

export enum PreviewDataSetResultStatus {
    SUCCESS=1,ERROR=2, EMPTY=3
}
export class PreviewDataSetResultEvent {
    public status: PreviewDataSetResultStatus;

    static EMPTY = new PreviewDataSetResultEvent(null,null)
    constructor(public dataSets: PreviewDataSet[], public errors: PreviewDataSet[]) {
        if((this.dataSets == undefined  || this.dataSets == null|| this.dataSets.length == 0) && (this.errors == undefined ||  this.errors == null|| this.errors.length == 0)) {
            this.status = PreviewDataSetResultStatus.EMPTY;
        }
        else if(errors && errors.length >0) {
            this.status = PreviewDataSetResultStatus.ERROR;
        }
        else {
            this.status = PreviewDataSetResultStatus.SUCCESS;
        }
    }
    public hasError() {
        return this.status == PreviewDataSetResultStatus.ERROR;
    }

    public isEmpty(){

    }
}

@Injectable()
export class DatasetPreviewStepperService {


    public dataSource$ = new Subject<DataSourceChangedEvent>();
    public dataSource: DataSource;
    public dataSourceParams: any;
    public stepIndex: number;

    public stepChanged$ = new Subject<number>();


    constructor(private _dialogService:TdDialogService,
                  private _fileMetadataTransformService: FileMetadataTransformService,
                  private previewSchemaService: PreviewSchemaService) {

    }


    public setDataSource(dataSource: DataSource, params?: any) {
        this.dataSource = dataSource;
        this.dataSourceParams = params;
        this.dataSource$.next(new DataSourceChangedEvent(dataSource, params));
    }

    public subscribeToDataSourceChanges(o: PartialObserver<DataSourceChangedEvent>) {
        return this.dataSource$.subscribe(o);
    }

    public subscribeToStepChanges(o: PartialObserver<number>) {
        return this.stepChanged$.subscribe(o);
    }

    public setStepIndex(index: number) {
        if (this.stepIndex == undefined || this.stepIndex != index) {
            this.stepIndex = index;
            this.stepChanged$.next(index);
        }
    }




   private preparePreviewFiles(node: Node, datasource:DataSource): Observable<PreviewDataSet[]> {
        let subject = new ReplaySubject<PreviewDataSet[]>(1);
        this._fileMetadataTransformService.detectFormatForNode(node, datasource).subscribe((response: FileMetadataTransformResponse) => {
            let dataSetMap = response.results.datasets;
            let previews: PreviewDataSet[] = [];

            if (dataSetMap) {
                let keys = Object.keys(dataSetMap);
                keys.forEach(key => {
                    let dataSet: PreviewDataSet = dataSetMap[key];
                    previews.push(dataSet);
                })
            }
            subject.next(previews);
            //TODO HANDLE ERRORS
        }, (error1:any) => {
//TODO FIX !!!

            this._dialogService.openAlert({
                message: 'Error parsing the file datasets',
                disableClose: true,
                title: 'Error parsing the file datasets'
            });

        });
        return subject;
    }

    private preparePreviewTables(node: Node, type: DataSetType): Observable<PreviewDataSet[]> {
        let datasets: PreviewDataSet[] = [];
        let selectedNodes = node.getSelectedDescendants();


        if (selectedNodes) {
            selectedNodes.forEach(node => {
                let dbObject: DatabaseObject = <DatabaseObject> node.getBrowserObject();
                let dataSet = this.prepareDatabaseObjectForPreview(<DatabaseObject>dbObject, type);
                datasets.push(dataSet);

            });
        }
        else {
            console.error("CANT FIND PATH!!!!!!")
        }
        return Observable.of(datasets);

    }

    private  prepareDatabaseObjectForPreview(dbObject: DatabaseObject,type: DataSetType): PreviewDataSet {
        if (DatabaseObjectType.isTableType(dbObject.type)) {
            let dataSet = null;
            if (DataSetType.HIVE == type) {
                dataSet = new PreviewHiveDataSet();
            }
            else { //if(DataSetType.JDBC == type) {
                dataSet = new PreviewJdbcDataSet()
            }

            let schema = dbObject.schema ? dbObject.schema : dbObject.catalog;
            let table = dbObject.name
            let key = schema + "." + table;
            dataSet.items = [key];
            dataSet.displayKey = key;
            dataSet.key = key;
            dataSet.allowsRawView = false;
            dataSet.updateDisplayKey();
            return dataSet;
        }
        else {
            return PreviewDataSet.EMPTY;
        }
    }

    private prepareBrowserObjectForPreview(obj: BrowserObject, datasource:DataSource): Observable<PreviewDataSet> {
        if (obj instanceof DatabaseObject) {
            let type:DataSetType = this.getDataSetType(datasource);
            let dataSet = this.prepareDatabaseObjectForPreview(<DatabaseObject>obj, type);
            //add in any cached preview responses
            //this.previewSchemaService.updateDataSetsWithCachedPreview([dataSet])
            return Observable.of(dataSet);
        }
        else if (obj instanceof RemoteFile) {
            let subject = new ReplaySubject<PreviewDataSet>(1);
            let o = subject.asObservable();
            this._fileMetadataTransformService.detectFormatForPaths([obj.getPath()], datasource).subscribe((response: FileMetadataTransformResponse) => {
                let obj = response.results.datasets;
                if (obj && Object.keys(obj).length > 0) {
                    let dataSet = obj[Object.keys(obj)[0]];
                    subject.next(dataSet);
                }
            }, (error1:any) => {
                subject.next(PreviewDataSet.EMPTY)
            });
            return o;
        }
    }


    private getDataSetType(datasource:DataSource): DataSetType {
        if (!datasource.connector.template || !datasource.connector.template.format) {
            return DataSetType.FILE;
        }
        else if (datasource.connector.template.format == "jdbc") {
            return DataSetType.JDBC;
        }
        else if (datasource.connector.template.format == "hive") {
            return DataSetType.HIVE;
        }
        else {
            console.log("Unsupported type, defaulting to file ", datasource.connector.template.format)
            return DataSetType.FILE;
        }
    }

    private preparePreviewDataSets(node: Node, datasource:DataSource): Observable<PreviewDataSet[]> {

        let type: DataSetType = this.getDataSetType(datasource);

        if (DataSetType.FILE == type) {
            return this.preparePreviewFiles(node,datasource);
        }
        else if (DataSetType.HIVE == type || DataSetType.JDBC == type) {
            return this.preparePreviewTables(node, type);
        }
        else {
            console.log("unsupported datasets")
            return Observable.of([]);
        }
    }

    private _populatePreview(dataSets: PreviewDataSet[],datasource:DataSource) :Observable<PreviewDataSetResultEvent> {
        let previewReady$ = new ReplaySubject<PreviewDataSetResultEvent>(1);
        let observable = previewReady$.asObservable();
        let previews: Observable<PreviewDataSet>[] = [];
        if (dataSets) {
            dataSets.forEach(dataSet => {
                let previewRequest = new PreviewDataSetRequest();
                previewRequest.dataSource = datasource;
                //catch all errors and handle in success of forkjoin
                previews.push(this.previewSchemaService.preview(dataSet, previewRequest).catch((e: any, obs: Observable<PreviewDataSet>) => Observable.of(e)));
            })
        }
        Observable.forkJoin(previews).subscribe((results: PreviewDataSet[]) => {
                let errors: PreviewDataSet[] = [];
                results.forEach(result => {
                    if (result.hasPreviewError()) {
                        errors.push(result);
                    }
                });
             let result = new PreviewDataSetResultEvent(results,errors);
              previewReady$.next(result)

            });
        return observable;

    }


    public prepareAndPopulatePreview(node:Node, datasource:DataSource) :Observable<PreviewDataSetResultEvent> {
        let previewReady$ = new ReplaySubject<PreviewDataSetResultEvent>(1);
        let o = previewReady$.asObservable();
            if (node.countSelectedDescendants() > 0) {
                /// preview and save to feed
                this.preparePreviewDataSets(node,datasource).subscribe(dataSets => {
                    this._populatePreview(dataSets, datasource).subscribe((ev:PreviewDataSetResultEvent) => {
                        previewReady$.next(ev);
                    });
                });
            }
            else {
                previewReady$.next(PreviewDataSetResultEvent.EMPTY)
            }
            return o;

    }


    public prepareAndPopulatePreviewDataSet(file: BrowserObject, datasource:DataSource)  :Observable<PreviewDataSetResultEvent> {
        let previewReady$ = new ReplaySubject<PreviewDataSetResultEvent>(1);
        let o = previewReady$.asObservable();
        this.prepareBrowserObjectForPreview(file,datasource).subscribe((dataSet:PreviewDataSet) => {
            this._populatePreview([dataSet],datasource).subscribe((ev: PreviewDataSetResultEvent) => {
                previewReady$.next(ev);
            });
        })
        return o;
    }






}