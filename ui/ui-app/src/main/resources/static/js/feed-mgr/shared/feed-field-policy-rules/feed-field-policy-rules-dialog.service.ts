import {Injectable} from "@angular/core";
import {MatDialog} from "@angular/material/dialog";
import {TableColumnDefinition} from "../../model/TableColumnDefinition";
import {FeedModel} from "../../feeds/define-feed-ng2/model/feed.model";
import {FeedFieldPolicyRulesDialogComponent} from "./feed-field-policy-rules-dialog.component";
import {FeedFieldPolicyDialogData} from "./feed-field-policy-dialog-data";
import {FieldPolicyOptionsService} from "../field-policies-angular2/field-policy-options.service";
import {TableFieldPolicy} from "../../model/TableFieldPolicy";


@Injectable()
export class FeedFieldPolicyRulesDialogService {

    constructor(public dialog:MatDialog, fieldPolicyOptionsService: FieldPolicyOptionsService){

    }

    openDialog(feed:FeedModel, field:TableFieldPolicy){

        let data:FeedFieldPolicyDialogData = {feed:feed,field:field};

        const dialogRef = this.dialog.open(FeedFieldPolicyRulesDialogComponent, {
            width: '600px',
            data: data
        });

        dialogRef.afterClosed().subscribe((result:any) => {
            console.log('The dialog was closed');

        });

    }

}