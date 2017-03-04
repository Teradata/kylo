/*-
 * #%L
 * thinkbig-ui-common
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
define(['angular','common/module-name'], function (angular,moduleName) {
    angular.module(moduleName).directive('fileModel', ['$parse', function ($parse) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var model = $parse(attrs.fileModel);
                var modelSetter = model.assign;

                element.bind('change', function () {
                    scope.$apply(function () {
                        modelSetter(scope, element[0].files[0]);
                    });
                });
            }
        };
    }]);

    function uploadFile($parse) {
        var directive = {
            restrict: 'E',
            template: '<input id="fileInput" type="file" class="ng-hide"> <md-button id="uploadButton" class="md-raised md-primary" aria-label="attach_file">    Choose file </md-button><md-input-container class="condensed-no-float" md-no-float  flex>    <input id="textInput" size="40" ng-model="fileName" type="text" placeholder="No file chosen" ng-readonly="true" style="margin-top: 20px;"></md-input-container>',
            link: function (scope, element, attrs) {
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
                var model = $parse(attrs.uploadFileModel);
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
                        button.removeClass("md-primary")
                    } else {
                        scope.fileName = null;
                        button.addClass("md-primary")
                    }
                    scope.$apply(function () {
                        modelSetter(scope, files[0]);
                    });
                });
            }
        };
        return directive;
    }

    angular.module(moduleName).directive('uploadFile', ['$parse', uploadFile]);



});
