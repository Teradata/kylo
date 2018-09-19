import {OperationsRestUrlConstants} from "../../services/operations-rest-url-constants";
import {Feed} from "../../feed-mgr/model/feed/feed.model";
import {HttpClient} from "@angular/common/http";
import {OperationsFeedUtil} from "../../services/operations-feed-util";
import {Input, Component, OnInit, OnDestroy} from "@angular/core";

@Component({
    selector: "feed-operations-health-info",
    templateUrl: "js/shared-components/feed-operations-health-info/feed-operations-health-info.component.html"
})
export class FeedOperationsHealthInfoComponent implements OnInit, OnDestroy{

    @Input()
    feed:Feed;

    feedData:any;

    /**
     * final feed health object
     */
    feedHealth:any = {};

    feedHealthAvailable:boolean;

    refreshInterval:any;

    refreshTime:number = 5000;

    constructor(private http:HttpClient){}



    getFeedHealth(){
        var successFn = (response: any)=> {
            if (response) {
                    //transform the data for UI
                    this.feedData = response;
                    if (this.feedData.feedSummary && this.feedData.feedSummary.length && this.feedData.feedSummary.length >0) {
                        let feedHealth = this.feedData.feedSummary[0];
                        OperationsFeedUtil.decorateFeedSummary(feedHealth);
                        this.feedHealth = feedHealth
                        this.feedHealthAvailable = true;
                    }
            }
        }
        var errorFn =  (err: any)=> {
        }
        var finallyFn =  ()=> {

        }
        let  feedName = this.feed.getFullName()


        this.http.get(OperationsRestUrlConstants.SPECIFIC_FEED_HEALTH_URL(feedName)).subscribe( successFn, errorFn);
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

}