import {FeedStepValidator} from "../../../../model/feed/feed-step-validator";
import {Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";


export class DefineFeedTableValidator  extends FeedStepValidator {





    public validate(feed:Feed, step:Step) : boolean{
        if(this.hasFormErrors || !step.visited){
            step.valid = false;
            step.setComplete(false);
        }
        else if(feed.table.feedDefinitionTableSchema.fields.length ==0) {
            step.valid = true;
         step.setComplete(false);
        }
        else {
            step.valid = true;
            step.setComplete(step.saved);
        }
        return step.valid;
    }
}