define(['angular','services/module-name'], function (angular,moduleName) {
    angular.module(moduleName).service('FileUpload', ['$http', function ($http) {
        this.uploadFileToUrl = function (files, uploadUrl, successFn, errorFn, params) {
            var fd = new FormData();
            var arr = files;
            if(!_.isArray(files)) {
                arr = [files];
            }
            if (arr.length > 1) {
                angular.forEach(arr, function(file, index) {
                    index += 1;
                    fd.append('file' + index, file);
                });
            } else {
                fd.append('file', arr[0]);
            }

            if (params) {
                angular.forEach(params, function (val, key) {
                    fd.append(key, val);
                })
            }
            $http.post(uploadUrl, fd, { 
                transformRequest: angular.identity,
                headers: {'Content-Type': undefined}
            })
                .then(function (data) {
                    if (successFn) {
                        successFn(data)
                    }
                },function (err) {
                    if (errorFn) {
                        errorFn(err)
                    }
                });
        }
    }]);
});