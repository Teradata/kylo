import {TableColumn, TableViewModel} from "../model/table-view-model";
import {TransformResponse} from "../../../../visual-query/wrangler/model/transform-response";
import {Injectable} from "@angular/core";
import {QueryResultColumn} from "../../../../visual-query/wrangler/model/query-result-column";


@Injectable()
export class TransformResponseTableBuilder {


    constructor(){

    }

    /**
     * get the column feild to work work.   If the column has a hiveColumnLabel, then use that
     * @param {QueryResultColumn} column
     * @return {string}
     */
    columnField(column:QueryResultColumn) : string {
        if(column.hiveColumnLabel != undefined) {
            return column.hiveColumnLabel;
        }
        else {
            return column.field;
        }
    }

    buildTable(data:TransformResponse) : TableViewModel{
        let columns = data.results.columns.map((column: QueryResultColumn) => {
            return {name: this.columnField(column), label: column.displayName,numeric:TableViewModel.isNumeric(column.dataType), dataType:column.dataType, sortable:true}
        });
        let rows = data.results.rows.map((row: any) => {
            const data1 = {};
            for (let i = 0; i < data.results.columns.length; ++i) {
                let field = this.columnField(data.results.columns[i]);
                data1[field] = row[i];
            }
            return data1;
        });
        return new TableViewModel({columns:columns, rows:rows})
    }
}