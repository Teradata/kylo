import * as angular from "angular";
import * as _ from "underscore";
import {Component, Input, Output, EventEmitter, ElementRef} from "@angular/core";
import * as $ from "jquery";
import { ObjectUtils } from "../utils/object-utils";

@Component({
    selector: "upload-file",
    template: `<input id="fileInput" type="file" class="ng-hide">
                <button id="uploadButton" class="md-raised md-primary md-button md-kylo-theme md-ink-ripple" aria-label="attach_file">{{"views.file-upload.btn-Choose" | translate}}</button>
                <div class="md-input-container condensed-no-float md-input-has-placeholder md-kylo-theme flex">
                <input class="ng-pristine ng-untouched ng-valid md-input ng-empty" id="textInput" size="40" [(ngModel)]="fileNames" type="text" placeholder="{{\'views.file-upload.placeholder\' | translate}}" [readonly]="true">
                </div>`,
    styleUrls: ['js/common/file-upload/file-upload-style.css']
})
export class UploadFileComponent {

    @Output()
    uploadFileModelChange: EventEmitter<string> = new EventEmitter<string>();
    
    @Input()
    uploadFileModel: any;
    @Input() inputSize: any;

    isMultiple: any;
    buttonText: any;
    fileNames: any;

    constructor(private elRef: ElementRef) {}

    ngOnInit() {
        var input = $(this.elRef.nativeElement.querySelector('#fileInput'));
        var button = $(this.elRef.nativeElement.querySelector('#uploadButton'));
        var textInput = $(this.elRef.nativeElement.querySelector('#textInput'));

        this.isMultiple = 'multiple' in this;
        if (this.isMultiple) {
            input.prop('multiple', true);
        }
        this.buttonText = this.isMultiple ? 'Choose files' : 'Choose file'

        var size = this.inputSize;
        if (size != null) {
            try {
                size = parseInt(size);
                input.attr('size', size);
            } catch (e) {
            }
        }
        if(ObjectUtils.isDefined(this.uploadFileModel) && this.uploadFileModel != null){
            if(_.isArray(this.uploadFileModel)){
                this.fileNames = this.uploadFileModel.map((f :any) => f.name).join(', ');
            }
            else {
                this.fileNames = this.uploadFileModel.name;
            }
        }

        if (input.length && button.length && textInput.length) {
            button.click((e: any)=>{
                input.click();
            });
            textInput.click((e: any)=> {
                input.click();
            });
        }

        input.on('change', (e: any)=>{
            var files = _.values(e.target.files);

            this.fileNames = files.map((f:any) => f.name).join(', ');

            if (this.fileNames !== '') {
                button.removeClass('md-primary')
            } else {
                button.addClass('md-primary')
            }
            if(files && files.length >0) {
            
                if (_.isArray(this.uploadFileModel)) {
                    this.uploadFileModel = files;
                }
                else {
                    this.uploadFileModel = files[0];
                }

                this.uploadFileModelChange.emit(this.uploadFileModel);
            
            }
            else {
                this.uploadFileModel = null
            }

        });
    }

}
