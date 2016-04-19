/**
 * The result of a Spark transformation.
 *
 * @typedef {Object} TransformResponse
 * @property {string} message the error message if status is "error"
 * @property {QueryResult} results the results if status is "success"
 * @property {string} status "success" if the script executed successfully or "error" if an exception occurred
 * @property {string} table the Hive table containing the results if status is "success"
 */

/**
 * The result of a SQL query or Spark DataFrame.
 *
 * @typedef {Object} QueryResult
 * @property {Object.<String, QueryResultColumn>} columnDisplayNameMap maps column display names to column details
 * @property {Object.<String, QueryResultColumn>} columnFieldMap maps field names to column details
 * @property {Array.<QueryResultColumn>} columns list of column details
 * @property {string} query the Spark script that was sent in the request
 * @property {Object.<String, *>} rows maps field names to values
 */

/**
 * A column in a QueryResult.
 *
 * @typedef {Object} QueryResultColumn
 * @property {string} databaseName name of the database containing the table
 * @property {string} dataType name of the data type for the column
 * @property {string} displayName a human-readable name for the column
 * @property {string} field name of the column in the table
 * @property {string} hiveColumnLabel suggested title for the column, usually specified by the AS clause
 * @property {number} index position of the column in the table
 * @property {string} tableName name of the source table
 */

angular.module(MODULE_FEED_MGR).factory("SparkShellService", function($http, $mdDialog) {
  // URL to the API server
  var API_URL = "http://" + window.location.hostname + ":8076/api/v1/spark/shell";

  /** TernJS directive for defined types */
  var DEFINE_DIRECTIVE = "!define";

  /** Regular expression for conversion strings */
  var FORMAT_REGEX = /%([cCdfs])/g;

  /** TernJS directive for the Spark code */
  var SPARK_DIRECTIVE = "!spark";

  /** Return type for columns in TernJS */
  var TERNJS_COLUMN_TYPE = "Column";

  /** TernJS directive for the return type */
  var TYPE_DIRECTIVE = "!sparkType";

  /**
   * Constructs a SparkShellService.
   *
   * @constructor
   * @param sql the source SQL for transformations
   */
  var SparkShellService = function(sql) {
    /**
     * Stack of the columns for each script as returned by the server.
     *
     * @private
     * @type {Array.<Array.<QueryResultColumn>>}
     */
    this.columns_ = [[]];

    /**
     * Transformation function definitions.
     *
     * @private
     * @type {Object}
     */
    this.defs_ = {};

    /**
     * Maximum number of results to return.
     *
     * @private
     * @type {number}
     */
    this.limit_ = 1000;

    /**
     * Stack of the scripts for each transformation.
     *
     * @private
     * @type {string[]}
     */
    this.script_ = [""];

    /**
     * The source SQL for transformations.
     *
     * @private
     * @type {string}
     */
    this.source_ = sql;
  };

  angular.extend(SparkShellService.prototype, {
    /**
     * Gets the type definitions for the output columns of the current script. These definitions are only available after
     * receiving a {@link SparkShellService#transform} response.
     *
     * @returns {Object} the column type definitions
     */
    getColumnDefs: function() {
      // Set directives
      var defs = {
        "!name": "columns"
      };

      defs[DEFINE_DIRECTIVE] = {};
      defs[DEFINE_DIRECTIVE][TERNJS_COLUMN_TYPE] = {};

      // Add column names
      var columns = (this.columns_.length !== 0) ? this.columns_[this.columns_.length - 1] : [];

      angular.forEach(columns, function(column) {
        defs[column.displayName] = TERNJS_COLUMN_TYPE;
      });

      return defs;
    },

    /**
     * Gets the function definitions being used.
     *
     * @return {Object} the function definitions
     */
    getFunctionDefs: function() {
      return this.defs_;
    },

    /**
     * Gets the Spark script.
     *
     * @returns {string} the Spark script
     */
    getScript: function() {
      var sparkScript = "import org.apache.spark.sql._\nsqlContext.sql(\"" + this.source_ + " LIMIT " + this.limit_ + "\")";

      angular.forEach(this.script_, function(script) {
        sparkScript += script;
      });

      return sparkScript;
    },

    /**
     * Removes the last transformation from the stack.
     */
    pop: function() {
      if (this.script_.length > 1) {
        this.columns_.pop();
        this.script_.pop();
      }
    },

    /**
     * Adds a transformation expression to the stack.
     *
     * @param {acorn.Node} tree the abstract syntax tree for the expression
     */
    push: function(tree) {
      this.script_.push(toScript(tree, this));
    },

    /**
     * Sets the function definitions to use.
     *
     * @param {Object} defs the function definitions
     */
    setFunctionDefs: function(defs) {
      this.defs_ = defs;
    },

    /**
     * Runs the current Spark script on the server.
     *
     * @return {HttpPromise} a promise for the response
     */
    transform: function() {
      // Create the response handlers
      var self = this;
      var successCallback = function(response) {
        self.columns_[self.script_.length - 1] = response.data.results.columns;
      };
      var errorCallback = function(response) {
        var alert = $mdDialog.alert()
            .parent($('body'))
            .clickOutsideToClose(true)
            .title("Error executing the query")
            .textContent(response.data.message)
            .ariaLabel("error executing the query")
            .ok("Got it!");
        $mdDialog.show(alert);
      };

      // Send the request
      var promise = $http({
        method: "POST",
        url: API_URL + "/transform",
        data: JSON.stringify({script: this.getScript(), sendResults: true}),
        headers: {"Content-Type": "application/json"},
        responseType: "json"
      });
      promise.then(successCallback, errorCallback);
      return promise;
    }
  });

  /**
   * Types supported by SparkExpression.
   *
   * @readonly
   * @enum {string}
   */
  var SparkType = {
    /** Represents a Spark SQL Column */
    COLUMN: "column",

    /** Represents a Spark SQL DataFrame */
    DATA_FRAME: "dataframe",

    /** Represents a Spark SQL GroupedData */
    GROUPED_DATA: "groupeddata",

    /** Represents a Scala number or string literal */
    LITERAL: "literal",

    /**
     * Gets the TernJS definition name for the specified type.
     *
     * @param {SparkType} sparkType the Spark type
     * @returns {string|null}
     */
    toTernjsName: function(sparkType) {
      switch (sparkType) {
        case SparkType.COLUMN:
          return TERNJS_COLUMN_TYPE;
                
        case SparkType.GROUPED_DATA:
          return "GroupedData";
                
        default:
          return null;
      }
    }
  };

  /**
   * Thrown to indicate that the abstract syntax tree could not be parsed.
   *
   * @constructor
   * @param {string} message the error message
   * @param {number} [opt_col] the column number
   */
  function ParseException(message, opt_col) {
    this.name = "ParseException";
    this.message = message + (opt_col ? opt_col : "");
  }

  ParseException.prototype = Object.create(Error.prototype);

  /**
   * An expression in a Spark script.
   *
   * @constructor
   * @param {string} source the Spark code
   * @param {SparkType} type the result type
   * @param {number} start the first column in the original expression
   * @param {number} end the last column in the original expression
   */
  function SparkExpression(source, type, start, end) {
    /**
     * Spark source code.
     * @type {string}
     */
    this.source = source;

    /**
     * Result type.
     * @type {SparkType}
     */
    this.type = type;

    /**
     * Column of the first character in the original expression.
     * @type {number}
     */
    this.start = start;

    /**
     * Column of the last character in the original expression.
     * @type {number}
     */
    this.end = end;
  }

  angular.extend(SparkExpression, {
    /**
     * Context for formatting a Spark conversion string.
     *
     * @typedef {Object} FormatContext
     * @property {SparkExpression[]} args the format parameters
     * @property {number} index the current position within {@code args}
     */

    /**
     * Formats the specified string by replacing the type specifiers with the specified parameters.
     *
     * @static
     * @param {string} str the Spark conversion string to be formatted
     * @param {...SparkExpression} var_args the format parameters
     * @returns {string} the formatted string
     * @throws {Error} if the conversion string is not valid
     * @throws {ParseException} if a format parameter cannot be converted to the specified type
     */
    format: function(str, var_args) {
      var context = {
        args: Array.prototype.slice.call(arguments, 1),
        index: 0
      };
      return str.replace(FORMAT_REGEX, angular.bind(str, SparkExpression.replace, context));
    },

    /**
     * Creates a Spark expression from a function definition.
     *
     * @static
     * @param {Object} definition the function definition
     * @param {acorn.Node} node the source abstract syntax tree
     * @param {...SparkExpression} var_args the format parameters
     * @returns {SparkExpression} the Spark expression
     * @throws {Error} if the function definition is not valid
     * @throws {ParseException} if a format parameter cannot be converted to the required type
     */
    fromDefinition: function(definition, node, var_args) {
      // Convert Spark string to code
      var args = [definition[SPARK_DIRECTIVE]];
      Array.prototype.push.apply(args, Array.prototype.slice.call(arguments, 2));

      var source = SparkExpression.format.apply(SparkExpression, args);

      // Return expression
      return new SparkExpression(source, definition[TYPE_DIRECTIVE], node.start, node.end);
    },

    /**
     * Converts the next argument to the specified type for a Spark conversion string.
     *
     * @private
     * @static
     * @param {FormatContext} context the format context
     * @param {string} match the conversion specification
     * @param {string} type the type specifier
     * @returns {string} the converted Spark code
     * @throws {Error} if the type specifier is not supported
     * @throws {ParseException} if the format parameter cannot be converted to the specified type
     */
    replace: function(context, match, type) {
      // Validate arguments
      if (context.args.length <= context.index) {
        throw new ParseException("Not enough arguments for conversion");
      }

      // Convert to requested type
      switch (type) {
        case "c":
          return SparkExpression.toColumn(context.args[context.index++]);

        case "C":
          return SparkExpression.toColumnArgs(context);

        case "s":
          return SparkExpression.toString(context.args[context.index++]);

        default:
          throw new Error("Not a recognized type specifier: " + match);
      }
    },

    /**
     * Converts the specified Spark expression to a Column type.
     *
     * @private
     * @static
     * @param {SparkExpression} expression the Spark expression
     * @returns {string} the Spark code for the new type
     * @throws {ParseException} if the expression cannot be converted to a column
     */
    toColumn: function(expression) {
      switch (expression.type) {
        case SparkType.COLUMN:
          return expression.source;

        case SparkType.LITERAL:
          return "functions.lit(" + expression.source + ")";

        default:
          throw new ParseException("Expression cannot be converted to a column: " + expression.type, expression.start);
      }
    },

    /**
     * Converts the specified Spark expressions to a list of function arguments.
     *
     * @param {FormatContext} context the format context
     * @returns {string} the Spark code for the function arguments
     * @throws {ParseException} if any expression cannot be converted to a column
     */
    toColumnArgs: function(context) {
      var result = "";

      for (; context.index < context.args.length; ++context.index) {
        if (context.index !== 0) {
          result += ", ";
        }
        result += SparkExpression.toColumn(context.args[context.index]);
      }

      return result;
    },

    /**
     * Converts the specified Spark expression to a string literal.
     *
     * @private
     * @static
     * @param {SparkExpression} expression the Spark expression
     * @returns {string} the Spark code for the string literal
     * @throws {ParseException} if the expression cannot be converted to a string
     */
    toString: function(expression) {
      if (SparkType.LITERAL) {
        return (expression.source.charAt(0) === "\"") ? expression.source : "\"" + expression.source + "\"";
      } else {
        throw new ParseException("Expression cannot be converted to a string: " + expression.type, expression.start);
      }
    }
  });

  /**
   * Converts a binary expression node to a Spark expression.
   *
   * @param {acorn.Node} node the binary expression node
   * @param {SparkShellService} sparkShellService the Spark shell service
   * @returns {SparkExpression} the Spark expression
   * @throws {Error} if the function definition is not valid
   * @throws {ParseException} if a function argument cannot be converted to the required type
   */
  function parseBinaryExpression(node, sparkShellService) {
    // Get the function definition
    var def = null;

    switch (node.operator) {
      case "+":
        def = sparkShellService.getFunctionDefs().add;
        break;

      case "-":
        def = sparkShellService.getFunctionDefs().subtract;
        break;

      case "*":
        def = sparkShellService.getFunctionDefs().multiply;
        break;

      case "/":
        def = sparkShellService.getFunctionDefs().divide;
        break;

      default:
    }

    if (def == null) {
      throw new ParseException("Binary operator not supported: " + node.operator, node.start);
    }

    // Convert to a Spark expression
    var left = toSpark(node.left, sparkShellService);
    var right = toSpark(node.right, sparkShellService);
    return SparkExpression.fromDefinition(def, node, left, right);
  }

  /**
   * Converts a call expression node to a Spark expression.
   *
   * @param {acorn.Node} node the call expression node
   * @param {SparkShellService} sparkShellService the Spark shelll service
   * @returns {SparkExpression} the Spark expression
   * @throws {Error} if the function definition is not valid
   * @throws {ParseException} if a function argument cannot be converted to the required type
   */
  function parseCallExpression(node, sparkShellService) {
    // Get the function definition
    var def;
    var name;
    var parent = null;

    switch (node.callee.type) {
      case "Identifier":
        def = sparkShellService.getFunctionDefs()[node.callee.name];
        name = node.callee.name;
        break;

      case "MemberExpression":
        parent = toSpark(node.callee.object, sparkShellService);

        // Find function definition
        var ternjsName = SparkType.toTernjsName(parent.type);

        if (ternjsName !== null) {
          def = sparkShellService.getFunctionDefs()[DEFINE_DIRECTIVE][ternjsName][node.callee.property.name];
        } else {
          throw new ParseException("Result type has no members: " + parent.type);
        }
        break;

      default:
        throw new ParseException("Function call type not supported: " + node.callee.type);
    }

    if (def == null) {
      throw new ParseException("Function is not defined: " + name);
    }

    // Convert to a Spark expression
    var args = [def, node];

    angular.forEach(node.arguments, function(arg) {
      args.push(toSpark(arg, sparkShellService));
    });

    var spark = SparkExpression.fromDefinition.apply(SparkExpression, args);
    return (parent !== null) ? new SparkExpression(parent.source + spark.source, spark.type, spark.start, spark.end) : spark;
  }

  /**
   * Converts the specified abstract syntax tree to a Scala expression for a Spark script.
   *
   * @param {acorn.Node} program the program node
   * @param {SparkShellService} sparkShellService the spark shell service
   * @returns {string} the Scala expression
   * @throws {Error} if a function definition is not valid
   * @throws {ParseException} if the program is not valid
   */
  function toScript(program, sparkShellService) {
    // Check node parameters
    if (program.type !== "Program") {
      throw new Error("Cannot convert non-program to Spark");
    }
    if (program.body.length !== 1) {
      throw new Error("Program is too long");
    }

    // Convert to a DataFrame
    var spark = toSpark(program.body[0], sparkShellService);

    switch (spark.type) {
      case SparkType.COLUMN:
        return ".select(new Column(\"*\"), " + spark.source + ")";

      case SparkType.DATA_FRAME:
        return spark.source;

      default:
        throw new Error("Result type not supported: " + spark.type);
    }
  }

  /**
   * Converts the specified abstract syntax tree to a Spark expression object.
   *
   * @param {acorn.Node} node the abstract syntax tree
   * @param {SparkShellService} sparkShellService the spark shell service
   * @returns {SparkExpression} the Spark expression
   * @throws {Error} if a function definition is not valid
   * @throws {ParseException} if the node is not valid
   */
  function toSpark(node, sparkShellService) {
    switch (node.type) {
      case "BinaryExpression":
        return parseBinaryExpression(node, sparkShellService);

      case "CallExpression":
        return parseCallExpression(node, sparkShellService);

      case "ExpressionStatement":
        return toSpark(node.expression, sparkShellService);

      case "Identifier":
        return new SparkExpression("new Column(\"" + StringUtils.quote(node.name) + "\")", SparkType.COLUMN, node.start,
            node.end);

      case "Literal":
        return new SparkExpression(node.raw, SparkType.LITERAL, node.start, node.end);

      default:
        throw new Error("Unsupported node type: " + node.type);
    }
  }

  return SparkShellService;
});
