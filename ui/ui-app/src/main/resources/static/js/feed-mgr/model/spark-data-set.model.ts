import {DataSource} from "../catalog/api/models/datasource";
import {Common} from "../../common/CommonTypes";
import {TableColumn} from "../catalog/datasource/preview-schema/model/table-view-model";

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

}