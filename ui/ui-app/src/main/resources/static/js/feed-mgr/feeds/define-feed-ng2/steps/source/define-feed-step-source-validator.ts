import {FeedStepValidator} from "../../../../model/feed/feed-step-validator";
import {SKIP_SOURCE_CATALOG_KEY, Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";

export class DefineFeedStepSourceValidator extends FeedStepValidator {

    constructor(){
        super();
    }

    public validate(feed:Feed, step:Step) : boolean {
      return  super.validate(feed,step);
        /*
        let userAcknowledgedContinueWithoutSource = step.getPropertyAsBoolean(SKIP_SOURCE_CATALOG_KEY);
        if(userAcknowledgedContinueWithoutSource || (feed.sourceDataSets && feed.sourceDataSets.length>0) || (feed.table.sourceTableSchema && feed.table.sourceTableSchema.isDefined() )){
            step.valid = true;
            step.setComplete(true)
        }
        else {
            step.valid = false;
            step.setComplete(false)
        }
        return step.valid;
        */
    }
}