import {Component, EventEmitter, Input, Output, ViewChild} from "@angular/core";


@Component({
    selector:'file-upload',
    templateUrl:'js/common/file-upload/file-upload.component.html'
})
export class FileUploadComponent {

    /**
     * Input for uploading files
     */
    @ViewChild("fileInput")
    fileInput: HTMLInputElement;

    @Input()
    files: File | FileList;

    @Input()
    validUpload:boolean = true;

    @ViewChild("submitButton")
    submitButton: HTMLInputElement;

    @Output()
    onSubmit :EventEmitter<File|FileList> = new EventEmitter<File|FileList>();

   constructor() { }


   submitFiles(){
       this.onSubmit.emit(this.files)
   }


   uploadButtonDisabled(){
       return !this.files || !this.validUpload
   }

}