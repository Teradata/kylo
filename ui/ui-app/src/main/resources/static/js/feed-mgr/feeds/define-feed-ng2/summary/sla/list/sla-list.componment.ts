import {Component, Injector, Input, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {FEED_DEFINITION_SECTION_STATE_NAME, FEED_DEFINITION_STATE_NAME} from '../../../../../model/feed/feed-constants';
import {Sla} from '../sla.componment';
import {Feed, FeedState} from '../../../../../model/feed/feed.model';
import {SlaDetailsComponent} from '../details/sla-details.componment';
import {LoadingMode, LoadingType, TdLoadingService} from '@covalent/core/loading';
import {FeedLoadingService} from '../../../services/feed-loading-service';
import AccessConstants from '../../../../../../constants/AccessConstants';

@Component({
    selector: "sla-list",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/sla/list/sla-list.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/sla/list/sla-list.component.html"
})
export class SlaListComponent implements OnInit {

    private static feedLoader: string = "SlaDetailsComponent.feedLoader";

    @Input() stateParams:any;

    private feedId: string;
    private slaService: any;
    private accessControlService: any;
    private loading = false;
    serviceLevelAgreements: Sla[] = [];
    private allowCreate = false;
    stateDisabled = FeedState.DISABLED;
    private feedModel: Feed;

    constructor(private $$angularInjector: Injector, private state: StateService, private loadingService: TdLoadingService, private feedLoadingService: FeedLoadingService) {
        this.slaService = $$angularInjector.get("SlaService");
        this.accessControlService = $$angularInjector.get("AccessControlService");
        this.createLoader(SlaListComponent.feedLoader);

        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                if (this.accessControlService.hasAction(AccessConstants.SLA_EDIT, actionSet.actions)) {
                    this.allowCreate = true;
                }
            });
    }

    ngOnInit() {
        this.feedId = this.stateParams ? this.stateParams.feedId : undefined;
        this.loadFeedSlas(this.feedId);
        this.loadFeed(this.feedId);
    }

    private createLoader(name: string) {
        this.loadingService.create({
            name: name,
            mode: LoadingMode.Indeterminate,
            type: LoadingType.Linear,
            color: 'accent',
        });
    }

    private loadFeed(feedId:string):void {
        this.loadingService.register(SlaListComponent.feedLoader);

        this.feedLoadingService.loadFeed(feedId).subscribe((feedModel:Feed) => {
            this.feedModel = feedModel;
            this.loadingService.resolve(SlaListComponent.feedLoader);
        },(error:any) =>{
            this.loadingService.resolve(SlaListComponent.feedLoader);
            console.log('error loading feed for id ' + feedId);
        });
    }

    loadFeedSlas(feedId: string) {
        this.slaService.getFeedSlas(feedId).then((response: any) => {
            if (response.data && response.data != undefined && response.data.length > 0) {
                this.serviceLevelAgreements = response.data;
            }
            this.loading = false;
        });
    }

    editExistingSla(sla: Sla): void {
        this.state.go(FEED_DEFINITION_STATE_NAME+".sla.edit", {slaId: sla.id});
    }

    createNewSla(): void {
        this.state.go(FEED_DEFINITION_STATE_NAME+".sla.new");
    }
}

