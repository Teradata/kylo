import {Feed} from "../../../model/feed/feed.model";

export class SaveFeedResponse {

    public newFeed:boolean

    constructor(public feed:Feed, public success:boolean, public message :string){

    }


}