import * as angular from "angular";
import {Component, Input} from "@angular/core";

@Component({
    selector: "vertical-section-layout",
    templateUrl: "js/common/vertical-section-layout/vertical-section-layout-template.html",
    styles:[
        `.ng-md-icon {
            margin: auto;
            background-repeat: no-repeat no-repeat;
            display: inline-block;
            vertical-align: middle;
            fill: currentColor;
            height: 24px;
            width: 24px;
        }`
    ]
})
export class VerticalSectionLayoutComponent {

    @Input() showVerticalCheck: any;
    @Input() allowEdit: any;
    @Input() sectionTitle: any;
    @Input() formName: any;
    @Input() onDelete: any;
    @Input() isDeleteVisible: any;
    @Input() allowDelete: any;
    @Input() onEdit: any;
    @Input() onSaveEdit: any;
    @Input() onCancelEdit: any;
    @Input() editable: any;
    @Input() keepEditableAfterSave: any;
    @Input() isValid: any;
    @Input() theForm: any;

    ngOnInit() {
        /**
         * Delete button is visible if this flag is true and if the method onDelete is set
         */
        if (this.isDeleteVisible == undefined) {
            this.isDeleteVisible = true;
        }

        if (this.editable == undefined) {
            this.editable = false;
        }

        if (this.showVerticalCheck == undefined) {
            this.showVerticalCheck = true;
        }

        if (this.allowEdit == undefined) {
            this.allowEdit = true;
        }
        if (this.isValid == undefined) {
            this.isValid = true;
        }

        if (this.keepEditableAfterSave == undefined) {
            this.keepEditableAfterSave = false;
        }

    }

    edit = (ev: any) => {
        this.editable = true;
        this.onEdit(ev);
    }

    cancel = (ev: any) => {
        this.onCancelEdit(ev);
        this.editable = false;
    }

    save = (ev: any) => {
        this.onSaveEdit(ev);
        if (!this.keepEditableAfterSave) {
            this.editable = false;
        }
    }

    delete = (ev: any) => {
        if (this.onDelete) {
            this.onDelete(ev);
        }
    }
}