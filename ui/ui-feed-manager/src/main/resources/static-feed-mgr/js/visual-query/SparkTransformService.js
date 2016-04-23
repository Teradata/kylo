angular.module(MODULE_FEED_MGR).factory('SparkTransformService', function () {
    var COLUMNS = [];
    var SPARK_DIRECTIVE = "!spark";
    var SQL_DIRECTIVE = "!sql";

    var SparkTransformService = function (sql)
    {
        this.script = [];
        this.script.push("sqlContext.sql(\"" + StringUtils.quote(sql) + " LIMIT 1000\")");
    };

    SparkTransformService.prototype.add = function (expr, columns)
    {
        this.script.push(toSpark(expr, columns, this.defs));
    };

    SparkTransformService.prototype.transform = function (cb)
    {
        var sc = "";

        for (var i=0; i < this.script.length; ++i) {
            sc += this.script[i];
        }

        $.ajax({
            contentType: 'application/json',
            data: JSON.stringify({script: sc}),
            method: 'POST',
            type: 'json',
            url: 'http://localhost:8076/api/v1/spark/shell/transform',
            success: function (data) {
                cb(data.table);
            },
            error: function () {
                alert("ERROR");
            }
        })
    };

    /**
     * Generates a new column name in the format "col#". The new column name is guaranteed to be
     * unique within the given list of columns.
     *
     * @param {Object[]} columns the list of existing columns
     * @returns {string} the new column name
     */
    function newColumnName (columns)
    {
        var match = false;
        var name;
        var num = columns.length;

        while (!match) {
            name = "col" + (++num);
            match = true;
            for (var i = 0; i < columns.length && match; ++i) {
                if (columns[i]["field"] === name) {
                    match = false;
                }
            }
            for (var i=0; i < COLUMNS.length && match; ++i) {
                if (COLUMNS[i] === name) {
                    match = false;
                }
            }
        }

        COLUMNS.push(name);
        return name;
    }

    /**
     * Converts the specified syntax tree to a Spark script in Scala.
     *
     * @param {Object} expr the abstract syntax tree to parse
     * @param {Object[]} columns the list of existing columns
     * @returns {string} the generated Spark script
     * @throws {Error} if the syntax tree cannot be parsed
     */
    function toSpark (expr, columns, defs)
    {
        switch (expr.type) {
                // A function call
            case "CallExpression":
                var def = {};
                var prepend = "";
                if (expr.callee.type == "MemberExpression") {
                    def = defs[expr.callee.property.name];
                    prepend = toSpark(expr.callee.object, columns, defs);
                }
                else {
                    def = defs[expr.callee.name];
                }

                // Convert the function to Spark code
                if (typeof(def[SPARK_DIRECTIVE]) !== "undefined") {
                    var result = def[SPARK_DIRECTIVE];
                    for (var i=0; i < expr.arguments.length; ++i) {
                        result = result.replace("{" + i + "}", "new org.apache.spark.sql.Column(new org.apache.spark.sql.catalyst.SqlParser().parseExpression(\"" + StringUtils.quote(toSql(expr.arguments[i], defs)) + "\")).alias(\"" + StringUtils.quote(newColumnName(columns)) + "\")");
                    }
                    return prepend + result;
                }
                if (typeof(def[SQL_DIRECTIVE]) !== "undefined") {
                    return prepend + ".withColumn(\"" + StringUtils.quote(newColumnName(columns)) + "\", new org.apache.spark.sql.Column(new org.apache.spark.sql.catalyst.SqlParser().parseExpression(\"" + StringUtils.quote(toSql(expr, defs)) + "\")))";
                }
                else {
                    throw new Error("Not a function: " + expr.callee.name);
                }

                // A line in the Program
            case "ExpressionStatement":
                return toSpark(expr.expression, columns, defs);

                // The transformation expression
            case "Program":
                var result = "";
                for (var i=0; i < expr.body.length; ++i) {
                    result += toSpark(expr.body[i], columns, defs);
                }
                return result;

            default:
                throw new Error("Unsupported expression type: " + expr.type);
        }
    }

    /**
     * Converts the specified syntax tree to a SQL script.
     *
     * @param {Object} expr the abstract syntax tree to parse
     * @returns {string} the generated SQL script
     * @throws {Error} if the syntax tree cannot be parsed
     */
    function toSql (expr, defs)
    {
        switch (expr.type) {
                // A function call
            case "CallExpression":
                var def = defs[expr.callee.name];
                if (typeof(def) !== "undefined" && typeof(def[SQL_DIRECTIVE]) !== "undefined") {
                    var result = def[SQL_DIRECTIVE];
                    for (var i = 0; i < expr.arguments.length; ++i) {
                        result = result.replace("{" + i + "}", toSql(expr.arguments[i], defs));
                    }
                    return result;
                }
                else {
                    throw new Error("Not a SQL function: " + expr.callee.name);
                }

                // A column name
            case "Identifier":
                return expr.name;

                // A literal value
            case "Literal":
                return expr.raw;

            default:
                throw new Error("Unsupported expression type: " + expr.type);
        }
    }

    return SparkTransformService;
});
