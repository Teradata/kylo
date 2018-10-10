import {Connector} from './connector';
import {DataSourceTemplate} from './datasource-template';

/**
 * DataSource knows how to connect to a source.
 * It has an instance of a Connector's DataSourceTemplate which is
 * configured with required properties, i.e. configured to connect to S3, JDBC, Kafka, Hive, etc
 */
export class DataSource {

    id: string;

    connector: Connector;

    roleMemberships: any;

    title: string;

    template: DataSourceTemplate;
}
