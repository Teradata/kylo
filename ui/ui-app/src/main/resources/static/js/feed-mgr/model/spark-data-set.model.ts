import * as _ from 'underscore';

import {DataSource} from "../catalog/api/models/datasource";
import {DataSourceTemplate} from "../catalog/api/models/datasource-template";
import {PreviewDataSet} from "../catalog/datasource/preview-schema/model/preview-data-set";
import {TableColumn} from "../catalog/datasource/preview-schema/model/table-view-model";
import {Common} from '../../../lib/common/CommonTypes';

/**
 * DataSet used by the Data Wrangler
 */
export class SparkDataSet {
    public dataSource: DataSource;
    public id: string
    public title:string
    public format: string;
    public options: Common.Map<string>;
    public paths: string[];
    public schema: TableColumn[];
    public preview?: PreviewDataSet;
    public previewPath?: string;
    public previewLoading?: boolean;
    public isUpload:boolean = false;

    public constructor(init?: Partial<SparkDataSet>) {
        this.initialize();
        Object.assign(this, init);
        if (this.preview) {
            this.preview = new PreviewDataSet(this.preview);
        }
    }

    initialize() {

    }

    hasPreview() {
        return this.preview != null && this.preview != undefined && this.preview.preview && _.isFunction(this.preview.preview.hasColumns) && this.preview.preview.hasColumns();
    }

    /**
     * resolve the path for the dataset
     * Optionally remove the last entry
     * @param {boolean} removeLast
     * @return {string}
     */
    resolvePath(removeLast ?: boolean) {
        let path = '';
        if (this.paths) {
            path = this.paths.join(",");
        }
        else if (this.options && this.options["path"]) {
            path = this.options["path"];
        }
        else {
            return this._getIdentifier();
        }
        if (removeLast) {
            return path.substring(0, path.lastIndexOf("/"));
        }
        else {
            return path;
        }
    }

    /**
     * return an array of all the paths used for this dataset.
     * for file based it will return the files used.
     * for database it will return the schema.table
     *
     * @return {string[]}
     */
    resolvePaths(): string[] {
        let paths: string[] = [];
        if (this.paths) {
            paths = this.paths;
        }
        else if (this.options && this.options["path"]) {
            paths = [this.options["path"]];
        }
        else if (this.options['dbtable']) {
            paths = [this.options["dbtable"]];
        }
        return paths;
    }

    getDisplayIdentifier() {
        let identifier =this._getIdentifier()
        if (this.format == "hive" || this.format == "jdbc" && identifier.indexOf(".") > 0) {
            return this.getSchemaName() + "." + this.getTableName();
        }
        else {
            return identifier;
        }
    }

    getTableName() {
        let identifier =this._getIdentifier()
        if (this.format == "hive" || this.format == "jdbc" && identifier.indexOf(".") > 0) {
            return identifier.substring(identifier.lastIndexOf(".") + 1);
        }
        else {
            return identifier
        }
    }

    getSchemaName() {
        let identifier =this._getIdentifier()
        if (this.format == "hive" || this.format == "jdbc" && identifier.indexOf(".") > 0) {

            return identifier.substring(0, identifier.lastIndexOf("."));
        } else {
            return identifier
        }
    }

    _getIdentifier(){
        return  this.title != undefined ? this.title : this.id;
    }

    mergeTemplates(): DataSourceTemplate {
        const template: DataSourceTemplate = {
            files: [], format: this.format, jars: [], options: {...this.options}, paths: (this.paths == null && this.previewPath != null) ? [this.previewPath] : this.paths
        };

        if (this.dataSource) {
            if (this.dataSource.template) {
                template.files = [...(this.dataSource.template.files || []), ...template.files];
                template.format = (template.format == null) ? this.dataSource.template.format : template.format;
                template.jars = [...(this.dataSource.template.jars || []), ...template.jars];
                template.options = {...this.dataSource.template.options, ...template.options};
                template.paths = (template.paths == null) ? this.dataSource.template.paths : template.paths;
            }
            if (this.dataSource.connector && this.dataSource.connector.template) {
                template.files = [...(this.dataSource.connector.template.files || []), ...template.files];
                template.format = (template.format == null) ? this.dataSource.connector.template.format : template.format;
                template.jars = [...(this.dataSource.connector.template.jars || []), ...template.jars];
                template.options = {...this.dataSource.connector.template.options, ...template.options};
                template.paths = (template.paths == null) ? this.dataSource.connector.template.paths : template.paths;
            }
        }

        return template;
    }
}
