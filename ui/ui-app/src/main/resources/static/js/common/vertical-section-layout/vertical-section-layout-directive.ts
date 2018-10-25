import * as angular from "angular";
import {moduleName} from "../module-name";

export default class VerticalSectionLayout {

    showVerticalCheck: any;
    allowEdit: any;
    sectionTitle: any;
    formName: any;
    onDelete: any;
    isDeleteVisible: any;
    allowDelete: any;
    onEdit: any;
    onSaveEdit: any;
    onCancelEdit: any;
    editable: any;
    keepEditableAfterSave: any;
    isValid: any;
    theForm: any;

    $onInit() {
        this.ngOnInit();
    }

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

    edit(ev: any) {
        this.editable = true;
        this.onEdit(ev);
    }

    cancel(ev: any) {
        this.onCancelEdit(ev);
        this.editable = false;
    }

    save(ev: any) {
        this.onSaveEdit(ev);
        if (!this.keepEditableAfterSave) {
            this.editable = false;
        }
    }

    delete(ev: any) {
        if (this.onDelete) {
            this.onDelete(ev);
        }
    }
}

angular.module(moduleName).component("verticalSectionLayout",{
    controller: VerticalSectionLayout,
    bindings: {
        showVerticalCheck: '=?',
        allowEdit: '=?',
        sectionTitle: '@',
        formName: '@',
        onDelete: '&?',
        isDeleteVisible: '=?',
        allowDelete: '=?',
        onEdit: '&',
        onSaveEdit: '&',
        onCancelEdit: '&',
        editable: '=?',
        keepEditableAfterSave: '=?',
        isValid: '=?',
        theForm: '=?'
    },
    transclude: {
        'readonly': '?readonlySection',
        'editable': '?editableSection'
    },
    templateUrl: './vertical-section-layout-template.html'
});
