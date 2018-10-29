import {Feed} from "../../model/feed/feed.model";
import {TableFieldPolicy} from "../../model/TableFieldPolicy";

export interface FeedFieldPolicyDialogData {

    feed:Feed;
    field:TableFieldPolicy;
}