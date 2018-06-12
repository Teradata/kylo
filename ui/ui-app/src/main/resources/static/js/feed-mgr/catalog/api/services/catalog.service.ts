import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";

import {Connector} from '../models/connector';
import {DataSource} from "../models/datasource";
import {SearchResult} from "../models/search-result";

@Injectable()
export class CatalogService {

    constructor(private http: HttpClient) {
    }

    /**
     * Gets the list of available connectors, e.g. s3, hdfs, hive, jdbc, kafka etc.
     */
    getConnectors(): Observable<Connector[]> {
        return this.http.get<Connector[]>("/proxy/v1/catalog/connector");
    }

    /**
     * Gets connector by id
     */
    getConnector(connectorId: string): Observable<Connector> {
        return this.http.get<Connector>("/proxy/v1/catalog/connector", {params: {"connectorId": connectorId}});
    }

    /**
     * Gets the list of data sources, i.e. instances of configured connectors, e.g. specific s3/hdfs location, kafka on certain port
     */
    getDataSources(): Observable<DataSource[]> {
        return this.http.get<SearchResult<DataSource>>("/proxy/v1/catalog/datasource")
            .map(data => data.data);
    }

    getDataSource(datasourceId: string): Observable<DataSource> {
        return this.http.get<DataSource>("/proxy/v1/catalog/datasource/" + datasourceId);
    }
}
