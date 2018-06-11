import {TableViewModel} from "../model/table-view-model";
import {TransformResponse} from "../../../../visual-query/wrangler/model/transform-response";
import {Injectable} from "@angular/core";


@Injectable()
export class TransformResponseTableBuilder {


    constructor(){

    }

    buildTable(data:TransformResponse) : TableViewModel{
        let columns = data.results.columns.map((column: any) => {
            return {name: column.field, label: column.displayName, dataType:column.dataType}
        });
        let rows = data.results.rows.map((row: any) => {
            const data1 = {};
            for (let i = 0; i < data.results.columns.length; ++i) {
                data1[data.results.columns[i].field] = row[i];
            }
            return data1;
        });
        return new TableViewModel({columns:columns, rows:rows})
    }
}