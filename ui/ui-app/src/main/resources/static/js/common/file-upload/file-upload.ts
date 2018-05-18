import * as angular from "angular";
import * as _ from "underscore";
import {moduleName} from "../module-name";

export default class UploadFile implements ng.IComponentController {

    static readonly $inject = ["$scope", "$element", "$parse", "$attrs"];

    constructor(private $scope: IScope, 
                private $element: JQuery,
                private $parse: angular.IParseService,
                private $attrs: any) {

        var input = $(this.$element[0].querySelector('#fileInput'));
        var button = $(this.$element[0].querySelector('#uploadButton'));
        var textInput = $(this.$element[0].querySelector('#textInput'));

        this.$scope.isMultiple = 'multiple' in this.$attrs;
        if (this.$scope.isMultiple) {
            input.prop('multiple', true);
        }
        this.$scope.buttonText = this.$scope.isMultiple ? 'Choose files' : 'Choose file'

        var size = this.$attrs.inputSize;
        if (size != null) {
            try {
                size = parseInt(size);
                input.attr('size', size);
            } catch (e) {
            }
        }
        var model = this.$parse(this.$attrs.uploadFileModel);
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

            this.$scope.fileNames = files.map(f => f.name).join(', ');

            if (this.$scope.fileNames !== '') {
                button.removeClass('md-primary')
            } else {
                button.addClass('md-primary')
            }
            this.$scope.$apply(()=>{
                if(isModelArray) {
                    modelSetter(this.$scope, files);
                }
                else {
                    modelSetter(this.$scope, files[0]);
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
                '<input id="textInput" size="40" ng-model="fileNames" type="text" placeholder="{{\'views.file-upload.placeholder\' | translate}}" ng-readonly="true" style="margin-top: 20px;">' +
                '</md-input-container>'
});
