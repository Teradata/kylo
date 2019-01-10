import {Component, EventEmitter, Inject, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {TdDialogService} from "@covalent/core/dialogs";

import {RestResponseStatus, RestResponseStatusType} from "../../../../../../../lib/common/common.model";
import {KyloIcons} from "../../../../../../kylo-utils/kylo-icons";
import {OpsManagerFeedService} from "../../../../../../ops-mgr/services/ops-manager-feed.service";
import {BroadcastService} from "../../../../../../services/broadcast-service";
import {FeedStats} from "../../../../../model/feed/feed-stats.model";
import {FeedSummary} from "../../../../../model/feed/feed-summary.model";
import {Feed} from "../../../../../model/feed/feed.model";
import {DefineFeedService} from "../../../services/define-feed.service";
import {FeedUploadFileDialogComponent, FeedUploadFileDialogComponentData} from "../feed-upload-file-dialog/feed-upload-file-dialog.component";
import {RestUrlService} from "../../../../../services/RestUrlService";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {MatSnackBar} from "@angular/material/snack-bar";
import {FeedOperationsSummary} from "../../../../../model/feed/feed-operations-summary.model";

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
    feedHealthRefreshed = new EventEmitter<FeedOperationsSummary>();

    /**
     * feed operations data
     */
    feedHealth: FeedOperationsSummary = new FeedOperationsSummary({});

    feedHealthAvailable: boolean;

    refreshInterval: any;

    refreshTime: number = 10000;

    constructor(private http: HttpClient, private opsManagerFeedService: OpsManagerFeedService,
                @Inject("BroadcastService") private broadcastService: BroadcastService,
                private _dialogService: TdDialogService,
                private defineFeedService: DefineFeedService,
                @Inject("RestUrlService") restUrlService: RestUrlService,
                private snackBar: MatSnackBar) {
        this.broadcastService.subscribe(null, 'ABANDONED_ALL_JOBS', this.getFeedHealth.bind(this));
        this.restUrlService = restUrlService;
    }

    feedStateChanging: boolean;

    kyloIcons = KyloIcons;

    startingFeed: boolean;

    uploadFileAllowed: boolean;

    exportFeedUrl: string;

    exportInProgress: boolean = false;

    restUrlService: RestUrlService;

    getFeedHealth() {

        this.opsManagerFeedService.getFeedHealth(this.feed.getFullName()).subscribe((response: FeedOperationsSummary) => {
            if (response) {
                this.feedHealth = response;
                this.feedHealthAvailable = true;
                this.feedHealthRefreshed.emit(response)
            }
        })
    }

    ngOnInit() {
        this.exportFeedUrl = this.restUrlService.ADMIN_EXPORT_FEED_URL + "/" + this.feed.id;
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
        if (this.feed.accessControl.allowStart) {
            let config = {data: new FeedUploadFileDialogComponentData(this.feed.id), width: "500px"};
            this._dialogService.open(FeedUploadFileDialogComponent, config);
        }
    }

    uintToString(uintArray) {
        return String.fromCharCode.apply(null, new Uint8Array(uintArray));
    }


    exportFeed() {
        var snackBarRef = this.snackBar.open("Feed export is processing. Notification will appear when complete.", null, {
            duration: 3000,
        });

        this.exportInProgress = true;
        this.http.get(this.exportFeedUrl, {observe: "response", responseType: "arraybuffer"})
            .catch((errorResponse) => {
                this.exportInProgress = false;
                snackBarRef.dismiss();
                return Observable.throw(errorResponse)
            })
            .map((response) => {
                return response;
            })
            .subscribe(
                data => {
                    this.getZipFile(data);
                    this.exportInProgress = false;
                    this.snackBar.open("Feed export complete", null, {
                        duration: 3000,
                    });
                }
            )
    }


    getZipFile(data: any) {
        var a: any = document.createElement("a");
        document.body.appendChild(a);

        a.style = "display: none";
        var blob = new Blob([data], {type: 'application/zip'});

        var url = window.URL.createObjectURL(blob);

        a.href = url;
        a.download = this.feed.systemFeedName + ".feed.zip";
        a.click();
        window.URL.revokeObjectURL(url);
    }

    initMenu() {
        this.uploadFileAllowed = false;
        if (this.feed && this.feed.inputProcessorType) {
            this.uploadFileAllowed = this.feed.inputProcessorType == 'org.apache.nifi.processors.standard.GetFile'
        }
    }


    enableFeed() {
        if (this.feed.accessControl.allowEdit) {
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
        if (this.feed.accessControl.allowEdit) {
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
        if (this.feed.accessControl.allowStart) {
            if (!this.startingFeed) {
                this.startingFeed = true;

                let error = (msg?: string) => {
                    let message = msg ? msg : "The feed could not be started.";
                    let alertTitle = msg ? "Started the feed" : "Error starting the feed";

                    this._dialogService.openAlert({
                        title: alertTitle,
                        message: message
                    })
                }
                this.opsManagerFeedService.startFeed(this.feed.id).subscribe((response: RestResponseStatus) => {
                    this.startingFeed = false;
                    if (response.status == RestResponseStatusType.SUCCESS) {
                        let msg = response.message ? response.message : "Feed started";
                        this.opsManagerFeedService.openSnackBar(msg, 5000)
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
