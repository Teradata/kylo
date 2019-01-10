import {HttpErrorResponse, HttpEvent, HttpEventType, HttpHandler, HttpHeaders, HttpInterceptor, HttpRequest, HttpResponse} from "@angular/common/http";
import {Injectable, Injector} from "@angular/core";
import * as angular from 'angular';
import {Observable} from "rxjs/Observable";
import {_throw} from "rxjs/observable/throw";
import {catchError} from "rxjs/operators/catchError";
import {map} from "rxjs/operators/map";

import {moduleName} from './module-name';
import {NotificationService} from "./notification.service";

const X_REQUESTED_WITH = "X-Requested-With";
const XML_HTTP_REQUEST = "XMLHttpRequest";

@Injectable()
export class AngularHttpInterceptor implements angular.IHttpInterceptor, HttpInterceptor {

    private notificationService: NotificationService;

    static readonly $inject = ["$$angularInjector"];

    constructor(private $injector: Injector) {
    }

    /**
     * Angular 4: Intercept the outgoing HTTP request and transform the response.
     */
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const headers = {};
        headers[X_REQUESTED_WITH] = XML_HTTP_REQUEST;

        return next.handle(req.clone({setHeaders: headers})).pipe(
            map(response => {
                if (response.type === HttpEventType.Response) {
                    this.checkResponse(response);
                }
                return response;
            }),
            catchError(event => {
                if (event instanceof HttpErrorResponse) {
                    this.checkErrorResponse(event);
                }
                return _throw(event)
            })
        );
    }

    /**
     * AngularJS: Intercepts and modifies HTTP requests.
     */
    request(request: angular.IRequestConfig): angular.IRequestConfig | angular.IPromise<angular.IRequestConfig> {
        // Add X-Requested-With header to disable basic auth
        if (angular.isUndefined(request.headers)) {
            request.headers = {};
        }
        request.headers[X_REQUESTED_WITH] = XML_HTTP_REQUEST;
        return request;
    }

    /**
     * AngularJS: Intercepts and handles HTTP responses.
     */
    response<T>(response: angular.IHttpResponse<T>): angular.IPromise<angular.IHttpResponse<T>> | angular.IHttpResponse<T> {
        this.checkResponse(this.toHttpResponse(response));
        return response || <any> Promise.resolve(response);
    }

    /**
     * AngularJS: Intercepts and handles HTTP error responses.
     */
    responseError<T>(rejection: any): angular.IPromise<angular.IHttpResponse<T>> | angular.IHttpResponse<T> {
        if (rejection.config && rejection.config.acceptStatus === rejection.status) {
            //sometimes 404 response is a valid response for which we don't want to show error message with NotificationService
            return <any> Promise.resolve(rejection);
        } else {
            this.checkErrorResponse(this.toHttpErrorResponse(rejection));
            return <any> Promise.reject(rejection);
        }
    }


    uintToString(uintArray) {
        /*        var encodedString = String.fromCharCode.apply(null, uintArray),
                    decodedString = decodeURIComponent(encodedString);
                return decodedString;
        */
        return String.fromCharCode.apply(null, new Uint8Array(uintArray));
    }


    /**
     * Handle error responses and redirect to login page if necessary.
     */
    private checkErrorResponse(rejection: HttpErrorResponse): void {
        const data = rejection.error || {};

        if (rejection.status === 401) {
            this.errorWithGroupKey("Login Required", "You are required to login to view this content.", "Login Required");
            window.location.href = "/login.html";
        } else if (rejection.status <= 0) {
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
        } else if (rejection.status === 400) {
            // Bad Request
            let message = "An error occurred ";
            let errorMessage = data["message"];
            let groupKey = errorMessage;
            if (groupKey == undefined || groupKey == '') {
                groupKey = 'OtherError';
            }
            let url = data["url"];
            if (url != undefined && url != null && url != "") {
                message += " attempting to access: " + url
            }
            message += ".";
            if (data['handledException'] == undefined || (data['handledException'] != undefined && data['handledException'] == false)) {
                if (data["url"]) {
                    this.errorWithGroupKey("Error", message, url, errorMessage);
                } else {
                    this.errorWithGroupKey("Error", message, groupKey, errorMessage);
                }
            }
        } else if (data['handledException'] == undefined || (data['handledException'] != undefined && data['handledException'] == false)) {
            let message = "An error occurred ";
            let detailedMessage = data['message'] ? data['message'] : (Array.isArray(data['errorMessages']) ? data['errorMessages'][0] : null);

            var rejectionMessage = data['message'];
            if (rejection.error && rejection.error.constructor == ArrayBuffer ) {
                var str = this.uintToString(rejection.error);
                var json = JSON.parse(str);
                console.log(json);
                message += json.message;
            }

            if (rejectionMessage == undefined || rejectionMessage == '') {
                rejectionMessage = 'OtherError';
            } else if (rejectionMessage.startsWith("AnalysisException:")) {
                // Don't notify for messages from wrangler. These are handled.
                return;
            }
            this.errorWithGroupKey("Error", message, rejectionMessage, detailedMessage);
        }
    }

    /**
     * Handle success response and redirect to login page if necessary.
     */
    private checkResponse(response: HttpResponse<any>): void {
        // Check if login needed
        let redirectLocation = null;

        if (response.headers.has("Location") && response.headers.get("Location").endsWith("login.html")) {
            redirectLocation = null;
        } else if (!response.url.endsWith(".html") && typeof response.body === "string" && response.body.indexOf("<!-- login.html -->") >= 0) {
            redirectLocation = "/login.html";
        }

        if (redirectLocation !== null) {
            this.errorWithGroupKey("Login Required", "You are required to login to view this content.", "Login Required");
            window.location.href = redirectLocation;
        }
    }

    /**
     * Broadcast the specified error notification.
     */
    private errorWithGroupKey(errorType: string, message: string, groupKey: string, detailMsg?: string): void {
        if (this.notificationService == null) {
            //injected manually to get around circular dependency problem.
            try {
                this.notificationService = this.$injector.get(NotificationService);
            } catch (e) {
                console.error(e);
            }
        }

        if (this.notificationService != null) {
            this.notificationService.errorWithGroupKey(errorType, message, groupKey, detailMsg);
        } else {
            console.warn(errorType + ": " + message);
        }
    }

    /**
     * Converts the specified AngularJS rejection to an Angular 4 error response.
     */
    private toHttpErrorResponse(rejection: any): HttpErrorResponse {
        return new HttpErrorResponse({
            error: rejection.data,
            headers: new HttpHeaders(rejection.headers()),
            status: rejection.status,
            statusText: rejection.statusText,
            url: rejection.config.url
        });
    }

    /**
     * Converts the specified AngularJS response to an Angular 4 response.
     */
    private toHttpResponse<T>(response: angular.IHttpResponse<T>): HttpResponse<T> {
        return new HttpResponse<T>({
            body: response.data,
            headers: new HttpHeaders(response.headers()),
            status: response.status,
            statusText: response.statusText,
            url: response.config.url
        });
    }
}

function buildHttpInterceptor($injector: any) {
    const interceptor = new AngularHttpInterceptor($injector);
    return {
        request: (request: any) => interceptor.request(request),
        response: (response: any) => interceptor.response(response),
        responseError: (rejection: any) => interceptor.responseError(rejection)
    };
}

function registerHttpInterceptor($httpProvider: angular.IHttpProvider) {
    $httpProvider.interceptors.push("httpInterceptor");
}

angular.module(moduleName)
    .factory("httpInterceptor", [...AngularHttpInterceptor.$inject, buildHttpInterceptor])
    .config(["$httpProvider", registerHttpInterceptor]);
