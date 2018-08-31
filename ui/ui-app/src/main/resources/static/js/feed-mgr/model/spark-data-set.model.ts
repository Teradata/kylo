import {DataSource} from "../catalog/api/models/datasource";
import {Common} from "../../common/CommonTypes";
import {TableColumn, TableViewModel} from "../catalog/datasource/preview-schema/model/table-view-model";

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
    public preview?:TableViewModel;
    public previewPath?:string;

    public constructor(init?:Partial<SparkDataSet>) {
        this.initialize();
        Object.assign(this, init);
    }
    initialize(){

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

}