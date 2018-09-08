import {SparkDataSet} from "../../../../model/spark-data-set.model";
import {PreviewDataSet} from "./preview-data-set";
import {PreviewDataSetRequest} from "./preview-data-set-request";
import {PreviewJdbcDataSet} from "./preview-jdbc-data-set";

/**
 * Hive Data Set
 */
export class PreviewHiveDataSet extends PreviewJdbcDataSet {

    public constructor(init?: Partial<PreviewHiveDataSet>) {
        super(init);
        Object.assign(this, init);
        this.type = "HiveDataSet"
    }


    public applyPreviewRequestProperties(previewRequest: PreviewDataSetRequest) {
        super.applyPreviewRequestProperties(previewRequest);
        previewRequest.properties = {};
        previewRequest.addPreviewItemToPath = true;

    }

    public toSparkDataSet(): SparkDataSet {
        let sparkDataSet = super.toSparkDataSet();
        sparkDataSet.format = "hive";
        sparkDataSet.paths = [];
        sparkDataSet.paths.push(this.getPreviewItemPath())
        return sparkDataSet;
    }

}
