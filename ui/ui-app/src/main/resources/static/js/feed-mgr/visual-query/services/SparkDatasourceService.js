define(["angular", "feed-mgr/visual-query/module-name"], function (angular, moduleName) {
    angular.module(moduleName).factory("SparkDatasourceService", ["SparkParserService", function (SparkParserService) {

        /** Name of the DatasourceProvider variable */
        var DATASOURCE_PROVIDER = "datasourceProvider";

        /**
         * Handles transformations from a visual query model to Spark.
         *
         * @type {Object}
         */
        var SparkDatasourceService = {
            /**
             * Generates a Spark script for the specified SQL query and optional data source.
             *
             * @private
             * @param {string} sql the SQL query
             * @param {Array.<Datasource>} datasources the data source
             * @returns {string} the Spark script
             * @throws {Error} if there are too many data sources
             */
            fromSql: function (sql, datasources) {
                if (datasources != null && datasources.length !== 1) {
                    throw new Error("Not valid datasources: " + datasources);
                } else if (datasources == null) {
                    return "var " + SparkParserService.DATA_FRAME_VARIABLE + " = sqlContext.sql(\"" + StringUtils.escapeScala(sql) + "\")\n";
                } else {
                    var subquery = "(" + sql + ") AS KYLO_SPARK_QUERY";
                    return "var " + SparkParserService.DATA_FRAME_VARIABLE + " = " + DATASOURCE_PROVIDER + ".getTableFromDatasource(\"" + StringUtils.escapeScala(subquery) + "\", \""
                           + datasources[0].id + "\", sqlContext)\n";
                }
            },

            /**
             * Generates a Spark script for the specified visual query model and data sources.
             *
             * @private
             * @param {VisualQueryModel} visualQueryModel the visual query model
             * @param {Array.<Datasource>} datasources the list of datasources used
             */
            fromVisualQueryModel: function (visualQueryModel) {
                throw new Error("Method not implemented");
            },

            /**
             * Generates a Spark script for the specified visual query model and data sources.
             *
             * @param {string|VisualQueryModel} source the SQL query or visual query model
             * @param {Array.<Datasource>} datasources the list of datasources used
             * @returns {string} the Spark script
             * @throws {Error} if there are too many data sources for the source
             */
            toScript: function (source, datasources) {
                if (angular.isString(source)) {
                    return SparkDatasourceService.fromSql(source, datasources);
                } else if (angular.isObject(source)) {
                    return SparkDatasourceService.fromVisualQueryModel(source);
                }
            }
        };

        return {
            toScript: SparkDatasourceService.toScript
        }
    }]);
});
