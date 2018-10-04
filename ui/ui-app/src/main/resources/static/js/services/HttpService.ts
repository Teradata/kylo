import * as _ from "underscore";
import { HttpClient } from '@angular/common/http';
import { Injectable } from "@angular/core";


@Injectable()
export default class HttpService {

    constructor(private http: HttpClient) { }
    /**
     * Cancel all Pending Requests.
     * This is useful when changing views
     */
    cancelPendingHttpRequests() {
        //     _.forEach(this.http.pendingRequests, (request: any) => {
        //         if (request.cancel && request.timeout) {
        //             request.cancel.resolve();
        //         }
        //     });
    }

    getJson(url: any) {
        return this.http.get(url, { headers: { 'Content-type': 'application/json' } });
    }

    AbortableRequestBuilder = function (url: any) {
        var builder = this;

        this.successFn;
        this.errorFn;
        this.finallyFn;
        this.transformFn;
        this.params;
        this.url = url;

        return {
            params: (getParameters: any) => {
                if (getParameters) {
                    builder.params = getParameters;
                }
                return this;
            },
            transform: (fn: any) => {
                if (fn) {
                    builder.transformFn = fn;
                }
                return this;
            },
            success: (fn: any) => {
                if (fn) {
                    builder.successFn = fn;
                }
                return this;
            },
            error: (fn: any) => {
                if (fn) {
                    builder.errorFn = fn;
                }
                return this;
            },
            finally: (fn: any) => {
                if (fn) {
                    builder.finallyFn = fn;
                }
                return this;
            },
            build: () => {
                var canceller: any = {
                    resolve: () => {
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
                var promise: any = this.http.get(builder.url, options);
                if (builder.successFn) {
                    promise.then(builder.successFn);
                }
                if (builder.errorFn) {
                    promise.catch(builder.errorFn);
                }

                promise.finally(() => {
                    if (builder.finallyFn) {
                        builder.finallyFn();
                    }
                });
                return {
                    promise: promise,
                    cancel: canceller,
                    abort: () => {
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
    /*  getAllRequestBuilder (urls: any) {
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
*/
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
    get(url: any) {
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
    newRequestBuilder(url: any) {
        return this.AbortableRequestBuilder(url);
    }

    getAndTransform(url: any, transformFn: any) {
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
    // getAll (urls: any, successFunction: any, errorFunction: any, finallyFunction: any) {
    //     return new this.getAllRequestBuilder(urls).success(successFunction).error(errorFunction).finally(finallyFunction).build();
    // }

    appendTransform(defaults: any, transform: any) {

        // We can't guarantee that the default transformation is an array
        defaults = Array.isArray(defaults) ? defaults : [defaults];

        // Append the new transformation to the defaults
        return defaults.concat(transform);
    }
}