import {FeedStepRequiredCheck} from "../../../../model/feed/feed-step-required-check";
import {Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";
import {UserProperty} from "../../../../model/user-property.model";

export class DefineFeedPropertiesRequiredCheck extends FeedStepRequiredCheck {

    constructor() {
        super();
    }

    isRequired(feed:Feed, step:Step):boolean {

        if(feed && feed.userProperties) {
            return feed.userProperties.find((prop:UserProperty) => prop.required == true && prop.locked == true) != undefined;
        }
        else {
            return step.required;
        }
    }
}