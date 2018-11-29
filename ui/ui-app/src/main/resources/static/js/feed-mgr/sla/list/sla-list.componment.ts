import {Component, Injector, Input, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {FEED_DEFINITION_SECTION_STATE_NAME, FEED_DEFINITION_STATE_NAME, FEED_DEFINITION_SUMMARY_STATE_NAME} from '../../model/feed/feed-constants';
import {Sla} from '../model/sla.model';
import {Feed, FeedState} from '../../model/feed/feed.model';
import {SlaDetailsComponent} from '../details/sla-details.componment';
import {LoadingMode, LoadingType, TdLoadingService} from '@covalent/core/loading';
import AccessConstants from '../../../constants/AccessConstants';
import {KyloIcons} from "../../../kylo-utils/kylo-icons";
import {SlaService} from "../../services/sla.service";
import {error} from "ng-packagr/lib/util/log";

@Component({
    selector: "sla-list",
    styleUrls: ["./sla-list.component.scss"],
    templateUrl: "./sla-list.component.html"
})
export class SlaListComponent implements OnInit {

    private static feedLoader: string = "SlaDetailsComponent.feedLoader";

    @Input()
    stateParams:any;

    @Input("feed")
    feedModel: Feed;

    private feedId: string;
    private accessControlService: any;
    loading = true;
    serviceLevelAgreements: Sla[] = [];
    allowCreate = false;
    stateDisabled = FeedState.DISABLED;


    public kyloIcons_Links_sla = KyloIcons.Links.sla;

    constructor(private $$angularInjector: Injector, private state: StateService, private loadingService: TdLoadingService, private slaService:SlaService) {
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
        if(this.feedModel) {
            this.loadFeedSlas(this.feedId);
        }
        else {
            this.loadSlas();
        }
      //  this.loadFeed(this.feedId);
    }

    private createLoader(name: string) {
        this.loadingService.create({
            name: name,
            mode: LoadingMode.Indeterminate,
            type: LoadingType.Linear,
            color: 'accent',
        });
    }
/*
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
    */

    loadFeedSlas(feedId: string) {
        this.loading = true;
        this.slaService.getFeedSlas(feedId).then((response: any) => {
                this.serviceLevelAgreements = response;
                this.loading = false;
        }, error => this.loading = false);
    }

    loadSlas() {
        this.loading = true;

        this.slaService.getAllSlas().then((response: any) => {
            this.serviceLevelAgreements = response;
            this.loading = false;
        }, error => this.loading = false);
    }

    editExistingSla(sla: Sla): void {
        this.state.go("^.edit", {slaId: sla.id});
    }

    createNewSla(): void {
        this.state.go("^.new");
    }
}

