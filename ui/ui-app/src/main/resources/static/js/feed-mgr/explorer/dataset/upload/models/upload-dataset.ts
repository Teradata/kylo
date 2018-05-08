import {DataSet} from "../../../catalog/models/dataset";
import {FileUpload} from "./file-upload";

export interface UploadDataSet extends DataSet {

    $fileUploads: FileUpload[];
}
