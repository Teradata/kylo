import {Component, Inject, Injector, OnInit} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Feed} from "../../model/feed/feed.model";
import {FormGroup} from "@angular/forms";
import {PolicyInputFormService} from "../field-policies-angular2/policy-input-form.service";
import * as angular from 'angular';
import * as _ from "underscore";

interface ViewText {
    modeText:string;
    title:string;
    titleText:string;
    addText:string;
    cancelText:string;
}

enum EditMode { NEW=1, EDIT=2 }

@Component({
    selector:"feed-precondition-dialog",
    templateUrl: "js/feed-mgr/shared/feed-precondition/feed-precondition-dialog.component.html"
})
export class FeedPreconditionDialogComponent implements OnInit{

    private loading = false;
    public preconditionForm:FormGroup;
    options:any[] = [];
    ruleType:any = null;
    editPrecondition:any = null;
    editIndex :number = null;
    editMode:EditMode = EditMode.NEW;
    preconditions: any[]= null;
    pendingEdits :boolean = false;
    isFormValid = false;

    viewText :ViewText = {modeText:"ADD",title:"Add precondition",titleText:"Add new precondition", addText:"ADD", cancelText:"CANCEL ADD"}

    constructor(public dialogRef: MatDialogRef<FeedPreconditionDialogComponent>,
                private policyInputFormService :PolicyInputFormService,
                private injector: Injector,
                @Inject(MAT_DIALOG_DATA) public data: Feed){
        let feedService = injector.get("FeedService");
        this.preconditionForm = new FormGroup({});
        let feed = this.policyInputFormService.currentFeedValue(this.data);
        feedService.getPossibleFeedPreconditions().then((response:any) => {
            this.options = this.policyInputFormService.groupPolicyOptions(response.data, feed);
        });
        this.ruleTypesAvailable();
        this.preconditionForm.statusChanges.debounceTime(10).subscribe(status => {
            this.isFormValid = status == "VALID";
        })
    }

    ngOnInit(): void {
    }

    cancelEdit() {
        this.ruleType = null;
        this.editPrecondition = this.emptyRule();
        this.viewText.addText = 'Add precondition';
        this.viewText.cancelText = 'CANCEL ADD';
        this.viewText.titleText = 'Add new precondition';
    }

    private emptyRule():any {
        return {name:"",groups:[],editable:false};
    }

    cancel() {
        this.cancelEdit();
        this.dialogRef.close();
    }

    /**
     * When a user adds a new precondition
     */
    addPrecondition(){

        var validForm = this.validateForm();
        if (validForm == true) {
            if (this.preconditions == null) {
                this.preconditions = [];
            }
            console.log(this.editPrecondition);
            this.editPrecondition.ruleType = this.ruleType;
            if (this.editMode == EditMode.NEW) {
                this.preconditions.push(this.editPrecondition);
            }
            else if (this.editMode == EditMode.EDIT) {
                this.preconditions[this.editIndex] = this.editPrecondition;
            }

            this.pendingEdits = true;
            this.cancelEdit();
        }
    }

    private validateForm(){
        return true;
    }

    deletePrecondition($index?: number) {
        if($index == undefined){
            $index = this.editIndex;
        }

        if (this.preconditions != null && $index != null) {
            this.preconditions.splice($index, 1);
        }
        this.pendingEdits = true;
        this.cancelEdit();
    }

    /**
     * react to when the form controls are painted on the screen
     * @param controls
     */
    onFormControlsAdded(controls:any){
        this.preconditionForm.updateValueAndValidity();
    }

    done() {
        this.addPrecondition();
        this.dialogRef.close('done');
    }

    ruleTypesAvailable() {
        if (this.editPrecondition != null) {
            this.ruleType = this.findRuleType(this.editPrecondition.name, this.editPrecondition.type);
        }
    }

    findRuleType(ruleName: any, type: any) {
        return _.find(this.options, (opt:any) => {
            return opt.name == ruleName && opt.type == type;
        });
    }

    onRuleTypeChange(selectedValue:any) {
        if (this.ruleType != null) {
            var rule = angular.copy(this.ruleType);
            rule.groups = this.policyInputFormService.groupProperties(rule);
            this.policyInputFormService.updatePropertyIndex(rule);
            //make all rules editable
            rule.editable = true;
            this.editPrecondition = rule;
        }
        else {
            this.editPrecondition = null;
        }
    }
}