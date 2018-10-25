import {EntityVersion} from "./entity-version.model";
import {Feed} from "./feed/feed.model";
import {FormGroup} from "@angular/forms";
import {NifiErrorMessage} from "./nifi-error-message.model";


export interface DeployFeedNifiProcessGroup {
    allErrors:NifiErrorMessage[];
    controllerServiceErrors:NifiErrorMessage[];
    rolledBack:boolean;
    success:boolean;
}

export interface NifiFeed {
    feedMetadata:Feed;
    success:boolean;
    feedProcessGroup:DeployFeedNifiProcessGroup;
}

export interface DeployEntityErrors {
    message:string;
    errorMap:{[key:string]: NifiErrorMessage[]}
    errorCount:number;
}

export interface DeployEntityVersionResponse extends EntityVersion{
    httpStatus?:number;
    httpStatusText?:string;
    errors?:DeployEntityErrors;

    feed:NifiFeed;

}