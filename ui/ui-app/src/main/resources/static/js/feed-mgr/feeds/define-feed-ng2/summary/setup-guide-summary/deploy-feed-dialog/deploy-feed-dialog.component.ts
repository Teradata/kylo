import {Component, Inject, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {MatDialogRef} from "@angular/material/dialog";
import FileUpload from "../../../../../../services/FileUploadService";
import {MAT_DIALOG_DATA} from "@angular/material/dialog";
import {RestUrlConstants} from "../../../../../services/RestUrlConstants";
import {MatSnackBar} from "@angular/material/snack-bar";
import {FEED_DEFINITION_SUMMARY_STATE_NAME} from "../../../../../model/feed/feed-constants";
import {DefineFeedService} from "../../../services/define-feed.service";
import {StateService} from "@uirouter/angular";
import {Feed} from "../../../../../model/feed/feed.model";
import {FeedScheduleComponent} from "../feed-schedule/feed-schedule.component";
import {FormControl, FormGroup} from "@angular/forms";
import {SaveFeedResponse} from "../../../model/save-feed-response.model";
import {FeedLoadingService} from "../../../services/feed-loading-service";

export class DeployFeedDialogComponentData{
    constructor(public feed:Feed){

    }
}

@Component({
    selector:"deploy-feed-dialog",
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/setup-guide-summary/deploy-feed-dialog/deploy-feed-dialog.component.html"
})
export class DeployFeedDialogComponent implements OnInit, OnDestroy{

    formGroup:FormGroup;


    @ViewChild("feedSchedule")
    feedSchedule: FeedScheduleComponent;

    feed:Feed;

    deployingFeed:boolean;

    deployError:boolean;

    deployErrorMessage:string;

    constructor(private dialog: MatDialogRef<DeployFeedDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: DeployFeedDialogComponentData, private defineFeedService:DefineFeedService, private  stateService: StateService) {
        this.feed = this.data.feed;
        this.formGroup = new FormGroup({});

    }


    ngOnInit() {
        this.formGroup.addControl("enableFeed",new FormControl(this.feed.active))
    }
    ngOnDestroy(){

    }

    deployFeed(){
this.deployError = false;
this.deployErrorMessage = '';
        let deploy = () => {
            this.deployingFeed = true;
            this.defineFeedService.deployFeed(this.data.feed).subscribe((response:any) =>{
                if(response){
                    this.dialog.close();
                    this.deployingFeed = false;
                    let  redirectState = FEED_DEFINITION_SUMMARY_STATE_NAME+".feed-activity";
                    this.stateService.go(redirectState,{feedId:this.data.feed.id,refresh:true}, {location:'replace'})
                }
                else {
                    this.deployingFeed = false;
                    this.deployError = true;
                    this.deployErrorMessage= "There was an error deploying your feed";
                }
            })
        }


        if(this.formGroup.dirty) {
            this.deployingFeed = true;
            //if user chooses to upate the model save first
            let feed = this.feedSchedule.updateModel();
            feed.active = this.formGroup.get("enableFeed").value
            this.defineFeedService.saveFeed(feed).subscribe((response:SaveFeedResponse) => {
                if(response.success) {
                    deploy();
                }
            }, error1 =>  {
                //ERROR!!
                this.deployError = true;
                this.deployErrorMessage= "There was an error deploying your feed";
                this.deployingFeed = false;
            })
        }
        else {
            deploy();
        }


    }


    /**
     * Cancel this dialog.
     */
    cancel() {
        this.dialog.close();
    }


}