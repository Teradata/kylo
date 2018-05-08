import {ConnectorTab} from "./connector-tab";
import {DataSetTemplate} from "./dataset-template";

export interface Connector {

    color?: string;

    hidden?: boolean;

    icon?: string;

    id?: string;

    tabs?: ConnectorTab[],

    template?: DataSetTemplate;

    title: string;
}
