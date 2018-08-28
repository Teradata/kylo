import {FeedStepValidator} from "../../../../model/feed/feed-step-validator";
import {Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";


export class DefineFeedTableValidator  extends FeedStepValidator {





    public validate(feed:Feed) : boolean{
        if(this.hasFormErrors){
            this.step.valid = false;
            this.step.setComplete(false);
        }
        else if(feed.table.tableSchema.fields.length ==0) {
            this.step.valid = true;
         this.step.setComplete(false);
        }
        else {
            this.step.valid = true;
            this.step.setComplete(true);
        }
        return this.step.valid;
    }
}