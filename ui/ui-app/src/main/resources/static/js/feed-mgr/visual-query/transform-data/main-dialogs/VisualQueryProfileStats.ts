import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

/**
 * Controls the Profile dialog of the Visual Query Transform page.
 *
 * @param $scope the application model
 * @param $mdDialog the dialog service
 * @param profile the profile model data
 * @constructor
 */
@Component({
    templateUrl: "js/feed-mgr/visual-query/transform-data/main-dialogs/profile-stats-dialog.html"
})
export default class VisualQueryProfileStatsController implements ng.IComponentController {

    constructor(private dialog: MatDialogRef<VisualQueryProfileStatsController>, @Inject(MAT_DIALOG_DATA) private profile: any) {
    }

    /**
     * Closes the dialog.
     */
    cancel() {
        this.dialog.close();
    }
}
