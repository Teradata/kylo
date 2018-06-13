import * as angular from 'angular';
import * as _ from "underscore";
import {Component, OnInit,Input,Output,EventEmitter} from "@angular/core";
import {PolicyInputFormService} from "./policy-input-form.service";
import {FlexLayoutModule} from "@angular/flex-layout";
import {AbstractControl,FormControl, FormGroup, Validators} from '@angular/forms';
import {MatDatepickerModule} from '@angular/material/datepicker'
import {ValidatorFn} from "@angular/forms/src/directives/validators";
//requires CovalentChipsModule


export function MultipleEmail(control: FormControl) {

        var EMAIL_REGEXP = /^(?=.{1,254}$)(?=.{1,64}@)[-!#$%&'*+/0-9=?A-Z^_`a-z{|}~]+(\.[-!#$%&'*+/0-9=?A-Z^_`a-z{|}~]+)*@[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?(\.[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$/;
        let emails :string = control.value;
        let invalidEmail  = emails.split(',').find((email: string) => !EMAIL_REGEXP.test(email.trim()));
        let isValid = invalidEmail == undefined;
        return isValid ? null : { 'multipleEmails': 'invalid email' }

    }


@Component({
    selector: "policy-input-form",
    templateUrl: "js/feed-mgr/shared/field-policies-angular2/policy-input-form.component.html"
})
export class PolicyInputFormComponent implements OnInit {

    @Input()
    rule:any;

    @Input()
    theForm:any;

    @Input()
    feed?: string
    @Input()
    mode: string //NEW or EDIT

    @Input()
    onPropertyChange :any

    editChips:any;

    formControls:any = {};

    formGroup :FormGroup;


    constructor(private policyInputFormService:PolicyInputFormService) {
        this.editChips = {};
        this.editChips.selectedItem = null;
        this.editChips.searchText = null;


    }
    ngOnInit() {
        this.formGroup = new FormGroup({});

        //call the onChange if the form initially sets the value
        if(this.onPropertyChange != undefined && angular.isFunction(this.onPropertyChange)) {
            _.each(this.rule.properties,  (property:any) => {
                if ((property.type == 'select' || property.type =='feedSelect' || property.type == 'currentFeed') && property.value != null) {
                    this.onPropertyChange()(property);
                }
            });
        }
        _.each(this.rule.properties,(property) => this.createFormControls(property));
    }


    private createFormControls(property:any) {
        let validatorOpts :any[] = [];
        let formControlConfig = {}
        if(property.patternRegExp){
            validatorOpts.push(Validators.pattern(property.patternRegExp))
        }
        if(property.required){
            validatorOpts.push(Validators.required)
        }
        if(property.type == "emails"){
            validatorOpts.push(MultipleEmail)
        }
        if(property.type == "email"){
            validatorOpts.push(Validators.email)
        }
        if(!this.rule.editable){
            formControlConfig = {value:property.value,disabled:true}
        }
        let fc = new FormControl(formControlConfig,validatorOpts);
        this.formGroup.addControl(property.formKey,fc)
    }

    queryChipSearch = this.policyInputFormService.queryChipSearch;
    transformChip = this.policyInputFormService.transformChip;


    validateRequiredChips(property:any) {
        return this.policyInputFormService.validateRequiredChips(this.theForm, property);
    }

    onPropertyChanged(property:any){
        if(this.onPropertyChange != undefined && angular.isFunction(this.onPropertyChange)){
            this.onPropertyChange()(property);
        }
    }

}
