import {FormControl, FormGroup, Validators} from "@angular/forms";
import {Component, Input, OnInit, TemplateRef, ViewChild} from "@angular/core";
import {JoinPreviewStepData} from "./join-preview-step-data";
import {PreviewStepperStep} from "../../../catalog-dataset-preview/preview-stepper/preview-stepper-step";
import {AbstractDatasetPreviewStepComponent} from "../../../catalog-dataset-preview/preview-stepper/abstract-dataset-preview-step.component";
import {DatasetPreviewStepperService} from "../../../catalog-dataset-preview/preview-stepper/dataset-preview-stepper.service";
import {ISubscription} from "rxjs/Subscription";
import {PreviewDataSet} from "../../../catalog/datasource/preview-schema/model/preview-data-set";

export class JoinData {

    dataFrameField:string;
    joinField:string;
    joinDataSet:PreviewDataSet;
    joinType:string;

    constructor() {


    }

}

@Component({selector:"dataset-join-step",
    template:`
         <ng-template let-data="data" #stepTemplate>      
         <form [formGroup]="stepControl">
         
            <mat-form-field>
              <mat-select placeholder="Join Type"  formControlName="joinType" required>
                <mat-option *ngFor="let joinType of joinTypes" [value]="joinType.value">
                  {{joinType.name}}
                </mat-option>
              </mat-select>
              <mat-error *ngIf="joinTypeControl.hasError('required')">Required</mat-error>
            </mat-form-field>
            
            <div fxLayout="row">
                  
              <mat-form-field>
                <mat-select placeholder="Current Schema"  formControlName="currentSchemaField" required>
                    <mat-option *ngFor="let field of data.cols" [value]="field.field">
                    {{field.displayName}} - {{field.dataType}}
                  </mat-option>
                </mat-select>
                <mat-error *ngIf="currentSchemaFieldControl.hasError('required')">Required</mat-error>
              </mat-form-field>
        
              <mat-form-field *ngIf="dataSet != undefined">
                <mat-select placeholder="Destination"  formControlName="joinSchemaField" required>
                  <mat-option *ngFor="let field of dataSet.schema" [value]="field.name">
                    {{field.label}} - {{field.dataType}}
                  </mat-option>
                </mat-select>
                <mat-error *ngIf="joinSchemaFieldControl.hasError('required')">Required</mat-error>
              </mat-form-field>
            </div>
    </form>
          </ng-template>`})
export class JoinPreviewStepperStep extends AbstractDatasetPreviewStepComponent<JoinPreviewStepData> {

    stepChangedSubscription:ISubscription;


    joinTypes: any = [{name: "Inner Join", value: "INNER JOIN"}, {name: "Left Join", value: "LEFT JOIN"}, {name: "Right Join", value: "RIGHT JOIN"}, {name: "Full Join", value: "FULL JOIN"}];


    currentSchemaFieldControl: FormControl;

    joinSchemaFieldControl: FormControl;

    joinTypeControl: FormControl;

    dataSet:PreviewDataSet;

    //TODO support passing in the keys for prepopulating the join when editing

    constructor(protected _datasetPreviewStepperService:DatasetPreviewStepperService) {
        super(_datasetPreviewStepperService);
        this.name = "Join";
        this.data = new JoinPreviewStepData();

        this.currentSchemaFieldControl = new FormControl('', [Validators.required]);
        this.joinSchemaFieldControl = new FormControl('', [Validators.required]);
        this.joinTypeControl = new FormControl('', [Validators.required]);

        this.stepChangedSubscription = this._datasetPreviewStepperService.subscribeToStepChanges(this.onStepChanged.bind(this))
    }



    init() {
        this.stepControl.addControl("joinType", this.joinTypeControl);
        this.stepControl.addControl("currentSchemaField", this.currentSchemaFieldControl);
        this.stepControl.addControl("joinSchemaField", this.joinSchemaFieldControl);
    }



    private onStepChanged(idx:number){
        if (idx == this.index) {
           if(this._datasetPreviewStepperService.previews && this._datasetPreviewStepperService.previews.length ==1){
               this.dataSet = this._datasetPreviewStepperService.previews[0];
           }
        }

    }

    onSave(): any {
        let joinData = new JoinData();
        joinData.dataFrameField = this.currentSchemaFieldControl.value;
        joinData.joinField = this.joinSchemaFieldControl.value;
        joinData.joinType = this.joinTypeControl.value;
        joinData.joinDataSet = this.dataSet;
        return joinData;
    }

}
