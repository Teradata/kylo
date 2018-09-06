import * as angular from "angular";
import {moduleName} from "../module-name";
import {Component, Inject, ElementRef, Input, Injectable} from "@angular/core";
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';

import "../module";
import { HttpClient } from "@angular/common/http";

@Component({
    selector: "about-kylo-dialog-controller",
    templateUrl: "js/common/about-kylo/about.html",
    styles:[`
        .md-transition-in, .md-transition-out {
            -webkit-transition: all .4s cubic-bezier(.25,.8,.25,1);
            transition: all .4s cubic-bezier(.25,.8,.25,1);
            opacity: 1;
            -webkit-transform: translate(0,0) scale(1);
            transform: translate(0,0) scale(1);
        }
        .md-kylo-theme {
            border-radius: 4px;
            background-color: rgb(255,255,255);
            color: rgba(0,0,0,0.87);
        }
        mat-dialog-content:not([layout=row])>:first-child:not(.md-subheader) {
            margin-top: 0;
        }
        .mat-dialog-actions {
            display: -webkit-box;
            display: -webkit-flex;
            display: flex;
            -webkit-box-ordinal-group: 3;
            -webkit-order: 2;
            order: 2;
            box-sizing: border-box;
            -webkit-box-align: center;
            -webkit-align-items: center;
            align-items: center;
            -webkit-box-pack: end;
            -webkit-justify-content: flex-end;
            justify-content: flex-end;
            margin-bottom: 0;
            padding-right: 8px;
            padding-left: 16px;
            min-height: 52px;
            overflow: hidden;
        }
    `]

})
export default class AboutKyloDialogController {

    @Input() version: any;
    @Input() hide: any;
    @Input() cancel: any;

    ngOnInit() {
        this.http.get("/proxy/v1/about/version", {responseType: 'text'}).toPromise()
        .then((response: any) => {
            this.version = response;
        },(response: any)=>{
            this.version = "Not Available"
        });
        this.hide = ()=> {
            this.dialogRef.close();
        };
        this.cancel = ()=> {
            this.dialogRef.close();
        };
    }
    constructor(private http: HttpClient,
                private dialogRef: MatDialogRef<AboutKyloDialogController>){}
}

@Injectable()
export class AboutKyloService{
    constructor(private dialog: MatDialog){}

    showAboutDialog = () => {
        let dialogRef = this.dialog.open(AboutKyloDialogController, {
            maxWidth: '35%',
            maxHeight: '100%',
            panelClass: "partial-screen-dialog"
        });
    
    }
}

