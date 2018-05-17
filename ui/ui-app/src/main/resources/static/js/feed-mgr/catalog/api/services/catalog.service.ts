import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";
import {ArrayObservable} from "rxjs/observable/ArrayObservable";

import {DataSource} from "../models/datasource";
import {DataSet} from "../models/dataset";
import {Connector} from '../models/connector';
import {dataSources} from "./data";

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

    createDataSet(dataSource: DataSource): Observable<DataSet> {
        const dataSet: DataSet = {
            id: uuidv4(),
            datasource: dataSource,
            connectorId: dataSource.connector.id ? dataSource.connector.id : dataSource.title
        };
        this.dataSets[dataSource.id] = dataSet;
        return ArrayObservable.of(dataSet);
    }

    /**
     * Gets the list of available connectors, e.g. s3, hdfs, hive, jdbc, kafka etc.
     */
    getConnectors(): Observable<Connector[]> {
        console.log('getConnectors');
        return this.http.get<Connector[]>("/proxy/v1/catalog/connector");
    }

    /**
     * Gets the list of data sources, i.e. instances of configured connectors, e.g. specific s3/hdfs location, kafka on certain port
     */
    getDataSources(): Observable<DataSource[]> {
        return ArrayObservable.of(dataSources);
    }

    getDataSet(datasourceId: string): Observable<DataSet> {
        let dataSet = this.dataSets[datasourceId];
        if (dataSet) {
            return ArrayObservable.of(dataSet);
        } else {
            const dataSource = dataSources.find(datasource => datasource.id === datasourceId);
            return this.createDataSet(dataSource);
        }
    }
}
