import "feed-mgr/module";
import {UnderscoreStatic} from "underscore";

declare const _: UnderscoreStatic;
declare const angular: angular.IAngularStatic;

const moduleName: string = require("feed-mgr/module-name");

/**
 * Prefix for table aliases.
 * @type {string}
 */
var TABLE_PREFIX = "tbl";

/**
 * Builds a SQL query from a visual query model.
 *
 * @constructor
 * @param model - the visual query model
 * @param dialect - SQL dialect
 */
export function SqlBuilder(model: VisualQueryModel, dialect: SqlDialect) {

    /**
     * SQL dialect.
     */
    this.dialect = dialect;

    /**
     * The visual query model.
     * @private
     * @type {VisualQueryModel}
     */
    this.model_ = model;

    /**
     * List of columns in the generated SQL.
     * @private
     * @type {Array|null}
     */
    this.selectedColumnsAndTables_ = null;
}

angular.extend(SqlBuilder.prototype, {
    /**
     * Generates the SQL for the model.
     *
     * @public
     * @return {string} the SQL
     * @throws {Error} if the model is invalid
     */
    build: function (): string {
        const self = this;
        let select = "";
        const tree = this.buildTree();

        if (tree === null) {
            return "";
        }

        // Build SELECT clause
        angular.forEach(tree.targetList, function (target) {
            select += (select.length === 0) ? "SELECT " : ", ";
            select += target.val.fields[0] + "." + self.quoteSql(target.val.fields[1]);
            if (target.name != null) {
                select += " AS " + self.quoteSql(target.name);
            }
        });

        // Parse fromClause
        const fromTables: string[] = [];
        const joinClauses: string[] = [];

        angular.forEach(tree.fromClause, function (node) {
            self.addFromClause(node, fromTables, joinClauses);
        });

        // Build FROM clause
        let sql = "";

        angular.forEach(fromTables, function (table) {
            sql += (sql.length === 0) ? select + " FROM " : ", ";
            sql += table;
        });
        angular.forEach(joinClauses, function (join) {
            sql += " " + join;
        });

        return sql;
    },

    /**
     * Generates a tree for the model.
     *
     * @public
     * @return {?SelectStmt} the abstract syntax tree, or NULL if the model is empty
     */
    buildTree: function (): SelectStmt {
        this.selectedColumnsAndTables_ = [];

        if (this.model_ === null || this.model_.nodes.length === 0) {
            return null;
        } else {
            var fromClause = this.buildFromTree();
            var targetList = this.buildTargetTree();
            return {fromClause: fromClause, targetList: targetList};
        }
    },

    /**
     * Gets the set of data sources used in the model.
     *
     * @public
     * @return {Array.<string>} the unique data source ids
     */
    getDatasourceIds: function (): string[] {
        return _.chain(this.model_.nodes)
            .map(function (node) {
                return node.datasourceId;
            })
            .uniq()
            .value();
    },

    /**
     * Returns the list of columns in the generated SQL.
     *
     * @public
     * @returns {Array} the list of columns
     */
    getSelectedColumnsAndTables: function (): any[] {
        if (this.selectedColumnsAndTables_ === null) {
            this.build();
        }
        return this.selectedColumnsAndTables_;
    },

    /**
     * Adds joins for the specified table to a SQL statement.
     *
     * @private
     * @param {(RangeVar|JoinExpr)} expr the join or table expression
     * @param {string[]} fromTables the list of tables to include in the FROM clause
     * @param {string[]} joinClauses the list of JOIN clauses
     * @throws {Error} if the model is invalid
     */
    addFromClause: function (expr: any, fromTables: any, joinClauses: any) {
        if (expr.type === VisualQueryService.NodeTag.RangeVar) {
            fromTables.push(this.quoteSql(expr.schemaname) + "." + this.quoteSql(expr.relname) + " " + expr.aliasName);
        } else if (expr.type === VisualQueryService.NodeTag.JoinExpr) {
            this.addFromClause(expr.larg, fromTables, joinClauses);

            var sql = "";
            switch (expr.jointype) {
                case VisualQueryService.JoinType.JOIN:
                    sql += "JOIN";
                    break;

                case VisualQueryService.JoinType.JOIN_INNER:
                    sql += "INNER JOIN";
                    break;

                case VisualQueryService.JoinType.JOIN_LEFT:
                    sql += "LEFT JOIN";
                    break;

                case VisualQueryService.JoinType.JOIN_RIGHT:
                    sql += "RIGHT JOIN";
                    break;

                default:
                    throw new Error("Unsupported join type: " + expr.jointype);
            }

            sql += " " + this.getTableSql(expr.rarg) + " " + expr.rarg.aliasName;

            if (expr.quals !== null) {
                sql += " ON " + this.getQualifierSql(expr.quals, false);
            }

            joinClauses.push(sql);
        } else {
            throw new Error("Not a recognized node type: " + expr.type);
        }
    },

    /**
     * Adds joins for the specified table to a SQL statement.
     *
     * @private
     * @param {TableInfo} tableInfo the table to search for joins
     * @param {TableJoinMap} graph the table join map
     * @param {Array.<RangeVar>} fromTables the list of tables to include in the FROM clause
     * @param {Array.<JoinExpr>} joinClauses the list of JOIN clauses
     */
    addTableJoins: function (tableInfo: any, graph: any, fromTables: any, joinClauses: any) {
        var self = this;

        // Add JOIN clauses for tables connected to this one
        var edges: any = [];
        var srcID = tableInfo.data.id;
        graph[srcID].seen = true;

        angular.forEach(graph[srcID].edges, function (connection, dstID) {
            if (connection !== null) {
                self.getJoinTree(tableInfo.data, graph[dstID].data, connection, graph, joinClauses);
                edges.push(dstID);
                graph[srcID].edges[dstID] = null;
                graph[dstID].edges[srcID] = null;
            }
        });

        // Add JOIN clauses for tables connected to child nodes
        angular.forEach(edges, function (nodeID) {
            self.addTableJoins(graph[nodeID], graph, null, joinClauses);
        });
    },

    /**
     * Gets the join tree for the specified join.
     *
     * @param {Object} src the node for the source table
     * @param {Object} dst the node for the destination table
     * @param {Object} connection the join description
     * @param {TableJoinMap} graph the table join map
     * @param {Array.<JoinExpr>} joinClauses the list of JOIN clauses
     * @returns {JoinExpr} the join tree
     */
    getJoinTree: function (src: any, dst: any, connection: any, graph: any, joinClauses: any) {
        var self = this;

        // Determine left arg
        var larg = (joinClauses.length > 0)
            ? joinClauses.pop()
            : self.getRangeVar(src);

        // Use default if missing join keys
        if (angular.isUndefined(connection.joinKeys.destKey) || angular.isUndefined(connection.joinKeys.sourceKey) || angular.isUndefined(connection.joinType)) {
            joinClauses.push({
                type: VisualQueryService.NodeTag.JoinExpr,
                jointype: VisualQueryService.JoinType.JOIN,
                larg: larg,
                rarg: self.getRangeVar(dst),
                quals: null
            });
            return;
        }

        // Create JOIN clause
        graph[dst.id].seen = true;

        var joinType;
        if (connection.joinType === "INNER JOIN") {
            joinType = VisualQueryService.JoinType.JOIN_INNER;
        } else if (connection.joinType === "LEFT JOIN") {
            joinType = VisualQueryService.JoinType.JOIN_LEFT;
        } else if (connection.joinType === "RIGHT JOIN") {
            joinType = VisualQueryService.JoinType.JOIN_RIGHT;
        } else {
            throw new Error("Not a supported join type: " + connection.joinType);
        }

        var tree: any = {
            type: VisualQueryService.NodeTag.JoinExpr,
            jointype: joinType,
            larg: larg,
            rarg: self.getRangeVar(dst),
            quals: {
                type: VisualQueryService.NodeTag.A_Expr,
                name: "=",
                lexpr: {
                    fields: [TABLE_PREFIX + dst.id, (connection.source.nodeID === src.id) ? connection.joinKeys.sourceKey : connection.joinKeys.destKey]
                },
                rexpr: {
                    fields: [TABLE_PREFIX + src.id, (connection.source.nodeID === src.id) ? connection.joinKeys.destKey : connection.joinKeys.sourceKey]
                }
            }
        };

        // Add conditions for 'seen' tables
        _.values(graph[dst.id].edges)
            .filter(function (edge: any) {
                return (edge != null && edge.source.nodeID !== src.id && graph[edge.source.nodeID].seen && edge.dest.nodeID !== src.id && graph[edge.dest.nodeID].seen);
            })
            .forEach(function (edge: any) {
                var lexpr = tree.quals;
                var rexpr = {
                    type: VisualQueryService.NodeTag.A_Expr,
                    name: "=",
                    lexpr: {
                        fields: [TABLE_PREFIX + edge.source.nodeID, edge.joinKeys.sourceKey]
                    },
                    rexpr: {
                        fields: [TABLE_PREFIX + edge.dest.nodeID, edge.joinKeys.destKey]
                    }
                };
                tree.quals = {
                    type: VisualQueryService.NodeTag.BoolExpr,
                    boolop: VisualQueryService.BoolExprType.AND_EXPR,
                    args: [lexpr, rexpr]
                };

                // Remove join from graph
                graph[edge.source.nodeID].edges[edge.dest.nodeID] = null;
                graph[edge.dest.nodeID].edges[edge.source.nodeID] = null;
            });

        joinClauses.push(tree);
    },

    /**
     * Builds the tree for the FROM clause.
     *
     * @private
     * @return {Array.<(RangeVar|JoinExpr)>} the FROM tree
     */
    buildFromTree: function () {
        var self = this;

        // Build clauses
        var fromTables: any = [];
        var graph = this.createTableJoinMap();
        var joinClauses: any = [];

        angular.forEach(graph, function (node) {
            if (_.size(node.edges) === 0) {
                fromTables.push(self.getRangeVar(node.data));
            }
            else {
                self.addTableJoins(node, graph, fromTables, joinClauses);
            }
        });

        Array.prototype.push.apply(fromTables, joinClauses);
        return fromTables;
    },

    /**
     * Builds the tree for the target column list.
     *
     * @private
     * @return {Array.<ResTarget>} the target trees
     */
    buildTargetTree: function () {
        var aliasCount: any = this.getAliasCount();
        var self = this;
        var targetList: any = [];

        angular.forEach(this.model_.nodes, function (node) {
            var table = TABLE_PREFIX + node.id;

            angular.forEach(node.nodeAttributes.attributes, function (attr) {
                if (attr.selected) {
                    // Determine column alias
                    var alias = _.find(self.getColumnAliases(node.name, attr.name), function (name: any) {
                        return (aliasCount[name] === 1)
                    });
                    if (typeof(alias) === "undefined") {
                        var i = 0;
                        do {
                            ++i;
                            alias = attr.name + "_" + i;
                        } while (aliasCount[alias] > 0);
                        aliasCount[alias] = 1;
                    }

                    // Add column to target list
                    targetList.push({
                        description: attr.description,
                        name: (alias !== attr.name) ? alias : null,
                        val: {fields: [table, attr.name]}
                    });
                    self.selectedColumnsAndTables_.push({
                        column: attr.name,
                        alias: TABLE_PREFIX + node.id, tableName: node.name,
                        tableColumn: attr.name, dataType: attr.dataType
                    });
                }
            });
        });

        return targetList;
    },

    /**
     * Creates a map indicating how tables may be joined. The key is the node ID and the value is a dictionary containing the node model and the connections for the joins.
     *
     * @private
     * @returns {TableJoinMap} the table join map
     */
    createTableJoinMap: function () {
        var map = {};

        // Add every node to the map
        angular.forEach(this.model_.nodes, function (node) {
            map[node.id] = {data: node, edges: {}, seen: false};
        });

        // Add edges to the map
        angular.forEach(this.model_.connections, function (connection) {
            map[connection.source.nodeID].edges[connection.dest.nodeID] = connection;
            map[connection.dest.nodeID].edges[connection.source.nodeID] = connection;
        });

        return map;
    },

    /**
     * Determine a unique alias for each column.
     *
     * @private
     * @return {Object.<string, number>} the alias name to count
     */
    getAliasCount: function () {
        var aliasCount = {};
        var self = this;

        angular.forEach(this.model_.nodes, function (node) {
            angular.forEach(node.nodeAttributes.attributes, function (attr) {
                if (attr.selected) {
                    angular.forEach(self.getColumnAliases(node.name, attr.name), function (alias) {
                        aliasCount[alias] = (typeof(aliasCount[alias]) !== "undefined") ? aliasCount[alias] + 1 : 1;
                    });
                }
            });
        });

        return aliasCount;
    },

    /**
     * Gets the SQL clause for the specified boolean type.
     *
     * @private
     * @param {BoolExprType} boolType the boolean expression type
     * @returns {string} the SQL
     */
    getBoolTypeSql: function (boolType: any) {
        switch (boolType) {
            case VisualQueryService.BoolExprType.AND_EXPR:
                return "AND";

            default:
                throw new Error("Not a supported BoolExprType: " + boolType);
        }
    },

    /**
     * Generates a list of possible aliases for the specified column.
     *
     * @private
     * @param tableName the name of the table
     * @param columnName the name of the column
     * @returns {string[]} the list of aliases
     */
    getColumnAliases: function (tableName: any, columnName: any) {
        var aliases = [];
        if (columnName !== "processing_dttm") {
            aliases.push(columnName);
        }
        aliases.push(tableName.replace(/.*\./, "") + "_" + columnName, tableName.replace(".", "_") + "_" + columnName);
        return aliases;
    },

    /**
     * Gets the SQL clause for the specified column reference.
     *
     * @private
     * @param {ColumnRef} column the column reference
     * @param {boolean} [quoteSchema] indicates if the schema should be quoted
     * @return {string} the SQL
     */
    getColumnSql: function (column: any, quoteSchema: any) {
        var self = this;
        var sql = "";
        angular.forEach(column.fields, function (field) {
            if (sql.length > 0) {
                sql += ".";
            }
            sql += (sql.length > 0 || quoteSchema) ? self.quoteSql(field) : field;
        });
        return sql;
    },

    /**
     * Gets the SQL clause for the specified table reference.
     *
     * @private
     * @param {RangeVar} table the table reference
     * @returns {string} the SQL
     */
    getTableSql: function (table: any) {
        var sql = "";
        if (table.schemaname !== null) {
            sql += this.quoteSql(table.schemaname) + ".";
        }
        sql += this.quoteSql(table.relname);
        return sql;
    },

    /**
     * Gets the SQL clause for the specified join qualifier.
     *
     * @private
     * @param {(A_Expr|BoolExpr)} qualifier the qualifier on join
     * @param {boolean} [quoteSchema] indicates if the schema should be quoted
     * @returns {string} the SQL
     */
    getQualifierSql: function (qualifier: any, quoteSchema: any) {
        if (qualifier.type === VisualQueryService.NodeTag.A_Expr) {
            return this.getColumnSql(qualifier.lexpr, quoteSchema) + " " + qualifier.name + " " + this.getColumnSql(qualifier.rexpr);
        } else if (qualifier.type === VisualQueryService.NodeTag.BoolExpr) {
            return this.getQualifierSql(qualifier.args[0]) + " " + this.getBoolTypeSql(qualifier.boolop) + " " + this.getQualifierSql(qualifier.args[1]);
        }
    },

    /**
     * Creates a RangeVar from the specified VisualQueryNode.
     *
     * @param {VisualQueryNode} node the table node
     * @returns {RangeVar} the range var
     */
    getRangeVar: function (node: any) {
        var schema;
        var table;
        if (angular.isArray(node.nodeAttributes.reference)) {
            schema = node.nodeAttributes.reference[0];
            table = node.nodeAttributes.reference[1];
        } else if (angular.isString(node.nodeAttributes.sql)) {
            var parts = node.nodeAttributes.sql.split(".");
            schema = parts[0].substr(1, parts[0].length - 2).replace(/``/g, "`");
            table = parts[1].substr(1, parts[1].length - 2).replace(/``/g, "`");
        }

        var rangeVar: any = {
            type: VisualQueryService.NodeTag.RangeVar,
            schemaname: schema,
            relname: table,
            aliasName: TABLE_PREFIX + node.id
        };
        if (angular.isString(node.datasourceId)) {
            rangeVar.datasourceId = node.datasourceId;
        }
        return rangeVar;
    },

    quoteSql: function (identifier: string): string {
        if (this.dialect === SqlDialect.HIVE) {
            return "`" + StringUtils.quoteSql(identifier, "`", "`") + "`";
        } else if (this.dialect === SqlDialect.TERADATA) {
            return "\"" + identifier + "\"";
        }
    }
});

/**
 * Supported SQL dialects
 */
export enum SqlDialect {

    /** Hive Query Language dialect */
    HIVE,

        /** Teradata SQL dialect */
    TERADATA
}

/**
 * Manages the state of the visual query pages.
 */
export var VisualQueryService = {

    /**
     * Type of boolean expression.
     *
     * @public
     * @readonly
     * @enum {number}
     */
    BoolExprType: {
        AND_EXPR: 0
    },

    /**
     * Identifier of the Hive datasource.
     * @type {string}
     */
    HIVE_DATASOURCE: "HIVE",

    /**
     * Enums for types of relation joins.
     *
     * @public
     * @readonly
     * @enum {number}
     */
    JoinType: {
        JOIN: 0,
        JOIN_INNER: 1,
        JOIN_LEFT: 2,
        JOIN_RIGHT: 3
    },

    /**
     * Type of node.
     *
     * @public
     * @readonly
     * @enum {string}
     */
    NodeTag: {
        A_Expr: "A_Expr",
        BoolExpr: "BoolExpr",
        JoinExpr: "JoinExpr",
        RangeVar: "RangeVar"
    },

    /**
     * Stored model for the Visual Query page.
     * @type {{selectedDatasourceId: string, visualQueryModel: VisualQueryModel, visualQuerySql: string}}
     */
    model: {},

    /**
     * Resets this model to default values.
     */
    resetModel: function (): void {
        this.model = {
            selectedDatasourceId: this.HIVE_DATASOURCE
        };
    },

    /**
     * Creates a SQL builder from the specified model.
     *
     * @param model - the model
     * @param dialect - SQL dialect
     * @returns {SqlBuilder} the SQL builder
     */
    sqlBuilder: function (model: VisualQueryModel, dialect: SqlDialect): any {
        return new (SqlBuilder as any)(model, dialect);
    }
};

VisualQueryService.resetModel();
angular.module(moduleName).factory("VisualQueryService", () => VisualQueryService);

/**
 * Type of boolean expression.
 */
export type BoolExprType = 0;

/**
 * Enums for types of relation joins.
 */
export type JoinType = 0 | 1 | 2 | 3;

/**
 * Type of node.
 */
export type NodeTag = "A_Expr" | "BoolExpr" | "JoinExpr" | "RangeVar";

/**
 * Model for the Visual Query page.
 */
export interface VisualQueryModel {

    /**
     * List of tables.
     */
    nodes: VisualQueryNode[];
}

/**
 * A table node.
 */
export interface VisualQueryNode {

    /**
     * Table information.
     */
    nodeAttributes: VisualQueryNodeAttributes;
}

/**
 * Attributes of a table node.
 */
export interface VisualQueryNodeAttributes {

    /**
     * List of all columns.
     */
    attributes: VisualQueryField[];

    /**
     * Schema and table name.
     */
    reference: string[];

    /**
     * List of selected columns.
     */
    selected: VisualQueryField[];

    /**
     * Database and table name escaped for a SQL query.
     */
    sql?: string;
}

/**
 * Field of a table.
 */
export interface VisualQueryField {

    /**
     * Field or column name.
     */
    name: string;

    /**
     * Business description of the field.
     */
    description: string;

    /**
     * Data type in reference of the source (e.g. an RDMS)
     */
    nativeDataType: string;

    /**
     * Data type in reference of the target (e.g. Hive)
     */
    derivedDataType: string;

    /**
     * True/false indicating if field is primary key
     */
    primaryKey: boolean;

    /**
     * True/false indicating if field can accept null value
     */
    nullable: boolean;

    /**
     * List of sample values for field
     */
    sampleValues: string[];

    /**
     * True/false indicating whether derived properties can be modified or not
     */
    modifiable: boolean;

    /**
     * Additional descriptor about the derived data type
     */
    dataTypeDescriptor: object;

    /**
     * Data type with precision and scale
     */
    dataTypeWithPrecisionAndScale: string;

    /**
     * Precision and scale portion of the data type
     */
    precisionScale: string;

    /**
     * True/false indicating whether field represents created timestamp
     */
    createdTracker: boolean;

    /**
     * True/false indicating whether field represents update timestamp
     */
    updatedTracker: boolean;

    /**
     * True if the column is selected, or false otherwise
     */
    selected: boolean;
}

/**
 * A query tree for a SELECT statement.
 */
export interface SelectStmt {

    /**
     * The FROM clause
     */
    fromClause: RangeVar[] | JoinExpr[];

    /**
     * Column list
     */
    targetList: ResTarget[];
}

/**
 * Infix, prefix, or postfix expressions.
 */
export interface A_Expr {

    /**
     * "A_Expr"
     */
    type: NodeTag;

    /**
     * The operator
     */
    name: string;

    /**
     * The left argument
     */
    lexpr: ColumnRef;

    /**
     * The right argument
     */
    rexpr: ColumnRef;
}

/**
 * The basic Boolean operators AND, OR, NOT
 */
export interface BoolExpr {

    /**
     * "BoolExpr"
     */
    type: NodeTag;

    boolop: BoolExprType;

    /**
     * Arguments to this expression
     */
    args: A_Expr[];
}

/**
 * Specifies a reference to a column.
 */
export interface ColumnRef {

    /**
     * Field names
     */
    fields: string[];
}

/**
 * For SQL JOIN expressions.
 */
export interface JoinExpr {

    /**
     * "JoinExpr"
     */
    type: NodeTag;

    /**
     * The type of join
     */
    jointype: JoinType;

    /**
     * The left subtree
     */
    larg: RangeVar | JoinExpr;

    /**
     * The right subtree
     */
    rarg: RangeVar;

    /**
     * The qualifiers on join, if any
     */
    quals?: A_Expr | BoolExpr;
}

/**
 * Represents a table name in FROM clauses.
 */
export interface RangeVar {

    /**
     * "RangeVar"
     */
    type: NodeTag;

    /**
     * The schema name, or NULL
     */
    schemaname?: string;

    /**
     * The relation name
     */
    relname: string;

    /**
     * The table alias
     */
    aliasName?: string;

    /**
     * The datasource id
     */
    datasourceId?: string;
}

/**
 * A result target.
 */
export interface ResTarget {

    /**
     * The comment for the column
     */
    description?: string;

    /**
     * The column label for an 'AS ColumnLabel' clause, or NULL if there was none
     */
    name?: string;

    /**
     * The value expression to compute or assign
     */
    val: ColumnRef;
}
