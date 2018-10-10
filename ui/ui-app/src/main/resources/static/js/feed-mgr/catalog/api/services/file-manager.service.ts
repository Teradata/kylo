import {HttpClient, HttpEvent, HttpRequest} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";
import {map} from "rxjs/operators/map";
import {SparkDataSet} from "../../../model/spark-data-set.model";

import {DataSetFile} from "../models/dataset-file";

@Injectable()
export class FileManagerService {

    constructor(private http: HttpClient) {
    }

    deleteFile(dataSetId: string, name: string): Observable<any> {
        return this.http.delete(FileManagerService.getUploadsUrl(dataSetId, name));
    }

    listFiles(dataSetId: string): Observable<DataSetFile[]> {
        return this.http.get<DataSetFile[]>(FileManagerService.getUploadsUrl(dataSetId));
    }

    uploadFile(dataSetId: string, file: File): Observable<HttpEvent<DataSetFile>> {
        const formData = new FormData();
        formData.append("file", file, file.name);

        const request = new HttpRequest("POST", FileManagerService.getUploadsUrl(dataSetId), formData, {reportProgress: true});
        return this.http.request<DataSetFile>(request);
    }

    private static getUploadsUrl(id: string, fileName?: string) {
        const url = `/proxy/v1/catalog/dataset/${encodeURIComponent(id)}/uploads`;
        return fileName ? `${url}/${encodeURIComponent(fileName)}` : url;
    }

    createDataSet(dataSourceId: string, title: string): Observable<SparkDataSet> {
        return this.http.post(`/proxy/v1/catalog/datasource/${encodeURIComponent(dataSourceId)}/dataset`, {title: title}).pipe(
            map(dataSet => new SparkDataSet(dataSet))
        );
    }
}
