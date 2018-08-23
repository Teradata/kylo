import {FeedStepValidator} from "../../../../model/feed/feed-step-validator";
import {Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";
import {PreviewDatasetCollectionService} from "../../../../catalog/api/services/preview-dataset-collection.service";


export class DefineFeedStepSourceSampleValidator extends FeedStepValidator {

    constructor(private collectionService:PreviewDatasetCollectionService){
        super();
    }

    public validate(feed:Feed) : boolean {
        if(feed.isDefineTable()) {
            if (feed.table.sourceTableSchema.fields.length == 0) {
                this.step.valid = true;
                this.step.setComplete(false);
            }
            else {
                this.step.setComplete(true);
                this.step.valid = true;
            }
        }
        else if(feed.isDataTransformation() ){
            if(this.collectionService.datasetCount() >0){
                this.step.setComplete(true);
                this.step.valid = true;
            }else {
                this.step.setComplete(false);
                this.step.valid = false;
            }
        }
        else {
            return this.step.valid;
        }
    }
}