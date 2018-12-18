import {FormControl, FormGroup, Validators} from "@angular/forms";
import {Component, Input, OnInit, TemplateRef, ViewChild} from "@angular/core";
import {JoinPreviewStepData} from "./join-preview-step-data";
import {PreviewStepperStep} from "../../../catalog-dataset-preview/preview-stepper/preview-stepper-step";
import {AbstractDatasetPreviewStepComponent} from "../../../catalog-dataset-preview/preview-stepper/abstract-dataset-preview-step.component";
import {DatasetPreviewStepperService} from "../../../catalog-dataset-preview/preview-stepper/dataset-preview-stepper.service";
import {ISubscription} from "rxjs/Subscription";
import {PreviewDataSet} from "../../../catalog/datasource/preview-schema/model/preview-data-set";
import {SparkDataSet} from "../../../model/spark-data-set.model";
import * as _ from "underscore";
import {TableColumn} from "../../../catalog/datasource/preview-schema/model/table-view-model";
import {QueryResultColumn} from "../../wrangler";
import {FormGroupUtil} from "../../../../services/form-group-util";

export class JoinData {

    dataFrameField:string;
    joinField:string;
    joinDataSet:PreviewDataSet;
    joinType:string;
    joinFields:string[];
    ds:SparkDataSet

    constructor() {


    }

}

@Component({selector:"dataset-join-step",
    template:`
         <ng-template let-data="data" #stepTemplate>      
           <div fxLayout="column" fxLayoutAlign="start center">
              <form [formGroup]="stepControl" fxLayout="column" fxLayoutAlign="start">
                <div *ngIf="dataSet != undefined" class="pad-bottom-md pad-top-md"> Join <span class="bold-title">{{dataSet.displayKey}}</span> with your data             
                </div>
         
            <mat-form-field>
              <mat-select placeholder="Join type"  formControlName="joinType" required>
                <mat-option *ngFor="let joinType of joinTypes" [value]="joinType.value">
                  {{joinType.name}}
                </mat-option>
              </mat-select>
              <mat-error *ngIf="joinTypeControl.hasError('required')">Required</mat-error>
            </mat-form-field>
            
            <div fxLayout="row">
                  
              <mat-form-field>
                <mat-select placeholder="Current field"  formControlName="currentSchemaField" required>
                    <mat-option *ngFor="let field of data.cols" [value]="field.field">
                    {{field.displayName}} - {{field.dataType}}
                  </mat-option>
                </mat-select>
                <mat-error *ngIf="currentSchemaFieldControl.hasError('required')">Required</mat-error>
              </mat-form-field>
              
              <div fxFlex="10" class="pad-left-sm pad-right-sm"> </div>
        
              <mat-form-field *ngIf="dataSet != undefined">
                <mat-select placeholder="Joining field"  formControlName="joinSchemaField" required>
                  <mat-option *ngFor="let field of dataSet.schema" [value]="field.name">
                    {{field.label}} - {{field.dataType}}
                  </mat-option>
                </mat-select>
                <mat-error *ngIf="joinSchemaFieldControl.hasError('required')">Required</mat-error>
              </mat-form-field>
            </div>
            <div>
               <mat-form-field *ngIf="dataSet != undefined">
                <mat-select placeholder="Joining schema"  formControlName="joinSchemaFields" required multiple >
                  <mat-option *ngFor="let field of dataSet.schema" [value]="field.name">
                    {{field.label}} - {{field.dataType}}
                  </mat-option>
                </mat-select>
                <mat-error *ngIf="joinSchemaFieldControl.hasError('required')">Required</mat-error>
                <mat-hint>Selected {{joinSchemaFieldsControl.value.length}} fields</mat-hint>
              </mat-form-field>
            </div>
    </form>
           </div>
          </ng-template>`,
    styles: ['.bold-title { font-weight: bold; }']})
export class JoinPreviewStepperStep extends AbstractDatasetPreviewStepComponent<JoinPreviewStepData> {

    stepChangedSubscription:ISubscription;


    joinTypes: any = [{name: "Inner Join", value: "INNER JOIN"}, {name: "Left Join", value: "LEFT JOIN"}, {name: "Right Join", value: "RIGHT JOIN"}, {name: "Full Join", value: "FULL JOIN"}];


    currentSchemaFieldControl: FormControl;

    joinSchemaFieldControl: FormControl;

    joinTypeControl: FormControl;

    joinSchemaFieldsControl: FormControl;

    dataSet:PreviewDataSet;

    constructor(protected _datasetPreviewStepperService:DatasetPreviewStepperService) {
        super(_datasetPreviewStepperService);
        this.name = "Join";
        this.data = new JoinPreviewStepData();

        this.currentSchemaFieldControl = new FormControl('', [Validators.required]);
        this.joinSchemaFieldControl = new FormControl('', [Validators.required]);
        this.joinTypeControl = new FormControl('', [Validators.required]);
        this.joinSchemaFieldsControl = new FormControl([],[Validators.required]);

        this.stepChangedSubscription = this._datasetPreviewStepperService.subscribeToStepChanges(this.onStepChanged.bind(this))

        this.stepControl.addControl("joinType", this.joinTypeControl);
        this.stepControl.addControl("currentSchemaField", this.currentSchemaFieldControl);
        this.stepControl.addControl("joinSchemaField", this.joinSchemaFieldControl);
        this.stepControl.addControl("joinSchemaFields",this.joinSchemaFieldsControl)
    }


    resetForm(){
        this.currentSchemaFieldControl.setValue("");
        this.joinSchemaFieldControl.setValue("");
        this.joinTypeControl.setValue("")
        this.joinSchemaFieldsControl.setValue([]);
    }

    init() {
        this.resetForm();
    }

    private autoMatch():void {
        //attempt to auto find matches
        let currentNames: any = [];
        let joinNames: any = [];
        _.forEach(this.data.cols, function (field: QueryResultColumn) {
            currentNames.push(field.field);
        });

        _.forEach(this.dataSet.schema, function (field: TableColumn) {
            joinNames.push(field.name);
        });

        let matches = _.intersection(currentNames, joinNames);
        if (matches && matches.length && matches.length > 0) {
            let col = matches[0];
            if (matches.length > 1) {
                // force id fields as match if they exist
                if (matches[0] == 'id') {
                    col = matches[1];
                }
            }
            this.currentSchemaFieldControl.setValue(<string>col);
            this.joinSchemaFieldControl.setValue(<string>col);
            this.joinTypeControl.setValue("INNER JOIN")
        }
    }

    private setJoinSchemaFields(){
        let joinNames: string[] = [];   _.forEach(this.dataSet.schema, function (field: TableColumn) {
            joinNames.push(field.name);
        });
        this.joinSchemaFieldsControl.setValue(joinNames)
    }



    private onStepChanged(idx:number){
        if (idx == this.index) {
           if(this._datasetPreviewStepperService.previews && this._datasetPreviewStepperService.previews.length ==1){
               this.dataSet = this._datasetPreviewStepperService.previews[0];
               if(this.joinTypeControl.value == "" || this.currentSchemaFieldControl.value == "" || this.joinSchemaFieldControl.value == "") {
                   this.autoMatch();
               }
               this.setJoinSchemaFields();
           }
        }

    }

    onSave(): any {
        let joinData = new JoinData();
        joinData.dataFrameField = this.currentSchemaFieldControl.value;
        joinData.joinField = this.joinSchemaFieldControl.value;
        joinData.joinType = this.joinTypeControl.value;
        joinData.joinDataSet = this.dataSet;
        joinData.joinFields = this.joinSchemaFieldsControl.value;
        return joinData;
    }

}
