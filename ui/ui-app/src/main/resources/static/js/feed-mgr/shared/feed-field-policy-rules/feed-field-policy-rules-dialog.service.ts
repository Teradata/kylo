import {Injectable} from "@angular/core";
import {MatDialog} from "@angular/material/dialog";
import {FeedFieldPolicyRulesDialogComponent} from "./feed-field-policy-rules-dialog.component";
import {FeedFieldPolicyDialogData} from "./feed-field-policy-dialog-data";
import {TableFieldPolicy} from "../../model/TableFieldPolicy";
import {Feed} from "../../model/feed/feed.model";
import {Observable} from "rxjs/Observable";


@Injectable()
export class FeedFieldPolicyRulesDialogService {

    constructor(public dialog:MatDialog){

    }

    openDialog(feed:Feed, field:TableFieldPolicy) :Observable<any>{

        let data:FeedFieldPolicyDialogData = {feed:feed,field:field};

        const dialogRef = this.dialog.open(FeedFieldPolicyRulesDialogComponent, {
            width: '600px',
            data: data
        });

        dialogRef.afterClosed().subscribe((result:any) => {
            console.log('The dialog was closed');

        });

        return dialogRef.afterClosed()


    }

}