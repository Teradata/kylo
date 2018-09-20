import {Component, Inject, OnDestroy, OnInit} from "@angular/core";
import {SimpleDynamicFormDialogComponent} from "../../../shared/dynamic-form/simple-dynamic-form/simple-dynamic-form-dialog.component";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DynamicFormDialogData} from "../../../shared/dynamic-form/simple-dynamic-form/dynamic-form-dialog-data";
import {Template} from "../../../model/template-models";
import {FormGroup} from "@angular/forms";
import CategoriesService from "../../../services/CategoriesService";
import {Observable} from "rxjs/Observable";
import {Category} from "../../../model/category/category.model";

export class NewFeedDialogData {

    constructor(public template:Template, public title?:string){}
}


export interface NewFeedDialogResponse{
    template:Template;
    category:Category;
    feedName:string;
    systemFeedName:string;
}

@Component({
    selector:"new-feed-dialog",
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/new-feed-dialog/new-feed-dialog.component.html"
})
export class NewFeedDialogComponent implements OnInit, OnDestroy{

    template:Template;
    /**
     * the form to validate
     */
    formGroup:FormGroup;

    title:string;


    constructor(private dialog: MatDialogRef<NewFeedDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: NewFeedDialogData) {
        this.template = data.template;
        this.formGroup = new FormGroup({});
        if(data.title){
            this.title = data.title;
        }
        else {
            this.title = "New "+this.template.templateName+" Feed";
        }
    }

    ngOnInit() {

    }
    ngOnDestroy(){

    }

    create(){
        let values = this.formGroup.value;
        values.template = this.template;
        let response:NewFeedDialogResponse = <NewFeedDialogResponse>values;
        this.dialog.close(response);
    }
    cancel(){
        this.dialog.close();
    }


}