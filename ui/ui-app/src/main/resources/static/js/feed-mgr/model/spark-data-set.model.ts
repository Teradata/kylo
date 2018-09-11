import {DataSource} from "../catalog/api/models/datasource";
import {Common} from "../../common/CommonTypes";
import {TableColumn, TableViewModel} from "../catalog/datasource/preview-schema/model/table-view-model";
import * as _ from 'underscore';
import {PreviewDataSet} from "../catalog/datasource/preview-schema/model/preview-data-set";
/**
 * DataSet used by the Data Wrangler
 */
export class SparkDataSet {
    public dataSource: DataSource;
    public id: string
    public format: string;
    public options: Common.Map<string>;
    public paths: string[];
    public schema:TableColumn[];
    public preview?:PreviewDataSet;
    public previewPath?:string;
    public previewLoading?:boolean;

    public constructor(init?:Partial<SparkDataSet>) {
        this.initialize();
        Object.assign(this, init);
        if(this.preview){
            this.preview = new PreviewDataSet(this.preview);
        }
    }
    initialize(){

    }

    hasPreview(){
        return this.preview != null && this.preview != undefined && this.preview.preview && _.isFunction(this.preview.preview.hasColumns) && this.preview.preview.hasColumns();
    }

    /**
     * resolve the path for the dataset
     * Optionally remove the last entry
     * @param {boolean} removeLast
     * @return {string}
     */
    resolvePath(removeLast ?:boolean){
        let path = '';
        if(this.paths){
            path = this.paths.join(",");
        }
        else if(this.options && this.options["path"]){
            path = this.options["path"];
        }
        else {
            return this.id;
        }
        if(removeLast){
            return path.substring(0,path.lastIndexOf("/"));
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
    resolvePaths():string[]{
        let paths :string[] = [];
        if(this.paths){
            paths = this.paths;
        }
        else if(this.options && this.options["path"]){
            paths = [this.options["path"]];
        }
        else if(this.options['dbtable']) {
            paths = [this.options["dbtable"]];
        }
        return paths;
    }

    getDisplayIdentifier(){
        if(this.format == "hive" || this.format == "jdbc" && this.id.indexOf(".") >0){
            return this.getSchemaName()+"."+this.getTableName();
        }
        else {
            return this.id;
        }
    }
    getTableName(){
        if(this.format == "hive" || this.format == "jdbc" && this.id.indexOf(".") >0){
            return this.id.substring(this.id.lastIndexOf(".")+1);
        }
        else {
            return this.id
        }
    }
    getSchemaName(){
        if(this.format == "hive" || this.format == "jdbc" && this.id.indexOf(".") >0){
            return this.id.substring(0,this.id.lastIndexOf("."));
        } else {
            return this.id
        }
    }

}