import {FeedStepValidator} from "../../../../model/feed/feed-step-validator";
import {SKIP_SOURCE_CATALOG_KEY, Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";

export class DefineFeedStepSourceSampleValidator extends FeedStepValidator {

    constructor(){
        super();
    }

    public validate(feed:Feed) : boolean {
        let userAcknowledgedContinueWithoutSource = this.step.getPropertyAsBoolean(SKIP_SOURCE_CATALOG_KEY);
        if(userAcknowledgedContinueWithoutSource || (feed.sourceDataSets && feed.sourceDataSets.length>0) || (feed.table.sourceTableSchema && feed.table.sourceTableSchema.isDefined() )){
            this.step.valid = true;
            this.step.setComplete(true)
        }
        else {
            this.step.valid = false;
            this.step.setComplete(false)
        }
        return this.step.valid;
    }
}