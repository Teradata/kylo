import {Component, EventEmitter, Input, OnInit, Output, SimpleChanges} from "@angular/core";
import {PolicyInputFormService} from "./policy-input-form.service";
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {FieldConfig} from "../dynamic-form/model/FieldConfig";
import {Select} from "../dynamic-form/model/Select";
import {InputText} from "../dynamic-form/model/InputText";
import {DynamicFormService} from "../dynamic-form/services/dynamic-form.service";
import {FieldPolicyProperty} from "../../model/field-policy";
import {Chip} from "../dynamic-form/model/Chip";

export function MultipleEmail(control: FormControl) {

    var EMAIL_REGEXP = /^(?=.{1,254}$)(?=.{1,64}@)[-!#$%&'*+/0-9=?A-Z^_`a-z{|}~]+(\.[-!#$%&'*+/0-9=?A-Z^_`a-z{|}~]+)*@[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?(\.[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$/;
    let emails: string = control.value;
    let invalidEmail = emails.split(',').find((email: string) => !EMAIL_REGEXP.test(email.trim()));
    let isValid = invalidEmail == undefined;
    return isValid ? null : {'multipleEmails': 'invalid email'}

}


export interface RuleGroupWithFieldConfig {
    fields: FieldConfig<any>[];
    group: any;
}

@Component({
    selector: "policy-input-form",
    templateUrl: "js/feed-mgr/shared/field-policies-angular2/policy-input-form.component.html"
})
export class PolicyInputFormComponent implements OnInit {

    @Input()
    rule: any;

    @Input()
    parentFormGroup: FormGroup;

    formGroup: FormGroup;

    @Input()
    feed?: string
    @Input()
    mode: string //NEW or EDIT

    @Output()
    onPropertyChange: EventEmitter<FieldPolicyProperty> = new EventEmitter<FieldPolicyProperty>();

    @Output()
    onFormControlsAdded: EventEmitter<any> = new EventEmitter<any>();

    editChips: any;

    fieldConfigGroup: RuleGroupWithFieldConfig[] = []

    constructor(private policyInputFormService: PolicyInputFormService, private dynamicFormService: DynamicFormService) {
        this.editChips = {};
        this.editChips.selectedItem = null;
        this.editChips.searchText = null;

        if (this.formGroup == undefined) {
            this.formGroup = new FormGroup({});
        }

    }

    ngOnInit() {

        this.parentFormGroup.registerControl("policyForm", this.formGroup);

        //call the onChange if the form initially sets the value
        /*
          if(this.onPropertyChange) {
              _.each(this.rule.properties,  (property:any) => {
                  if ((property.type == 'select' || property.type =='feedSelect' || property.type == 'currentFeed') && property.value != null) {
                      this.onPropertyChange()(property);
                  }
              });
          }
          */

        if (this.rule) {
            this.createFormFieldConfig();
        }
        else {
            this.rule = {name: ''}
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes && changes.rule) {
            setTimeout(() => {
                //clear old controls
                Object.keys(this.formGroup.controls).forEach(controlName => this.formGroup.removeControl(controlName));
                let fieldConfigList = this.createFormFieldConfig();
                if (fieldConfigList.length == 0) {
                    //force the call to emit the formControlsAdded
                    this.formControlsAdded([]);
                }
            })
        }
    }

    formControlsAdded(controls: FormControl[]) {
        if (this.onFormControlsAdded) {
            this.onFormControlsAdded.emit(controls);
        }
    }

    private createFormFieldConfig(): FieldConfig<any>[] {
        let order = 0;
        this.fieldConfigGroup = [];
        let allFields: FieldConfig<any>[] = [];
        this.rule.groups.forEach((group: any, idx: number) => {
            if (group.properties) {
                let fieldConfigList: FieldConfig<any>[] = [];
                group.properties.forEach((property: any) => {
                    if (property.hidden == false) {
                        let fieldConfig = this.toFieldConfig(property, order);
                        fieldConfigList.push(fieldConfig);
                        allFields.push(fieldConfig);
                        order++;
                    }
                });
                this.fieldConfigGroup[idx] = {fields: fieldConfigList, group: group};
            }
        });
        return allFields;
    }

    private isInputText(property: FieldPolicyProperty) {
        return (property.type == null || property.type == "string" || property.type == "text" || property.type == "email" || property.type == "number" || property.type == "password" || property.type == 'regex' || property.type == 'email' || property.type == 'emails');
    }

    private isSelect(property: FieldPolicyProperty) {
        return property.type == 'select' || property.type == 'feedSelect' || property.type == 'currentFeed' || property.type == 'velocityTemplate';
    }

    private isChip(property: FieldPolicyProperty) {
        return property.type == 'feedChips';
    }

    private toFieldConfigOptions(property: FieldPolicyProperty): any {
        let key: string = property.formKey;
        let options = {
            key: key,
            label: property.displayName,
            required: property.required,
            placeholder: property.placeholder,
            value: property.value,
            hint: property.hint,
            pattern: property.patternRegExp,
            values: property.values
        };
        return options;

    }

    private toFieldConfig(property: FieldPolicyProperty, order: number): FieldConfig<any> {
        let fieldConfig: FieldConfig<any> = null;
        let fieldConfigOptions: any = this.toFieldConfigOptions(property);
        fieldConfigOptions.order = order;

        if (this.isInputText(property)) {
            if (property.type == "string") {
                fieldConfigOptions.type = "text";
            }
            else if (property.type == "regex") {
                fieldConfigOptions.type = "text";
            }
            else if (property.type == "emails") {
                fieldConfigOptions.type = "email";
            }
            else {
                fieldConfigOptions.type = "text";
            }
            fieldConfig = new InputText(fieldConfigOptions);
        }
        else if (this.isSelect(property)) {

            let options: any[] = [];
            if (property.selectableValues.length > 0) {
                //already in label,value objects
                options = property.selectableValues;
                //TODO know when to change the model propertyValue to 'values' on multi select
            }
            fieldConfigOptions.options = options;
            fieldConfig = new Select(fieldConfigOptions);
        }
        else if (this.isChip(property)) {
            let items: any[] = [];
            if (property.selectableValues.length > 0) {
                items = property.selectableValues;
            }
            fieldConfigOptions.items = items;
            fieldConfig = new Chip(fieldConfigOptions);
        }

        let validatorOpts: any[] = [];

        if (property.patternRegExp) {
            validatorOpts.push(Validators.pattern(property.patternRegExp))
        }
        if (property.pattern) {
            validatorOpts.push(Validators.pattern(property.pattern))
        }
        if (property.type == "emails") {
            validatorOpts.push(MultipleEmail)
        }
        if (property.type == "email") {
            validatorOpts.push(Validators.email)
        }

        fieldConfig.disabled = !this.rule.editable;
        fieldConfig.validators = validatorOpts;

        fieldConfig.model = property

        if (this.onPropertyChange) {
            fieldConfig.onModelChange = (property: FieldPolicyProperty) => this.onPropertyChange.emit(property)
        }

        return fieldConfig;
    }


    queryChipSearch = this.policyInputFormService.queryChipSearch;
    transformChip = this.policyInputFormService.transformChip;


    validateRequiredChips(property: any) {
        return this.policyInputFormService.validateRequiredChips(this.formGroup, property);
    }


}
