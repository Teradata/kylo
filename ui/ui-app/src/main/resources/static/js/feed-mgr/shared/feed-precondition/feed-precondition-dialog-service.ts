import {MatDialog} from "@angular/material/dialog";
import {Observable} from "rxjs/Observable";
import {Injectable} from "@angular/core";
import {FeedPreconditionDialogComponent} from "./feed-precondition-dialog.component";
import {Feed} from "../../model/feed/feed.model";

@Injectable()
export class FeedPreconditionDialogService {
    constructor(public dialog:MatDialog){
    }

    openDialog(feed:Feed) : Observable<any> {
        const dialogRef = this.dialog.open(FeedPreconditionDialogComponent, {
            width: '600px',
            data: feed
        });

        dialogRef.afterClosed().subscribe((result:any) => {
            console.log('Precondition dialog was closed');
        });

        return dialogRef.afterClosed();
    }
}