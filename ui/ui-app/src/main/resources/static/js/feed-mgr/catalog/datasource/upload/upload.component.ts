import {HttpErrorResponse, HttpEvent, HttpEventType} from "@angular/common/http";
import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from "@angular/core";
import {TdDialogService} from "@covalent/core/dialogs";

import {FileManagerService} from "../../api/services/file-manager.service";
import {FileUpload, FileUploadStatus} from "./models/file-upload";
import {UploadDataSource} from "./models/upload-dataset";
import {DataSetFile} from '../../api/models/dataset-file';
import {Observable} from "rxjs/Observable";
import {map} from "rxjs/operators";


export class UploadFilesChangeEvent {

    constructor(public isReady: boolean, public files: FileUpload[]) {
    }
}


/**
 * Provides a form for uploading files and managing uploaded files for a data set.
 */
@Component({
    selector: "local-files",
    styleUrls: ["js/feed-mgr/catalog/datasource/upload/upload.component.css"],
    templateUrl: "js/feed-mgr/catalog/datasource/upload/upload.component.html"
})
export class UploadComponent implements OnInit {

    /**
     * Dataset for uploaded files
     */
    @Input()
    public datasource: UploadDataSource;

    /**
     * Input for uploading files
     */
    @ViewChild("fileInput")
    fileInput: HTMLInputElement;

    /**
     * Only allow 1 file to be uploaded at a time
     */
    @Input()
    singleFile:boolean;

    @Input()
    renderContinueButton:boolean;

    /**
     * Called when there is at least 1 valid uploaded file
     * @type {EventEmitter<FileUpload[]>}
     */
    @Output()
    onUploadFilesChange:EventEmitter<UploadFilesChangeEvent> = new EventEmitter<UploadFilesChangeEvent>();

    /**
     * Uploads pending, in-progress, failed, and successful
     */
    files: FileUpload[] = [];

    /**
     * Indicates at least one upload is successful
     */
    isReady = false;

    /**
     * The dataset to be used for this upload set
     */
    uploadDataSetId:string;

    /**
     * Loading flag for server operation to create datasetId
     */
    loading: boolean = false;

    constructor(private dialogs: TdDialogService, private fileManager: FileManagerService ) {
    }

    public ngOnInit(): void {
        if (this.datasource.$fileUploads) {
            // Read uploads cached locally in dataset
            this.files = this.datasource.$fileUploads;
        } else {
            this.datasource.$fileUploads = this.files;

            // Parse uploads from dataset paths and server
            if (this.datasource.template && this.datasource.template.paths) {
               /** this.files = this.datasource.template.paths.map(path => {
                    const name = path.substr(path.lastIndexOf("/") + 1);
                    const file = new FileUpload(name);
                    file.path = path;
                    file.status = FileUploadStatus.SUCCESS;
                    return file;
                });
                **/
               if(this.uploadDataSetId) {
                   this.fileManager.listFiles(this.uploadDataSetId)
                       .subscribe(files => this.setFiles(files));
               }
            }
        }
    }

    /**
     * Cancels an upload and removes the path from the dataset.
     */
    cancelFile(file: FileUpload): void {
        // Cancel upload
        if (file.upload && !file.upload.closed) {
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
     * Create a temp dataset id for this upload routine
     * @return {Observable<string>}
     */
    private ensureUploadDataSet():Observable<string>{
        if(this.uploadDataSetId){
            return Observable.of(this.uploadDataSetId);
        }
        else {
            this.loading = true;
            return this.fileManager.createDataSet(this.datasource.id).pipe(map((ds:any)=> { this.uploadDataSetId = ds.id; this.loading=false;return <string>ds.id }));
        }
    }

    /**
     * Uploads a file or list of files
     */
    upload(event: FileList | File) {
        if (event instanceof FileList) {
            // Upload files individually
            this.ensureUploadDataSet().subscribe((datasetId:string) => {
                for (let i = 0; i < event.length; ++i) {
                    this.upload(event.item(i));
                }
            });

        } else if (this.files.find(file => file.name === event.name)) {
            this.dialogs.openAlert({
                message: "File already exists."
            });
        } else {
            // Upload single file
            const file = new FileUpload(event.name);
            this.files.push(file);
            this.ensureUploadDataSet().subscribe((datasetId:string) => {
                file.upload = this.fileManager.uploadFile(datasetId, event)
                    .subscribe(event => this.setStatus(file, event), error => this.setError(file, error));
            });

        }
    }

    /**
     * Deletes a file that has been uploaded.
     */
    private deleteFile(file: FileUpload): void {
        const isFailed = (file.status === FileUploadStatus.FAILED);
        this.fileManager.deleteFile(this.uploadDataSetId, file.name)
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
        this.datasource.$fileUploads = this.files;
        this.datasource.template.paths = this.files
            .filter(file => file.status === FileUploadStatus.SUCCESS)
            .map(file => file.path)
            .filter(path => path != null);
        this.isReady = (this.datasource.template.paths.length > 0);
        this.onUploadFilesChange.emit(new UploadFilesChangeEvent(this.isReady,this.files));

    }
}
