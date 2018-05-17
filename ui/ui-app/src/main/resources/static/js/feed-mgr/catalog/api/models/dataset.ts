import {DataSource} from "./datasource";
import {DataSetTemplate} from "./dataset-template";

export interface DataSet extends DataSetTemplate {

    id: string;

    datasource?: DataSource;

    connectorId: string;
}
