import {Subject} from "rxjs/Subject";
import {Observable} from "rxjs/Observable";
import {PartialObserver} from "rxjs/Observer";
import {switchMap} from 'rxjs/operators/switchMap';
import {catchError} from "rxjs/operators/catchError";
import {map} from "rxjs/operators/map";
import {tap} from "rxjs/operators/tap"
import {take} from "rxjs/operators/take"
import {ReplaySubject} from "rxjs/ReplaySubject";
import {DatabaseObject, DatabaseObjectType} from "../../tables/database-object";
import {Node} from "../../../api/models/node";
import {DataSource} from "../../../api/models/datasource";
import {FileMetadataTransformResponse} from "../model/file-metadata-transform-response";
import {PreviewDataSet} from "../model/preview-data-set";
import {PreviewJdbcDataSet} from "../model/preview-jdbc-data-set";
import {PreviewDataSetRequest} from "../model/preview-data-set-request";
import {PreviewHiveDataSet} from "../model/preview-hive-data-set";
import {PreviewFileDataSet} from "../model/preview-file-data-set";
import {BrowserObject} from "../../../api/models/browser-object";
import {SchemaParser} from "../../../../model/field-policy";
import {SchemaParseSettingsDialog} from "../schema-parse-settings-dialog.component";
import {RemoteFile} from "../../files/remote-file";
import {PreviewSchemaService} from "./preview-schema.service";
import {FileMetadataTransformService} from "./file-metadata-transform.service";
import {CloneUtil} from "../../../../../common/utils/clone-util";
import {TdDialogService} from "@covalent/core/dialogs";
import {TdLoadingService} from "@covalent/core/loading";
import {Injectable} from "@angular/core";

import 'rxjs/add/observable/forkJoin';


export enum DataSetType {
    FILE = 1, HIVE = 2, JDBC = 3
}

export class DataSourceChangedEvent {
    constructor(public dataSource: DataSource, public params: any) {
    }
}

export enum PreviewDataSetResultStatus {
    SUCCESS = 1, ERROR = 2, EMPTY = 3
}

export class PreviewDataSetResultEvent {
    public status: PreviewDataSetResultStatus;

    static EMPTY = new PreviewDataSetResultEvent(null, null)

    constructor(public dataSets: PreviewDataSet[], public errors: PreviewDataSet[]) {
        if ((this.dataSets == undefined || this.dataSets == null || this.dataSets.length == 0) && (this.errors == undefined || this.errors == null || this.errors.length == 0)) {
            this.status = PreviewDataSetResultStatus.EMPTY;
        }
        else if (errors && errors.length > 0) {
            this.status = PreviewDataSetResultStatus.ERROR;
        }
        else {
            this.status = PreviewDataSetResultStatus.SUCCESS;
        }
    }

    public hasError() {
        return this.status == PreviewDataSetResultStatus.ERROR;
    }

    public isEmpty(): boolean {
        return this.dataSets == undefined || this.dataSets == null || this.dataSets.length == 0;
    }
}

/**
 * Core service used to determine file metadata, preview datasets, view raw data, etc
 */
@Injectable()
export class DatasetPreviewService {

    static PREVIEW_LOADING = "CatalogPreviewDatasetComponent.LOADER"

    public dataSource$ = new Subject<DataSourceChangedEvent>();
    public dataSource: DataSource;
    public dataSourceParams: any;

    public updateViewEvent$ = new Subject<any>();

    /**
     * the datasets to preview
     */
    public datasets: PreviewDataSet[];


    constructor(private _dialogService: TdDialogService,
                private _loadingService: TdLoadingService,
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

    public subscribeToUpdateView(o: PartialObserver<any>) {
        return this.updateViewEvent$.subscribe(o);
    }

    public notifyToUpdateView() {
        this.updateViewEvent$.next();
    }


    private preparePreviewFiles(node: Node, datasource: DataSource): Observable<PreviewDataSet[]> {
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
        }, (error1: any) => {

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

    private prepareDatabaseObjectForPreview(dbObject: DatabaseObject, type: DataSetType): PreviewDataSet {
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
            dataSet.items = [dbObject.qualifiedIdentifier];
            dataSet.displayKey = key;
            dataSet.key = key;
            dataSet.allowsRawView = false;
            return dataSet;
        }
        else {
            return PreviewDataSet.EMPTY;
        }
    }

    private prepareBrowserObjectForPreview(obj: BrowserObject, datasource: DataSource): Observable<PreviewDataSet> {
        if (obj instanceof DatabaseObject) {
            let type: DataSetType = this.getDataSetType(datasource);
            let dataSet = this.prepareDatabaseObjectForPreview(<DatabaseObject>obj, type);
            return Observable.of(dataSet);
        }
        else if (obj instanceof RemoteFile) {

            return this._fileMetadataTransformService.detectFormatForPaths([obj.getPath()], datasource)
                .pipe(
                    catchError((error1: any) => {
                        return Observable.of(PreviewDataSet.EMPTY)
                    }),
                    map((response: FileMetadataTransformResponse) => {
                        let obj = response.results.datasets;
                        if (obj && Object.keys(obj).length > 0) {
                            let dataSet = obj[Object.keys(obj)[0]];
                            return dataSet;
                        }
                        return PreviewDataSet.EMPTY
                    }),
                    take(1)
                );
        }
    }


    private getDataSetType(datasource: DataSource): DataSetType {
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

    private preparePreviewDataSets(node: Node, datasource: DataSource): Observable<PreviewDataSet[]> {

        let type: DataSetType = this.getDataSetType(datasource);

        if (DataSetType.FILE == type) {
            return this.preparePreviewFiles(node, datasource);
        }
        else if (DataSetType.HIVE == type || DataSetType.JDBC == type) {
            return this.preparePreviewTables(node, type);
        }
        else {
            console.log("unsupported datasets")
            return Observable.of([]);
        }
    }

    private _populatePreview(dataSets: PreviewDataSet[], datasource: DataSource, fallbackToTextOnError: boolean = true): Observable<PreviewDataSetResultEvent> {
        let previewReady$ = new ReplaySubject<PreviewDataSetResultEvent>(1);
        let observable = previewReady$.asObservable();
        let previews: Observable<PreviewDataSet>[] = [];
        if (dataSets) {
            dataSets.forEach(dataSet => {
                let previewRequest = new PreviewDataSetRequest();
                previewRequest.dataSource = datasource;
                //catch all errors and handle in success of forkjoin
                previews.push(this.previewSchemaService.preview(dataSet, previewRequest, false, fallbackToTextOnError).pipe(catchError((e: any, obs: Observable<PreviewDataSet>) => {
                    console.log("Error previewing dataset ", e);
                    return Observable.of(e);
                })));
            })
        }
        Observable.forkJoin(previews).subscribe((results: PreviewDataSet[]) => {
            let errors: PreviewDataSet[] = [];
            results.forEach(result => {
                if (result.hasPreviewError()) {
                    errors.push(result);
                }
            });
            let result = new PreviewDataSetResultEvent(results, errors);
            previewReady$.next(result)

        });
        return observable;

    }

    previewAsTextOrBinary(previewDataSet: PreviewFileDataSet, binary: boolean, rawData: boolean): Observable<PreviewDataSet> {
        return this.previewSchemaService.previewAsTextOrBinary(previewDataSet, binary, rawData);
    }


    public prepareAndPopulatePreview(node: Node, datasource: DataSource): Observable<PreviewDataSetResultEvent> {
        let previewReady$ = new ReplaySubject<PreviewDataSetResultEvent>(1);
        let o = previewReady$.asObservable();
        if (node.countSelectedDescendants() > 0) {
            /// preview and save to feed
            this.preparePreviewDataSets(node, datasource).subscribe(dataSets => {
                this._populatePreview(dataSets, datasource).subscribe((ev: PreviewDataSetResultEvent) => {
                    previewReady$.next(ev);
                });
            });
        }
        else {
            previewReady$.next(PreviewDataSetResultEvent.EMPTY)
        }
        return o;

    }


    public prepareAndPopulatePreviewDataSet(file: BrowserObject, datasource: DataSource): Observable<PreviewDataSetResultEvent> {
        let previewReady$ = new ReplaySubject<PreviewDataSetResultEvent>(1);
        let o = previewReady$.asObservable();
        this.prepareBrowserObjectForPreview(file, datasource).subscribe((dataSet: PreviewDataSet) => {
            this._populatePreview([dataSet], datasource).subscribe((ev: PreviewDataSetResultEvent) => {
                previewReady$.next(ev);
            });
        })
        return o;
    }


    public prepareAndPopulatePreviewDataSets(files: BrowserObject[], datasource: DataSource): Observable<PreviewDataSetResultEvent> {
        let previewReady$ = new ReplaySubject<PreviewDataSetResultEvent>(1);
        let o = previewReady$.asObservable();
        let observables: Observable<PreviewDataSet>[] = [];
        files.forEach((obj: BrowserObject) => {
            observables.push(this.prepareBrowserObjectForPreview(obj, datasource));
        })

        return Observable.forkJoin(observables).pipe(
            switchMap((dataSets: PreviewDataSet[]) => {
                return this._populatePreview(dataSets, datasource)
            })
        );

    }

    /**
     * Open the schema parser dialog settings
     * @param {PreviewFileDataSet} dataset
     * @return {Observable<PreviewDataSet>}
     */
    openSchemaParseSettingsDialog(dataset: PreviewFileDataSet): Observable<PreviewDataSet> {
        //copy the dataset so the orig doesnt get modified
        let copy = CloneUtil.deepCopy(dataset);
        let schemaParser = copy.schemaParser;

        //reapply the user modified parser only if it was errored out
        if (copy.hasPreviewError() && copy.userModifiedSchemaParser) {
            schemaParser = copy.userModifiedSchemaParser;
        }


        let dialogRef = this._dialogService.open(SchemaParseSettingsDialog, {
            width: '500px',
            data: {
                schemaParser: schemaParser
            }
        });

        return dialogRef.afterClosed().filter(result => {
            return result != undefined
        }).pipe(switchMap((result: SchemaParser) => {
            return this._previewWithSchemaParser(copy, result);
        }));
    }


    /**
     * Preview the dataset using the supplied schema.
     * The incoming dataset will get updated with the schemaParser and preview data.
     * A copy should be passed in if it does not want to be modified.
     * @param {PreviewFileDataSet} dataset
     * @param {SchemaParser} schemaParser
     * @return {Observable<PreviewDataSet>}
     * @private
     */
    private _previewWithSchemaParser = (dataset: PreviewFileDataSet, schemaParser: SchemaParser) => {
        dataset.schemaParser = schemaParser

        let previewRequest = new PreviewDataSetRequest();
        previewRequest.dataSource = dataset.dataSource;
        //reset the preview
        dataset.preview = undefined;
        this._loadingService.register(DatasetPreviewService.PREVIEW_LOADING)
        this.notifyToUpdateView();
        //clear the options so they can be reapplied via the incoming schema parser
        dataset.sparkOptions = undefined;


        return this.previewSchemaService.preview(dataset, previewRequest, false, false)
            .pipe(
                catchError((error: any, o: Observable<PreviewDataSet>) => {
                    this._loadingService.resolve(DatasetPreviewService.PREVIEW_LOADING)
                    if (error instanceof PreviewDataSet) {
                        return Observable.throw(error);
                    }
                    else {
                        return Observable.of(PreviewDataSet.EMPTY);
                    }
                }),
                tap((result: PreviewDataSet) => {
                    this._loadingService.resolve(DatasetPreviewService.PREVIEW_LOADING)
                    this.notifyToUpdateView();
                    return result;
                })
            )
    }

}
