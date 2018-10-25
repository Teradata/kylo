import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';

import {DynamicFormService} from "./services/dynamic-form.service";
import {FieldConfig} from "./model/FieldConfig";
import {FieldGroup} from "./model/FieldGroup";


@Component({
    selector: 'dynamic-form',
    templateUrl: './dynamic-form.component.html'
})
export class DynamicFormComponent implements OnInit, OnChanges {

    @Input()
    public fieldGroups: FieldGroup[] = [];

    @Input()
    public fields: FieldConfig<any>[];

    @Input()
    public form: FormGroup;

    @Input()
    public readonly: boolean;

    @Output()
    onFormControlsAdded: EventEmitter<FormControl[]> = new EventEmitter<FormControl[]>();


    payLoad = '';

    constructor(private dynamicFormService: DynamicFormService) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.addFields();
    }

    ngOnInit() {
        this.addFields();
    }

    addFields() {
        if (this.fieldGroups == undefined) {
            this.fieldGroups = [];
        }
        if (this.fieldGroups.length == 0 && this.fields != undefined) {
            //add the fields to a column group
            let defaultGroup = new FieldGroup()
            defaultGroup.fields = this.fields;
            this.fieldGroups.push(defaultGroup)
        }
        let allControls: FormControl[] = []
        this.fieldGroups.forEach(group => {
            let controls = this.dynamicFormService.addToFormGroup(group.fields, this.form);
            if (controls) {
                controls.forEach(control => allControls.push(control));
            }
        });
        //notify any subscribers the form controls have been added
        this.onFormControlsAdded.emit(allControls);
    }


    onSubmit() {
        this.payLoad = JSON.stringify(this.form.value);
    }
}