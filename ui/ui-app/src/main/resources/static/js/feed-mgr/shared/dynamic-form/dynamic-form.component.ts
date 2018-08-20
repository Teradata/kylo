import {Component, EventEmitter, Input, OnInit, Output, SimpleChanges} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';

import {DynamicFormService} from "./services/dynamic-form.service";
import {FieldConfig} from "./model/FieldConfig";

@Component({
    selector: 'dynamic-form',
    templateUrl: 'js/feed-mgr/shared/dynamic-form/dynamic-form.component.html'
})
export class DynamicFormComponent implements OnInit {

    @Input()
   public fields: FieldConfig<any>[];

    @Input()
    public form: FormGroup;

    @Input()
        public readonly :boolean;

    @Output()
    onFormControlsAdded :EventEmitter<FormControl[]> = new EventEmitter<FormControl[]>();


    payLoad = '';

    constructor(private dynamicFormService: DynamicFormService) {  }

    ngOnInit() {
        if(this.fields == undefined){
            this.fields = [];
        }
        console.log("new Form ",this.form, this.fields)
       let controls = this.dynamicFormService.addToFormGroup(this.fields, this.form);
        if(this.onFormControlsAdded){
            this.onFormControlsAdded.emit(controls);
        }
    }


    onSubmit() {
        this.payLoad = JSON.stringify(this.form.value);
    }
}