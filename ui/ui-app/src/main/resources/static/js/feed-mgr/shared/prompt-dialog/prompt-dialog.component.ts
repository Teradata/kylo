import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {FormControl} from '@angular/forms';
import * as angular from 'angular';


export class PromptDialogData {
    value: string;
    placeholder: string;
    hint: string;
    title: string;
    disableClose: boolean = false;
    okButtonLabel: string = "Ok";
    cancelButtonLabel: string = "Cancel";
}

export class PromptDialogResult {
    isValueUpdated: boolean;
    value: string;

    constructor(updated: boolean, value: any) {
        this.isValueUpdated = updated;
        this.value = value;
    }
}

/**
 * Unlike standard Covalent dialogs, this PromptDialogComponent allows to remove existing value
 */
@Component({
    selector: 'prompt-dialog',
    styleUrls: ['./prompt-dialog.component.scss'],
    templateUrl: './prompt-dialog.component.html',
})
export class PromptDialogComponent implements OnInit {

    inputControl = new FormControl();

    constructor(private selfReference: MatDialogRef<PromptDialogComponent>,
                @Inject(MAT_DIALOG_DATA) public data: PromptDialogData) {
    }

    public ngOnInit(): void {
        const copy = angular.copy(this.data.value);
        this.inputControl.setValue(copy);
        this.selfReference.disableClose = this.data.disableClose;
    }

    onOk() {
        this.selfReference.close(new PromptDialogResult(this.isValueUpdated(), this.inputControl.value));
    }

    onCancel() {
        this.selfReference.close(new PromptDialogResult(false, this.data.value));
    }

    private isValueUpdated(): boolean {
        return this.inputControl.value !== this.data.value;
    }
}
