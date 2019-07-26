import {HttpBackend, HttpClient, HttpParams} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";
import {CloneUtil} from "../../../../common/utils/clone-util";

import {Connector} from '../models/connector';
import {ConnectorPlugin} from '../models/connector-plugin';
import {DataSource} from "../models/datasource";
import {SearchResult} from "../models/search-result";
import {SparkDataSet} from "../../../model/spark-data-set.model";
import {RestUrlConstants} from "../../../services/RestUrlConstants";
import {TableSchema} from "../../../visual-query/wrangler";
import {DatasetTable} from "../models/dataset-table";
import {map} from "rxjs/operators";
import {HttpBackendClient} from "../../../../services/http-backend-client";

@Injectable()
export class CatalogService {

    constructor(private http: HttpClient, private http2:HttpBackendClient) {
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

    /**
     * Gets the datasources matching the supplied pluginIds
     * @param {string[]} pluginIds
     * @return {Observable<DataSource[]>}
     */
    getDataSourcesForPluginIds(pluginIds:string[]): Observable<DataSource[]> {
        let params = new HttpParams();
        params = params.append('pluginIds', pluginIds.join(','));
        return this.http.get<DataSource[]>("/proxy/v1/catalog/datasource/plugin-id",{params:params});
    }



    getDataSource(datasourceId: string): Observable<DataSource> {
        return this.http.get<DataSource>("/proxy/v1/catalog/datasource/" + datasourceId);
    }

    createDataSource(datasource: DataSource): Observable<DataSource> {
        if (typeof datasource.id === "string") {
            return this.http2.put<DataSource>("/proxy/v1/catalog/datasource/" + encodeURIComponent(datasource.id), datasource);
        } else {
            return this.http2.post<DataSource>("/proxy/v1/catalog/datasource/", datasource);
        }
    }

    deleteDataSource(datasource: DataSource): Observable<any> {
        return this.http.delete<DataSource>("/proxy/v1/catalog/datasource/" + datasource.id);
    }

    testDataSource(datasource: DataSource): Observable<any> {
        return this.http.post<DataSource>("/proxy/v1/catalog/datasource/test", datasource);
    }
    createDataSet(dataSet: SparkDataSet): Observable<SparkDataSet> {
        // Ensure data sets are uploaded with no title. Titles must be unique if set.
        const body = CloneUtil.deepCopy(dataSet);
        body.title = null;
        return this.http.post<SparkDataSet>("/proxy/v1/catalog/dataset/", body);
    }

    createDataSetWithTitle(dataSet: SparkDataSet): Observable<SparkDataSet> {
        const body = CloneUtil.deepCopy(dataSet);
        return this.http.post<SparkDataSet>("/proxy/v1/catalog/dataset/", body);
    }

    /**
     * Ensure the incoming dataset has an ID and is registered with Kylo.
     * If not it will create the relationship, register with Kylo and return the updated dataset
     * @param dataset
     */
    ensureDataSetId(dataset:SparkDataSet) :Observable<SparkDataSet>{
        if(dataset.id == undefined){
            if(dataset.isUpload){
                //create random title and new dataset for uploads
                return this.createDataSet(dataset).pipe(map((ds:SparkDataSet) => {
                    dataset.id = ds.id;
                    return dataset;
                }))
            }else {
                return this.createDataSetWithTitle(dataset).pipe(map((ds: SparkDataSet) => {
                    dataset.id = ds.id;
                    return dataset;
                }));
            }
        }
        else {
            return Observable.of(dataset);
        }

    }


    /**
     * Gets the schema for the specified table.
     *
     * @param schema - name of the database or schema
     * @param table - name of the table
     * @param datasourceId - id of the datasource
     * @returns the table schema
     */
    createJdbcTableDataSet(dataSourceId:string,schema: string, table: string): Observable<DatasetTable> {
        const self = this;
        let params = new HttpParams();
        params = params.append('schema', schema);
        return <Observable<DatasetTable>> this.http.post("/proxy/v1/catalog/datasource/" + dataSourceId + "/tables/"+table+"/dataset",null,{params:params});
    }


    /**
     * Lists the tables for the specified data source.
     * @param {string} id the data source id
     * @param {string} [opt_query] the table name query
     */
    listTables (dataSourceId:string, filter:string) {
        let params = new HttpParams();
        params = params.append('filter', "%" + filter + "%");

        return this.http.get("/proxy/v1/catalog/datasource/" + dataSourceId + "/tables/filter", {params:params})
            .map( (tables:any[]) =>  {
                let list = tables.map(table => {
                    let schema = table.schema;
                    let catalog = table.catalog;
                    let tableName = table.name;
                    let fullName = (schema ? schema : catalog )+"."+tableName;
                    return {catalog:catalog,schema: schema, tableName:tableName, fullName: fullName, fullNameLower: fullName.toLowerCase()};
                });
                if(filter &&  filter != undefined && (typeof filter == 'string' ) && filter != ""){
                    let lowercaseQuery = filter.toLowerCase();
                    return list.filter((table:any) => {
                        return table.fullNameLower.indexOf(lowercaseQuery) !== -1;
                    });
                }
                else {

                    return list;
                }
            }).catch((e:any) => {
                throw e;
            });
    }


}
