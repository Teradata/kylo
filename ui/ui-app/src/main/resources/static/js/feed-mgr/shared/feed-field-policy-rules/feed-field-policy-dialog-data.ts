import {Feed} from "../../model/feed/feed.model";
import {TableColumnDefinition} from "../../model/TableColumnDefinition";
import {TableFieldPolicy} from "../../model/TableFieldPolicy";

export interface FeedFieldPolicyDialogData {

    feed:Feed;
    field:TableFieldPolicy;
}