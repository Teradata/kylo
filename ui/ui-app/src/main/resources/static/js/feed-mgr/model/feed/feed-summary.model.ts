import {FeedOperationsState, FeedState} from "./feed.model";

/**
 * light weight summary about the feed
 */
export class FeedSummary {

    categoryName:string;
    systemCategoryName:string;
    categoryId:string;
    categoryIcon:string;
    categoryIconColor:string;
    id:string;
    feedId:string;
    feedName:string;
    systemFeedName:string;
    active:boolean;
    state:FeedState;
    mode:string;
    updateDate:Date;
    templateName:string;
    templateId:string;
    healthText?:string;
    running?:boolean;
    operationsState:FeedOperationsState


    public constructor(init?: Partial<FeedSummary>) {
        this.initialize();
        Object.assign(this, init);
    }

    initialize(){

    }




}