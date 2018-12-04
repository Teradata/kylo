import {Component, Inject, OnDestroy, OnInit} from "@angular/core";
import {MatDialogRef} from "@angular/material/dialog";
import {FileUpload} from "../../../../../../services/FileUploadService";
import {MAT_DIALOG_DATA} from "@angular/material/dialog";
import {RestUrlConstants} from "../../../../../services/RestUrlConstants";
import {MatSnackBar} from "@angular/material/snack-bar";

export class FeedUploadFileDialogComponentData{
    constructor(public feedId:string){

    }
}

@Component({
    selector:"feed-upload-file-dialog",
    templateUrl: "./feed-upload-file-dialog.component.html"
})
export class FeedUploadFileDialogComponent implements OnInit, OnDestroy{

    constructor(private dialog: MatDialogRef<FeedUploadFileDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: FeedUploadFileDialogComponentData, @Inject("FileUpload")private fileUploadService:FileUpload, private snackBar:MatSnackBar) {
    }

    uploading:boolean;
    errorMessage:string = '';

    ngOnInit() {

    }
    ngOnDestroy(){

    }

    /**
     * callback after a user selects a file from the local file system
     */
    onUploadFiles(files:File|FileList) {
       this.doUpload(<File>files);
    }

    doUpload(file:File) {
        this.errorMessage = '';
        this.uploading = true;
        var uploadUrl = RestUrlConstants.UPLOAD_FILE_FEED_URL(this.data.feedId);
        var params = {};
        var successFn = (response:any) => {
           this.uploading = false;
            this.cancel();
            this.snackBar.open("Uploaded the file", null, {
                duration: 5000,
            });
        }
        var errorFn = (response:any) => {
            let message = 'Failed to upload file.';
            if(response && response.data && response.data.message) {
                message += " "+response.data.message;
            }
            this.uploading=false;
            this.errorMessage = message ;
        }
        this.fileUploadService.uploadFileToUrl(file, uploadUrl, successFn, errorFn, params);
    };


    /**
     * Cancel this dialog.
     */
    cancel() {
        this.dialog.close();
    }


}