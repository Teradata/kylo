import {Subscription} from "rxjs/Subscription";
import {DataSetFile} from "../../../api/models/dataset-file";

export class FileUpload {

    error: string;

    name: string;

    path: string;

    progress = 0;

    size: number;

    status: FileUploadStatus = FileUploadStatus.PENDING;

    upload: Subscription;

    constructor(file: DataSetFile | string) {
        if (typeof file === "string") {
            this.name = file;
        }
        else {
            this.setFile(file);
        }
    }

    get buttonIcon() {
        if (this.status === FileUploadStatus.SUCCESS) {
            return "delete";
        } else {
            return "cancel";
        }
    }

    get listIcon() {
        if (this.status === FileUploadStatus.FAILED) {
            return "error";
        } else if (this.status === FileUploadStatus.SUCCESS) {
            return "check_circle";
        } else {
            return "file_upload";
        }
    }

    get listIconClass() {
        if (this.status === FileUploadStatus.FAILED) {
            return "mat-warn";
        } else if (this.status === FileUploadStatus.SUCCESS) {
            return "icon-success";
        } else {
            return "icon-pending";
        }
    }

    setFile(file: DataSetFile) {
        this.error = null;
        this.name = file.name;
        this.path = file.path;
        this.size = file.length;
        this.status = FileUploadStatus.SUCCESS;
    }
}

export enum FileUploadStatus {

    PENDING = 0,
    SUCCESS = 1,
    FAILED = 2
}
