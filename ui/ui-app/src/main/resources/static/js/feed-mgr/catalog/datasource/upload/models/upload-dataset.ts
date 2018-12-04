import {SparkDataSet} from "../../../../model/spark-data-set.model";
import {DataSource} from '../../../api/models/datasource';
import {FileUpload} from "./file-upload";

export interface UploadDataSource extends DataSource {

    $fileUploads: FileUpload[];

    $uploadDataSet: SparkDataSet;
}
