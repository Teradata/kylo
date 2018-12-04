import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {ISubscription} from "rxjs/Subscription";
import {FormConfig} from "../services/dynamic-form-builder";

@Component({
    selector:"simple-dynamic-form",
    templateUrl: "./simple-dynamic-form.component.html"
})
export class SimpleDynamicFormComponent implements OnInit, OnDestroy{

    @Input()
    formConfig:FormConfig;

    @Output()
    onApply :EventEmitter<any> = new EventEmitter<any>();

    @Output()
    onCancel :EventEmitter<any> = new EventEmitter<any>();

    onApplySubscription:ISubscription;

    onCancelSubscription:ISubscription;

    constructor(){

    }

ngOnInit(){
    if(this.formConfig.onApplyFn) {
        this.onApplySubscription = this.onApply.subscribe(this.formConfig.onApplyFn)
    }
    if(this.formConfig.onCancelFn) {
        this.onCancelSubscription = this.onCancel.subscribe(this.formConfig.onCancelFn)
    }
}
ngOnDestroy() {
    if(this.onApplySubscription){
        this.onApplySubscription.unsubscribe();
    }
    if(this.onCancelSubscription){
        this.onCancelSubscription.unsubscribe();
    }
    }

    apply() {
        this.onApply.emit(this.formConfig.form.value);

    }

    /**
     * Cancel this dialog.
     */
    cancel() {
        this.onCancel.emit(this.formConfig.form.value);
    }


}
