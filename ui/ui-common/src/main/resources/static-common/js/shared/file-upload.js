
angular.module(COMMON_APP_MODULE_NAME).directive('fileModel', ['$parse', function ($parse) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;

            element.bind('change', function(){
                scope.$apply(function(){
                    modelSetter(scope, element[0].files[0]);
                });
            });
        }
    };
}]);

angular.module(COMMON_APP_MODULE_NAME).service('FileUpload', ['$http', function ($http) {
    this.uploadFileToUrl = function(file, uploadUrl, successFn, errorFn,params){
        var fd = new FormData();
        fd.append('file', file);
        if(params){
            angular.forEach(params,function(val,key){
                fd.append(key,val);
            })
        }
        $http.post(uploadUrl, fd, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
        })
            .success(function(data){
                if(successFn){
                    successFn(data)
                }
            })
            .error(function(err){
                if(errorFn){
                    errorFn(err)
                }
            });
    }
}]);