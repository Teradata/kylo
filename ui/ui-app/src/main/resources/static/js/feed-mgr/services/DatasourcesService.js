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

define(["angular", "feed-mgr/module-name"], function (angular, moduleName) {
    angular.module(moduleName).factory("DatasourcesService", ["$http", "$q", "RestUrlService","EntityAccessControlService", function ($http, $q, RestUrlService,EntityAccessControlService) {

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

        /**
         * Interacts with the Data Sources REST API.
         * @constructor
         */
        function DatasourcesService() {
        }

        angular.extend(DatasourcesService.prototype, {
            /**
             * Deletes the data source with the specified id.
             * @param {string} id the data source id
             * @returns {Promise} for when the data source is deleted
             */
            deleteById: function (id) {
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
            filterArrayByIds: function (ids, array) {
                var idList = angular.isArray(ids) ? ids : [ids];
                return array.filter(function (datasource) {
                    return (idList.indexOf(datasource.id) > -1);
                });
            },

            /**
             * Finds all user data sources.
             * @returns {Promise} with the list of data sources
             */
            findAll: function () {
                return $http.get(RestUrlService.GET_DATASOURCES_URL, {params: {type: USER_TYPE}})
                    .then(function (response) {
                        return response.data;
                    });
            },

            /**
             * Finds the data source with the specified id.
             * @param {string} id the data source id
             * @returns {Promise} with the data source
             */
            findById: function (id) {
                return $http.get(RestUrlService.GET_DATASOURCES_URL + "/" + id)
                    .then(function (response) {
                        return response.data;
                    });
            },

            findControllerServiceReferences:function(controllerServiceId){
                return $http.get(RestUrlService.GET_NIFI_CONTROLLER_SERVICE_REFERENCES_URL(controllerServiceId))
                    .then(function (response) {
                        return response.data;
                    });
            },

            /**
             * Gets the schema for the specified table.
             * @param {string} id the data source id
             * @param {string} table the table name
             * @param {string} [opt_schema] the schema name
             */
            getTableSchema: function (id, table, opt_schema) {
                var options = {params: {}};
                if (angular.isString(opt_schema)) {
                    options.params.schema = opt_schema;
                }

                return $http.get(RestUrlService.GET_DATASOURCES_URL + "/" + id + "/tables/" + table, options)
                    .then(function (response) {
                        return response.data;
                    });
            },

            /**
             * Lists the tables for the specified data source.
             * @param {string} id the data source id
             * @param {string} [opt_query] the table name query
             */
            listTables: function (id, opt_query) {
                var options = {params: {}};
                if (angular.isString(opt_query) && opt_query.length > 0) {
                    options.params.tableName = "%" + opt_query + "%";
                }

                return $http.get(RestUrlService.GET_DATASOURCES_URL + "/" + id + "/tables", options)
                    .then(function (response) {
                        // Get the list of tables
                        var tables = [];
                        if (angular.isArray(response.data)) {
                            tables = response.data.map(function (table) {
                                var schema = table.substr(0, table.indexOf("."));
                                var tableName = table.substr(table.indexOf(".") + 1);
                                return {schema: schema, tableName: tableName, fullName: table, fullNameLower: table.toLowerCase()};
                            });
                        }

                        // Search for tables matching the query
                        if (angular.isString(opt_query) && opt_query.length > 0) {
                            var lowercaseQuery = opt_query.toLowerCase();
                            return tables.filter(function (table) {
                                return table.fullNameLower.indexOf(lowercaseQuery) !== -1;
                            });
                        } else {
                            return tables;
                        }
                    }).catch(function(e){
                            throw e;
                       });
            },

            /**
             * Creates a new JDBC data source.
             * @returns {JdbcDatasource} the JDBC data source
             */
            newJdbcDatasource: function () {
                return {
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
            },

            saveRoles:function(datasource){

               return EntityAccessControlService.saveRoleMemberships('datasource',datasource.id,datasource.roleMemberships);

            },

            /**
             * Saves the specified data source.
             * @param {JdbcDatasource} datasource the data source to be saved
             * @returns {Promise} with the updated data source
             */
            save: function (datasource) {
                return $http.post(RestUrlService.GET_DATASOURCES_URL, datasource)
                    .then(function (response) {
                        return response.data;
                    });
            }
        });

        return new DatasourcesService();
    }]);
});
