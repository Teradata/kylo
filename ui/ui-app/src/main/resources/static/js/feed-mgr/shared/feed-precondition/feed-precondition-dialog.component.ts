import {Component, EventEmitter, Inject, Injector, Output} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Feed} from "../../model/feed/feed.model";
import {FormGroup} from "@angular/forms";
import {PolicyInputFormService} from "../../../../lib/feed-mgr/shared/field-policies-angular2/policy-input-form.service";
import * as angular from 'angular';
import * as _ from "underscore";
import {CloneUtil} from "../../../common/utils/clone-util";

enum EditMode { NEW=1, EDIT=2 }

@Component({
    selector:"feed-precondition-dialog",
    templateUrl: "./feed-precondition-dialog.component.html"
})
export class FeedPreconditionDialogComponent{

    public preconditionForm: FormGroup = new FormGroup({});
    options: any[] = [];
    ruleType: any = null;
    editPrecondition: any = null;
    editIndex: number = null;
    editMode: EditMode = EditMode.NEW;
    preconditions: any[]= null;
    pendingEdits: boolean = false;
    isFormValid: boolean = false;
    feed: Feed;
    title: string;
    private edit: boolean = false;

    originalPreconditions :any[];

    @Output()
    preconditionsChange = new EventEmitter<any[]>();

    constructor(public dialogRef: MatDialogRef<FeedPreconditionDialogComponent>,
                private policyInputFormService :PolicyInputFormService,
                private injector: Injector,
                @Inject(MAT_DIALOG_DATA) public data: any){
        this.feed = data.feed;


        let preconditions = data.preconditions || this.feed.schedule.preconditions;
        this.editIndex = data.itemIndex;
        this.edit = this.editIndex !== undefined && this.editIndex !== null;
        this.title = "Add precondition";

        let feedService = injector.get("FeedService");
        let feed = this.policyInputFormService.currentFeedValue(this.feed);
        this.originalPreconditions = CloneUtil.shallowCopy(preconditions);
        this.preconditions =  CloneUtil.shallowCopy(preconditions);

        feedService.getPossibleFeedPreconditions().then((response:any) => {
            this.options = this.policyInputFormService.groupPolicyOptions(response.data, feed);
            if(this.edit){
                this.title = "Edit precondition";
                this.editMode = EditMode.EDIT;
                this.editPrecondition = this.preconditions[this.editIndex];
                this.ruleTypesAvailable();
                this.onRuleTypeChange(this.editPrecondition);
            }

        });
        this.preconditionForm.statusChanges.debounceTime(10).subscribe(status => {
            this.isFormValid = status == "VALID";
        })
    }

    cancelEdit() {
        this.ruleType = null;
        this.editPrecondition = this.emptyRule();
    }

    private emptyRule():any {
        return {name:"",groups:[],editable:false};
    }

    cancel() {
        this.cancelEdit();
        this.dialogRef.close(this.originalPreconditions);
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
            this.editPrecondition.properties = this.editPrecondition.groups[0].properties;
            if (this.editMode == EditMode.NEW) {
                this.preconditions.push(this.editPrecondition);
            }
            else if (this.editMode == EditMode.EDIT) {
                this.preconditions[this.editIndex] = this.editPrecondition;
            }

            this.buildDisplayString();
            this.pendingEdits = true;

            this.cancelEdit();
        }
    }

    private buildDisplayString() {
        if (this.editPrecondition != null) {
            var str = '';
            _.each(this.editPrecondition.properties, (prop:any) => {
                if (prop.type != 'currentFeed') {
                    //chain it to the display string
                    if (str != '') {
                        str += ';';
                    }
                    str += ' ' + prop.displayName;
                    var val = prop.value;
                    if ((val == null || val == undefined || val == '') && (prop.values != null && prop.values.length > 0)) {
                        val = _.map(prop.values, (labelValue:any) => {
                            return labelValue.value;
                        }).join(",");
                    }
                    str += ": " + val;
                }
            });
            this.editPrecondition.propertyValuesDisplayString = str;
        }
    }

    isEdit() {
        return this.editMode === EditMode.EDIT;
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
        this.preconditionsChange.emit(this.preconditions)
        this.cancelEdit();
        this.close();
    }

    close() {
        this.dialogRef.close(this.preconditions);
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
        this.preconditionsChange.emit(this.preconditions)
        this.close();
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

            if(selectedValue){
                rule.groups = this.policyInputFormService.groupProperties(selectedValue);
            }
            //make all rules editable
            rule.editable = true;
            this.editPrecondition = rule;
        }
        else {
            this.editPrecondition = null;
        }
    }
}
