import {DataSource} from './datasource';
import {TableColumn} from '../../datasource/preview-schema/model/table-view-model';

export class Dataset {
    title: string;
    description: string;
    tags: any[];
    id: string;
    datasource: DataSource;
    schema: TableColumn[];
    preview: any;
    lineage: any;
    queries: any;
}