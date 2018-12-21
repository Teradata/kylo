import {PreviewStepperStep} from "./preview-stepper-step";
import {Component, Input, OnInit, TemplateRef, ViewChild} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {DatasetPreviewStepperService} from "./dataset-preview-stepper.service";
/*
Example impl of this class

@Component({selector:"dataset-preview-step",
template:`<ng-template let-data #stepTemplate>
           This is my template {{data}}
          </ng-template>`})
          */
export abstract class AbstractDatasetPreviewStepComponent<T> implements OnInit, PreviewStepperStep<T>{

    additionalStepIndex:number;
    index:number;
    name:string = "Step";

    data:T;

    stepControl:FormGroup;

    @ViewChild("stepTemplate")
    templateRef: TemplateRef<any>;

    protected constructor(protected _datasetPreviewStepperService:DatasetPreviewStepperService) {
        this.stepControl = new FormGroup({})
    }

    init(){

    }

    ngOnInit(): void {

    }

    setAdditionalStepIndex(additionalStepIndex:number){
        this.additionalStepIndex = additionalStepIndex;
        this.index = this.additionalStepIndex+2+1;
    }

    onSave(): any {
        return null;
    }


}