import {DataSetTemplate} from "./dataset-template";
import {Connector} from './connector';

export interface DataSource {

    id?: string;

    connector: Connector,

    title: string;
}
