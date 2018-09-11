import {DataSource} from "../../../../catalog/api/models/datasource";
import {Injectable} from "@angular/core";
import {PreviewDataSet} from "../../../../catalog/datasource/preview-schema/model/preview-data-set";
import {FEED_DEFINITION_SECTION_STATE_NAME} from "../../../../model/feed/feed-constants";
import {DefineFeedService} from "../../services/define-feed.service";
import {TdDialogService} from "@covalent/core/dialogs";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {StateRegistry, StateService} from "@uirouter/angular";
import {Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";

export enum SourceSampleActiveView {
    VIEW_CONNECTORS="VIEW_CONNECTORS",VIEW_DATASOURCE="VIEW_DATASOURCE", NONE="NONE"
}

/**
 * TODO REMOVE THIS CLASS
 */
@Injectable()
export class DefineFeedSourceSampleService {
    constructor(private state: StateService, private defineFeedService:DefineFeedService, private _dialogService:TdDialogService, private feedLoadingService:FeedLoadingService){

    }

    currentDataSource:DataSource;
    activeView:SourceSampleActiveView = SourceSampleActiveView.NONE;
    feed:Feed;
    previews:PreviewDataSet[] = [];
    singleSelection:boolean;
    step:Step;

    setStep(step:Step){
        this.step = step;
    }

    setFeed(feed:Feed){
        this.feed = feed;
    }

    setPreviews(previews:PreviewDataSet[]){
        this.previews = previews;
    }

    setDataSource(ds:DataSource){
        this.currentDataSource = ds;
    }

    viewingConnectors(){
        this.activeView = SourceSampleActiveView.VIEW_CONNECTORS;
    }
    viewingDataSource(datasource?:DataSource){
        this.activeView = SourceSampleActiveView.VIEW_DATASOURCE;
        if(datasource){
            this.currentDataSource = datasource;
        }
    }
    viewingNone(){
        this.activeView = SourceSampleActiveView.NONE;
    }

    isViewingDataSource(){
        return this.activeView == SourceSampleActiveView.VIEW_DATASOURCE;
    }

    isViewingConnectors(){
        return this.activeView == SourceSampleActiveView.VIEW_CONNECTORS;
    }


    public saveFeedAndUpdateTarget() {
        if (this.previews && this.previews.length) {
            let feedDataSets = this.feed.sourceDataSets;
            //check to see if schema differs
            if (feedDataSets && feedDataSets.length > 0) {
                let feedDatasetKeys = feedDataSets.map(ds => ds.id).sort().toString();
                let newDatasetKeys = this.previews.map(ds => ds.key).sort().toString();
                if (feedDatasetKeys != "" && feedDatasetKeys != newDatasetKeys) {
                    //WARN different datasets
                    this._dialogService.openConfirm({
                        message: 'The dataset you have selected differs from the one existing on this feed. Switching the source will result in a new target schema.  Are you sure you want to do this?',
                        disableClose: true,
                        title: 'Confirm source dataset change',
                    }).afterClosed().subscribe((accept: boolean) => {
                        if (accept) {
                            this._saveFeed();
                        } else {
                            // no op
                        }
                    });


                }

            }
            else {
                this._saveFeed();
            }
        }
        else {
            this._dialogService.openAlert({
                message:"Unable to save the feed.  No datasets were detected",
                title: 'Unable to save the feed'
            })
        }
    }

    public backToCatalog(){
        this.state.go(FEED_DEFINITION_SECTION_STATE_NAME+".datasources",{feedId:this.feed.id,jumpToSource:false})
    }


    private _saveFeed() {
        if (this.previews && this.previews.length) {
            if (this.singleSelection) {
                this.feed.setSourceDataSetAndUpdateTarget(this.previews.map(ds => ds.toSparkDataSet())[0])
            }
            else {
                //wrangler feed
                this.feed.sourceDataSets = this.previews.map(ds => ds.toSparkDataSet());
                //TODO reset feed.tableSchema
            }
        }
        else {
            //set the source and target to empty
            this.feed.setSourceDataSetAndUpdateTarget(null);
        }

        this.step.setComplete(true)
        this.defineFeedService.saveFeed(this.feed).subscribe(result => {
            this.feedLoadingService.resolveLoading()

            this.backToCatalog();
        }, error1 => {
            this.step.setComplete(false)
            this.feedLoadingService.resolveLoading()
            this._dialogService.openAlert({
                message: "There was an error saving the source selection " + error1,
                title: "Error saving source selection"
            });
        });
    }
}