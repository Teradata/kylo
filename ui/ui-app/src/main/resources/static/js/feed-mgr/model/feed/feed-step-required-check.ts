import {Feed} from "./feed.model";
import {Step} from "./feed-step.model";


/**
 * Allow steps to write in custom logic to identify if it should be required or not
 * default will go back to the Step model and use the required flag there
 */
export class FeedStepRequiredCheck {

    constructor() { }

    isRequired(feed:Feed, step:Step) : boolean{
        return step.required;
    }
}