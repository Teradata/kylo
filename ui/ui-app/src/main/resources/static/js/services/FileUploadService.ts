import * as _ from "underscore";
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";

@Injectable()
export default class FileUpload{
    constructor(private http: HttpClient){
    }
    uploadFileToUrl (files: any, uploadUrl: any, successFn: any, errorFn: any, params: any) {
        var fd = new FormData();
        var arr = files;
        if(!_.isArray(files)) {
            arr = [files];
        }
        if (arr.length > 1) {
            _.forEach(arr, (file: any, index: any) => {
                index += 1;
                fd.append('file' + index, file);
            });
        } else {
            fd.append('file', arr[0]);
        }

        if (params) {
            _.forEach(params, (val: any, key: any) => {
                fd.append(key, val);
            })
        }
        this.http.post(uploadUrl, fd, { 
            // transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
        }).toPromise().then((data: any) => {
                if (successFn) {
                    successFn(data)
                }
            },(err: any) => {
                if (errorFn) {
                    errorFn(err)
                }
        });
    }
}