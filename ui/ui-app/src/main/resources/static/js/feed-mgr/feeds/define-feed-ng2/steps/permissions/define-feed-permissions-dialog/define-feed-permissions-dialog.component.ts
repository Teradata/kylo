import {Component, Inject, OnInit, ViewChild} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {Feed} from "../../../../../model/feed/feed.model";
import {EntityAccessControlComponent} from "../../../../../shared/entity-access-control/entity-access-control.component";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DefineFeedService} from "../../../services/define-feed.service";

export class DefineFeedPermissionsDialogComponentData {
    constructor(public feed: Feed) {

    }
}

@Component({
    selector: "define-feed-permissions-dialog",
    styleUrls: ["./define-feed-permissions-dialog.component.scss"],
    templateUrl: "./define-feed-permissions-dialog.component.html"
})

export class DefineFeedPermissionsDialogComponent implements OnInit {

    formGroup: FormGroup;
    feed: Feed;
    saving: boolean;
    saveButtonLabelKey: string;
    cancelButtonLabelKey: string;

    @ViewChild("entityAccessControl")
    private entityAccessControl: EntityAccessControlComponent;

    ngOnInit(): void {
        this.saving = false;
        this.saveButtonLabelKey = "views.entity-access-control.Save";
        this.cancelButtonLabelKey = "views.entity-access-control.Cancel";
    }

    constructor(private dialog: MatDialogRef<DefineFeedPermissionsDialogComponent>,
                @Inject(MAT_DIALOG_DATA) public data: DefineFeedPermissionsDialogComponentData,
                private defineFeedService: DefineFeedService
                ) {
        this.feed = this.data.feed;
        this.formGroup = new FormGroup({});
    }

    /**
     * Cancel the dialog (no save)
     */
    cancel() {
        this.dialog.close();
        this.saveButtonLabelKey = "views.entity-access-control.Save";
        this.saving = false;
    }

    /**
     * Save permissions
     */
    savePermissions() {
        this.saveButtonLabelKey = "views.entity-access-control.Saving";
        this.saving = true;
        this.entityAccessControl.onSave()
            .subscribe((updatedRoleMemberships) => {
                this.defineFeedService.updateFeedRoleMemberships(updatedRoleMemberships);
                this.dialog.close();
                this.saveButtonLabelKey = "views.entity-access-control.Save";
                this.saving = false;
            }, error => {
                this.saveButtonLabelKey = "views.entity-access-control.Save";
                this.saving = false;
            });
    }

    /**
     * Are permissions being saved?
     * @returns {boolean}
     */
    isSaving() {
        return this.saving;
    }
}