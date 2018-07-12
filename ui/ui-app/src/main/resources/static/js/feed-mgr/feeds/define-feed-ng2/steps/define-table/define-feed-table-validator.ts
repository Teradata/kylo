import {FeedStepValidator} from "../../model/feed-step-validator";
import {FeedModel} from "../../model/feed.model";


export class DefineFeedTableValidator  extends FeedStepValidator {





    public validate(feed:FeedModel) : boolean{
        if(this.hasFormErrors){
            this.step.setComplete(false);
        }
        else if(feed.table.tableSchema.fields.length ==0) {
         this.step.setComplete(false);
        }
        else {
            this.step.setComplete(true);
        }
        console.log("Validate step finished",this.step, feed)
        return this.step.complete;
    }
}