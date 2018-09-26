import {Component, Inject, OnDestroy, OnInit} from "@angular/core";
import {MatDialogRef} from "@angular/material/dialog";
import FileUpload from "../../../../../../services/FileUploadService";
import {MAT_DIALOG_DATA} from "@angular/material/dialog";
import {RestUrlConstants} from "../../../../../services/RestUrlConstants";
import {MatSnackBar} from "@angular/material/snack-bar";
import {FEED_DEFINITION_SUMMARY_STATE_NAME} from "../../../../../model/feed/feed-constants";
import {DefineFeedService} from "../../../services/define-feed.service";
import {StateService} from "@uirouter/angular";
import {Feed} from "../../../../../model/feed/feed.model";

export class DeployFeedDialogComponentData{
    constructor(public feed:Feed){

    }
}

@Component({
    selector:"deploy-feed-dialog",
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/setup-guide-summary/deploy-feed-dialog/deploy-feed-dialog.component.html"
})
export class DeployFeedDialogComponent implements OnInit, OnDestroy{

    constructor(private dialog: MatDialogRef<DeployFeedDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: DeployFeedDialogComponentData, private defineFeedService:DefineFeedService, private  stateService: StateService) {
    }


    ngOnInit() {

    }
    ngOnDestroy(){

    }

    deployFeed(){
        this.defineFeedService.deployFeed(this.data.feed).subscribe((response:any) =>{
            if(response){
                let  redirectState = FEED_DEFINITION_SUMMARY_STATE_NAME+".feed-activity";
                this.stateService.go(redirectState,{feedId:this.data.feed.id,refresh:true}, {location:'replace'})
            }
        })
    }


    /**
     * Cancel this dialog.
     */
    cancel() {
        this.dialog.close();
    }


}