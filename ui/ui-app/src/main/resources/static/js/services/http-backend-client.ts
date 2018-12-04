import { Injectable } from '@angular/core';
import { HttpClient, HttpBackend } from '@angular/common/http';

/**
 * Http Client that bypasses the AngularHttpInterceptor
 *
 */
@Injectable()
export class HttpBackendClient extends HttpClient {
    constructor(handler: HttpBackend) {
        super(handler);
    }
}