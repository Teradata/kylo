var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/common/http", "@angular/core", "@covalent/core/dialogs", "../../catalog/services/file-manager.service", "./models/file-upload"], function (require, exports, http_1, core_1, dialogs_1, file_manager_service_1, file_upload_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Provides a form for uploading files and managing uploaded files for a data set.
     */
    var UploadComponent = /** @class */ (function () {
        function UploadComponent(dialogs, fileManager) {
            this.dialogs = dialogs;
            this.fileManager = fileManager;
            /**
             * Uploads pending, in-progress, failed, and successful
             */
            this.files = [];
            /**
             * Indicates at least one upload is successful
             */
            this.isReady = false;
        }
        UploadComponent.prototype.ngOnInit = function () {
            var _this = this;
            if (this.dataSet.$fileUploads) {
                // Read uploads cached locally in dataset
                this.files = this.dataSet.$fileUploads;
            }
            else {
                this.dataSet.$fileUploads = this.files;
                // Parse uploads from dataset paths and server
                if (this.dataSet.paths) {
                    this.files = this.dataSet.paths.map(function (path) {
                        var name = path.substr(path.lastIndexOf("/") + 1);
                        var file = new file_upload_1.FileUpload(name);
                        file.path = path;
                        file.status = file_upload_1.FileUploadStatus.SUCCESS;
                        return file;
                    });
                    this.fileManager.listFiles(this.dataSet.id)
                        .subscribe(function (files) { return _this.setFiles(files); });
                }
            }
        };
        /**
         * Cancels an upload and removes the path from the dataset.
         */
        UploadComponent.prototype.cancelFile = function (file) {
            var _this = this;
            // Cancel upload
            if (file.upload) {
                file.upload.unsubscribe();
            }
            // Delete server file
            if (file.status === file_upload_1.FileUploadStatus.SUCCESS) {
                this.dialogs.openConfirm({
                    message: "Are you sure you want to delete " + file.name + "?",
                    acceptButton: "Delete"
                }).afterClosed().subscribe(function (accept) { return _this.deleteFile(file); });
            }
            else {
                this.deleteFile(file);
            }
        };
        /**
         * Uploads a file or list of files
         */
        UploadComponent.prototype.upload = function (event) {
            var _this = this;
            if (event instanceof FileList) {
                // Upload files individually
                for (var i = 0; i < event.length; ++i) {
                    this.upload(event.item(i));
                }
            }
            else if (this.files.find(function (file) { return file.name === event.name; })) {
                this.dialogs.openAlert({
                    message: "File already exists."
                });
            }
            else {
                // Upload single file
                var file_1 = new file_upload_1.FileUpload(event.name);
                file_1.upload = this.fileManager.uploadFile(this.dataSet.id, event)
                    .subscribe(function (event) { return _this.setStatus(file_1, event); }, function (error) { return _this.setError(file_1, error); });
                this.files.push(file_1);
            }
        };
        /**
         * Deletes a file that has been uploaded.
         */
        UploadComponent.prototype.deleteFile = function (file) {
            var _this = this;
            var isFailed = (file.status === file_upload_1.FileUploadStatus.FAILED);
            this.fileManager.deleteFile(this.dataSet.id, file.name)
                .subscribe(null, function (error) {
                if (isFailed) {
                    _this.removeFile(file);
                }
                else {
                    _this.setError(file, error);
                }
            }, function () { return _this.removeFile(file); });
        };
        /**
         * Removes a file from the file list and dataset.
         */
        UploadComponent.prototype.removeFile = function (file) {
            this.files = this.files.filter(function (item) { return item.path !== file.path; });
            this.updateDataSet();
        };
        /**
         * Sets an error message for a file upload.
         */
        // noinspection JSMethodCanBeStatic
        UploadComponent.prototype.setError = function (file, error) {
            file.error = (error.error && error.error.message) ? error.error.message : error.message;
            file.status = file_upload_1.FileUploadStatus.FAILED;
            this.updateDataSet();
        };
        /**
         * Associates the specified dataset paths with the file uploads.
         */
        UploadComponent.prototype.setFiles = function (dataSetFiles) {
            // Map paths to file uploads
            var fileMap = new Map();
            this.files
                .filter(function (file) { return file.status === file_upload_1.FileUploadStatus.SUCCESS; })
                .filter(function (file) { return file.path != null; })
                .forEach(function (file) { return fileMap.set(file.path, file); });
            // Associate with dataset paths
            dataSetFiles.forEach(function (dataSetFile) {
                var fileUpload = fileMap.get(dataSetFile.path);
                if (fileUpload) {
                    fileUpload.setFile(dataSetFile);
                }
            });
        };
        /**
         * Updates the specified file upload.
         */
        UploadComponent.prototype.setStatus = function (file, event) {
            if (event.type === http_1.HttpEventType.UploadProgress) {
                file.progress = event.loaded / event.total;
                file.size = event.total;
            }
            else if (event.type === http_1.HttpEventType.Response) {
                file.setFile(event.body);
                this.updateDataSet();
            }
        };
        /**
         * Updates the dataset paths.
         */
        UploadComponent.prototype.updateDataSet = function () {
            this.dataSet.$fileUploads = this.files;
            this.dataSet.paths = this.files
                .filter(function (file) { return file.status === file_upload_1.FileUploadStatus.SUCCESS; })
                .map(function (file) { return file.path; })
                .filter(function (path) { return path != null; });
            this.isReady = (this.dataSet.paths.length > 0);
        };
        __decorate([
            core_1.Input(),
            __metadata("design:type", Object)
        ], UploadComponent.prototype, "dataSet", void 0);
        __decorate([
            core_1.ViewChild("fileInput"),
            __metadata("design:type", HTMLInputElement)
        ], UploadComponent.prototype, "fileInput", void 0);
        UploadComponent = __decorate([
            core_1.Component({
                selector: "local-files",
                styleUrls: ["js/feed-mgr/explorer/dataset/upload/upload.component.css"],
                templateUrl: "js/feed-mgr/explorer/dataset/upload/upload.component.html"
            }),
            __metadata("design:paramtypes", [dialogs_1.TdDialogService, file_manager_service_1.FileManagerService])
        ], UploadComponent);
        return UploadComponent;
    }());
    exports.UploadComponent = UploadComponent;
});
//# sourceMappingURL=upload.component.js.map