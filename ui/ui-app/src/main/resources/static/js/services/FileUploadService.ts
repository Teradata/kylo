import * as angular from 'angular';
import {moduleName} from './module-name';
import * as _ from "underscore";


export class FileUpload{
    uploadFileToUrl: any;
    constructor(private $http: any){
         this.uploadFileToUrl = function (files: any, uploadUrl: any, successFn: any, errorFn: any, params: any) {
            var fd = new FormData();
            var arr = files;
            if(!_.isArray(files)) {
                arr = [files];
            }
            if (arr.length > 1) {
                angular.forEach(arr, function(file: any, index: any) {
                    index += 1;
                    fd.append('file' + index, file);
                });
            } else {
                fd.append('file', arr[0]);
            }

            if (params) {
                angular.forEach(params, function (val: any, key: any) {
                    fd.append(key, val);
                })
            }
            $http.post(uploadUrl, fd, { 
                transformRequest: angular.identity,
                headers: {'Content-Type': undefined}
            })
                .then(function (data: any) {
                    if (successFn) {
                        successFn(data)
                    }
                },function (err: any) {
                    if (errorFn) {
                        errorFn(err)
                    }
                });
        }
    }
}
angular.module(moduleName).service('FileUpload', ['$http', FileUpload]);