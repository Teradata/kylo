import { Injectable } from "@angular/core";

/**
 * Used to store temporary state of the Edit Feed Nifi Properties
 * when a user clicks the Edit link for the Feed Details so the object can be passed to the template factory
 *
 */
@Injectable()
export class EditFeedNifiPropertiesService {
    editFeedModel:any;
    constructor () {
        this.editFeedModel = {};
    }
}
