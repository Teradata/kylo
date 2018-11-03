import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild} from "@angular/core";


@Component({
    selector:'file-upload',
    templateUrl:'./file-upload.component.html'
})
export class FileUploadComponent implements OnChanges{

    /**
     * Input for uploading files
     */
    @ViewChild("fileInput")
    fileInput: HTMLInputElement;

    @Input()
    files: File | FileList;

    @Input()
    validUpload:boolean = true;

    @Input()
    placeholder?:string = "Select or drop files here"

    @ViewChild("submitButton")
    submitButton: HTMLInputElement;

    @Output()
    filesChange = new EventEmitter< File | FileList>()

    @Output()
    onSubmit :EventEmitter<File|FileList> = new EventEmitter<File|FileList>();

    @Input()
    renderSubmitButton?:boolean = true;

    @Output()
    onCancel: EventEmitter<any> = new EventEmitter<any>();

   constructor() { }


   submitFiles(){
       this.onSubmit.emit(this.files)
   }


   uploadButtonDisabled(){
       return !this.files || !this.validUpload
   }

   ngOnChanges(changes:SimpleChanges){
      if(changes.files.currentValue){
          this.filesChange.emit(this.files);
      }
   }

   fileSelected(event:File | FileList) {
       this.filesChange.emit(event);
   }

    getInputValue(): any {
        return this.files ? (this.files instanceof FileList ? (this.files.length + ' files') : this.files.name) : null;
    }

}
