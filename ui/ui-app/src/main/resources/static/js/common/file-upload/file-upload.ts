import * as angular from "angular";
import {moduleName} from "../module-name";

angular.module(moduleName).directive('fileModel', ['$parse', ($parse: any)=> {
        return {
            restrict: 'A',
            link: function (scope: any, element: any, attrs: any) {
                var model = this.$parse(attrs.fileModel);
                var modelSetter = model.assign;

                element.bind('change',  ()=>{
                    scope.$apply(()=>{
                        modelSetter(scope, element[0].files[0]);
                    });
                });
            }
        };
    }
]);

angular.module(moduleName).directive('uploadFile', ['$parse', 
      ($parse: any) => {return {
            restrict: 'E',
            template: '<input id="fileInput" type="file" class="ng-hide"> <md-button id="uploadButton" class="md-raised md-primary" aria-label="attach_file">{{"views.file-upload.btn-Choose" | translate}} </md-button><md-input-container class="condensed-no-float" md-no-float  flex>    <input id="textInput" size="40" ng-model="fileName" type="text" placeholder="{{\'views.file-upload.placeholder\' | translate}}" ng-readonly="true" style="margin-top: 20px;"></md-input-container>',
            link: function (scope: any, element: any, attrs: any) {
                var input = $(element[0].querySelector('#fileInput'));
                var button = $(element[0].querySelector('#uploadButton'));
                var textInput = $(element[0].querySelector('#textInput'));

                var size = attrs.inputSize;
                if (size != null) {
                    try {
                        size = parseInt(size);
                        input.attr("size", size)
                    } catch (e) {
                    }
                }
                var model = this.$parse(attrs.uploadFileModel);
                var modelSetter = model.assign;

                if (input.length && button.length && textInput.length) {
                    button.click((e: any)=>{
                        input.click();
                    });
                    textInput.click((e: any)=> {
                        input.click();
                    });
                }

                input.on('change', (e: any)=>{
                    var files = e.target.files;
                    if (files[0]) {
                        scope.fileName = files[0].name;
                        button.removeClass("md-primary")
                    } else {
                        scope.fileName = null;
                        button.addClass("md-primary")
                    }
                    scope.$apply(()=>{
                        modelSetter(scope, files[0]);
                    });
                });
            }
        }
      }
   ]);