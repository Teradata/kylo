import * as angular from 'angular';
import * as _ from 'underscore';
import {PreviewFileDataSet} from "../model/preview-file-data-set";
import {FileMetadataTransformResponse} from "../model/file-metadata-transform-response"
import {Node} from "../../../api/models/node";
import {Observable} from "rxjs/Observable";
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/share';
import 'rxjs/add/operator/map';
import {DataSource} from "../../../api/models/datasource";
import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";

export interface DetectionResult {
    filePaths ?: string[];
    result ?: FileMetadataTransformResponse;
}

@Injectable()
export class FileMetadataTransformService  {

    /**
     * Array of the selected file paths that have been detected
     */
   private detectionResult:DetectionResult;

    cache:  {[key: string]: FileMetadataTransformResponse} = {}

    constructor(private http: HttpClient) {

    }

    detectFormatForNode(node:Node, datasource:DataSource): Observable<FileMetadataTransformResponse> {
        let paths = this.getSelectedItems(node,datasource);
        //return if the paths are the same
        if (this.detectionResult && this.detectionResult.filePaths != undefined) {
            let selectedFilePathsString = this.detectionResult.filePaths.toString();
            let pathsString = paths.toString();
            if (selectedFilePathsString == pathsString) {
                return Observable.of(this.detectionResult.result);
            }
        }

        return this.detectFormat(paths, datasource);
    }

    detectFormatForPaths(paths:string[], datasource:DataSource): Observable<FileMetadataTransformResponse> {
        //return if the paths are the same
        if (this.detectionResult && this.detectionResult.filePaths != undefined) {
            let selectedFilePathsString = this.detectionResult.filePaths.toString();
            let pathsString = paths.toString();
            if (selectedFilePathsString == pathsString) {
                return Observable.of(this.detectionResult.result);
            }
        }
        return this.detectFormat(paths, datasource);
    }


    getSelectedItems(node:Node, datasource:DataSource) :string[] {
        let paths = node.getSelectedDescendants().map((node) => {
            let path = node.getBrowserObject().getPath();
            if (datasource.connector.id == "local-file-system") {
                //ensure the path starts with file://
                if (path.indexOf("file:/") != 0) {
                    path = "file://" + path;
                }
            }
            return path;
        });
        return paths;
    }


    detectFormat(paths:string[], datasource:DataSource) :Observable<FileMetadataTransformResponse>{
        let observable =null;
        let cacheKey = datasource.id+"_"+paths.sort().toString();
        if(this.cache[cacheKey]){
            observable = Observable.of(this.cache[cacheKey]);

        }
        else {
            observable = new Observable<FileMetadataTransformResponse>((observer) => {

                let request: any = {
                    dataSource: datasource,
                    paths: paths
                }
                let statusCheckTime = 300

                let formatDetected = (data: FileMetadataTransformResponse): void => {

                    this.detectionResult = {}

                    this.detectionResult.filePaths = paths;
                    this.detectionResult.result = new FileMetadataTransformResponse(data)
                    if (this.detectionResult.result.results) {
                        _.each(this.detectionResult.result.results.datasets, (dataset: PreviewFileDataSet, key: string) => {
                            dataset.dataSource = datasource;
                        })
                    }
                    this.cache[cacheKey] = this.detectionResult.result
                    observer.next(this.detectionResult.result);
                }

                let formatError = (data: FileMetadataTransformResponse): void => {
                    console.log('FORMAT ERROR ', data);
                    observer.error(data)
                }

                formatError.bind(this)
                formatDetected.bind(this)


                let checkProgress = (data: FileMetadataTransformResponse): void => {

                    if (data.status == "PENDING") {
                        setTimeout(() => fileMetadataProgress(data.table), statusCheckTime)
                    }
                    else if (data.status == "SUCCESS") {
                        formatDetected(data);
                    }
                    else if (data.status == "ERROR") {
                        formatError(data);
                    }
                }

                let fileMetadataProgress = (id: string) => {

                    this.http.get("/proxy/v1/spark/shell/file-metadata/" + id).subscribe((data: any) => {
                        checkProgress(data);
                    }, error1 => {
                        formatError(error1)
                    })
                }


                this.http.post("/proxy/v1/spark/shell/file-metadata", request)
                    .subscribe((data: any) => {

                        checkProgress(data);
                    });


            });
        }
        return observable;

    }
}
