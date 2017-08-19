declare namespace VisualQueryServiceStatic {

    /**
     * Builds a SQL query from a visual query model.
     */
    interface SqlBuilder {

        /**
         * Generates the SQL for the model.
         *
         * @return the SQL
         * @throws {Error} if the model is invalid
         */
        build(): string;

        /**
         * Generates a tree for the model.
         *
         * @return the abstract syntax tree, or NULL if the model is empty
         */
        buildTree(): SelectStmt;

        /**
         * Gets the set of data sources used in the model.
         *
         * @return the unique data source ids
         */
        getDatasourceIds(): string[];

        /**
         * Returns the list of columns in the generated SQL.
         *
         * @returns the list of columns
         */
        getSelectedColumnsAndTables(): any[];
    }

    /**
     * Manages the state of the visual query pages.
     */
    interface VisualQueryService {

        /**
         * Enums for types of relation joins.
         */
        JoinType: {
            JOIN: 0,
            JOIN_INNER: 1,
            JOIN_LEFT: 2,
            JOIN_RIGHT: 3
        }

        /**
         * Type of node.
         */
        NodeTag: {
            A_Expr: "A_Expr",
            BoolExpr: "BoolExpr",
            JoinExpr: "JoinExpr",
            RangeVar: "RangeVar"
        }

        /**
         * Stored model for the Visual Query page.
         */
        model: { selectedDatasourceId: string, visualQueryModel: VisualQueryModel, visualQuerySql: string };

        /**
         * Resets this model to default values.
         */
        resetModel(): void;

        /**
         * Creates a SQL builder from the specified model.
         *
         * @param model - the model
         * @returns the SQL builder
         */
        sqlBuilder(model: VisualQueryModel): SqlBuilder;
    }

    /**
     * Type of boolean expression.
     */
    type BoolExprType = 0;

    /**
     * Enums for types of relation joins.
     */
    type JoinType = 0 | 1 | 2 | 3;

    /**
     * Type of node.
     */
    type NodeTag = "A_Expr" | "BoolExpr" | "JoinExpr" | "RangeVar";

    /**
     * Model for the Visual Query page.
     */
    interface VisualQueryModel {

        /**
         * List of tables.
         */
        nodes: VisualQueryNode[];
    }

    /**
     * A table node.
     */
    interface VisualQueryNode {

        /**
         * Table information.
         */
        nodeAttributes: VisualQueryNodeAttributes;
    }

    /**
     * Attributes of a table node.
     */
    interface VisualQueryNodeAttributes {

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
    interface VisualQueryField {

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
    interface SelectStmt {

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
    interface A_Expr {

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
    interface BoolExpr {

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
    interface ColumnRef {

        /**
         * Field names
         */
        fields: string[];
    }

    /**
     * For SQL JOIN expressions.
     */
    interface JoinExpr {

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
    interface RangeVar {

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
    interface ResTarget {

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
}
