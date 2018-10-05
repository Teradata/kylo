import {SparkDataSet} from "../../../../model/spark-data-set.model";
import {PreviewDataSet} from "./preview-data-set";
import {PreviewFileDataSet} from "./preview-file-data-set";

export class PreviewUploadDataSet extends PreviewFileDataSet {

    constructor(private init: Partial<PreviewDataSet>, private sparkDataSet: SparkDataSet) {
        super(init);
    }

    toSparkDataSet(): SparkDataSet {
        const dataSet = super.toSparkDataSet();
        dataSet.id = this.sparkDataSet.id;
        dataSet.paths = this.sparkDataSet.paths;
        return dataSet;
    }
}
