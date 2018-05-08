import {HttpErrorResponse, HttpEvent, HttpEventType} from "@angular/common/http";
import {Component, Input, OnInit, ViewChild} from "@angular/core";
import {TdDialogService} from "@covalent/core/dialogs";

import {DataSetFile} from "../../catalog/models/dataset-file";
import {FileManagerService} from "../../catalog/services/file-manager.service";
import {FileUpload, FileUploadStatus} from "./models/file-upload";
import {UploadDataSet} from "./models/upload-dataset";

/**
 * Provides a form for uploading files and managing uploaded files for a data set.
 */
@Component({
    selector: "local-files",
    styleUrls: ["js/feed-mgr/explorer/dataset/upload/upload.component.css"],
    templateUrl: "js/feed-mgr/explorer/dataset/upload/upload.component.html"
})
export class UploadComponent implements OnInit {

    /**
     * Dataset for uploaded files
     */
    @Input()
    public dataSet: UploadDataSet;

    /**
     * Input for uploading files
     */
    @ViewChild("fileInput")
    fileInput: HTMLInputElement;

    /**
     * Uploads pending, in-progress, failed, and successful
     */
    files: FileUpload[] = [];

    /**
     * Indicates at least one upload is successful
     */
    isReady = false;

    constructor(private dialogs: TdDialogService, private fileManager: FileManagerService) {
    }

    public ngOnInit(): void {
        if (this.dataSet.$fileUploads) {
            // Read uploads cached locally in dataset
            this.files = this.dataSet.$fileUploads;
        } else {
            this.dataSet.$fileUploads = this.files;

            // Parse uploads from dataset paths and server
            if (this.dataSet.paths) {
                this.files = this.dataSet.paths.map(path => {
                    const name = path.substr(path.lastIndexOf("/") + 1);
                    const file = new FileUpload(name);
                    file.path = path;
                    file.status = FileUploadStatus.SUCCESS;
                    return file;
                });
                this.fileManager.listFiles(this.dataSet.id)
                    .subscribe(files => this.setFiles(files));
            }
        }
    }

    /**
     * Cancels an upload and removes the path from the dataset.
     */
    cancelFile(file: FileUpload): void {
        // Cancel upload
        if (file.upload) {
            file.upload.unsubscribe();
        }

        // Delete server file
        if (file.status === FileUploadStatus.SUCCESS) {
            this.dialogs.openConfirm({
                message: `Are you sure you want to delete ${file.name}?`,
                acceptButton: "Delete"
            }).afterClosed().subscribe((accept: boolean) => this.deleteFile(file));
        } else {
            this.deleteFile(file);
        }
    }

    /**
     * Uploads a file or list of files
     */
    upload(event: FileList | File) {
        if (event instanceof FileList) {
            // Upload files individually
            for (let i = 0; i < event.length; ++i) {
                this.upload(event.item(i));
            }
        } else if (this.files.find(file => file.name === event.name)) {
            this.dialogs.openAlert({
                message: "File already exists."
            });
        } else {
            // Upload single file
            const file = new FileUpload(event.name);
            file.upload = this.fileManager.uploadFile(this.dataSet.id, event)
                .subscribe(event => this.setStatus(file, event), error => this.setError(file, error));
            this.files.push(file);
        }
    }

    /**
     * Deletes a file that has been uploaded.
     */
    private deleteFile(file: FileUpload): void {
        const isFailed = (file.status === FileUploadStatus.FAILED);
        this.fileManager.deleteFile(this.dataSet.id, file.name)
            .subscribe(null,
                error => {
                    if (isFailed) {
                        this.removeFile(file);
                    } else {
                        this.setError(file, error)
                    }
                },
                () => this.removeFile(file));
    }

    /**
     * Removes a file from the file list and dataset.
     */
    private removeFile(file: FileUpload) {
        this.files = this.files.filter(item => item.path !== file.path);
        this.updateDataSet();
    }

    /**
     * Sets an error message for a file upload.
     */
    // noinspection JSMethodCanBeStatic
    private setError(file: FileUpload, error: HttpErrorResponse) {
        file.error = (error.error && error.error.message) ? error.error.message : error.message;
        file.status = FileUploadStatus.FAILED;
        this.updateDataSet();
    }

    /**
     * Associates the specified dataset paths with the file uploads.
     */
    private setFiles(dataSetFiles: DataSetFile[]) {
        // Map paths to file uploads
        const fileMap = new Map<string, FileUpload>();
        this.files
            .filter(file => file.status === FileUploadStatus.SUCCESS)
            .filter(file => file.path != null)
            .forEach(file => fileMap.set(file.path, file));

        // Associate with dataset paths
        dataSetFiles.forEach(dataSetFile => {
            const fileUpload = fileMap.get(dataSetFile.path);
            if (fileUpload) {
                fileUpload.setFile(dataSetFile);
            }
        });
    }

    /**
     * Updates the specified file upload.
     */
    private setStatus(file: FileUpload, event: HttpEvent<DataSetFile>) {
        if (event.type === HttpEventType.UploadProgress) {
            file.progress = event.loaded / event.total;
            file.size = event.total;
        } else if (event.type === HttpEventType.Response) {
            file.setFile(event.body);
            this.updateDataSet();
        }
    }

    /**
     * Updates the dataset paths.
     */
    private updateDataSet() {
        this.dataSet.$fileUploads = this.files;
        this.dataSet.paths = this.files
            .filter(file => file.status === FileUploadStatus.SUCCESS)
            .map(file => file.path)
            .filter(path => path != null);
        this.isReady = (this.dataSet.paths.length > 0);
    }
}
