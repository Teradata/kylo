import {Component, Inject, OnInit} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

@Component({
    selector: 'status-dialog',
    styleUrls: ['./status-dialog.component.scss'],
    templateUrl: './status-dialog.component.html',
})
export class SatusDialogComponent implements OnInit {

    private renderActionButtons :boolean = false;
    showProgress :boolean;
    title :string = "Working";
    message : string = '';
    constructor(private selfReference: MatDialogRef<SatusDialogComponent>,  @Inject(MAT_DIALOG_DATA) public data: any){
        this.renderActionButtons = data.renderActionButtons;
        this.title = data.title;
        this.message = data.message;
        this.showProgress = data.showProgress
    }

    public ngOnInit(): void {

    }

    onOk() {
            this.selfReference.close(false);
    }
}