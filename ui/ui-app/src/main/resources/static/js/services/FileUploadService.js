define(["require", "exports", "angular", "./module-name", "underscore"], function (require, exports, angular, module_name_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var FileUpload = /** @class */ (function () {
        function FileUpload($http) {
            this.$http = $http;
            this.uploadFileToUrl = function (files, uploadUrl, successFn, errorFn, params) {
                var fd = new FormData();
                var arr = files;
                if (!_.isArray(files)) {
                    arr = [files];
                }
                if (arr.length > 1) {
                    angular.forEach(arr, function (file, index) {
                        index += 1;
                        fd.append('file' + index, file);
                    });
                }
                else {
                    fd.append('file', arr[0]);
                }
                if (params) {
                    angular.forEach(params, function (val, key) {
                        fd.append(key, val);
                    });
                }
                $http.post(uploadUrl, fd, {
                    transformRequest: angular.identity,
                    headers: { 'Content-Type': undefined }
                })
                    .then(function (data) {
                    if (successFn) {
                        successFn(data);
                    }
                }, function (err) {
                    if (errorFn) {
                        errorFn(err);
                    }
                });
            };
        }
        return FileUpload;
    }());
    exports.default = FileUpload;
    angular.module(module_name_1.moduleName).service('FileUpload', ['$http', FileUpload]);
});
//# sourceMappingURL=FileUploadService.js.map