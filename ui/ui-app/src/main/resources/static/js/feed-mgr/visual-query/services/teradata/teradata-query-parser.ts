import {UserDatasource} from "../../../model/user-datasource";
import {QueryParser} from "../query-parser";
import {UnderscoreStatic} from "underscore";
import {SqlDialect, VisualQueryModel} from "../../../services/VisualQueryService";

declare const _: UnderscoreStatic;

/**
 * Handles transformations from a visual query model to Teradata.
 */
export class TeradataQueryParser extends QueryParser {

    /**
     * Constructs a {@code SparkQueryParser}.
     */
    constructor(VisualQueryService: any) {
        super(VisualQueryService);
    }

    /**
     * Generates a Spark script for the specified SQL query and optional data source.
     *
     * @param  sql - the SQL query
     * @param datasources - the data source
     * @returns the Spark script
     * @throws {Error} if there are too many data sources
     */
    protected fromSql(sql: string, datasources: UserDatasource[]): string {
        return sql;
    }

    /**
     * Generates a Spark script for the specified visual query model and data sources.
     *
     * @param visualQueryModel - the visual query model
     */
    protected fromVisualQueryModel(visualQueryModel: VisualQueryModel): string {
        return this.VisualQueryService.sqlBuilder(visualQueryModel, SqlDialect.TERADATA).build();
    }
}
