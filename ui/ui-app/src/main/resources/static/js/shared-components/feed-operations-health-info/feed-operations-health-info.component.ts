import {OperationsRestUrlConstants} from "../../services/operations-rest-url-constants";
import {Feed,FeedAccessControl} from "../../feed-mgr/model/feed/feed.model";
import {HttpClient} from "@angular/common/http";
import {Input, Output, EventEmitter, Component, OnInit, OnDestroy, Inject} from "@angular/core";
import {OpsManagerFeedService} from "../../ops-mgr/services/ops-manager-feed.service";
import {FeedSummary} from "../../feed-mgr/model/feed/feed-summary.model";
import {KyloIcons} from "../../kylo-utils/kylo-icons";
import BroadcastService from "../../services/broadcast-service";
import {TdDialogService} from "@covalent/core/dialogs";
import {RestResponseStatus, RestResponseStatusType} from "../../common/common.model";
import {FeedStats} from "../../feed-mgr/model/feed/feed-stats.model";

@Component({
    selector: "feed-operations-health-info",
    templateUrl: "js/shared-components/feed-operations-health-info/feed-operations-health-info.component.html"
})
export class FeedOperationsHealthInfoComponent implements OnInit, OnDestroy{

    @Input()
    feed:Feed;

    @Input()
    feedStats?:FeedStats;

    @Output()
    feedChange = new EventEmitter<Feed>()

    @Output()
    feedHealthRefreshed = new EventEmitter<FeedSummary>();

    /**
     * feed operations data
     */
    feedHealth:FeedSummary = new FeedSummary({});

    feedHealthAvailable:boolean;

    refreshInterval:any;

    refreshTime:number = 5000;

    constructor(private opsManagerFeedService:OpsManagerFeedService,   private broadcastService: BroadcastService, private _dialogService:TdDialogService){

        this.broadcastService.subscribe(null, 'ABANDONED_ALL_JOBS', this.getFeedHealth.bind(this));
    }

    feedStateChanging:boolean;

    kyloIcons = KyloIcons;

    startingFeed:boolean;

    accessControl:FeedAccessControl = FeedAccessControl.NO_ACCESS;


    getFeedHealth(){

        this.opsManagerFeedService.getFeedHealth(this.feed.getFullName()).subscribe((response:FeedSummary) => {
            if(response){
                this.feedHealth = response;
                this.feedHealthAvailable = true;
                this.feedHealthRefreshed.emit(response)
            }
        })
    }

    ngOnInit(){
        this.accessControl = this.feed.accessControl;
        this.getFeedHealth();
        this.refreshInterval = setInterval(this.getFeedHealth.bind(this),this.refreshTime)

    }

    ngOnDestroy(){
        if(this.refreshInterval){
            clearInterval(this.refreshInterval);
        }
    }





    enableFeed(){
        if(this.accessControl.allowEdit) {
            this.feedStateChanging = true;
            this.opsManagerFeedService.enableFeed(this.feed.id).subscribe((feedSummary: FeedSummary) => {
                this.feed.state = feedSummary.state;
                this.feedStateChanging = false;
                this.feedChange.emit(this.feed)
                this.opsManagerFeedService.openSnackBar("Enabled the feed", 5000)
            }, error1 => {
                this.feedStateChanging = false;
                this._dialogService.openAlert({
                    title: "Error enabling the feed",
                    message: "There was an error enabling the feed"
                })
            });
        }
    }
    disableFeed(){
        if(this.accessControl.allowEdit) {
            this.feedStateChanging = true;
            this.opsManagerFeedService.disableFeed(this.feed.id).subscribe((feedSummary: FeedSummary) => {
                this.feed.state = feedSummary.state;
                this.feedStateChanging = false;
                this.feedChange.emit(this.feed)
                this.opsManagerFeedService.openSnackBar("Disabled the feed", 5000)
            }, error1 => {
                this.feedStateChanging = false;
                this._dialogService.openAlert({
                    title: "Error disabling the feed",
                    message: "There was an error disabling the feed"
                })
            });
        }
    }
    
    startFeed(){
        if(this.accessControl.allowStart) {
            if (!this.startingFeed) {
                this.startingFeed = true;

                let error = (msg?:string) => {
                    let errorMessage = msg ? msg : "The feed could not be started.";

                    this._dialogService.openAlert({
                        title: "Error starting the feed",
                        message: errorMessage
                    })
                }
                this.opsManagerFeedService.startFeed(this.feed.id).subscribe((response: RestResponseStatus) => {
                    this.startingFeed = false;
                    if(response.status == RestResponseStatusType.SUCCESS) {
                        let msg = response.message ? response.message : "Feed started";
                        this.opsManagerFeedService.openSnackBar(msg, 5000)
                    }
                    else {
                        error(response.message)
                    }

                }, (error1: any) => {
                    this.startingFeed = false
                    let msg = error1 && (typeof error1 == "string") ? error1 : undefined;
                    error(msg)
                });
            }
        }
    }

}