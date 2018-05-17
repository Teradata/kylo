import {Connector} from './connector';
import {DataSourceTemplate} from './datasource-template';

export interface DataSource extends DataSourceTemplate {

    id?: string;

    connector: Connector,

    title: string;
}
