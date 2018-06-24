import * as angular from 'angular';
import {moduleName} from './module-name';

export default class httpInterceptor {
     constructor($provide: any,
                 $httpProvider: any) {
        $provide.factory('httpInterceptor', ["$q","$location","$window","$injector","Utils",
                        function($q: any, $location: any, $window: any, $injector: any, Utils: any) {
            return {
                /**
                 * Intercepts and modifies HTTP requests.
                 *
                 * @param {Object} request the HTTP request
                 */
                request: function(request: any) {
                    // Add X-Requested-With header to disable basic auth
                    if (angular.isUndefined(request.headers)) {
                        request.headers = {};
                    }
                    request.headers["X-Requested-With"] = "XMLHttpRequest";
                    return request;
                },

                /**
                 * Intercepts and handles HTTP responses.
                 *
                 * @param {Object} response the response
                 * @returns {Promise} the response
                 */
                response: function(response: any) {
                    //injected manually to get around circular dependency problem.
                    var NotificationService = $injector.get('NotificationService');

                    // Check if login needed
                    var redirectLocation;

                    if (response.headers() && response.headers()['Location'] && response.headers()['Location'].endsWith('login.html')) {
                        redirectLocation = null;
                    } else if (response.data && response.config && !Utils.endsWith(response.config.url, ".html") && typeof response.data == 'string') {
                        if (response.data.indexOf('<!-- login.html -->') >= 0) {
                            redirectLocation = "/login.html";
                        }
                    }

                    if (angular.isDefined(redirectLocation)) {
                        NotificationService.errorWithGroupKey("Login Required", "You are required to login to view this content.", "Login Required");
                        if (redirectLocation !== null) {
                            $window.location.href = redirectLocation;
                        }
                    }

                    return response || $q.when(response);
                },

                /**
                 * Intercepts and handles HTTP error responses.
                 *
                 * @param {Object} rejection the response
                 * @returns {Promise} the response
                 */
                responseError: function(rejection: any) {
                    //injected manually to get around circular dependency problem.
                    var NotificationService = $injector.get('NotificationService');

                    if (rejection.data == undefined) {
                        rejection.data = {};
                    }
                    if (rejection.status === 401) {
                        NotificationService.errorWithGroupKey("Login Required", "You are required to login to view this content.", "Login Required");
                        $window.location.href = "/login.html";
                    }
                    else if (rejection.status <= 0) {
                        //Usually -1 means aborted request
                        //for now remove this logic as it is cause errors to appear which are not errors.
                        //re visit if needed
                        /*   if(rejection.config && rejection.config.timeout && rejection.config.timeout.$$state && rejection.config.timeout.$$state ==1){
                         //aborted
                         }
                         else {
                         //internet is down
                         NotificationService.errorWithGroupKey("Connection Error", "Not Connected. Server is down.", "Connection Error");
                         }
                         */
                    }
                    else if (rejection.status === 400) {
                        // Bad Request
                        var message = "An error occurred ";
                        var errorMessage = rejection.data["message"];
                        var groupKey = errorMessage;
                        if (groupKey == undefined || groupKey == '') {
                            groupKey = 'OtherError';
                        }
                        var url = rejection.data["url"];
                        if (url != undefined && url != null && url != "") {
                            message += " attempting to access: " + url
                        }
                        message += ".";
                        if (rejection.data['handledException'] == undefined || (rejection.data['handledException'] != undefined && rejection.data['handledException'] == false )) {
                            if (rejection.data["url"]) {
                                NotificationService.errorWithGroupKey("Error", message, url, errorMessage);
                            }
                            else {
                                NotificationService.errorWithGroupKey("Error", message, groupKey, errorMessage);
                            }
                        }

                    }
                    else {
                        if (rejection.config && rejection.config.acceptStatus === rejection.status) {
                            //sometimes 404 response is a valid response for which we don't want to show error message with NotificationService
                            return $q.when(rejection);
                        }

                        if (rejection.data['handledException'] == undefined || (rejection.data['handledException'] != undefined && rejection.data['handledException'] == false )) {
                            var message = "An error occurred ";
                            var rejectionMessage = rejection.data['message'];
                            if (rejectionMessage == undefined || rejectionMessage == '') {
                                rejectionMessage = 'OtherError';
                            } else if (rejectionMessage.startsWith("AnalysisException:")) {
                                // Don't notify for messages from wrangler. These are handled.
                                return $q.reject(rejection);
                            }
                            NotificationService.errorWithGroupKey("Error", message, rejectionMessage, rejection.data["message"]);
                        }
                    }
                    return $q.reject(rejection);
                }
            };
        }]);
        $httpProvider.interceptors.push('httpInterceptor');
    } // ending constructor here
}

angular.module(moduleName).config(['$provide', '$httpProvider',httpInterceptor]);
