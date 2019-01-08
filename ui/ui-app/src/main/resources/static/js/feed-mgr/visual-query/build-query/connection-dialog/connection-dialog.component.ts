import {Component, Inject} from "@angular/core";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import * as _ from "underscore";

import {FlowChart} from "../flow-chart/model/flow-chart.model";

export interface ConnectionDialogConfig {
    isNew: boolean;
    connectionDataModel: any,
    connectionViewModel: FlowChart.ConnectionViewModel
    source: any;
    dest: any;
}

export enum ConnectionDialogResponseStatus {
    SAVE = 1, DELETE = 2, CANCEL = 3
}

export interface ConnectionDialogResponse {
    id: string;
    connectionName?: string;
    source: string;
    dest: string;
    joinType: string;
    status: ConnectionDialogResponseStatus;

    /****  enhanced  ****/
    source1: string;
    dest1: string;
    source2: string;
    dest2: string;
}

/**
 * Controls the connection dialog for creating a join between two nodes in the Build Query flow chart.
 */
@Component({
    styleUrls: ["./connection-dialog.component.css"],
    templateUrl: "./connection-dialog.component.html"
})
export class ConnectionDialog {

    form: FormGroup;

    sourceControl: FormControl;

    destControl: FormControl;

    joinTypeControl: FormControl;

    connectionName: FormControl;

    title: string;

    connectionDataModel: any;
    isValid: boolean = false;

    source: any;
    dest: any;
    joinTypes: any = [{name: "Inner Join", value: "INNER JOIN"}, {name: "Left Join", value: "LEFT JOIN"}, {name: "Right Join", value: "RIGHT JOIN"}, {name: "Full Join", value: "FULL JOIN"}];

    sourceKey: string;
    destKey: string;
    joinType: string;

    /****  enhanced  ****/
    sourceKey1: string;
    destKey1: string;
    sourceControl1: FormControl;
    destControl1:FormControl;
    sourceKey2: string;
    destKey2: string;
    sourceControl2: FormControl;
    destControl2:FormControl;
    message = null;
    isNew = false;

    constructor(private dialog: MatDialogRef<ConnectionDialog>, formBuilder: FormBuilder, @Inject(MAT_DIALOG_DATA) public data: ConnectionDialogConfig) {
        this.form = formBuilder.group({});

        if (this.data.source && this.data.dest) {
            this.title = "Connection Details"// for " + this.data.source.name + " to " + this.data.dest.name
        }
        else {
            this.title = "Connection details"
        }

        this.init();
    }

    private init() {
        if (this.data.isNew) {
            //attempt to auto find matches
            let sourceNames: any = [];
            let destNames: any = [];
            _.forEach(this.data.source.data.nodeAttributes.attributes, function (attr: any) {
                sourceNames.push(attr.name);
            });

            _.forEach(this.data.dest.data.nodeAttributes.attributes, function (attr: any) {
                destNames.push(attr.name);
            });

            let matches = _.intersection(sourceNames, destNames);
            if (matches && matches.length && matches.length > 0) {
                let col = matches[0];
                if (matches.length > 1) {
                    if (matches[0] == 'id') {
                        col = matches[1];
                    }
                }
                this.sourceKey = <string>col;
                this.destKey = <string> col;
                /****  enhanced  ****/
                this.sourceKey1 = undefined;
                this.destKey1 = undefined;
                this.sourceKey2 = undefined;
                this.destKey2 = undefined;

                this.joinType = "INNER JOIN"
            }
        } else {
            this.joinType = this.data.connectionDataModel.joinType;
            this.sourceKey = this.data.connectionDataModel.joinKeys.sourceKey;
            this.destKey = this.data.connectionDataModel.joinKeys.destKey;
            /****  enhanced  ****/
            this.sourceKey1 = this.data.connectionDataModel.joinKeys.sourceKey1;
            this.destKey1 = this.data.connectionDataModel.joinKeys.destKey1;
            this.sourceKey2 = this.data.connectionDataModel.joinKeys.sourceKey2;
            this.destKey2 = this.data.connectionDataModel.joinKeys.destKey2;
        }

        this.connectionName = new FormControl(this.data.connectionDataModel.name, []);
        this.sourceControl = new FormControl(this.sourceKey, [Validators.required]);
        this.destControl = new FormControl(this.destKey, [Validators.required]);
        this.joinTypeControl = new FormControl(this.joinType, [Validators.required]);

        this.sourceControl1 = new FormControl(this.sourceKey1);
        this.destControl1 = new FormControl(this.destKey1);
        this.sourceControl2 = new FormControl(this.sourceKey2);
        this.destControl2 = new FormControl(this.destKey2);

        this.form.addControl("connectionName", this.connectionName);
        this.form.addControl("joinType", this.joinTypeControl);
        this.form.addControl("source", this.sourceControl);
        this.form.addControl("dest", this.destControl);

        this.form.addControl("source1", this.sourceControl1);
        this.form.addControl("dest1", this.destControl1);
        this.form.addControl("source2", this.sourceControl2);
        this.form.addControl("dest2", this.destControl2);

        this.validate();
    }

    onJoinTypeChange() {
        //    .log('joinType changed')
    }

    /**
     * Closes this dialog and returns the ConnectionDialogResponse
     */
    apply() {
        
        let values = this.form.value;
        let response: ConnectionDialogResponse = {
            id: this.data.connectionDataModel.id,
            connectionName: values.joinType,
            source: values.source,
            dest: values.dest,
            joinType: values.joinType,
            status: ConnectionDialogResponseStatus.SAVE,

            source1: values.source1,
            dest1: values.dest1,
            source2: values.source2,
            dest2: values.dest2
        };
        this.dialog.close(response);
    }

    /**
     * Cancel this dialog.
     */
    cancel() {
        this.dialog.close({status: ConnectionDialogResponseStatus.CANCEL});
    }

    validate(): boolean {
        return this.form.valid;
    }

    deleteConnection() {
        this.dialog.close({
            id: this.data.connectionDataModel.id,
            connectionName: this.data.connectionDataModel.name,
            source: this.data.connectionDataModel.joinKeys.sourceKey,
            dest: this.data.connectionDataModel.joinKeys.destKey,
            joinType: this.data.connectionDataModel.joinType,
            status: ConnectionDialogResponseStatus.DELETE
        });
    }
}
