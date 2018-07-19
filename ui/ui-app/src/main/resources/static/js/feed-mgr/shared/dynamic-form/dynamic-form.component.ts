import { Component, Input, OnInit }  from '@angular/core';
import { FormGroup }                 from '@angular/forms';

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

    payLoad = '';

    constructor(private dynamicFormService: DynamicFormService) {  }

    ngOnInit() {
        if(this.fields == undefined){
            this.fields = [];
        }
        console.log("new Form ",this.form, this.fields)
        this.dynamicFormService.addToFormGroup(this.fields, this.form);
    }

    onSubmit() {
        this.payLoad = JSON.stringify(this.form.value);
    }
}