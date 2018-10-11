import * as angular from 'angular';
import {moduleName} from './module-name';
import {IDGenerator} from '../common/utils/IDGenerator';


export default class HttpService{
    cancelPendingHttpRequests: any;
    getJson: any;
    AbortableRequestBuilder: any;        
    getAllRequestBuilder: any;
    get: any;
    newRequestBuilder: any;
    getAndTransform: any;
    getAll: any;
    appendTransform: any;

constructor(private $q: any,
            private $http: any){
        /**
         * Cancel all Pending Requests.
         * This is useful when changing views
         */
        this.cancelPendingHttpRequests = function () {
            angular.forEach($http.pendingRequests, function (request: any) {
                if (request.cancel && request.timeout) {
                    request.cancel.resolve();
                }
            });
        }

        this.getJson = function (url: any) {
            return $http.get(url, {headers: {'Content-type': 'application/json'}});
        }

        this.AbortableRequestBuilder = function (url: any) {
            var builder = this;

            this.successFn;
            this.errorFn;
            this.finallyFn;
            this.transformFn;
            this.params;
            this.url = url;

            return {
                params: function (getParameters: any) {
                    if (getParameters) {
                        builder.params = getParameters;
                    }
                    return this;
                },
                transform: function (fn: any) {
                    if (fn) {
                        builder.transformFn = fn;
                    }
                    return this;
                },
                success: function (fn: any) {
                    if (fn) {
                        builder.successFn = fn;
                    }
                    return this;
                },
                error: function (fn: any) {
                    if (fn) {
                        builder.errorFn = fn;
                    }
                    return this;
                },
                finally: function (fn: any) {
                    if (fn) {
                        builder.finallyFn = fn;
                    }
                    return this;
                },
                build: function () {
                    var canceller: any = {
                        resolve: function () {
                        }
                    };//$q.defer();
                    var options: any = {}//timeout: canceller.promise, cancel: canceller};
                    if (builder.params) {
                        options.params = builder.params;
                    }
                    if (builder.transformFn) {
                        // options.transformResponse = this.appendTransform($http.defaults.transformResponse,function(value){
                        //    return transformResponseFn(value);
                        //  })
                        options.transformResponse = builder.transformFn;
                    }
                    var promise: any = $http.get(builder.url, options);
                    if (builder.successFn) {
                        promise.then(builder.successFn);
                    }
                    if (builder.errorFn) {
                        promise.catch(builder.errorFn);
                    }

                    promise.finally(function () {
                        if (builder.finallyFn) {
                            builder.finallyFn();
                        }
                    });

                    return {
                        promise: promise,
                        cancel: canceller,
                        abort: function () {
                            if (this.cancel != null) {
                                this.cancel.resolve('Aborted');
                            }
                            this.cancel = null;
                            this.promise = null;
                        }
                    };
                }
            }
        }

        /**
         * Creates an Abortable Request Builder for multiple Urls and allows you to listen for the final
         * success return
         * @param urls
         * @returns {{success: Function, error: Function, finally: Function, build: Function}}
         */
        this.getAllRequestBuilder = function (urls: any) {
            this.id = IDGenerator.generateId('requestBuilder');
            var builder: any = this;
            this.urls = urls;
            this.requestBuilders = [];

            this.finalSuccessFn;
            this.finalErrorFn;
            this.finallyFn;

            this.allData = [];

            var successFn = function (data: any) {
                if (data.length === undefined) {
                    data = [data];
                }
                if (data.length > 0) {
                    builder.allData.push.apply(builder.allData, data);
                }

            };
            var errorFn = function (data: any, status: any, headers: any, config: any) {
                if (status && status == 0) {
                    //cancelled request
                }
                else {
                    console.log("Failed to execute query  ", data, status, headers, config);
                }
            };

            for (var i = 0; i < urls.length; i++) {
                var rqst: any = new this.AbortableRequestBuilder(urls[i]).success(successFn).error(errorFn);
                builder.requestBuilders.push(rqst);
            }

            return {

                success: function (fn: any) {
                    if (fn) {

                        builder.finalSuccessFn = fn;
                    }
                    return this;
                },
                error: function (fn: any) {
                    if (fn) {
                        builder.finalErrorFn = fn;
                    }
                    return this;
                },
                finally: function (fn: any) {
                    if (fn) {
                        builder.finallyFn = fn;
                    }
                    return this;
                },
                build: function () {
                    var deferred = $q.defer();

                    var promises = [];
                    var requests = [];
                    for (var i = 0; i < builder.requestBuilders.length; i++) {
                        var rqst = builder.requestBuilders[i].build();
                        requests.push(rqst);
                        promises.push(rqst.promise);
                    }

                    deferred.promise.then(function (data: any) {

                        if (builder.finalSuccessFn) {
                            builder.finalSuccessFn(data);
                        }
                    }, function () {
                        if (builder.finalErrorFn) {
                            builder.finalErrorFn();
                        }
                    }).finally(function () {
                        if (builder.finallyFn) {
                            builder.finallyFn();
                        }
                    });

                    $q.all(promises).then(function (returnData: any) {
                        deferred.resolve(builder.allData);
                    }, function (e: any) {
                        if (e && e.status && e.status == 0) {
                            //cancelled request... dont log
                        } else {
                            console.log("Error occurred", e);
                        }
                    });

                    return {
                        requests: requests,
                        promise: deferred.promise,
                        abort: function () {
                            if (this.requests) {
                                for (var i = 0; i < this.requests.length; i++) {
                                    this.requests[i].abort('Aborted');
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * Return an Abortable Request
         * Usage:
         * var rqst = HttpService.get("/feed/" + feed + "/exitCode/" + exitCode);
         * rqst.promise.success(function(data){ ..})
         *     .error(function() { ...})
         *     .finally(function() { ... });
         * //to abort:
         * rqst.abort();
         *
         * @param url
         * @returns {*|{promise, cancel, abort}|{requests, promise, abort}}
         */
        this.get = function (url: any) {
            return this.newRequestBuilder(url).build();
        }
        /**
         * creates a new AbortableRequestBuilder
         * This needs to call build to execute.
         * Example:
         *    HttpService.newRequestBuilder(url).build();
         * @param url
         * @returns {AbortableRequestBuilder}
         */
        this.newRequestBuilder = function (url: any) {
            return new this.AbortableRequestBuilder(url);
        }

        this.getAndTransform = function (url: any, transformFn: any) {
            return this.newRequestBuilder(url).transform(transformFn).build();
        }

        /**
         * Example Usage:
         * 1. pass in callbacks:
         * var rqst = HttpService.getAll(urls,successFn,errorFn,finallyFn);
         * //to abort
         * rqst.abort()
         * 2. add callbacks
         * var rqst = HttpService.getAll(urls);
         * rqst.promise.then(successFn,errorFn).finally(finallyFn);
         * //to abort
         * rqst.abort();
         *
         * @param urls
         * @param successFunction
         * @param errorFunction
         * @param finallyFunction
         * @returns {*|{promise, cancel, abort}|{requests, promise, abort}}
         */
        this.getAll = function (urls: any, successFunction: any, errorFunction: any, finallyFunction: any) {
            return new this.getAllRequestBuilder(urls).success(successFunction).error(errorFunction).finally(finallyFunction).build();
        }

        this.appendTransform = function (defaults: any, transform: any) {

            // We can't guarantee that the default transformation is an array
            defaults = angular.isArray(defaults) ? defaults : [defaults];

            // Append the new transformation to the defaults
            return defaults.concat(transform);
        }
            }
}
angular.module(moduleName).service('HttpService', ['$q', '$http', HttpService]);