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
const moduleName = require('feed-mgr/module-name');

/**
 * Interacts with the Data Sources REST API.
 * @constructor
 */

export class DatasourcesService {
    
}

// export class DatasourcesService {
    function DatasourcesServiceClass ($http:any, $q:any, RestUrlService:any, EntityAccessControlService:any) {

        /**
         * Type name for JDBC data sources.
         * @type {string}
         */
        var JDBC_TYPE = "JdbcDatasource";

        /**
         * Type name for user data sources.
         * @type {string}
         */
        var USER_TYPE = "UserDatasource";

        var ICON = "grid_on";
        var ICON_COLOR = "orange";
        var HIVE_DATASOURCE = {id: 'HIVE', name: "Hive", isHive: true, icon: ICON, iconColor: ICON_COLOR};

        function ensureDefaultIcon(datasource:any) {
            if (datasource.icon === undefined) {
                datasource.icon = ICON;
                datasource.iconColor = ICON_COLOR;
            }
        }



        angular.extend(DatasourcesService.prototype, {

            getHiveDatasource: function() {
                return HIVE_DATASOURCE;
            },

            /**
             * Default icon name and color is used for data sources which  were created prior to
             * data sources supporting icons
             * @returns {string} default icon name
             */
            defaultIconName: function() {
                return ICON;
            },

            /**
             * Default icon name and color is used for data sources which  were created prior to
             * data sources supporting icons
             * @returns {string} default icon color
             */
            defaultIconColor: function() {
                return ICON_COLOR;
            },

            /**
             * Deletes the data source with the specified id.
             * @param {string} id the data source id
             * @returns {Promise} for when the data source is deleted
             */
            deleteById: function (id:any) {
                return $http({
                    method: "DELETE",
                    url: RestUrlService.GET_DATASOURCES_URL + "/" + encodeURIComponent(id)
                });
            },

            /**
             * Filters the specified array of data sources by matching ids.
             *
             * @param {string|Array.<string>} ids the list of ids
             * @param {Array.<JdbcDatasource>} array the data sources to filter
             * @return {Array.<JdbcDatasource>} the array of matching data sources
             */
            filterArrayByIds: function (ids:any, array:any) {
                var idList = angular.isArray(ids) ? ids : [ids];
                return array.filter(function (datasource:any) {
                    return (idList.indexOf(datasource.id) > -1);
                });
            },

            /**
             * Finds all user data sources.
             * @returns {Promise} with the list of data sources
             */
            findAll: function () {
                return $http.get(RestUrlService.GET_DATASOURCES_URL, {params: {type: USER_TYPE}})
                    .then(function (response:any) {
                        _.each(response.data, ensureDefaultIcon);
                        return response.data;
                    });
            },

            /**
             * Finds the data source with the specified id.
             * @param {string} id the data source id
             * @returns {Promise} with the data source
             */
            findById: function (id:any) {
                if (HIVE_DATASOURCE.id === id) {
                    return Promise.resolve(HIVE_DATASOURCE);
                }
                return $http.get(RestUrlService.GET_DATASOURCES_URL + "/" + id)
                    .then(function (response:any) {
                        ensureDefaultIcon(response.data);
                        return response.data;
                    });
            },

            findControllerServiceReferences:function(controllerServiceId:any){
                return $http.get(RestUrlService.GET_NIFI_CONTROLLER_SERVICE_REFERENCES_URL(controllerServiceId))
                    .then(function (response:any) {
                        return response.data;
                    });
            },

            /**
             * Gets the schema for the specified table.
             * @param {string} id the data source id
             * @param {string} table the table name
             * @param {string} [opt_schema] the schema name
             */
            getTableSchema: function (id:any, table:any, opt_schema:any) {
                var options:any = {params: {}};
                if (angular.isString(opt_schema)) {
                    options.params.schema = opt_schema;
                }

                return $http.get(RestUrlService.GET_DATASOURCES_URL + "/" + id + "/tables/" + table, options)
                    .then(function (response:any) {
                        return response.data;
                    });
            },

            /**
             * Lists the tables for the specified data source.
             * @param {string} id the data source id
             * @param {string} [opt_query] the table name query
             */
            listTables: function (id:any, opt_query:any) {
                var options:any = {params: {}};
                if (angular.isString(opt_query) && opt_query.length > 0) {
                    options.params.tableName = "%" + opt_query + "%";
                }

                return $http.get(RestUrlService.GET_DATASOURCES_URL + "/" + id + "/tables", options)
                    .then(function (response:any) {
                        // Get the list of tables
                        var tables = [];
                        if (angular.isArray(response.data)) {
                            tables = response.data.map(function (table:any) {
                                var schema = table.substr(0, table.indexOf("."));
                                var tableName = table.substr(table.indexOf(".") + 1);
                                return {schema: schema, tableName: tableName, fullName: table, fullNameLower: table.toLowerCase()};
                            });
                        }

                        // Search for tables matching the query
                        if (angular.isString(opt_query) && opt_query.length > 0) {
                            var lowercaseQuery = opt_query.toLowerCase();
                            return tables.filter(function (table:any) {
                                return table.fullNameLower.indexOf(lowercaseQuery) !== -1;
                            });
                        } else {
                            return tables;
                        }
                    }).catch(function(e:any){
                            throw e;
                       });
            },

            query: function(datasourceId:any, sql:any) {
                return $http.get(RestUrlService.GET_DATASOURCES_URL + "/" + datasourceId + "/query?query=" + sql)
                    .then(function (response:any) {
                        return response;
                    }).catch(function(e:any){
                        throw e;
                    });
            },

            preview: function(datasourceId:any, schema:string, table:string, limit:number) {
                return $http.post(RestUrlService.PREVIEW_DATASOURCE_URL(datasourceId, schema, table, limit))
                    .then(function (response:any) {
                        return response;
                    }).catch(function(e:any){
                        throw e;
                    });
            },

            getPreviewSql: function(datasourceId:any, schema:string, table:string, limit:number) {
                return $http.get(RestUrlService.PREVIEW_DATASOURCE_URL(datasourceId, schema, table, limit))
                    .then(function (response:any) {
                        return response.data;
                    }).catch(function(e:any){
                        throw e;
                    });
            },

            getTablesAndColumns: function(datasourceId:any, schema:any) {
                var params = {schema: schema};
                return $http.get(RestUrlService.GET_DATASOURCES_URL + "/" + datasourceId + "/table-columns", {params: params});
            },

            /**
             * Creates a new JDBC data source.
             * @returns {JdbcDatasource} the JDBC data source
             */
            newJdbcDatasource: function () {
                let d:any = {
                    "@type": JDBC_TYPE,
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
            },

            saveRoles:function(datasource:any){

               return EntityAccessControlService.saveRoleMemberships('datasource',datasource.id,datasource.roleMemberships);

            },

            /**
             * Saves the specified data source.
             * @param {JdbcDatasource} datasource the data source to be saved
             * @returns {Promise} with the updated data source
             */
            save: function (datasource:any) {
                return $http.post(RestUrlService.GET_DATASOURCES_URL, datasource)
                    .then(function (response:any) {
                        return response.data;
                    });
            },

            testConnection: function(datasource: any) {
                return $http.post(RestUrlService.GET_DATASOURCES_URL + "/test", datasource)
                    .then(function (response:any) {
                        return response.data;
                    });
            }
        });

        

        return new DatasourcesService();
    }
// }

angular.module(moduleName).factory("DatasourcesService", ["$http", "$q", "RestUrlService","EntityAccessControlService", DatasourcesServiceClass]);