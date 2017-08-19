import {UserDatasource} from "../../model/user-datasource";
import VisualQueryService = VisualQueryServiceStatic.VisualQueryService;
import VisualQueryModel = VisualQueryServiceStatic.VisualQueryModel;
import SelectStmt = VisualQueryServiceStatic.SelectStmt;

/**
 * Parses a query and generates a transform script.
 */
export abstract class QueryParser {

    /**
     * Constructs a {@code QueryParser}.
     */
    constructor(protected VisualQueryService: VisualQueryService) {
    }

    /**
     * Generates a Spark script for the specified visual query model and data sources.
     *
     * @param source - the SQL query or visual query model
     * @param datasources - the list of datasources used
     * @returns the Spark script
     * @throws {Error} if the source or datasources are not valid
     */
    toScript(source: string | VisualQueryModel, datasources: UserDatasource[]) {
        if (typeof source === "string") {
            return this.fromSql(source, datasources);
        } else if (typeof source === "object") {
            return this.fromVisualQueryModel(source);
        }
    }

    /**
     * Generates a transform script for the specified select statement and data sources.
     *
     * @param tree - the select statement
     * @returns  the transform script
     * @throws {Error} if the tree is not valid
     */
    protected fromSelectStmt(tree: SelectStmt): string {
        throw new Error("method not supported");
    }

    /**
     * Generates a Spark script for the specified SQL query and optional data source.
     *
     * @param sql - the SQL query
     * @param datasources - the data source
     * @returns the transform script
     * @throws {Error} if there are too many data sources
     */
    protected abstract fromSql(sql: string, datasources: UserDatasource[]): string;

    /**
     * Generates a Spark script for the specified visual query model and data sources.
     *
     * @param visualQueryModel - the visual query model
     * @returns the transform script
     * @throws {Error} if the model is not valid
     */
    protected fromVisualQueryModel(visualQueryModel: VisualQueryModel): string {
        let tree = this.VisualQueryService.sqlBuilder(visualQueryModel).buildTree();
        return this.fromSelectStmt(tree);
    }
}
