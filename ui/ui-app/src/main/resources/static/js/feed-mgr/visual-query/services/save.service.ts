import * as angular from "angular";
import "rxjs/add/operator/let";
import "rxjs/add/operator/share";
import {Observable} from "rxjs/Observable";

import {KyloNotification, NotificationService} from "../../../services/notification.service";
import {QueryEngine} from "../wrangler";
import {SaveRequest, SaveResponse, SaveResponseStatus} from "../wrangler/api/rest-model";

/**
 * Handles saving a transformation.
 */
export class VisualQuerySaveService {

    /**
     * Map of save id to notification.
     */
    private notifications: { [k: string]: KyloNotification } = {};

    static readonly $inject: string[] = ["$mdDialog", "NotificationService"];

    constructor(private $mdDialog: angular.material.IDialogService, private notificationService: NotificationService) {

    }

    /**
     * Saves the specified transformation.
     *
     * @param request - destination
     * @param engine - transformation
     * @returns an observable to tracking the progress
     */
    save(request: SaveRequest, engine: QueryEngine<any>): Observable<SaveResponse> {
        const save = engine.saveResults(request).share();
        save.subscribe(response => this.onSaveNext(request, response), response => this.onSaveError(response));
        return save;
    }

    /**
     * Gets a notification message for the specified save request.
     */
    private getMessage(request: SaveRequest) {
        if (request.tableName) {
            return "Saving transformation to " + request.tableName;
        } else {
            return "Preparing transformation for download";
        }
    }

    /**
     * Handles save errors.
     */
    private onSaveError(response: SaveResponse) {
        const notification = this.notifications[response.id];
        if (notification) {
            // Add error notification
            const error = this.notificationService.addNotification("Failed to save transformation", "error");
            if (response.message) {
                const message = (response.message.length <= 1024) ? response.message : response.message.substr(0, 1021) + "...";
                error.callback = () => {
                    this.$mdDialog.show(
                        this.$mdDialog.alert()
                            .parent(angular.element("body"))
                            .clickOutsideToClose(true)
                            .title("Error saving the transformation")
                            .textContent(message)
                            .ariaLabel("Save Failed")
                            .ok("Got it!")
                    );
                };
            }

            // Remove old notification
            this.notificationService.removeNotification(notification);
            delete this.notifications[response.id];
        }
    }

    /**
     * Handle save progress.
     */
    private onSaveNext(request: SaveRequest, response: SaveResponse) {
        // Find or create notification
        let notification = this.notifications[response.id];
        if (notification == null && response.status !== SaveResponseStatus.SUCCESS) {
            notification = this.notificationService.addNotification(this.getMessage(request), "transform");
            notification.loading = (response.status === SaveResponseStatus.PENDING);
            this.notifications[response.id] = notification;
        }

        // Add success notification
        if (response.status === SaveResponseStatus.SUCCESS) {
            if (request.tableName) {
                this.notificationService.addNotification("Transformation saved to " + request.tableName, "grid_on");
            } else {
                const download = this.notificationService.addNotification("Transformation ready for download", "file_download");
                download.callback = () => window.open(response.location, "_blank");
            }

            // Remove old notification
            if (notification) {
                this.notificationService.removeNotification(notification);
                delete this.notifications[response.id];
            }
        }
    }
}

angular.module(require("../module-name"))
    .service("VisualQuerySaveService", VisualQuerySaveService);
