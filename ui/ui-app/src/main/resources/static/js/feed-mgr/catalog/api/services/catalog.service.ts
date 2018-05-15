import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";
import {ArrayObservable} from "rxjs/observable/ArrayObservable";
import {ErrorObservable} from "rxjs/observable/ErrorObservable";

import {Connector} from "../models/connector";
import {DataSet} from "../models/dataset";
import {ConnectorType} from '../models/connectorType';
import {connectors} from "./data";

// TODO testing only
function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

@Injectable()
export class CatalogService {

    private dataSets: {[k: string]: DataSet} = {};

    constructor(private http: HttpClient) {
    }

    createDataSet(connector: Connector): Observable<DataSet> {
        const dataSet: DataSet = {
            id: uuidv4(),
            connector: connector,
            connectorId: connector.id ? connector.id : connector.title
        };
        this.dataSets[dataSet.id] = dataSet;
        return ArrayObservable.of(dataSet);
    }

    /**
     * Gets the list of available connector types, e.g. s3, hdfs, hive, jdbc, kafka etc.
     */
    getConnectorTypes(): Observable<ConnectorType[]> {
        console.log('getConnectorTypes');
        return this.http.get<Connector[]>("/proxy/v1/catalog/connector");
    }

    /**
     * Gets the list of connectors (data sources), i.e. instances of configured connector type's, e.g. specific s3/hdfs location, kafka on certain port
     */
    getConnectors(): Observable<Connector[]> {
        return ArrayObservable.of(connectors);
    }

    getDataSet(dataSetId: string): Observable<DataSet> {
        if (this.dataSets[dataSetId]) {
            return ArrayObservable.of(this.dataSets[dataSetId]);
        } else {
            return ErrorObservable.create("Not found");
        }
    }
}
