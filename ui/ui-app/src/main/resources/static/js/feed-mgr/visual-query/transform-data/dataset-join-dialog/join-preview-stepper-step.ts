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
import {CloneUtil} from "../../../../common/utils/clone-util";
import {MatButtonToggleChange, MatButtonToggleGroup} from "@angular/material/button-toggle";
import {importer} from "ui-grid";
import {ITdDataTableSelectAllEvent, ITdDataTableSelectEvent} from "@covalent/core";
import {SelectionSummary} from "../../../catalog/datasource/preview-schema/dataset-simple-table.component";

export enum JoinMode {
    JOIN = "JOIN", UNION = "UNION"
}

export class JoinData {

    dataFrameField:string;
    joinField:string;
    joinDataSet:PreviewDataSet;
    joinType:string;
    joinFields:string[];
    ds:SparkDataSet
    joinOrUnion:JoinMode
    unionModel:UnionField[]

    constructor() {


    }

}


export class UnionField {

    static UNMATCHED_VALUE:string = "";

    joinField:TableColumn;
    dataFrameField:string;
    /**
     * The name to use for the join.
     * During the union process this can change if the name conflicts with the current dataframe
     */
    joinFieldName:string;
    removed:boolean;

    tableModelJoinFieldName:string;
    tableModelJoinFieldDataType:string;
    public constructor(init?: Partial<UnionField>) {
        Object.assign(this, init);
        if(this.joinField){
            this.tableModelJoinFieldName = this.joinField.name;
            this.tableModelJoinFieldDataType = this.joinField.dataType
        }
    }
}

export class UnionTableModel {
    columns:TableColumn[] = [];
    constructor(public rows:UnionField[], dataSetName:string) {
        this.columns.push({"name":"dataFrameField","label":"Current Schema","dataType":"string","sortable":false})
        this.columns.push({name:"tableModelJoinFieldName","label":"Data Set Field","dataType":"string","sortable":true})
        this.columns.push({name:"tableModelJoinFieldDataType","label":"Data Type","dataType":"string","sortable":true})

    }

    update(rows:UnionField[], dataSetName:string){
       // this.columns[0].label = dataSetName;
        this.rows = rows;
    }
}

@Component({selector:"dataset-join-step",
    templateUrl:'./join-preview-stepper-step.template.html',
    styles: ['.bold-title { font-weight: bold; }',
        '.strike { text-decoration: line-through;}',
        '.unionRow {height:56px !important;}'] })
export class JoinPreviewStepperStep extends AbstractDatasetPreviewStepComponent<JoinPreviewStepData> {

    stepChangedSubscription:ISubscription;


    joinTypes: any = [{name: "Inner Join", value: "INNER JOIN"}, {name: "Left Join", value: "LEFT JOIN"}, {name: "Right Join", value: "RIGHT JOIN"}, {name: "Full Join", value: "FULL JOIN"}];


    currentSchemaFieldControl: FormControl;

    joinSchemaFieldControl: FormControl;

    joinTypeControl: FormControl;

    joinSchemaFieldsControl: FormControl;

    unionSchemaFieldsControl: FormControl;

    dataSet:PreviewDataSet;

    lastDataSetTitle:string;

    unionModel: UnionField[] = [];

    @ViewChild("joinOrUnion")
    joinOrUnion:MatButtonToggleGroup;

    unionTableModel:UnionTableModel = new UnionTableModel([],"DataSet");

    constructor(protected _datasetPreviewStepperService:DatasetPreviewStepperService) {
        super(_datasetPreviewStepperService);
        this.name = "Join";
        this.data = new JoinPreviewStepData();

        this.stepChangedSubscription = this._datasetPreviewStepperService.subscribeToStepChanges(this.onStepChanged.bind(this))
        /** Join Form Controls **/
        this.currentSchemaFieldControl = new FormControl('', [Validators.required]);
        this.joinSchemaFieldControl = new FormControl('', [Validators.required]);
        this.joinTypeControl = new FormControl('', [Validators.required]);
        this.joinSchemaFieldsControl = new FormControl([],[Validators.required]);


        this.stepControl.addControl("joinType", this.joinTypeControl);
        this.stepControl.addControl("currentSchemaField", this.currentSchemaFieldControl);
        this.stepControl.addControl("joinSchemaField", this.joinSchemaFieldControl);
        this.stepControl.addControl("joinSchemaFields",this.joinSchemaFieldsControl)

        //UnionFormControls

        this.unionSchemaFieldsControl = new FormControl([],[]);
        this.stepControl.addControl("unionSchemaFields",this.unionSchemaFieldsControl)
    }


    /**
     * when the user opens the dialog
     */
    init() {
        this.resetForm();
    }

    /**
     * The first time the form is rendered
     */
    ngOnInit(): void {
        super.ngOnInit();
    }

    /**
     * When the user jumps between steps
     * detect if the current form needs refreshing (i.e. if the user changed datasets or is coming here for the first time)
     * @param idx
     */
    private onStepChanged(idx:number){
        if (idx == this.index) {
            if(this._datasetPreviewStepperService.previews && this._datasetPreviewStepperService.previews.length ==1){
                this.dataSet = this._datasetPreviewStepperService.previews[0];
                if(this.lastDataSetTitle == "" || this.dataSet.displayKey != this.lastDataSetTitle) {
                    this.autoMatch();
                }
                //select all columns
                this.setJoinSchemaFields();
                this.setUnionModel();
                this.lastDataSetTitle = this.dataSet.displayKey;
            }
        }

    }

    /**
     * Reset the form and validaiton
     */
    private resetForm(){
        if(this.joinOrUnion != null) {
            this.joinOrUnion.value = JoinMode.JOIN;
        }

        this.currentSchemaFieldControl.setValue("");
        this.joinSchemaFieldControl.setValue("");
        this.joinTypeControl.setValue("")
        this.joinSchemaFieldsControl.setValue([]);
        this.setupFormValidation(this.joinOrUnion != null ? this.joinOrUnion.value : JoinMode.JOIN);

    }

    /**
     * reset the validation based upon the JoinMode
     * @param mode
     */
    private setupFormValidation(mode:JoinMode){
        if(mode == JoinMode.JOIN){
            this.currentSchemaFieldControl.setValidators([Validators.required])
            this.joinSchemaFieldControl.setValidators([Validators.required]);
            this.joinTypeControl.setValidators([Validators.required])
            this.joinSchemaFieldsControl.setValidators([Validators.required])
            this.unionSchemaFieldsControl.clearValidators();
        }
        else {
            this.currentSchemaFieldControl.clearValidators();
            this.joinSchemaFieldControl.clearValidators();
            this.joinTypeControl.clearValidators();
            this.joinSchemaFieldsControl.clearValidators();
            this.unionSchemaFieldsControl.setValidators([Validators.required]);
        }
        FormGroupUtil.updateValidity(this.stepControl);
    }


    /**
     * When the join mode between JOIN and UNION changes
     * @param $event
     */
    onJoinOrUnionChange($event:MatButtonToggleChange) {
        // update the validation since the join mode changed
        this.setupFormValidation($event.value);
    }


    /**
     * when a user clicks the Add button
     */
    onSave(): any {
        let joinData = new JoinData();
        joinData.dataFrameField = this.currentSchemaFieldControl.value;
        joinData.joinField = this.joinSchemaFieldControl.value;
        joinData.joinType = this.joinTypeControl.value;
        joinData.joinDataSet = this.dataSet;
        joinData.joinFields = this.joinSchemaFieldsControl.value;
        joinData.joinOrUnion = this.joinOrUnion.value
        joinData.unionModel = this.unionModel
        return joinData;
    }



//// Join form methods


    /**
     * When the Join form row selection changes
     * @param $event
     */
    selectionChange($event:SelectionSummary){
        let selectedRows = $event.getSelectedRows().map((row :TableColumn) => row.name);
        this.joinSchemaFieldsControl.setValue(selectedRows);
        console.log('set selection to be ',selectedRows)
    }

    /**
     * Attempt to auto match the columns for the Join form
     */
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
        else {
            this.currentSchemaFieldControl.setValue("");
            this.joinSchemaFieldControl.setValue("");
            this.joinTypeControl.setValue("")
        }
    }

    /**
     * Set the join fields value equal to all columns in the dataset
     */
    private setJoinSchemaFields(){
        let joinNames: string[] = [];   _.forEach(this.dataSet.schema, function (field: TableColumn) {
            joinNames.push(field.name);
        });
        this.joinSchemaFieldsControl.setValue(joinNames)
    }

///// Union form methods

    unionSelectionChange($event:SelectionSummary) {
        if($event.selectedCount >0) {
           this.unionSchemaFieldsControl.setValue($event.selectedCount)
        }
        else {
            this.unionSchemaFieldsControl.setValue("")
        }

    }

    unionSelectAll($event:ITdDataTableSelectAllEvent){
        this.unionModel.forEach(unionField=> unionField.removed = !$event.selected)
    }

    unionRowSelect($event:ITdDataTableSelectEvent){
        let row :UnionField = $event.row as UnionField;
        row.removed = !$event.selected
    }

    /**
     * Setup the model needed for the Union form
     */
    private setUnionModel() :void{
        let dfFields = CloneUtil.deepCopy(this.data.cols);
        this.unionModel =  this.dataSet.schema.map((field:TableColumn) =>  {
            let joinField = '';
            let matchIdx = dfFields.findIndex( dfField => dfField.hiveColumnLabel == field.name || dfField.displayName == field.name || dfField.hiveColumnLabel == field.label || dfField.displayName == field.label  );
            if(matchIdx >=0){
                let match = dfFields[matchIdx];
                dfFields.splice(matchIdx,1);
                joinField = match.hiveColumnLabel;
            }
            return {joinField:field,dataFrameField:joinField, removed:false, joinFieldName:field.name, tableModelJoinFieldName:field.name, tableModelJoinFieldDataType:field.dataType};
        });

        this.unionTableModel.update(this.unionModel,this.dataSet.displayKey);
    }










}
