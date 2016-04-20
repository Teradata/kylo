
/**
 * Generic Interceptor to handle any Http Request errors and push them to the NotificationService
 */
(function(){
    var httpInterceptor = function ($provide, $httpProvider) {
        $provide.factory('httpInterceptor', function ($q,$location,$window,$injector, Utils) {
            return {
                // optional method
/*
                'request': function(config) {
                    var path = $location.absUrl();
                    var pathArray = path.split('/');
                    var appContext = pathArray[3];
                   console.log('path',path,'pathArray',pathArray,'appContext',appContext,'config.url',config.url)
                    //if(config.url.indexOf("/api/") ==0) {
                    if(appContext.indexOf("#") != 0 && appContext.indexOf("/#") != 0 ) {
                        config.url = "/" + appContext + (config.url.indexOf("/") == 0 ? "" : "/") + config.url;
                    }
                    return config;
                },

                // optional method
                'requestError': function(rejection) {
                    console.log('REQUEST ERROR',rejection)
                    //if (canRecover(rejection)) {
                    //    return responseOrNewPromise
                   // }
                    return $q.reject(rejection);
                },
                */
                response: function (response) {
                    //injected manually to get around circular dependency problem.
                    var NotificationService = $injector.get('NotificationService');

                    if(response.headers() && response.headers()['Location'] &&  response.headers()['Location'].endsWith('login.html')){
                        NotificationService.errorWithGroupKey("Login Required","You are required to Login to view this content.","Login Required");

                    }
                    var data = response.data;
                    if(response && response.data && response.config && !Utils.endsWith(response.config.url,".html") && typeof response.data == 'string'){
                        if(response.data.indexOf('<!-- login.html -->') >=0){
                            NotificationService.errorWithGroupKey("Login Required","You are required to Login to view this content.","Login Required");
                            $window.location.href = '/login.html';
                        }
                    }
                    return response || $q.when(response);
                },
                responseError: function (rejection) {
                    //injected manually to get around circular dependency problem.
                    var NotificationService = $injector.get('NotificationService');

                    if(rejection.data == undefined){
                        rejection.data = {};
                    }
                    if(rejection.status === 401) {
                        // you are not autorized
                        NotificationService.errorWithGroupKey("Unauthorized","You are unauthorized to view this content.","Unauthorized");
                    }
                    else if(rejection.status == 0){
                        //internet is down
                        NotificationService.errorWithGroupKey("Connection Error","Not Connected. Pipeline Controller Server is down.","Connection Error");
                    }
                    else  if(rejection.status === 400) {
                        // Bad Request
                        var  message = "An unexpected error occurred ";
                        var errorMessage = rejection.data["message"];
                        var groupKey = errorMessage;
                        if(groupKey == undefined || groupKey == '') {
                            groupKey = 'OtherError';
                        }
                        var url = rejection.data["url"];
                        if(url != undefined && url != null && url != ""){
                            message +=" attempting to access: "+url
                        }
                        message +=".";
                        if( rejection.data['handledException'] == undefined || (rejection.data['handledException'] != undefined && rejection.data['handledException'] == false )) {
                            if (rejection.data["url"]) {
                                NotificationService.errorWithGroupKey("Error", message, url, errorMessage);
                            }
                            else {
                                NotificationService.errorWithGroupKey("Error", message, groupKey, errorMessage);
                            }
                        }

                    }
                    else{
                        if( rejection.data['handledException'] == undefined || (rejection.data['handledException'] != undefined && rejection.data['handledException'] == false )) {
                            var message = "An unexpected error occurred ";
                            var rejectionMessage = rejection.data['message'];
                            if (rejectionMessage == undefined || rejectionMessage == '') {
                                rejectionMessage = 'OtherError';
                            }
                            NotificationService.errorWithGroupKey("Error", message, rejectionMessage, rejection.data["message"]);
                        }
                    }
                    return $q.reject(rejection);
                }
            };
        });
        $httpProvider.interceptors.push('httpInterceptor');
    };
    angular.module(COMMON_APP_MODULE_NAME).config(httpInterceptor);
}());