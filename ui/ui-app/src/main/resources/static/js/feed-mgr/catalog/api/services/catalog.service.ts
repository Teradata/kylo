import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";

import {Connector} from '../models/connector';
import {ConnectorPlugin} from '../models/connector-plugin';
import {DataSource} from "../models/datasource";
import {SearchResult} from "../models/search-result";
import {SparkDataSet} from "../../../model/spark-data-set.model";

@Injectable()
export class CatalogService {

    constructor(private http: HttpClient) {
    }

    /**
     * Gets the list of available connector plugins.
     */
    getConnectorPlugins(): Observable<ConnectorPlugin[]> {
        return this.http.get<ConnectorPlugin[]>("/proxy/v1/catalog/connector/plugin");
    }

    /**
     * Gets connector plugin by id
     */
    getConnectorPlugin(pluginId: string): Observable<ConnectorPlugin> {
        return this.http.get<ConnectorPlugin>("/proxy/v1/catalog/connector/plugin/" + pluginId);
    }

    /**
     * Gets connector plugin by connector id
     */
    getPluginOfConnector(connectorId: string): Observable<ConnectorPlugin> {
        return this.http.get<ConnectorPlugin>("/proxy/v1/catalog/connector/" + connectorId + "/plugin");
    }

    /**
     * Gets connector plugin associated with the connector of the data source with the specified ID.
     */
    getDataSourceConnectorPlugin(dataSourceId: string): Observable<ConnectorPlugin> {
        return this.http.get<ConnectorPlugin>("/proxy/v1/catalog/datasource/" + dataSourceId + "/plugin");
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
        return this.http.get<Connector>("/proxy/v1/catalog/connector/" + connectorId);
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

    createDataSource(datasource: DataSource): Observable<DataSource> {
        return this.http.post<DataSource>("/proxy/v1/catalog/datasource/", datasource);
    }

    deleteDataSource(datasource: DataSource): Observable<any> {
        return this.http.delete<DataSource>("/proxy/v1/catalog/datasource/" + datasource.id);
    }

    testDataSource(datasource: DataSource): Observable<any> {
        return this.http.post<DataSource>("/proxy/v1/catalog/datasource/test", datasource);
    }
    createDataSet(dataSet: SparkDataSet): Observable<SparkDataSet> {
        return this.http.post<SparkDataSet>("/proxy/v1/catalog/dataset/", dataSet);
    }
}
