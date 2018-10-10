/**
 * Defines a connection to a JDBC data source.
 *
 * @typedef {Object} JdbcDatasource
 * @property {string} [id] unique identifier for this data source
 * @property {string} name the name of this data source
 * @property {string} description a description of this data source
 * @property {Array} sourceForFeeds list of feeds using this data source
 * @property {string} type type name of this data source
 * @property {string} databaseConnectionUrl a database URL of the form jdbc:subprotocol:subname
 * @property {string} databaseDriverClassName database driver class name
 * @property {string} databaseDriverLocation comma-separated list of files/folders and/or URLs containing the driver JAR and its dependencies (if any)
 * @property {string} databaseUser database user name
 * @property {string} password password to use when connecting to this data source
 */
import * as angular from 'angular';
import * as _ from "underscore";
import {EntityAccessControlService} from '../shared/entity-access-control/EntityAccessControlService';
import "../module";
import {RestUrlConstants} from "./RestUrlConstants";
import {Observable} from "rxjs/Observable";
import 'rxjs/add/observable/fromPromise';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/observable/of';

/**
 * Interacts with the Data Sources REST API.
 * @constructor
 */
 export  class DatasourcesService {
       /**
         * Type name for JDBC data sources.
         * @type {string}
         */
        JDBC_TYPE = "JdbcDatasource";

        /**
         * Type name for user data sources.
         * @type {string}
         */
        USER_TYPE = "UserDatasource";

        ICON = "grid_on";
        ICON_COLOR = "orange";
        HIVE_DATASOURCE:any = {id: 'HIVE', name: "Hive", isHive: true, icon: this.ICON, iconColor: this.ICON_COLOR};

        getHiveDatasource() {
                return this.HIVE_DATASOURCE;
            }
        ensureDefaultIcon(datasource:any) {
            if (datasource.icon === undefined) {
                datasource.icon = this.ICON;
                datasource.iconColor = this.ICON_COLOR;
            }
        }

        static readonly $inject = ["$http", "$q", "EntityAccessControlService"];
        constructor(private $http:angular.IHttpService, 
                    private $q:angular.IQService,
                    private entityAccessControlService:EntityAccessControlService) {
      //  angular.extend(DatasourcesService.prototype, {
            
        }//);
        //return new DatasourcesService();
        

        /**
             * Default icon name and color is used for data sources which  were created prior to
             * data sources supporting icons
             * @returns {string} default icon name
             */
            defaultIconName(){
                return this.ICON;
            }

            /**
             * Default icon name and color is used for data sources which  were created prior to
             * data sources supporting icons
             * @returns {string} default icon color
             */
            defaultIconColor(){
                return this.ICON_COLOR;
            }

            /**
             * Deletes the data source with the specified id.
             * @param {string} id the data source id
             * @returns {Promise} for when the data source is deleted
             */
            deleteById(id:any) {
                return this.$http({
                    method: "DELETE",
                    url: RestUrlConstants.GET_DATASOURCES_URL + "/" + encodeURIComponent(id)
                });
            }

            /**
             * Filters the specified array of data sources by matching ids.
             *
             * @param {string|Array.<string>} ids the list of ids
             * @param {Array.<JdbcDatasource>} array the data sources to filter
             * @return {Array.<JdbcDatasource>} the array of matching data sources
             */
            filterArrayByIds (ids:any, array:any) {
                var idList = angular.isArray(ids) ? ids : [ids];
                return array.filter((datasource:any) => {
                    return (idList.indexOf(datasource.id) > -1);
                });
            }

            /**
             * Finds all user data sources.
             * @returns {Promise} with the list of data sources
             */
            findAll() {
                return this.$http.get(RestUrlConstants.GET_DATASOURCES_URL, {params: {type: this.USER_TYPE}})
                    .then((response:any) => {
                        _.each(response.data, this.ensureDefaultIcon.bind(this));
                        return response.data;
                    });
            }

            /**
             * Finds the data source with the specified id.
             * @param {string} id the data source id
             * @returns {Promise} with the data source
             */
            findById(id:any) : any {
                if (this.HIVE_DATASOURCE.id === id) {
                    return Observable.of(this.HIVE_DATASOURCE).toPromise();
                }
                return this.$http.get(RestUrlConstants.GET_DATASOURCES_URL + "/" + id)
                    .then((response:any) => {
                        this.ensureDefaultIcon(response.data);
                        return response.data;
                    });
            }

            findControllerServiceReferences (controllerServiceId:any) {
                return this.$http.get(RestUrlConstants.GET_NIFI_CONTROLLER_SERVICE_REFERENCES_URL(controllerServiceId))
                    .then((response:any) => {
                        return response.data;
                    });
            }

            /**
             * Gets the schema for the specified table.
             * @param {string} id the data source id
             * @param {string} table the table name
             * @param {string} [opt_schema] the schema name
             */
            getTableSchema (id:any, table:any, opt_schema:any) {
                var options:any = {params: {}};
                if (angular.isString(opt_schema)) {
                    options.params.schema = opt_schema;
                }

                return this.$http.get(RestUrlConstants.GET_DATASOURCES_URL + "/" + id + "/tables/" + table, options)
                    .then((response:any) => {
                        return response.data;
                    });
            }

            /**
             * Lists the tables for the specified data source.
             * @param {string} id the data source id
             * @param {string} [opt_query] the table name query
             */
            listTables (id:any, opt_query:any) {
                var options:any = {params: {}};
                if (angular.isString(opt_query) && opt_query.length > 0) {
                    options.params.tableName = "%" + opt_query + "%";
                }

                return this.$http.get(RestUrlConstants.GET_DATASOURCES_URL + "/" + id + "/tables", options)
                    .then((response:any) => {
                        // Get the list of tables
                        var tables = [];
                        if (angular.isArray(response.data)) {
                            tables = response.data.map((table:any) => {
                                var schema = table.substr(0, table.indexOf("."));
                                var tableName = table.substr(table.indexOf(".") + 1);
                                return {schema: schema, tableName: tableName, fullName: table, fullNameLower: table.toLowerCase()};
                            });
                        }

                        // Search for tables matching the query
                        if (angular.isString(opt_query) && opt_query.length > 0) {
                            var lowercaseQuery = opt_query.toLowerCase();
                            return tables.filter((table:any) => {
                                return table.fullNameLower.indexOf(lowercaseQuery) !== -1;
                            });
                        } else {
                            return tables;
                        }
                    }).catch((e:any) => {
                            throw e;
                       });
            }

            query(datasourceId:any, sql:any) {
                return this.$http.get(RestUrlConstants.GET_DATASOURCES_URL + "/" + datasourceId + "/query?query=" + sql)
                    .then((response:any) => {
                        return response;
                    }).catch((e:any) => {
                        throw e;
                    });
            }

            preview(datasourceId:any, schema:string, table:string, limit:number) {
                return this.$http.post(RestUrlConstants.PREVIEW_DATASOURCE_URL(datasourceId, schema, table, limit),null)
                    .then((response:any) => {
                        return response;
                    }).catch((e:any) => {
                        throw e;
                    });
            }

           getPreviewSql(datasourceId:any, schema:string, table:string, limit:number) {
                return this.$http.get(RestUrlConstants.PREVIEW_DATASOURCE_URL(datasourceId, schema, table, limit))
                    .then((response:any) => {
                        return response.data;
                    }).catch((e:any) => {
                        throw e;
                    });
            }

            getTablesAndColumns(datasourceId:any, schema:any){
                var params = {schema: schema};
                return this.$http.get(RestUrlConstants.GET_DATASOURCES_URL + "/" + datasourceId + "/table-columns", {params: params});
            }

            /**
             * Creates a new JDBC data source.
             * @returns {JdbcDatasource} the JDBC data source
             */
            newJdbcDatasource(){
                let d:any = {
                    "@type": this.JDBC_TYPE,
                    name: "",
                    description: "",
                    owner: null,
                    roleMemberships: [],
                    sourceForFeeds: [],
                    type: "",
                    databaseConnectionUrl: "",
                    databaseDriverClassName: "",
                    databaseDriverLocation: "",
                    databaseUser: "",
                    password: ""
                };
                return d;
            }

            saveRoles(datasource:any){

               return this.entityAccessControlService.saveRoleMemberships('datasource',datasource.id,datasource.roleMemberships);

            }

            /**
             * Saves the specified data source.
             * @param {JdbcDatasource} datasource the data source to be saved
             * @returns {Promise} with the updated data source
             */
            save(datasource:any){
                return this.$http.post(RestUrlConstants.GET_DATASOURCES_URL, datasource)
                    .then((response:any) => {
                        return response.data;
                    });
            }

           testConnection(datasource: any){
                return this.$http.post(RestUrlConstants.GET_DATASOURCES_URL + "/test", datasource)
                    .then((response:any) => {
                        return response.data;
                    });
            }
    //}
}