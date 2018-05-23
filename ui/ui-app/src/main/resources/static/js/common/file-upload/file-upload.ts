import * as angular from "angular";
import * as _ from "underscore";
import {moduleName} from "../module-name";

export default class UploadFile {

    isMultiple: any;
    buttonText: any;
    fileNames: any;

    static readonly $inject = ["$scope", "$element", "$parse", "$attrs"];

    constructor(private $scope: IScope, 
                private $element: JQuery,
                private $parse: angular.IParseService,
                private $attrs: any) {

        var input = $($element[0].querySelector('#fileInput'));
        var button = $($element[0].querySelector('#uploadButton'));
        var textInput = $($element[0].querySelector('#textInput'));

        this.isMultiple = 'multiple' in $attrs;
        if (this.isMultiple) {
            input.prop('multiple', true);
        }
        this.buttonText = this.isMultiple ? 'Choose files' : 'Choose file'

        var size = $attrs.inputSize;
        if (size != null) {
            try {
                size = parseInt(size);
                input.attr('size', size);
            } catch (e) {
            }
        }
        var model = $parse($attrs.uploadFileModel);
        var modelSetter = model.assign;
        var isModelArray = _.isArray(model);

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

            this.fileNames = files.map(f => f.name).join(', ');

            if (this.fileNames !== '') {
                button.removeClass('md-primary')
            } else {
                button.addClass('md-primary')
            }
            $scope.$apply(()=>{
                if(isModelArray) {
                    modelSetter($scope, files);
                }
                else {
                    modelSetter($scope, files[0]);
                }
            });
        });

    }

}

angular.module(moduleName).component('uploadFile', {
    controller: UploadFile,
    template: '<input id="fileInput" type="file" class="ng-hide">' +
                '<md-button id="uploadButton" class="md-raised md-primary" aria-label="attach_file">{{"views.file-upload.btn-Choose" | translate}}</md-button>' +
                '<md-input-container class="condensed-no-float" md-no-float  flex>' +
                '<input id="textInput" size="40" ng-model="$ctrl.fileNames" type="text" placeholder="{{\'views.file-upload.placeholder\' | translate}}" ng-readonly="true" style="margin-top: 20px;">' +
                '</md-input-container>'
});
