import * as angular from "angular";

import {DomainType} from "../../../services/DomainTypesService.d";
import {DomainTypeDetailsService} from "../../services/details.service";
import {AbstractSectionComponent} from "../abstract-section.component";
import { Component, Input } from "@angular/core";
import {FeedFieldPolicyRuleDialogComponent } from "../../../shared/feed-field-policy-rules/FeedFieldPolicyRuleDialog";
import {FeedFieldPolicyRuleService} from "../../../shared/feed-field-policy-rules/services/FeedFieldPolicyRuleService";
import { FeedService } from "../../../services/FeedService";
import { MatDialog } from "@angular/material/dialog";
import { FeedTagService } from "../../../services/FeedTagService";

/**
 * Properties section of the {@link DomainTypeDetailsComponent}.
 */
@Component({
    selector: 'domain-type-rules-details',
    templateUrl: 'js/feed-mgr/domain-types/details/rules/rules.component.html'
})    
export class DomainTypeRulesDetailsComponent extends AbstractSectionComponent {

    @Input() allowEdit: any;
    @Input() model: any;
    /**
     * Standard data types for column definitions
     */
    availableDefinitionDataTypes: string[] = [];

    /**
     * Metadata for the tags.
     */
    tagChips: any = {searchText: null, selectedItem: null};

    tagList : string[] = [];


    constructor(DomainTypeDetailsService: DomainTypeDetailsService, 
                private FeedFieldPolicyRuleService: FeedFieldPolicyRuleService, 
                private FeedService: FeedService,
                private FeedTagService: FeedTagService,
                private dialog: MatDialog) {
        super(DomainTypeDetailsService);
        this.availableDefinitionDataTypes = FeedService.columnDefinitionDataTypes.slice();
    }

    /**
     * Gets a list of all field policies for the specified domain type.
     */
    getAllFieldPolicies(domainType: DomainType) {
        return this.FeedFieldPolicyRuleService.getAllPolicyRules(domainType.fieldPolicy);
    }

    /**
     * Indicates if the specified domain type has any field policies.
     */
    hasFieldPolicies(domainType: DomainType) {
        return (domainType.fieldPolicy.standardization.length > 0 || domainType.fieldPolicy.validation.length > 0);
    }

    /**
     * Updates the field when the data type is changed.
     */
    onDataTypeChange() {
        if (this.editModel.field.derivedDataType !== "decimal") {
            this.editModel.field.precisionScale = null;
        }
    }

    /**
     * Creates a copy of the domain type model for editing.
     */
    onEdit() {
        super.onEdit();

        // Ensure tags is array
        if (!angular.isObject(this.editModel.field)) {
            this.editModel.field = {};
        }
        if (!angular.isArray(this.editModel.field.tags)) {
            this.editModel.field.tags = [];
        }
        this.tagList = this.editModel.field.tags.map((tag : any) => { return tag.name});
    }

    /**
     * Saves changes to the model.
     */
    onSave() {
        this.onUpdate({
            field: angular.copy(this.editModel.field),
            fieldPolicy: angular.copy(this.editModel.fieldPolicy)
        });
    }

    /**
     * Shows the dialog for updating field policy rules.
     */
    showFieldRuleDialog(domainType: DomainType) {

        let dialogRef = this.dialog.open(FeedFieldPolicyRuleDialogComponent, {
            data: { feed: null,
                    field: domainType.fieldPolicy },
            panelClass: "full-screen-dialog"
          });
    }

    /**
     * Transforms the specified chip into a tag.
     * @param chip - the chip
     * @returns the tag
     */
    transformChip = function (chip: string) {
        return angular.isObject(chip) ? chip : {name: chip};
    }

    addGroup = ($event : any)  => {
        let newTag = {"name": ""};
        newTag.name = $event;
        this.editModel.field.tags.push(newTag);
        this.onSave();

    }

    removeGroup = (value : string ) => {
        this.editModel.field.tags = this.editModel.field.tags.filter((group : any) => {
            return group.name !== value;
        });
    }
}
