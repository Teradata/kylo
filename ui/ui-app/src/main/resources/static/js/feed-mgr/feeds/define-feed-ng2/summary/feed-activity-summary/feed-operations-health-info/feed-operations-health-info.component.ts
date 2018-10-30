import {Component, EventEmitter, Inject, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {TdDialogService} from "@covalent/core/dialogs";

import {RestResponseStatus, RestResponseStatusType} from "../../../../../../common/common.model";
import {KyloIcons} from "../../../../../../kylo-utils/kylo-icons";
import {OpsManagerFeedService} from "../../../../../../ops-mgr/services/ops-manager-feed.service";
import {BroadcastService} from "../../../../../../services/broadcast-service";
import {FeedStats} from "../../../../../model/feed/feed-stats.model";
import {FeedSummary} from "../../../../../model/feed/feed-summary.model";
import {Feed, FeedAccessControl} from "../../../../../model/feed/feed.model";
import {DefineFeedService} from "../../../services/define-feed.service";
import {FeedUploadFileDialogComponent, FeedUploadFileDialogComponentData} from "../feed-upload-file-dialog/feed-upload-file-dialog.component";
import {RestUrlService} from "../../../../../services/RestUrlService";

@Component({
    selector: "feed-operations-health-info",
    templateUrl: "./feed-operations-health-info.component.html"
})
export class FeedOperationsHealthInfoComponent implements OnInit, OnDestroy {

    @Input()
    feed: Feed;

    @Input()
    feedStats?: FeedStats;

    @Output()
    feedChange = new EventEmitter<Feed>()

    @Output()
    feedHealthRefreshed = new EventEmitter<FeedSummary>();

    /**
     * feed operations data
     */
    feedHealth: FeedSummary = new FeedSummary({});

    feedHealthAvailable: boolean;

    refreshInterval: any;

    refreshTime: number = 5000;

    constructor(private opsManagerFeedService: OpsManagerFeedService,
                @Inject("BroadcastService") private broadcastService: BroadcastService,
                private _dialogService: TdDialogService,
                private defineFeedService: DefineFeedService,
                @Inject("RestUrlService") restUrlService: RestUrlService) {
        this.broadcastService.subscribe(null, 'ABANDONED_ALL_JOBS', this.getFeedHealth.bind(this));
        this.restUrlService = restUrlService;
    }

    feedStateChanging: boolean;

    kyloIcons = KyloIcons;

    startingFeed: boolean;

    accessControl: FeedAccessControl = FeedAccessControl.NO_ACCESS;

    uploadFileAllowed: boolean;

    exportFeedUrl: string;

    restUrlService: RestUrlService;

    getFeedHealth() {

        this.opsManagerFeedService.getFeedHealth(this.feed.getFullName()).subscribe((response: FeedSummary) => {
            if (response) {
                this.feedHealth = response;
                this.feedHealthAvailable = true;
                this.feedHealthRefreshed.emit(response)
            }
        })
    }

    ngOnInit() {
        this.exportFeedUrl = this.restUrlService.ADMIN_EXPORT_FEED_URL + "/" + this.feed.id;
        this.accessControl = this.feed.accessControl;
        this.getFeedHealth();
        this.refreshInterval = setInterval(this.getFeedHealth.bind(this), this.refreshTime)
        this.initMenu();

    }

    ngOnDestroy() {
        if (this.refreshInterval) {
            clearInterval(this.refreshInterval);
        }
    }

    onDelete() {
        //confirm then delete
        this.defineFeedService.deleteFeed();
    }

    uploadFile() {
        if (this.accessControl.allowStart) {
            let config = {data: new FeedUploadFileDialogComponentData(this.feed.id), width: "500px"};
            this._dialogService.open(FeedUploadFileDialogComponent, config);
        }
    }

    initMenu() {
        this.uploadFileAllowed = false;
        if (this.feed && this.feed.inputProcessorType) {
            this.uploadFileAllowed = this.feed.inputProcessorType == 'org.apache.nifi.processors.standard.GetFile'
        }
    }


    enableFeed() {
        if (this.accessControl.allowEdit) {
            this.feedStateChanging = true;
            this.opsManagerFeedService.enableFeed(this.feed.id).subscribe((feedSummary: FeedSummary) => {
                this.feed.state = feedSummary.state;
                this.feedStateChanging = false;
                this.feedChange.emit(this.feed)
                this.opsManagerFeedService.openSnackBar("Enabled the feed", 5000)
            }, error1 => {
                this.feedStateChanging = false;
                this._dialogService.openAlert({
                    title: "Error enabling the feed",
                    message: "There was an error enabling the feed"
                })
            });
        }
    }

    disableFeed() {
        if (this.accessControl.allowEdit) {
            this.feedStateChanging = true;
            this.opsManagerFeedService.disableFeed(this.feed.id).subscribe((feedSummary: FeedSummary) => {
                this.feed.state = feedSummary.state;
                this.feedStateChanging = false;
                this.feedChange.emit(this.feed)
                this.opsManagerFeedService.openSnackBar("Disabled the feed", 5000)
            }, error1 => {
                this.feedStateChanging = false;
                this._dialogService.openAlert({
                    title: "Error disabling the feed",
                    message: "There was an error disabling the feed"
                })
            });
        }
    }

    startFeed() {
        if (this.accessControl.allowStart) {
            if (!this.startingFeed) {
                this.startingFeed = true;

                let error = (msg?: string) => {
                    let message = msg ? msg : "The feed could not be started.";
                    let alertTitle = msg? "Started the feed" : "Error starting the feed";

                    this._dialogService.openAlert({
                        title: alertTitle,
                        message: message
                    })
                }
                this.opsManagerFeedService.startFeed(this.feed.id).subscribe((response: RestResponseStatus) => {
                    this.startingFeed = false;
                    if (response.status == RestResponseStatusType.SUCCESS) {
                        let msg = response.message ? response.message : "Feed started";
                        this.opsManagerFeedService.openSnackBar('Riaz'+msg, 5000)
                    }
                    else {
                        error(response.message)
                    }

                }, (error1: any) => {
                    this.startingFeed = false
                    let msg = error1 && (typeof error1 == "string") ? error1 : undefined;
                    error(msg)
                });
            }
        }
    }

}
