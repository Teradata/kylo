import {MatDialog} from "@angular/material/dialog";
import {Observable} from "rxjs/Observable";
import {Injectable, ViewContainerRef} from "@angular/core";
import {FeedPreconditionDialogComponent} from "./feed-precondition-dialog.component";
import {Feed} from "../../model/feed/feed.model";

@Injectable()
export class FeedPreconditionDialogService {
    constructor(public dialog:MatDialog){
    }

    openDialog(input: any, viewContainerRef?:ViewContainerRef) : Observable<any> {
        const dialogRef = this.dialog.open(FeedPreconditionDialogComponent, {
            width: '600px',
            data: input,
            viewContainerRef:viewContainerRef
        });



        return dialogRef.afterClosed();
    }
}