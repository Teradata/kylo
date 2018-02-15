var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
define(["require", "exports", "moment", "rxjs/Observable", "../../wrangler/api/column", "../../wrangler/api/services/dialog.service", "../../wrangler/column-delegate", "rxjs/add/observable/empty", "rxjs/add/observable/of", "rxjs/add/operator/catch", "rxjs/add/operator/map"], function (require, exports, moment, Observable_1, column_1, dialog_service_1, column_delegate_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Data types supported by Spark and Hive.
     */
    var SparkDataType = {
        // Numeric types
        BYTE: new column_1.DataType("byte", "Byte"),
        SHORT: new column_1.DataType("short", "Short"),
        INT: new column_1.DataType("int", "Int", "fa-hashtag"),
        BIGINT: new column_1.DataType("bigint", "Bigint", "fa-hashtag"),
        FLOAT: new column_1.DataType("float", "Float"),
        DOUBLE: new column_1.DataType("double", "Double", "fa-hashtag"),
        DECIMAL: new column_1.DataType("decimal", "Decimal"),
        // Date/time types
        DATE: new column_1.DataType("date", "Date", "today"),
        TIMESTAMP: new column_1.DataType("timestamp", "Timestamp", "access_time"),
        // Misc types
        BOOLEAN: new column_1.DataType("boolean", "Boolean"),
        BINARY: new column_1.DataType("binary", "Binary"),
        STRING: new column_1.DataType("string", "String", "format_quote"),
        // Complex types
        ARRAY: new column_1.DataType("array", "Array"),
        MAP: new column_1.DataType("map", "Map"),
        STRUCT: new column_1.DataType("struct", "Struct")
    };
    /**
     * Handles user interactions with a Spark column.
     */
    var SparkColumnDelegate = /** @class */ (function (_super) {
        __extends(SparkColumnDelegate, _super);
        function SparkColumnDelegate(column, dataType, controller, $mdDialog, uiGridConstants, dialog, http, RestUrlService) {
            var _this = _super.call(this, dataType, controller, $mdDialog, uiGridConstants, dialog) || this;
            _this.column = column;
            _this.http = http;
            _this.RestUrlService = RestUrlService;
            return _this;
        }
        Object.defineProperty(SparkColumnDelegate.prototype, "displayName", {
            /**
             * Gets the display name of this column.
             */
            get: function () {
                return this.getColumnDisplayName(this.column);
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(SparkColumnDelegate.prototype, "fieldName", {
            /**
             * Gets the field name of this column.
             */
            get: function () {
                return this.getColumnFieldName(this.column);
            },
            enumerable: true,
            configurable: true
        });
        /**
         * Casts this column to the specified type.
         */
        SparkColumnDelegate.prototype.castTo = function (dataType) {
            if (dataType === SparkDataType.BIGINT) {
                var formula = this.toFormula(this.fieldName + ".cast(\"bigint\")", this.column, { columns: this.controller.tableColumns });
                this.controller.addFunction(formula, { formula: formula, icon: "fa-hashtag", name: "Cast " + this.displayName + " to bigint" });
            }
            if (dataType === SparkDataType.DATE) {
                return this.castToDate();
            }
            if (dataType === SparkDataType.DOUBLE) {
                var formula = this.toFormula(this.fieldName + ".cast(\"double\")", this.column, { columns: this.controller.tableColumns });
                this.controller.addFunction(formula, { formula: formula, icon: "fa-hashtag", name: "Cast " + this.displayName + " to double" });
            }
            if (dataType === SparkDataType.INT) {
                var formula = this.toFormula(this.fieldName + ".cast(\"int\")", this.column, { columns: this.controller.tableColumns });
                this.controller.addFunction(formula, { formula: formula, icon: "fa-hashtag", name: "Cast " + this.displayName + " to int" });
            }
            if (dataType === SparkDataType.STRING) {
                return this.castToString();
            }
            if (dataType === SparkDataType.TIMESTAMP) {
                return this.castToTimestamp();
            }
        };
        /**
         * Gets the target data types supported for casting this column.
         */
        SparkColumnDelegate.prototype.getAvailableCasts = function () {
            switch (this.dataType) {
                case SparkDataType.BIGINT.value:
                case SparkDataType.INT.value:
                    return [SparkDataType.DATE, SparkDataType.STRING];
                case SparkDataType.DATE.value:
                    return [SparkDataType.STRING, SparkDataType.TIMESTAMP];
                case SparkDataType.STRING.value:
                    return [SparkDataType.BIGINT, SparkDataType.DATE, SparkDataType.DOUBLE, SparkDataType.INT, SparkDataType.TIMESTAMP];
                default:
                    return [SparkDataType.STRING];
            }
        };
        /**
         * Casts this column to a date type.
         */
        SparkColumnDelegate.prototype.castToDate = function () {
            var _this = this;
            // Generate preview function
            var sampleValue = this.getSampleValue();
            var preview = (sampleValue != null) ? function (format) { return _this.parseDate(sampleValue, format).map(function (date) { return date.toLocaleDateString(); }); } : null;
            // Get date pattern from user
            if (this.dataCategory === column_delegate_1.DataCategory.NUMERIC) {
                this.dialog.openDateFormat({
                    message: "Enter the pattern for parsing this column as a date:",
                    preview: preview,
                    title: "Convert " + this.dataType.toLowerCase() + " to date",
                    type: dialog_service_1.DateFormatType.TIMESTAMP
                }).subscribe(function (response) {
                    // Build script
                    var script = _this.fieldName;
                    if (response.unit === dialog_service_1.DateFormatUnit.MILLISECONDS) {
                        script = "(" + script + "/1000)";
                    }
                    script = "to_date(" + script + ".cast(\"timestamp\")).as(\"" + StringUtils.quote(_this.displayName) + "\")";
                    // Add function to wrangler
                    var formula = _this.toFormula(script, _this.column, { columns: _this.controller.tableColumns });
                    _this.controller.addFunction(formula, { formula: formula, icon: "event", name: "Cast " + _this.displayName + " to date" });
                });
            }
            else if (this.dataCategory === column_delegate_1.DataCategory.STRING) {
                this.dialog.openDateFormat({
                    message: "Enter the pattern for parsing this column as a date:",
                    pattern: "yyyy-MM-dd",
                    patternHint: "See java.text.SimpleDateFormat for pattern letters.",
                    preview: preview,
                    title: "Convert " + this.dataType.toLowerCase() + " to date"
                }).subscribe(function (response) {
                    // Build script
                    var script;
                    if (response.type === dialog_service_1.DateFormatType.STRING) {
                        script = "to_date(unix_timestamp(" + _this.fieldName + ", \"" + StringUtils.quote(response.pattern) + "\").cast(\"timestamp\")).as(\"" + StringUtils.quote(_this.displayName) + "\")";
                    }
                    else if (response.type === dialog_service_1.DateFormatType.TIMESTAMP) {
                        script = _this.fieldName + ".cast(\"bigint\")";
                        if (response.unit === dialog_service_1.DateFormatUnit.MILLISECONDS) {
                            script = "(" + script + "/1000)";
                        }
                        script = "to_date(" + script + ".cast(\"timestamp\")).as(\"" + StringUtils.quote(_this.displayName) + "\")";
                    }
                    // Add function to wrangler
                    var formula = _this.toFormula(script, _this.column, { columns: _this.controller.tableColumns });
                    _this.controller.addFunction(formula, { formula: formula, icon: "event", name: "Cast " + _this.displayName + " to date" });
                });
            }
        };
        /**
         * Converts this column to a string type.
         */
        SparkColumnDelegate.prototype.castToString = function () {
            var _this = this;
            if (this.dataCategory === column_delegate_1.DataCategory.DATETIME) {
                var sampleValue_1 = this.getSampleValue();
                // Get date pattern from user
                this.dialog.openDateFormat({
                    message: "Enter the pattern for formatting this column as a string:",
                    pattern: "yyyy-MM-dd",
                    patternHint: "See java.text.SimpleDateFormat for pattern letters.",
                    preview: (sampleValue_1 != null) ? function (format) { return _this.formatDate(new Date(sampleValue_1), format); } : null,
                    title: "Convert " + this.dataType.toLowerCase() + " to string"
                }).subscribe(function (response) {
                    // Build script
                    var script;
                    if (response.type === dialog_service_1.DateFormatType.STRING) {
                        script = "date_format(" + _this.fieldName + ", \"" + StringUtils.quote(response.pattern) + "\").as(\"" + StringUtils.quote(_this.displayName) + "\")";
                    }
                    else if (response.type === dialog_service_1.DateFormatType.TIMESTAMP) {
                        script = (_this.dataCategory === column_delegate_1.DataCategory.NUMERIC) ? _this.fieldName : "unix_timestamp(" + _this.fieldName + ")";
                        if (response.unit === dialog_service_1.DateFormatUnit.SECONDS) {
                            script += ".cast(\"bigint\")";
                        }
                        else if (response.unit === dialog_service_1.DateFormatUnit.MILLISECONDS) {
                            script = "(" + script + ".cast(\"decimal(23,3)\") * 1000).cast(\"bigint\")";
                        }
                        script += ".cast(\"string\").as(\"" + StringUtils.quote(_this.displayName) + "\")";
                    }
                    // Add function to wrangler
                    var formula = _this.toFormula(script, _this.column, { columns: _this.controller.tableColumns });
                    _this.controller.addFunction(formula, { formula: formula, icon: "format_quote", name: "Cast " + _this.displayName + " to string" });
                });
            }
            else {
                var formula = this.toFormula(this.fieldName + ".cast(\"string\")", this.column, { columns: this.controller.tableColumns });
                this.controller.addFunction(formula, { formula: formula, icon: "format_quote", name: "Cast " + this.displayName + " to string" });
            }
        };
        /**
         * Cast this column to a timestamp type.
         */
        SparkColumnDelegate.prototype.castToTimestamp = function () {
            var _this = this;
            var sampleValue = this.getSampleValue();
            if (this.dataCategory === column_delegate_1.DataCategory.DATETIME) {
                var formula = this.toFormula("unix_timestamp(" + this.fieldName + ")", this.column, { columns: this.controller.tableColumns });
                this.controller.addFunction(formula, { formula: formula, icon: "access_time", name: "Cast " + this.displayName + " to timestamp" });
            }
            else if (this.dataCategory === column_delegate_1.DataCategory.STRING) {
                this.dialog.openDateFormat({
                    message: "Enter the pattern for parsing this column as a timestamp:",
                    pattern: "yyyy-MM-dd HH:mm:ss",
                    patternHint: "See java.text.SimpleDateFormat for pattern letters.",
                    preview: (sampleValue != null) ? function (format) { return _this.parseDate(sampleValue, format).map(function (date) { return moment(date).format("YYYY-MM-DD HH:mm:ss"); }); } : null,
                    title: "Convert " + this.dataType.toLowerCase() + " to timestamp",
                    type: dialog_service_1.DateFormatType.STRING
                }).subscribe(function (response) {
                    var script = "unix_timestamp(" + _this.fieldName + ", \"" + StringUtils.quote(response.pattern) + "\").as(\"" + StringUtils.quote(_this.displayName) + "\")";
                    var formula = _this.toFormula(script, _this.column, { columns: _this.controller.tableColumns });
                    _this.controller.addFunction(formula, { formula: formula, icon: "access_time", name: "Cast " + _this.displayName + " to timestamp" });
                });
            }
        };
        /**
         * Formats the specified Date as a string.
         *
         * @param value - the date
         * @param format - the format configuration
         * @returns the date string
         */
        SparkColumnDelegate.prototype.formatDate = function (value, format) {
            if (format.type === dialog_service_1.DateFormatType.STRING) {
                return this.http
                    .get(this.RestUrlService.FORMAT_DATE, {
                    params: {
                        date: value.getTime().toString(),
                        pattern: format.pattern
                    }
                })
                    .map(function (response) { return response.text; })
                    .catch(function (response) {
                    throw response.error.message;
                });
            }
            else if (format.type === dialog_service_1.DateFormatType.TIMESTAMP) {
                if (format.unit === dialog_service_1.DateFormatUnit.SECONDS) {
                    return Observable_1.Observable.of(Math.floor(value.getTime() / 1000).toString());
                }
                else if (format.unit === dialog_service_1.DateFormatUnit.MILLISECONDS) {
                    return Observable_1.Observable.of(value.getTime().toString());
                }
            }
            return Observable_1.Observable.empty();
        };
        /**
         * Gets a sample value for this column.
         */
        SparkColumnDelegate.prototype.getSampleValue = function () {
            var _this = this;
            var columnIndex = this.controller.tableColumns.findIndex(function (column) { return column.field === _this.column.field; });
            return this.controller.tableRows.map(function (row) { return row[columnIndex]; }).find(function (value) { return value != null; });
        };
        /**
         * Parses the specified string into a Date.
         *
         * @param value - the date string
         * @param format - the format configuration
         * @returns the Date
         */
        SparkColumnDelegate.prototype.parseDate = function (value, format) {
            var date = null;
            if (format.type === dialog_service_1.DateFormatType.STRING) {
                date = this.http
                    .get(this.RestUrlService.PARSE_DATE, {
                    params: {
                        pattern: format.pattern,
                        text: value
                    }
                })
                    .map(function (response) { return new Date(response.date); })
                    .catch(function (response) {
                    throw response.error.message;
                });
            }
            else if (format.type === dialog_service_1.DateFormatType.TIMESTAMP) {
                if (format.unit === dialog_service_1.DateFormatUnit.SECONDS) {
                    date = Observable_1.Observable.of(new Date(parseInt(value) * 1000));
                }
                else if (format.unit === dialog_service_1.DateFormatUnit.MILLISECONDS) {
                    date = Observable_1.Observable.of(new Date(parseInt(value)));
                }
            }
            if (date !== null) {
                return date.map(function (utc) { return new Date(utc.getUTCFullYear(), utc.getUTCMonth(), utc.getUTCDate(), utc.getUTCHours(), utc.getUTCMinutes(), utc.getUTCSeconds(), utc.getUTCMilliseconds()); });
            }
            else {
                return Observable_1.Observable.empty();
            }
        };
        return SparkColumnDelegate;
    }(column_delegate_1.ColumnDelegate));
    exports.SparkColumnDelegate = SparkColumnDelegate;
});
//# sourceMappingURL=spark-column.js.map