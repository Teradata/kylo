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
                this.joinType = "INNER JOIN"
            }
        } else {
            this.joinType = this.data.connectionDataModel.joinType;
            this.sourceKey = this.data.connectionDataModel.joinKeys.sourceKey;
            this.destKey = this.data.connectionDataModel.joinKeys.destKey;
        }

        this.connectionName = new FormControl(this.data.connectionDataModel.name, []);
        this.sourceControl = new FormControl(this.sourceKey, [Validators.required]);
        this.destControl = new FormControl(this.destKey, [Validators.required]);
        this.joinTypeControl = new FormControl(this.joinType, [Validators.required]);

        this.form.addControl("connectionName", this.connectionName);
        this.form.addControl("joinType", this.joinTypeControl);
        this.form.addControl("source", this.sourceControl);
        this.form.addControl("dest", this.destControl);

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
            status: ConnectionDialogResponseStatus.SAVE
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
