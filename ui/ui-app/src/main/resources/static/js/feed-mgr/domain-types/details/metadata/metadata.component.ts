import * as angular from "angular";

import {moduleName} from "../../module-name";
import {DomainTypeDetailsService} from "../../services/details.service";
import {AbstractSectionComponent} from "../abstract-section.component";
import { Component, Input } from "@angular/core";
import { TdDialogService } from "@covalent/core/dialogs";
import { IconPickerDialog } from "../../../../common/icon-picker-dialog/icon-picker-dialog.component";
import { FeedTagService } from "../../../services/FeedTagService";

/**
 * Metadata section of the {@link DomainTypeDetailsComponent}.
 */
@Component({
    selector:'domain-type-metadata-details',
    templateUrl: 'js/feed-mgr/domain-types/details/metadata/metadata.component.html'
})
export class DomainTypeMetadataDetailsComponent extends AbstractSectionComponent {

    @Input() allowEdit: any;
    @Input() model: any;

    constructor(DomainTypeDetailsService: DomainTypeDetailsService, 
                private FeedTagService: FeedTagService,
                private dialog: TdDialogService) {

        super(DomainTypeDetailsService);
    }

    /**
     * Indicates if the domain type can be deleted. The main requirement is that the domain type exists.
     */
    get canDelete() {
        return !this.isNew;
    }

    /**
     * Saves changes to the model.
     */
    onSave() {
        this.onUpdate({
            title: this.editModel.title,
            description: this.editModel.description,
            icon: this.editModel.icon,
            iconColor: this.editModel.iconColor
        });
    }

    /**
     * Shows the icon picker dialog.
     */
    showIconPicker() {
        this.dialog.open(IconPickerDialog, {
            data: {
                iconModel: this.editModel
            },
            panelClass: "full-screen-dialog"
        }).afterClosed().subscribe((msg: any) => {
            if (msg) {
                this.editModel.icon = msg.icon;
                this.editModel.iconColor = msg.color;
            }
        });

    }
}
