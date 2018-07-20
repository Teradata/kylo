import * as angular from "angular";
import {Component, EventEmitter, Input,Output} from "@angular/core";

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

    @Input() showVerticalCheck: boolean;
    @Input() allowEdit: boolean;
    @Input() sectionTitle: string;
    @Input() formName: string;

    @Input() isDeleteVisible: boolean;
    @Input() allowDelete: boolean;
    @Input() editable: boolean;
    @Input() keepEditableAfterSave: boolean;
    @Input() isValid: any;
    @Input() theForm: any;

    @Output() onEdit:EventEmitter<any> = new EventEmitter<any>();
    @Output() onSaveEdit:EventEmitter<any> = new EventEmitter<any>();
    @Output() onCancelEdit:EventEmitter<any> = new EventEmitter<any>();
    @Output() onDelete:EventEmitter<any> = new EventEmitter<any>();

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

    edit(ev: any){
        this.editable = true;
        if(this.onEdit) {
            this.onEdit.emit(ev);
        }
    }

    cancel(ev: any){
        if(this.onCancelEdit) {
            this.onCancelEdit.emit()
        }
        this.editable = false;
    }

    save(ev: any){
        if(this.onSaveEdit) {
            this.onSaveEdit.emit()
        }
        if (!this.keepEditableAfterSave) {
            this.editable = false;
        }
    }

    delete(ev: any){
        if (this.onDelete) {
            this.onDelete.emit()
        }
    }
}