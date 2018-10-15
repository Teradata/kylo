import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';

@Component({
    selector: 'service-level-agreement-form',
    templateUrl: 'js/feed-mgr/sla/service-level-agreement-form.html'
})
export default class ServiceLevelAgreementForm {

    @Input() allowEdit: any;
    @Input() editSla: any;
    @Input() editSlaIndex: any;
    @Input() feed: any;
    @Input() mode: any;
    @Input() addingSlaCondition: any;
    @Input() ruleType: any;
    @Input() slaForm: any;
    @Input() options: any;

    @Output() slaFormChange: EventEmitter<any> = new EventEmitter<any>();
    @Output() editedSla: EventEmitter<any> = new EventEmitter<any>();
    @Output() viewSlaAssessments: EventEmitter<any> = new EventEmitter<any>();
    @Output() propertyChange: EventEmitter<any> = new EventEmitter<any>();
    @Output() addNewCondition: EventEmitter<any> = new EventEmitter<any>();
    @Output() deleteSla: EventEmitter<any> = new EventEmitter<any>();
    @Output() cancelEditSla: EventEmitter<any> = new EventEmitter<any>();
    @Output() saveSla: EventEmitter<any> = new EventEmitter<any>();
    @Output() nameChange: EventEmitter<any> = new EventEmitter<any>();
    @Output() descriptionChange: EventEmitter<any> = new EventEmitter<any>();
    @Output() addConditionRuleTypeChange: EventEmitter<any> = new EventEmitter<any>();
    @Output() deleteSlaMetric: EventEmitter<any> = new EventEmitter<any>();
    @Output() deleteSlaAction: EventEmitter<any> = new EventEmitter<any>();

    @ViewChild("slaForm")
    set childForm(slaForm: NgForm) {
        this.onChange(slaForm);
    }

    onChange(slaForm: NgForm) {
        this.slaFormChange.emit(slaForm);
    }

    onEditSla() {
        this.editedSla.emit();
    }

    onViewSlaAssessments() {
        this.viewSlaAssessments.emit();
    }

    onPropertyChange(property: any) {
        this.propertyChange.emit(property);
    }

    onAddNewCondition() {
        this.addNewCondition.emit();
    }

    onDeleteSla() {
        this.deleteSla.emit();
    }

    onCancelEditSla() {
        this.cancelEditSla.emit();
    }

    onNameChange() {
        this.nameChange.emit();
    }

    onDescriptionChange() {
        this.descriptionChange.emit();
    }

    onAddConditionRuleTypeChange(ruleType: any) {
        this.addConditionRuleTypeChange.emit(ruleType);
    }

    onDeleteSlaMetric() {
        this.deleteSlaMetric.emit();
    }

    onDeleteSlaAction() {
        this.deleteSlaAction.emit();
    }

    onSaveSla() {
        this.saveSla.emit();
    }

}

