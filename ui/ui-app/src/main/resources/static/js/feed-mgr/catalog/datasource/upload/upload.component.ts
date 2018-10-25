import {HttpErrorResponse, HttpEvent, HttpEventType} from "@angular/common/http";
import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from "@angular/core";
import {TdDialogService} from "@covalent/core/dialogs";
import {Observable} from "rxjs/Observable";
import {of} from "rxjs/observable/of";
import {concatMap} from "rxjs/operators/concatMap";
import {finalize} from "rxjs/operators/finalize";
import {publishLast} from "rxjs/operators/publishLast";
import {refCount} from "rxjs/operators/refCount";
import {tap} from "rxjs/operators/tap";

import {SparkDataSet} from "../../../model/spark-data-set.model";
import {DataSetFile} from '../../api/models/dataset-file';
import {FileManagerService} from "../../api/services/file-manager.service";
import {FileUpload, FileUploadStatus} from "./models/file-upload";
import {UploadDataSource} from "./models/upload-dataset";
import {StateService} from "@uirouter/angular";
import {KyloRouterService} from "../../../../services/kylo-router.service";
import {RemoteFile} from "../files/remote-file";


export class UploadFilesChangeEvent {

    constructor(public isReady: boolean, public files: FileUpload[]) {
    }
}

/**
 * Provides a form for uploading files and managing uploaded files for a data set.
 */
@Component({
    selector: "local-files",
    styleUrls: ["./upload.component.scss"],
    templateUrl: "./upload.component.html"
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
    singleFile: boolean;

    @Input()
    renderContinueButton: boolean;

    @Input()
    displayInCard?:boolean = true;

    /**
     * Called when there is at least 1 valid uploaded file
     * @type {EventEmitter<FileUpload[]>}
     */
    @Output()
    onUploadFilesChange: EventEmitter<UploadFilesChangeEvent> = new EventEmitter<UploadFilesChangeEvent>();

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
    uploadDataSet: Observable<SparkDataSet>;

    /**
     * Loading flag for server operation to create datasetId
     */
    loading: boolean = false;



    constructor(private dialogs: TdDialogService, private fileManager: FileManagerService, private state:StateService, private kyloRouterService: KyloRouterService) {
    }

    public ngOnInit(): void {
        if (this.datasource.$uploadDataSet) {
            this.uploadDataSet = of(this.datasource.$uploadDataSet);
        }
        if(this.state.params && this.state.params.renderContinueButton){
            this.renderContinueButton = this.state.params.renderContinueButton;
        }

        if (this.datasource.$fileUploads) {
            // Read uploads cached locally in dataset
            this.files = this.datasource.$fileUploads;
            if(this.files && this.files.length >0){
                this.isReady = true;
            }
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
                if (this.uploadDataSet) {
                    this.uploadDataSet.pipe(
                        concatMap(ds => this.fileManager.listFiles(ds.id))
                    ).subscribe(files => this.setFiles(files));
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
    private ensureUploadDataSet(title: string): Observable<SparkDataSet> {

        if (typeof this.uploadDataSet === "undefined" || this.uploadDataSet === null) {
            this.loading = true;
            const tmpName = title+"_"+new Date().getTime();
            this.uploadDataSet = this.fileManager.createDataSet(this.datasource.id, tmpName).pipe(
                tap(ds => this.datasource.$uploadDataSet = ds),
                finalize(() => this.loading = false),
                publishLast(),
                refCount()
            );
        }
        return this.uploadDataSet;
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
            this.files.push(file);
            this.ensureUploadDataSet(file.name).pipe(
                concatMap(dataset => this.fileManager.uploadFile(dataset.id, event))
            ).subscribe(
                event => this.setStatus(file, event),
                error => this.setError(file, error),
                () => this.updateDataSet()
            );
        }
    }

    goBackToDatasourceList(){
        this.state.go("catalog.datasources");
    }

    goBack(){
        this.kyloRouterService.back("catalog.datasources");
    }



    /**
     * Deletes a file that has been uploaded.
     */
    private deleteFile(file: FileUpload): void {
        const isFailed = (file.status === FileUploadStatus.FAILED);
        this.uploadDataSet.pipe(
            concatMap(ds => this.fileManager.deleteFile(ds.id, file.name))
        ).subscribe(null,
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
        this.files = this.files.filter(item => item.name !== file.name);
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
        // Update data source
        this.datasource.$fileUploads = this.files;
        this.datasource.template.paths = this.files
            .filter(file => file.status === FileUploadStatus.SUCCESS)
            .map(file => file.path)
            .filter(path => path != null);
        if (this.datasource.$uploadDataSet) {
            this.datasource.$uploadDataSet.paths = this.datasource.template.paths;
        }
        this.isReady = (this.datasource.template.paths.length > 0);
        this.onUploadFilesChange.emit(new UploadFilesChangeEvent(this.isReady, this.files));
    }

    preview(){
        //convert the dataset files to RemoteFile objects
        let fileObjects:RemoteFile[] = this.files.map((file:FileUpload)=> {
            return new RemoteFile(file.name,file.path,false,file.size,new Date());
        });
        this.state.go("catalog.datasource.preview",{datasource:this.datasource,displayInCard:true, objectsToPreview:fileObjects});//, {location: "replace"});
    }
}
