import {FeedStepValidator} from "../../../../model/feed/feed-step-validator";
import {Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";


export class DefineFeedStepSourceSampleValidator extends FeedStepValidator {

    public validate(feed:Feed) : boolean {
        if (feed.table.sourceTableSchema.fields.length == 0) {
            this.step.valid = true;
        }
        else {
            this.step.setComplete(true);
            this.step.valid = true;
        }
        return this.step.valid;
    }
}