define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var FileUpload = /** @class */ (function () {
        function FileUpload(file) {
            this.progress = 0;
            this.status = FileUploadStatus.PENDING;
            if (typeof file === "string") {
                this.name = file;
            }
            else {
                this.setFile(file);
            }
        }
        Object.defineProperty(FileUpload.prototype, "buttonIcon", {
            get: function () {
                if (this.status === FileUploadStatus.SUCCESS) {
                    return "delete";
                }
                else {
                    return "cancel";
                }
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(FileUpload.prototype, "listIcon", {
            get: function () {
                if (this.status === FileUploadStatus.FAILED) {
                    return "error";
                }
                else if (this.status === FileUploadStatus.SUCCESS) {
                    return "check_circle";
                }
                else {
                    return "file_upload";
                }
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(FileUpload.prototype, "listIconClass", {
            get: function () {
                if (this.status === FileUploadStatus.FAILED) {
                    return "mat-warn";
                }
                else if (this.status === FileUploadStatus.SUCCESS) {
                    return "icon-success";
                }
                else {
                    return "icon-pending";
                }
            },
            enumerable: true,
            configurable: true
        });
        FileUpload.prototype.setFile = function (file) {
            this.error = null;
            this.name = file.name;
            this.path = file.path;
            this.size = file.length;
            this.status = FileUploadStatus.SUCCESS;
        };
        return FileUpload;
    }());
    exports.FileUpload = FileUpload;
    var FileUploadStatus;
    (function (FileUploadStatus) {
        FileUploadStatus[FileUploadStatus["PENDING"] = 0] = "PENDING";
        FileUploadStatus[FileUploadStatus["SUCCESS"] = 1] = "SUCCESS";
        FileUploadStatus[FileUploadStatus["FAILED"] = 2] = "FAILED";
    })(FileUploadStatus = exports.FileUploadStatus || (exports.FileUploadStatus = {}));
});
//# sourceMappingURL=file-upload.js.map