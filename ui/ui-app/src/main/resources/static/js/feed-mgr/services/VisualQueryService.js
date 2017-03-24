/**
 * The model for the Visual Query page.
 *
 * @typedef {Object} VisualQueryModel
 * @param {Array.<VisualQueryNode>} nodes the list of tables
 */

/**
 * A table node.
 *
 * @typedef {Object} VisualQueryNode
 * @param {VisualQueryNodeAttributes} nodeAttributes the table information
 */

/**
 * Attributes of a table node.
 *
 * @typedef {Object} VisualQueryNodeAttributes
 * @param {Array.<VisualQueryField>} attributes the list of all columns
 * @param {Array.<VisualQueryField>} selected the list of selected columns
 * @param {string} sql the database and table name escaped for a SQL query
 */

/**
 * Field of a table.
 *
 * @typedef {Object} VisualQueryField
 * @param {string} name field or column name
 * @param {string|null} description business description of the field
 * @param {string} nativeDataType the data type in reference of the source (e.g. an RDBMS)
 * @param {string} derivedDataType the data type in reference of the target (e.g. Hive)
 * @param {boolean} primaryKey true/false indicating if field is primary key
 * @param {boolean} nullable true/false indicating if field can accept null value
 * @param {Array.<string>} sampleValues list of sample values for field
 * @param {boolean} modifiable true/false indicating whether derived properties can be modified or not
 * @param {Object|null} dataTypeDescriptor additional descriptor about the derived data type
 * @param {string} dataTypeWithPrecisionAndScale the data type with precision and scale
 * @param {string|null} precisionScale the precision and scale portion of the data type
 * @param {boolean} createdTracker true/false indicating whether field represents created timestamp
 * @param {boolean} updatedTracker true/false indicating whether field represents update timestamp
 * @param {boolean} selected true if the column is selected, or false otherwise
 */

define(["angular", "feed-mgr/module-name"], function (angular, moduleName) {
    return angular.module(moduleName).factory("VisualQueryService", function () {
        var VisualQueryService = {

            /**
             * Identifier of the Hive datasource.
             * @type {string}
             */
            HIVE_DATASOURCE: "HIVE",

            /**
             * Stored model for the Visual Query page.
             * @type {{selectedDatasourceId: string, visualQueryModel: VisualQueryModel, visualQuerySql: string}}
             */
            model: {},

            /**
             * Resets this model to default values.
             */
            resetModel: function () {
                this.model = {
                    selectedDatasourceId: this.HIVE_DATASOURCE
                };
            }
        };

        VisualQueryService.resetModel();
        return VisualQueryService;
    });
});
