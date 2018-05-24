import {FileUpload} from "./file-upload";
import {DataSource} from '../../../api/models/datasource';

export interface UploadDataSource extends DataSource {

    $fileUploads: FileUpload[];
}
