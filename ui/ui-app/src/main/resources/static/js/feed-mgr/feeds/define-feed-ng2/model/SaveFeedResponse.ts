import {FeedModel} from "./feed.model";

export class SaveFeedResponse {

    public newFeed:boolean

    constructor(public feed:FeedModel, public success:boolean, public message :string){

    }


}