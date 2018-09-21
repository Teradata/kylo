import {OperationsRestUrlConstants} from "../../services/operations-rest-url-constants";
import {Feed} from "../../feed-mgr/model/feed/feed.model";
import {HttpClient} from "@angular/common/http";
import {Input, Output, EventEmitter, Component, OnInit, OnDestroy} from "@angular/core";
import {OpsManagerFeedService} from "../../ops-mgr/services/ops-manager-feed.service";
import {FeedSummary} from "../../feed-mgr/model/feed/feed-summary.model";
import {KyloIcons} from "../../kylo-utils/kylo-icons";
import BroadcastService from "../../services/broadcast-service";
import {TdDialogService} from "@covalent/core/dialogs";

@Component({
    selector: "feed-operations-health-info",
    templateUrl: "js/shared-components/feed-operations-health-info/feed-operations-health-info.component.html"
})
export class FeedOperationsHealthInfoComponent implements OnInit, OnDestroy{

    @Input()
    feed:Feed;

    @Output()
    feedChange = new EventEmitter<Feed>()

    @Output()
    feedHealthRefreshed = new EventEmitter<FeedSummary>();

    feedData:any;

    /**
     * final feed health object
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
        this.getFeedHealth();
        this.refreshInterval = setInterval(this.getFeedHealth.bind(this),this.refreshTime)
    }

    ngOnDestroy(){
        if(this.refreshInterval){
            clearInterval(this.refreshInterval);
        }
    }

    enableFeed(){
        this.feedStateChanging = true;
        this.opsManagerFeedService.enableFeed(this.feed.id).subscribe((feedSummary:FeedSummary)=> {
            this.feed.state = feedSummary.state;
            this.feedStateChanging = false;
            this.feedChange.emit(this.feed)
            this.opsManagerFeedService.openSnackBar("Enabled the feed",5000)
        }, error1 => {
            this.feedStateChanging = false;
            this._dialogService.openAlert({
                title:"Error enabling the feed",
                message:"There was an error enabling the feed"
            })
        });
    }
    disableFeed(){
        this.feedStateChanging = true;
        this.opsManagerFeedService.disableFeed(this.feed.id).subscribe((feedSummary:FeedSummary)=> {
            this.feed.state = feedSummary.state;
            this.feedStateChanging = false;
            this.feedChange.emit(this.feed)
            this.opsManagerFeedService.openSnackBar("Disabled the feed",5000)
        },error1 => {
            this.feedStateChanging = false;
            this._dialogService.openAlert({
                title:"Error disabling the feed",
                message:"There was an error disabling the feed"
            })
        });
    }

}