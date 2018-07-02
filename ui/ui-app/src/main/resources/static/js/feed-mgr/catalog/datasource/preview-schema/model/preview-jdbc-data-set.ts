import {PreviewDataSet, SparkDataSet} from "./preview-data-set";
import {PreviewDataSetRequest} from "./preview-data-set-request";

/**
 * JDBC Data Set
 */
export class PreviewJdbcDataSet extends PreviewDataSet {

    public constructor(init?:Partial<PreviewJdbcDataSet>) {
        super(init);
        Object.assign(this, init);
        this.type = "JDBCDataSet"

    }
    public updateDisplayKey(){
        this.displayKey = this.getPreviewItemPath()
    }

    public  getPreviewItemPath() :string{
        if(this.items && this.items.length >0){
            var item :string = <string> this.items[0];
            var itemMap = {};
            item.split("&").forEach(v => {
                var arr = v.split("=");
                var obj:any = {};
                itemMap[arr[0]] = arr[1];
            });

            var schema = itemMap["catalog"] != undefined ? itemMap["catalog"] : itemMap["schema"];
            var table = itemMap["name"];
            return schema+"."+table;
        }
        //error
        return "";
    }

    public applyPreviewRequestProperties(previewRequest: PreviewDataSetRequest){
        super.applyPreviewRequestProperties(previewRequest);
        previewRequest.properties = {};
        previewRequest.properties.dbtable = previewRequest.previewItem;
    }

    public toSparkDataSet(): SparkDataSet {
        let sparkDataSet = super.toSparkDataSet();
        let path = this.getPreviewItemPath();
        sparkDataSet.options['dbtable'] = path;
        sparkDataSet.format = "jdbc"
        return sparkDataSet;
    }
}
