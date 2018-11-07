import {FeedOperationsState} from "./feed.model";
import {FeedSummary} from "./feed-summary.model";

export class FeedOperationsSummary {

    /*
    the name of the feed
     */
    feed:string;
    state:FeedOperationsState;
    lastStatus:FeedOperationsState;
    waiting:boolean;
    running:boolean;
    timeSinceEndTime:number;
    timeSinceEndTimeString;
    runTime:number;
    runTimeString:string;
    healthy:boolean;
    lastExitCode:string;
    stream:boolean;
    displayStatus:string;
    sinceTimeString;
    icon:string;
    iconstyle:string;
    healthText:string;


    public constructor(init?: Partial<FeedOperationsSummary>) {
        this.initialize();
        Object.assign(this, init);
    }

    initialize(){

    }
}