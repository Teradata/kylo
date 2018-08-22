import {Component, Input, ViewChild} from "@angular/core";


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

    constructor() {

    }
}