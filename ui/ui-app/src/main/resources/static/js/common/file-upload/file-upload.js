define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    angular.module(module_name_1.moduleName).directive('fileModel', ['$parse', function ($parse) {
            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                    var model = this.$parse(attrs.fileModel);
                    var modelSetter = model.assign;
                    element.bind('change', function () {
                        scope.$apply(function () {
                            modelSetter(scope, element[0].files[0]);
                        });
                    });
                }
            };
        }
    ]);
    angular.module(module_name_1.moduleName).directive('uploadFile', ['$parse',
        function ($parse) {
            return {
                restrict: 'E',
                template: '<input id="fileInput" type="file" class="ng-hide"> <md-button id="uploadButton" class="md-raised md-primary" aria-label="attach_file">{{"views.file-upload.btn-Choose" | translate}} </md-button><md-input-container class="condensed-no-float" md-no-float  flex>    <input id="textInput" size="40" ng-model="fileName" type="text" placeholder="{{\'views.file-upload.placeholder\' | translate}}" ng-readonly="true" style="margin-top: 20px;"></md-input-container>',
                link: function (scope, element, attrs) {
                    var input = $(element[0].querySelector('#fileInput'));
                    var button = $(element[0].querySelector('#uploadButton'));
                    var textInput = $(element[0].querySelector('#textInput'));
                    var size = attrs.inputSize;
                    if (size != null) {
                        try {
                            size = parseInt(size);
                            input.attr("size", size);
                        }
                        catch (e) {
                        }
                    }
                    var model = this.$parse(attrs.uploadFileModel);
                    var modelSetter = model.assign;
                    if (input.length && button.length && textInput.length) {
                        button.click(function (e) {
                            input.click();
                        });
                        textInput.click(function (e) {
                            input.click();
                        });
                    }
                    input.on('change', function (e) {
                        var files = e.target.files;
                        if (files[0]) {
                            scope.fileName = files[0].name;
                            button.removeClass("md-primary");
                        }
                        else {
                            scope.fileName = null;
                            button.addClass("md-primary");
                        }
                        scope.$apply(function () {
                            modelSetter(scope, files[0]);
                        });
                    });
                }
            };
        }
    ]);
});
//# sourceMappingURL=file-upload.js.map