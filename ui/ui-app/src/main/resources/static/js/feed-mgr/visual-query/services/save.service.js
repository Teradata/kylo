define(["require", "exports", "angular", "../wrangler/api/rest-model", "rxjs/add/operator/let", "rxjs/add/operator/share"], function (require, exports, angular, rest_model_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Handles saving a transformation.
     */
    var VisualQuerySaveService = /** @class */ (function () {
        function VisualQuerySaveService($mdDialog, notificationService) {
            this.$mdDialog = $mdDialog;
            this.notificationService = notificationService;
            /**
             * Map of save id to notification.
             */
            this.notifications = {};
        }
        /**
         * Saves the specified transformation.
         *
         * @param request - destination
         * @param engine - transformation
         * @returns an observable to tracking the progress
         */
        VisualQuerySaveService.prototype.save = function (request, engine) {
            var _this = this;
            var save = engine.saveResults(request).share();
            save.subscribe(function (response) { return _this.onSaveNext(request, response); }, function (response) { return _this.onSaveError(response); });
            return save;
        };
        /**
         * Gets a notification message for the specified save request.
         */
        VisualQuerySaveService.prototype.getMessage = function (request) {
            if (request.tableName) {
                return "Saving transformation to " + request.tableName;
            }
            else {
                return "Preparing transformation for download";
            }
        };
        /**
         * Handles save errors.
         */
        VisualQuerySaveService.prototype.onSaveError = function (response) {
            var _this = this;
            var notification = this.notifications[response.id];
            if (notification) {
                // Add error notification
                var error = this.notificationService.addNotification("Failed to save transformation", "error");
                if (response.message) {
                    var message_1 = (response.message.length <= 1024) ? response.message : response.message.substr(0, 1021) + "...";
                    error.callback = function () {
                        _this.$mdDialog.show(_this.$mdDialog.alert()
                            .parent(angular.element("body"))
                            .clickOutsideToClose(true)
                            .title("Error saving the transformation")
                            .textContent(message_1)
                            .ariaLabel("Save Failed")
                            .ok("Got it!"));
                    };
                }
                // Remove old notification
                this.notificationService.removeNotification(notification);
                delete this.notifications[response.id];
            }
        };
        /**
         * Handle save progress.
         */
        VisualQuerySaveService.prototype.onSaveNext = function (request, response) {
            // Find or create notification
            var notification = this.notifications[response.id];
            if (notification == null && response.status !== rest_model_1.SaveResponseStatus.SUCCESS) {
                notification = this.notificationService.addNotification(this.getMessage(request), "transform");
                notification.loading = (response.status === rest_model_1.SaveResponseStatus.PENDING);
                this.notifications[response.id] = notification;
            }
            // Add success notification
            if (response.status === rest_model_1.SaveResponseStatus.SUCCESS) {
                if (request.tableName) {
                    this.notificationService.addNotification("Transformation saved to " + request.tableName, "grid_on");
                }
                else {
                    var download = this.notificationService.addNotification("Transformation ready for download", "file_download");
                    download.callback = function () { return window.open(response.location, "_blank"); };
                }
                // Remove old notification
                if (notification) {
                    this.notificationService.removeNotification(notification);
                    delete this.notifications[response.id];
                }
            }
        };
        VisualQuerySaveService.$inject = ["$mdDialog", "NotificationService"];
        return VisualQuerySaveService;
    }());
    exports.VisualQuerySaveService = VisualQuerySaveService;
    angular.module(require("../module-name"))
        .service("VisualQuerySaveService", VisualQuerySaveService);
});
//# sourceMappingURL=save.service.js.map