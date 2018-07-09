import * as angular from 'angular';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ITdDataTableColumn, ITdDataTableSortChangeEvent, TdDataTableSortingOrder, TdDataTableService } from '@covalent/core/data-table';
import { IPageChangeEvent } from '@covalent/core/paging';
import { filter } from 'rxjs/operator/filter';
import { NgForm } from '@angular/forms';

@Component({
    selector: 'service-level-agreement-form',
    templateUrl: 'js/feed-mgr/sla/service-level-agreement-form.html'
})
export default class ServiceLevelAgreementForm {

    @Input() allowEdit: any;
    @Input() editSla: any;
    @Input() onEditSla: any;
    @Input() editSlaIndex: any;

    @Input() viewSlaAssessments: any;
    @Input() feed: any;
    @Input() mode: any;
    @Input() onPropertyChange: any;
    @Input() addingSlaCondition: any;
    @Input() addNewCondition: any;
    @Input() ruleType: any;
    @Input() onDeleteSla: any;
    @Input() cancelEditSla: any;
    @Input() saveSla: any;

    @Input() onNameChange: any;
    @Input() onDescriptionChange: any;

    @Input() slaForm: any;
    @Input() options: any;
    @Input() onAddConditionRuleTypeChange: any;

    @Input() onDeleteSlaMetric: any;
    @Input() onDeleteSlaAction: any;
    
    @Output() ruleTypeChange: EventEmitter<any> = new EventEmitter<any>();
    @Output() slaFormChange: EventEmitter<any> = new EventEmitter<any>();

    onRuleTypeChange(ruleType: any) {
        this.ruleTypeChange.emit(ruleType);
    }

    onChange(slaForm: NgForm) {
        this.slaFormChange.emit(slaForm);
    }


}

