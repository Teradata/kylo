define(['angular','services/module-name'], function (angular,moduleName) {
    angular.module(moduleName).service('FileUpload', ['$http', function ($http) {
        this.uploadFileToUrl = function (file, uploadUrl, successFn, errorFn, params) {
            var fd = new FormData();
            fd.append('file', file);
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