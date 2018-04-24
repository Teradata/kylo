define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    /**
     * Interacts with the Data Sources REST API.
     * @constructor
     */
    //export class DatasourcesService {
    // DatasourcesServiceClass();
    //}
    // export class DatasourcesService {
    var DatasourcesService = /** @class */ (function () {
        function DatasourcesService($http, $q, RestUrlService, EntityAccessControlService) {
            //  angular.extend(DatasourcesService.prototype, {
            /**
              * Type name for JDBC data sources.
              * @type {string}
              */
            this.JDBC_TYPE = "JdbcDatasource";
            /**
             * Type name for user data sources.
             * @type {string}
             */
            this.USER_TYPE = "UserDatasource";
            this.ICON = "grid_on";
            this.ICON_COLOR = "orange";
            this.HIVE_DATASOURCE = { id: 'HIVE', name: "Hive", isHive: true, icon: this.ICON, iconColor: this.ICON_COLOR };
            //return new DatasourcesService();
            /**
                 * Default icon name and color is used for data sources which  were created prior to
                 * data sources supporting icons
                 * @returns {string} default icon name
                 */
            this.defaultIconName = function () {
                return this.ICON;
            };
            /**
             * Default icon name and color is used for data sources which  were created prior to
             * data sources supporting icons
             * @returns {string} default icon color
             */
            this.defaultIconColor = function () {
                return this.ICON_COLOR;
            };
            /**
             * Deletes the data source with the specified id.
             * @param {string} id the data source id
             * @returns {Promise} for when the data source is deleted
             */
            this.deleteById = function (id) {
                return this.$http({
                    method: "DELETE",
                    url: this.RestUrlService.GET_DATASOURCES_URL + "/" + encodeURIComponent(id)
                });
            };
            /**
             * Filters the specified array of data sources by matching ids.
             *
             * @param {string|Array.<string>} ids the list of ids
             * @param {Array.<JdbcDatasource>} array the data sources to filter
             * @return {Array.<JdbcDatasource>} the array of matching data sources
             */
            this.filterArrayByIds = function (ids, array) {
                var idList = angular.isArray(ids) ? ids : [ids];
                return array.filter(function (datasource) {
                    return (idList.indexOf(datasource.id) > -1);
                });
            };
            /**
             * Finds all user data sources.
             * @returns {Promise} with the list of data sources
             */
            this.findAll = function () {
                return this.$http.get(this.RestUrlService.GET_DATASOURCES_URL, { params: { type: this.USER_TYPE } })
                    .then(function (response) {
                    _.each(response.data, this.ensureDefaultIcon);
                    return response.data;
                });
            };
            /**
             * Finds the data source with the specified id.
             * @param {string} id the data source id
             * @returns {Promise} with the data source
             */
            this.findById = function (id) {
                if (this.HIVE_DATASOURCE.id === id) {
                    return Promise.resolve(this.HIVE_DATASOURCE);
                }
                return this.$http.get(this.RestUrlService.GET_DATASOURCES_URL + "/" + id)
                    .then(function (response) {
                    this.ensureDefaultIcon(response.data);
                    return response.data;
                });
            };
            this.findControllerServiceReferences = function (controllerServiceId) {
                return this.$http.get(this.RestUrlService.GET_NIFI_CONTROLLER_SERVICE_REFERENCES_URL(controllerServiceId))
                    .then(function (response) {
                    return response.data;
                });
            };
            /**
             * Gets the schema for the specified table.
             * @param {string} id the data source id
             * @param {string} table the table name
             * @param {string} [opt_schema] the schema name
             */
            this.getTableSchema = function (id, table, opt_schema) {
                var options = { params: {} };
                if (angular.isString(opt_schema)) {
                    options.params.schema = opt_schema;
                }
                return this.$http.get(this.RestUrlService.GET_DATASOURCES_URL + "/" + id + "/tables/" + table, options)
                    .then(function (response) {
                    return response.data;
                });
            };
            /**
             * Lists the tables for the specified data source.
             * @param {string} id the data source id
             * @param {string} [opt_query] the table name query
             */
            this.listTables = function (id, opt_query) {
                var options = { params: {} };
                if (angular.isString(opt_query) && opt_query.length > 0) {
                    options.params.tableName = "%" + opt_query + "%";
                }
                return this.$http.get(this.RestUrlService.GET_DATASOURCES_URL + "/" + id + "/tables", options)
                    .then(function (response) {
                    // Get the list of tables
                    var tables = [];
                    if (angular.isArray(response.data)) {
                        tables = response.data.map(function (table) {
                            var schema = table.substr(0, table.indexOf("."));
                            var tableName = table.substr(table.indexOf(".") + 1);
                            return { schema: schema, tableName: tableName, fullName: table, fullNameLower: table.toLowerCase() };
                        });
                    }
                    // Search for tables matching the query
                    if (angular.isString(opt_query) && opt_query.length > 0) {
                        var lowercaseQuery = opt_query.toLowerCase();
                        return tables.filter(function (table) {
                            return table.fullNameLower.indexOf(lowercaseQuery) !== -1;
                        });
                    }
                    else {
                        return tables;
                    }
                }).catch(function (e) {
                    throw e;
                });
            };
            this.query = function (datasourceId, sql) {
                return this.$http.get(this.RestUrlService.GET_DATASOURCES_URL + "/" + datasourceId + "/query?query=" + sql)
                    .then(function (response) {
                    return response;
                }).catch(function (e) {
                    throw e;
                });
            };
            this.preview = function (datasourceId, schema, table, limit) {
                return this.$http.post(this.RestUrlService.PREVIEW_DATASOURCE_URL(datasourceId, schema, table, limit))
                    .then(function (response) {
                    return response;
                }).catch(function (e) {
                    throw e;
                });
            };
            this.getPreviewSql = function (datasourceId, schema, table, limit) {
                return this.$http.get(this.RestUrlService.PREVIEW_DATASOURCE_URL(datasourceId, schema, table, limit))
                    .then(function (response) {
                    return response.data;
                }).catch(function (e) {
                    throw e;
                });
            };
            this.getTablesAndColumns = function (datasourceId, schema) {
                var params = { schema: schema };
                return this.$http.get(this.RestUrlService.GET_DATASOURCES_URL + "/" + datasourceId + "/table-columns", { params: params });
            };
            /**
             * Creates a new JDBC data source.
             * @returns {JdbcDatasource} the JDBC data source
             */
            this.newJdbcDatasource = function () {
                var d = {
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
            };
            this.saveRoles = function (datasource) {
                return this.EntityAccessControlService.saveRoleMemberships('datasource', datasource.id, datasource.roleMemberships);
            };
            /**
             * Saves the specified data source.
             * @param {JdbcDatasource} datasource the data source to be saved
             * @returns {Promise} with the updated data source
             */
            this.save = function (datasource) {
                return this.$http.post(this.RestUrlService.GET_DATASOURCES_URL, datasource)
                    .then(function (response) {
                    return response.data;
                });
            };
            this.testConnection = function (datasource) {
                return this.$http.post(this.RestUrlService.GET_DATASOURCES_URL + "/test", datasource)
                    .then(function (response) {
                    return response.data;
                });
            };
        } //);
        DatasourcesService.prototype.getHiveDatasource = function () {
            return this.HIVE_DATASOURCE;
        };
        DatasourcesService.prototype.ensureDefaultIcon = function (datasource) {
            if (datasource.icon === undefined) {
                datasource.icon = this.ICON;
                datasource.iconColor = this.ICON_COLOR;
            }
        };
        return DatasourcesService;
    }());
    exports.DatasourcesService = DatasourcesService;
    angular.module(moduleName).service("DatasourcesService", ["$http", "$q", "RestUrlService", "EntityAccessControlService", DatasourcesService]);
});
//# sourceMappingURL=DatasourcesService.js.map