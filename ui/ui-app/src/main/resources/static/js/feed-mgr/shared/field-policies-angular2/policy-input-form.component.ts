import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges, ViewChild} from "@angular/core";
import {PolicyInputFormService} from "../../../../lib/feed-mgr/shared/field-policies-angular2/policy-input-form.service";
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {FieldConfig} from "../../../../lib/dynamic-form/model/FieldConfig";
import {InputType} from "../../../../lib/dynamic-form/model/InputText";
import {DynamicFormService} from "../../../../lib/dynamic-form/services/dynamic-form.service";
import {FieldPolicyProperty, FieldPolicySelectableValue} from "../../model/field-policy";
import {DynamicFormFieldGroupBuilder} from "../../../../lib/dynamic-form/services/dynamic-form-field-group-builder";
import {DynamicFormBuilder} from "../../../../lib/dynamic-form/services/dynamic-form-builder";
import {FieldGroup, Layout} from "../../../../lib/dynamic-form/model/FieldGroup";
import {ConfigurationFieldBuilder, RadioButtonFieldBuilder, SelectFieldBuilder} from "../../../../lib/dynamic-form/services/field-config-builder";
import {DynamicFormComponent} from "../../../../lib/dynamic-form/dynamic-form.component";

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
    templateUrl: "./policy-input-form.component.html"
})
export class PolicyInputFormComponent implements OnInit, OnChanges, OnDestroy {


    /**
     * Optional arbitrary string which groups policy-input-forms where provided 'index' is unique.
     * This is required if there are multiple policy-input-forms in the same 'parentFormGroup'.
     */
    @Input()
    groupId?: string;

    /**
     * Unique index
     */
    @Input()
    index: number;

    @Input()
    rule: any;

    /**
     * Parent form group which will host this form group
     */
    @Input()
    parentFormGroup: FormGroup;


    formGroup: FormGroup;

    @Output()
    onPropertyChange: EventEmitter<FieldPolicyProperty> = new EventEmitter<FieldPolicyProperty>();

    @Output()
    onFormControlsAdded: EventEmitter<any> = new EventEmitter<any>();

    @ViewChild(DynamicFormComponent)
    dynamicForm: DynamicFormComponent;

    editChips: any;

    formBuilder: DynamicFormBuilder

    fieldConfigGroup: RuleGroupWithFieldConfig[] = []


    fieldGroups: FieldGroup[] = [];

    initialized: boolean = false;
    private POLICY_FORM = "policyForm_";
    private policyFormId: string;

    constructor(private policyInputFormService: PolicyInputFormService, private dynamicFormService: DynamicFormService) {
        this.editChips = {};
        this.editChips.selectedItem = null;
        this.editChips.searchText = null;

        if (this.formGroup == undefined) {
            this.formGroup = new FormGroup({});
        }

        this.formBuilder = new DynamicFormBuilder().setForm(this.formGroup)


    }

    ngOnInit() {
        this.policyFormId = (this.groupId ? this.groupId + '_' : '') + this.POLICY_FORM + this.index;
        if (this.parentFormGroup != undefined) {
            this.parentFormGroup.registerControl(this.policyFormId, this.formGroup);
        }
        if (this.rule) {
            this.buildAndSetFieldGroups();
        }
        else {
            this.rule = {name: ''}
        }
        this.initialized = true;
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes && changes.rule && this.initialized) {
            let propertiesAdded = this.buildAndSetFieldGroups();
            this.dynamicForm.addFields();
            if (propertiesAdded == 0) {
                //force the call to emit the formControlsAdded
                //otherwise it will be called from the dynamic-form component after adding to the formgroup
                this.onFormControlsAdded.emit([]);
            }
        }
    }

    ngOnDestroy(): void {
        if (this.parentFormGroup != undefined) {
            this.parentFormGroup.removeControl(this.policyFormId);
        }
    }


    formControlsAdded(controls: FormControl[]) {
        this.onFormControlsAdded.emit(controls);
    }

    resetForm() {
        this.formBuilder.resetForm();
    }

    /**
     * reset the form and build the new fields
     *
     * @return {number}the number of form fields added
     */
    private buildAndSetFieldGroups(): number {
        let propertyCount = 0;
        let order = 0;
        //clear old controls
        this.formBuilder.resetForm();
        this.rule.groups.forEach((group: any, idx: number) => {
            if (group.properties) {

                let formGroupBuilder: DynamicFormFieldGroupBuilder = null;
                let layout = group.layout;
                if (layout == Layout.ROW) {
                    formGroupBuilder = this.formBuilder.row();
                }
                else {
                    formGroupBuilder = this.formBuilder.column();
                }

                let fieldConfigList: FieldConfig<any>[] = [];
                group.properties.forEach((property: any) => {
                    if (property.hidden == false) {
                        if (this.addField(property, order, formGroupBuilder)) {
                            propertyCount++;
                        }
                        order++;
                    }
                });
            }
        });
        //clear the array
        this.fieldGroups.length = 0
        let newGroups = this.formBuilder.buildFieldConfiguration();
        newGroups.forEach(group => {
            this.fieldGroups.push(group);
        })
        //this.fieldGroups = this.formBuilder.buildFieldConfiguration();
        return propertyCount;
    }


    private addField(property: FieldPolicyProperty, order: number, formGroupBuilder: DynamicFormFieldGroupBuilder): boolean {
        let added = false;
        //build the generic options to be used by all fields
        let label = property.displayName || property.placeholder;
        //   values: property.values
        let configBuilder = new ConfigurationFieldBuilder().setKey(property.formKey).setOrder(order).setDisabled(!this.rule.editable).setPlaceholder(label).setRequired(property.required).setValue(property.value).setPattern(property.pattern).setModel(property).setHint(property.hint)
            .onChange((newValue: any, form: FormGroup, model?: any) => {
                this.onPropertyChange.emit(<FieldPolicyProperty>model)
            });
        if (property.pattern) {
            configBuilder.addValidator(Validators.pattern(property.pattern))
        }
        if (property.patternRegExp) {
            configBuilder.addValidator(Validators.pattern(property.patternRegExp))
        }


        if (this.isInputText(property)) {

            //create the field
            let builder = formGroupBuilder.text().update(configBuilder)
            //get the correct input type
            let type = property.type;
            if (type == "string") {
                type = "text";
            }
            else if (type == "regex") {
                type = "text";
            }
            else if (type == "cron") {
                type = "cron";
            }
            else if (type == "emails") {
                type = "email";
                builder.addValidator(MultipleEmail)
            }
            else if (type == "email") {
                builder.addValidator(Validators.email)
            }
            else {
                type = "text";
            }
            let inputType: InputType = <InputType>InputType[type] || InputType.text;
            builder.setType(inputType)

            added = true;

        }
        else if (this.isSelect(property)) {
            let builder: SelectFieldBuilder = formGroupBuilder.select().update(configBuilder);
            if (property.selectableValues.length > 0) {
                builder.setOptions(property.selectableValues)
                //TODO know when to change the model propertyValue to 'values' on multi select
                //if the valud doesnt exist in the selectable values, null it out
                if(property.value && property.selectableValues.find((p:FieldPolicySelectableValue)=> p.value == property.value) == undefined){
                   builder.setValue(null)
                }
            }
            else if (property.values && property.values.length > 0) {
                builder.setOptionsArray((<any[]>property.values))
            }
            added = true
        }
        else if (this.isRadio(property)) {
            let builder: RadioButtonFieldBuilder = formGroupBuilder.radio().update(configBuilder);
            if (property.selectableValues.length > 0) {
                builder.setOptions(property.selectableValues)
                //TODO know when to change the model propertyValue to 'values' on multi select
                // fieldConfigOptions.modelValueProperty = 'values';
                //if the valud doesnt exist in the selectable values, null it out
                if(property.value && property.selectableValues.find((p:FieldPolicySelectableValue)=> p.value == property.value) == undefined){
                    builder.setValue(null)
                }
            }
            else if (property.values && property.values.length > 0) {
                builder.setOptionsArray((<any[]>property.values))
            }
        }
        else if (this.isChip(property)) {
            let items: any[] = [];
            if (property.selectableValues.length > 0) {
                items = property.selectableValues;
            }
            formGroupBuilder.chips().update(configBuilder).setModelValueProperty("values").setItems(items).setValue(property.values).setStacked(true);
            added = true
        }
        else if (this.isCheckbox(property)) {
            formGroupBuilder.checkbox().update(configBuilder).setTrueValue("true").setFalseValue("false")
            added = true
        }
        else if (this.isTextarea(property)) {
            formGroupBuilder.textarea().update(configBuilder)
            added = true
        }
        return added;
    }


    private isInputText(property: FieldPolicyProperty) {
        return (property.type == null || property.type == "string" || property.type == "text" || property.type == "email"
            || property.type == "number" || property.type == "password" || property.type == 'regex' || property.type == 'email'
            || property.type == 'emails' || property.type == 'cron');
    }

    private isSelect(property: FieldPolicyProperty) {
        return property.type == 'select' || property.type == 'feedSelect' || property.type == 'currentFeed' || property.type == 'velocityTemplate';
    }

    private isCheckbox(property: FieldPolicyProperty) {
        return property.type == 'checkbox';
    }

    private isTextarea(property: FieldPolicyProperty) {
        return property.type == 'textarea';
    }


    private isRadio(property: FieldPolicyProperty) {
        return property.type == 'radio';
    }


    private isChip(property: FieldPolicyProperty) {
        return property.type == 'feedChips';
    }

    private toFieldConfigOptions(property: FieldPolicyProperty): any {
        let key: string = property.formKey;
        let options = {
            key: key,
            required: property.required,
            placeholder: property.displayName,
            value: property.value,
            hint: property.hint,
            pattern: property.patternRegExp,
            values: property.values
        };
        return options;

    }

    /**
     queryChipSearch = this.policyInputFormService.queryChipSearch;
     transformChip = this.policyInputFormService.transformChip;


     validateRequiredChips(property: any) {
        return this.policyInputFormService.validateRequiredChips(this.formGroup, property);
    }
     **/

}
