import {Connector} from "./connector";
import {DataSetTemplate} from "./dataset-template";

export interface DataSet extends DataSetTemplate {

    id: string;

    connector?: Connector;

    connectorId: string;
}
