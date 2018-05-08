var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/common/http", "@angular/core"], function (require, exports, http_1, core_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var FileManagerService = /** @class */ (function () {
        function FileManagerService(http) {
            this.http = http;
        }
        FileManagerService_1 = FileManagerService;
        FileManagerService.prototype.deleteFile = function (dataSetId, name) {
            return this.http.delete(FileManagerService_1.getUploadsUrl(dataSetId, name));
        };
        FileManagerService.prototype.listFiles = function (dataSetId) {
            return this.http.get(FileManagerService_1.getUploadsUrl(dataSetId));
        };
        FileManagerService.prototype.uploadFile = function (dataSetId, file) {
            var formData = new FormData();
            formData.append("file", file, file.name);
            var request = new http_1.HttpRequest("POST", FileManagerService_1.getUploadsUrl(dataSetId), formData, { reportProgress: true });
            return this.http.request(request);
        };
        FileManagerService.getUploadsUrl = function (id, fileName) {
            var url = "/proxy/v1/catalog/dataset/" + encodeURIComponent(id) + "/uploads";
            return fileName ? url + "/" + encodeURIComponent(fileName) : url;
        };
        FileManagerService = FileManagerService_1 = __decorate([
            core_1.Injectable(),
            __metadata("design:paramtypes", [http_1.HttpClient])
        ], FileManagerService);
        return FileManagerService;
        var FileManagerService_1;
    }());
    exports.FileManagerService = FileManagerService;
});
//# sourceMappingURL=file-manager.service.js.map