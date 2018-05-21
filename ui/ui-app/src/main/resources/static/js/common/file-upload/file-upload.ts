import * as angular from "angular";
import * as _ from "underscore";
import {moduleName} from "../module-name";

angular.module(moduleName).directive('uploadFile', ['$parse', 
      ($parse: any) => {return {
            restrict: 'E',
             scope: {uploadFileModel: '='
              },
            template: '<input id="fileInput" type="file" class="ng-hide">' +
                      '<md-button id="uploadButton" class="md-raised md-primary" aria-label="attach_file">{{"views.file-upload.btn-Choose" | translate}}</md-button>' +
                      '<md-input-container class="condensed-no-float" md-no-float  flex>' +
                        '<input id="textInput" size="40" ng-model="fileNames" type="text" placeholder="{{\'views.file-upload.placeholder\' | translate}}" ng-readonly="true" style="margin-top: 20px;">' +
                      '</md-input-container>',
            link: function (scope: any, element: any, attrs: any) {
                var input = $(element[0].querySelector('#fileInput'));
                var button = $(element[0].querySelector('#uploadButton'));
                var textInput = $(element[0].querySelector('#textInput'));

                scope.isMultiple = 'multiple' in attrs;
                if (scope.isMultiple) {
                    input.prop('multiple', true);
                }
                scope.buttonText = scope.isMultiple ? 'Choose files' : 'Choose file'

                var size = attrs.inputSize;
                if (size != null) {
                    try {
                        size = parseInt(size);
                        input.attr('size', size);
                    } catch (e) {
                    }
                }
                if(angular.isDefined(scope.uploadFileModel) && scope.uploadFileModel != null){
                    if(_.isArray(scope.uploadFileModel)){
                        scope.fileNames = scope.uploadFileModel.map((f :any) => f.name).join(', ');
                    }
                    else {
                        scope.fileNames = scope.uploadFileModel.name;
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

                    scope.fileNames = files.map((f:any) => f.name).join(', ');

                    if (scope.fileNames !== '') {
                        button.removeClass('md-primary')
                    } else {
                        button.addClass('md-primary')
                    }
                    if(files && files.length >0) {
                       // scope.$apply(() => {
                            if (_.isArray(scope.uploadFileModel)) {
                                scope.uploadFileModel = files;
                            }
                            else {
                                scope.uploadFileModel = files[0];
                            }
                      //  });
                    }
                    else {
                        scope.uploadFileModel = null
                    }

                });
            }
        }
      }
   ]);
