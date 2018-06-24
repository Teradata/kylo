import * as angular from "angular";
import * as _ from "underscore";
import {moduleName} from "../module-name";

export default class UploadFile {

    uploadFileModel: any;
    isMultiple: any;
    buttonText: any;
    fileNames: any;

    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {
        var input = $(this.$element[0].querySelector('#fileInput'));
        var button = $(this.$element[0].querySelector('#uploadButton'));
        var textInput = $(this.$element[0].querySelector('#textInput'));

        this.isMultiple = 'multiple' in this.$attrs;
        if (this.isMultiple) {
            input.prop('multiple', true);
        }
        this.buttonText = this.isMultiple ? 'Choose files' : 'Choose file'

        var size = this.$attrs.inputSize;
        if (size != null) {
            try {
                size = parseInt(size);
                input.attr('size', size);
            } catch (e) {
            }
        }
        if(angular.isDefined(this.uploadFileModel) && this.uploadFileModel != null){
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
            
            }
            else {
                this.uploadFileModel = null
            }

        });
    }

    static readonly $inject = ["$element", "$parse", "$attrs"];

    constructor(private $element: JQuery,
                private $parse: angular.IParseService,
                private $attrs: any) {

        
        
    }
}

angular.module(moduleName).component('uploadFile', {
    controller: UploadFile,
	bindings: {
		uploadFileModel: '='
    },
    template: '<input id="fileInput" type="file" class="ng-hide">' +
                      '<md-button id="uploadButton" class="md-raised md-primary" aria-label="attach_file">{{"views.file-upload.btn-Choose" | translate}}</md-button>' +
                      '<md-input-container class="condensed-no-float" md-no-float  flex>' +
                        '<input id="textInput" size="40" ng-model="$ctrl.fileNames" type="text" placeholder="{{\'views.file-upload.placeholder\' | translate}}" ng-readonly="true" style="margin-top: 20px;">' +
                      '</md-input-container>'
});
