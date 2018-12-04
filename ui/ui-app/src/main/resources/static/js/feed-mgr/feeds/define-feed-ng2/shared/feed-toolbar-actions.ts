import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {DefineFeedService} from "../services/define-feed.service";
import {Feed} from "../../../model/feed/feed.model";

@Component({
    selector:"feed-toolbar-actions",
    template:`
      <div fxLayout="row" fxLayoutAlign="space-between" *ngIf="feed != undefined">
        <button mat-raised-button button color="accent" (click)="edit()" *ngIf="showEditLink">Edit</button>
        <button mat-button (click)="cancelEdit()" *ngIf="showCancelLink && feed.canEdit() &&  !feed.readonly ">Cancel</button>     
      </div>
        <div *ngIf="feed && feed.accessControl && feed.accessControl.accessMessage != ''">
          <button mat-button color="tc-grey-500" [matMenuTriggerFor]="accessMessageMenu" >
            <mat-icon >warning</mat-icon>
            {{"FeedDefinition.AccessControl.AccessControlIssues" | translate}}
          </button>
          <mat-menu #accessMessageMenu="matMenu">
            {{feed.accessControl.accessMessage | translate}}
          </mat-menu>
        </div>
    `
    }
)
export class FeedToolbarActions implements OnInit {


    @Input()
    feed:Feed;

    @Input()
    showEditLink:boolean = false; //feed.canEdit() && feed.readonly

    @Input()
    showDeleteLink:boolean = false;

    @Input()
    showCancelLink:boolean = true;

    @Output()
    feedChange=new EventEmitter<Feed>();


    @Output()
    onEdit = new EventEmitter<Feed>();

    @Output()
    onCancelEdit = new EventEmitter<Feed>();

    @Output()
    onDelete = new EventEmitter<Feed>();


    constructor(private defineFeedService:DefineFeedService){}


    ngOnInit(){

    }

    edit(){
        this.feed.readonly = false;
        this.defineFeedService.markFeedAsEditable();
        this.feedChange.emit(this.feed);
        this.onEdit.emit(this.feed);

    }

    cancelEdit(){
        if(this.onCancelEdit.observers.length ==0) {
            this.feed.readonly = true;
            this.defineFeedService.markFeedAsReadonly();
            //get the old copy of the feed prior to editing
            this.feed = this.defineFeedService.getFeed();
            this.feedChange.emit(this.feed);
            this.onCancelEdit.emit(this.feed);
        }
        else {
            this.onCancelEdit.emit(this.feed);
        }

    }

    deleteFeed(){
        //caller needs to add logic
        this.onDelete.emit(this.feed)
    }

}