define(["require", "exports", "angular", "underscore", "../module-name"], function (require, exports, angular, _, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    angular.module(module_name_1.moduleName).directive('uploadFile', ['$parse',
        function ($parse) {
            return {
                restrict: 'E',
                template: '<input id="fileInput" type="file" class="ng-hide">' +
                    '<md-button id="uploadButton" class="md-raised md-primary" aria-label="attach_file">{{"views.file-upload.btn-Choose" | translate}}</md-button>' +
                    '<md-input-container class="condensed-no-float" md-no-float  flex>' +
                    '<input id="textInput" size="40" ng-model="fileNames" type="text" placeholder="{{\'views.file-upload.placeholder\' | translate}}" ng-readonly="true" style="margin-top: 20px;">' +
                    '</md-input-container>',
                link: function (scope, element, attrs) {
                    var input = $(element[0].querySelector('#fileInput'));
                    var button = $(element[0].querySelector('#uploadButton'));
                    var textInput = $(element[0].querySelector('#textInput'));
                    scope.isMultiple = 'multiple' in attrs;
                    if (scope.isMultiple) {
                        input.prop('multiple', true);
                    }
                    scope.buttonText = scope.isMultiple ? 'Choose files' : 'Choose file';
                    var size = attrs.inputSize;
                    if (size != null) {
                        try {
                            size = parseInt(size);
                            input.attr('size', size);
                        }
                        catch (e) {
                        }
                    }
                    var model = $parse(attrs.uploadFileModel);
                    var modelSetter = model.assign;
                    var isModelArray = _.isArray(model);
                    if (input.length && button.length && textInput.length) {
                        button.click(function (e) {
                            input.click();
                        });
                        textInput.click(function (e) {
                            input.click();
                        });
                    }
                    input.on('change', function (e) {
                        var files = _.values(e.target.files);
                        scope.fileNames = files.map(function (f) { return f.name; }).join(', ');
                        if (scope.fileNames !== '') {
                            button.removeClass('md-primary');
                        }
                        else {
                            button.addClass('md-primary');
                        }
                        scope.$apply(function () {
                            if (isModelArray) {
                                modelSetter(scope, files);
                            }
                            else {
                                modelSetter(scope, files[0]);
                            }
                        });
                    });
                }
            };
        }
    ]);
});
//# sourceMappingURL=file-upload.js.map