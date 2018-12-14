import {Feed} from "../../../model/feed/feed.model";
import {ItemSaveResponse} from '../../../shared/info-item/item-save-response';

export class SaveFeedResponse extends ItemSaveResponse {

    public newFeed: boolean;

    constructor(public feed:Feed, public success:boolean, public message :string){
        super(success, message);
    }


}