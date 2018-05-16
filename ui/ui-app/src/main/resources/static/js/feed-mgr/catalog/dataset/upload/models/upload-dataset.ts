import {DataSet} from "../../../api/models/dataset";
import {FileUpload} from "./file-upload";

export interface UploadDataSet extends DataSet {

    $fileUploads: FileUpload[];
}
