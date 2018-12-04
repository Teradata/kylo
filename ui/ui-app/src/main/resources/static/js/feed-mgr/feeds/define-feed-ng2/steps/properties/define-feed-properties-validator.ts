import {FeedStepValidator} from "../../../../model/feed/feed-step-validator";
import {Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";
import {UserProperty} from "../../../../model/user-property.model";


export class DefineFeedPropertiesValidator  extends FeedStepValidator {



    public validate(feed:Feed, step:Step) : boolean{

        let valid = super.validate(feed,step);

        if(step.isRequired(feed) && (!this.checkRequiredProperties(feed) )) {
            step.valid = false;
            step.setComplete(false);
            valid = false;
        }
        return valid;
    }

    /**
     * check for any required properties
     * @param {Feed} feed
     * @return {boolean} true if valid, false if missing required properties
     */
    checkRequiredProperties(feed:Feed){
        if(feed && feed.userProperties) {
            return feed.userProperties.find((prop:UserProperty) => prop.required == true && prop.locked == true && this.isUndefined(prop.value)) == undefined;
        }
        else {
            return true;
        }
    }

    private isUndefined(value:any){
        return value == undefined || value == null || value == '';
    }
}