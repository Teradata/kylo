import {FeedModel} from "../../feeds/define-feed-ng2/model/feed.model";
import {TableColumnDefinition} from "../../model/TableColumnDefinition";
import {TableFieldPolicy} from "../../model/TableFieldPolicy";

export interface FeedFieldPolicyDialogData {

    feed:FeedModel;
    field:TableFieldPolicy;
}