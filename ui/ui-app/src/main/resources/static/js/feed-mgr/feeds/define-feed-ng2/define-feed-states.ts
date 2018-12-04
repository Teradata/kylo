import {Ng2StateDeclaration, StateService} from "@uirouter/angular";
import {catchError} from "rxjs/operators/catchError";
import {first} from "rxjs/operators/first";
import {DefineFeedComponent} from "./define-feed.component";
import {DefineFeedSelectTemplateComponent} from "./select-template/define-feed-select-template.component";
import {DefineFeedService} from "./services/define-feed.service";
import {DefineFeedContainerComponent} from "./steps/define-feed-container/define-feed-container.component";
import {DefineFeedStepFeedDetailsComponent} from "./steps/feed-details/define-feed-step-feed-details.component";
import {DefineFeedTableComponent} from "./steps/define-table/define-feed-table.component";
import {FEED_DEFINITION_SECTION_STATE_NAME, FEED_DEFINITION_STATE_NAME, FEED_DEFINITION_SUMMARY_STATE_NAME, FEED_OVERVIEW_STATE_NAME} from "../../model/feed/feed-constants";
import {DefineFeedStepWranglerComponent} from "./steps/wrangler/define-feed-step-wrangler.component";
import {ProfileComponent} from './summary/profile/profile.component';
import {OverviewComponent} from './summary/overview/overview.component';
import {FeedLineageComponment} from "./summary/feed-lineage/feed-lineage.componment";
import {ProfileContainerComponent} from './summary/profile/container/profile-container.component';
import {ProfileHistoryComponent} from './summary/profile/history/profile-history.component';
import {DefineFeedPermissionsComponent} from "./steps/permissions/define-feed-permissions.component";
import {DefineFeedPropertiesComponent} from "./steps/properties/define-feed-properties.component";
import {FeedSlaComponent} from './summary/sla/feed-sla.component';
import {DefineFeedStepSourceComponent} from "./steps/source/define-feed-step-source.component";
import {FeedActivitySummaryComponent} from "./summary/feed-activity-summary/feed-activity-summary.component";
import {SetupGuideSummaryComponent} from "./summary/setup-guide-summary/setup-guide-summary.component";
import {FeedSummaryContainerComponent} from "./summary/feed-summary-container.component";
import {LoadMode} from "../../model/feed/feed.model";
import {ImportFeedComponent} from "../define-feed-ng2/import/import-feed.component";
import {FeedVersionsComponent} from "./summary/versions/feed-versions.component";
import {SlaListComponent} from "../../sla/list/sla-list.componment";
import {SlaDetailsComponent} from "../../sla/details/sla-details.componment";
import {Observable} from "rxjs/Observable";
import AccessConstants from "../../../constants/AccessConstants";
import {AccessDeniedComponent} from "../../../common/access-denied/access-denied.component";
const resolveFeed = {
    token: 'feed',
    deps: [StateService, DefineFeedService],
    resolveFn: loadFeed
};

export const defineFeedStates: Ng2StateDeclaration[] = [
    {
        name: FEED_DEFINITION_STATE_NAME,
        url: "/"+FEED_DEFINITION_STATE_NAME,
        redirectTo: FEED_DEFINITION_STATE_NAME+".select-template",
        views: {
            "content": {
                component: DefineFeedComponent
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: ""
        }
    },
    {
        name: FEED_DEFINITION_STATE_NAME+".import-feed",
        url: "/import-feed",
        component:ImportFeedComponent,
        data: {
            breadcrumbRoot: true,
            displayName: "",
            permissionsKey:"IMPORT_FEED"
        }
    },
    {
        name: FEED_DEFINITION_STATE_NAME+".select-template",
        url: "/select-template",
        component: DefineFeedSelectTemplateComponent
    },
    {
        name: FEED_DEFINITION_SECTION_STATE_NAME,
        url: "/section",
        redirectTo: FEED_DEFINITION_SECTION_STATE_NAME+".setup-guide",
        component: DefineFeedContainerComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            }
        ]
    },
    {
        name: FEED_DEFINITION_SECTION_STATE_NAME+".setup-guide",
        url: "/:feedId/setup-guide",
        component: SetupGuideSummaryComponent,
        params:{feedId:{type:"string"},
            loadMode:LoadMode.LATEST, squash: true},
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            }
        ],
        data: {
            permissionsKey:"FEED_DETAILS"
        }
    },
    {
        name: FEED_DEFINITION_SECTION_STATE_NAME+".deployed-setup-guide",
        url: "/:feedId/deployed-setup-guide",
        component: SetupGuideSummaryComponent,
        params:{feedId:{type:"string"},
            loadMode:LoadMode.DEPLOYED,
            refresh:false},
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            },
            {
                token: 'loadMode',
                resolveFn: resolveLoadModeDeployed
            },
            {
                token: 'refresh',
                resolveFn: resolveFalse
            }
        ],
        data: {
            permissionsKey:"FEED_DETAILS"
        }
    },
    {
        name: FEED_DEFINITION_SECTION_STATE_NAME+".access-denied",
        url: "/:feedId/access-denied",
        params:{feedId:null, attemptedState:null},
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            }],
        component: AccessDeniedComponent
    },

    {
        name: FEED_DEFINITION_SECTION_STATE_NAME+".feed-permissions",
        url: "/:feedId/feed-permissions",
        component: DefineFeedPermissionsComponent
    },
    {
        name: FEED_DEFINITION_SECTION_STATE_NAME+".feed-properties",
        url: "/:feedId/feed-properties",
        component: DefineFeedPropertiesComponent
    },
    {
        name: FEED_DEFINITION_SECTION_STATE_NAME+".feed-details",
        url: "/:feedId/feed-details",
        component: DefineFeedStepFeedDetailsComponent
    },
    {
        name: FEED_DEFINITION_SECTION_STATE_NAME+".feed-table",
        url: "/:feedId/feed-table",
        component: DefineFeedTableComponent
    },
    {
        name: FEED_DEFINITION_SECTION_STATE_NAME+".wrangler",
        url: "/:feedId/wrangler",
        component: DefineFeedStepWranglerComponent,
        data:{
            permissionsKey:"FEED_STEP_WRANGLER",
            accessRedirect:"feed-definition.section.access-denied",
        }
    },
    {
        name: FEED_DEFINITION_SECTION_STATE_NAME + ".datasources",
        url: "/:feedId/source-sample",
        component: DefineFeedStepSourceComponent,
    },

    {
        name: FEED_DEFINITION_SUMMARY_STATE_NAME,
        url: "/:feedId/summary",
        component: FeedSummaryContainerComponent,
        params: {feedId:{type:"string"},
                 refresh:false},
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            }

            ]
    },
    /*
    {
        name: FEED_DEFINITION_SUMMARY_STATE_NAME,
        url: "/:feedId/summary",
        redirectTo: (trans:Transition) => {
           // const uiInjector = trans.injector();
           // const $injector = uiInjector.get('$injector'); // native injector
            return trans.injector().getAsync('redirectState');
        },

        component: FeedSummaryContainerComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: (state: StateService) => state.transition.params()
            },
            {
                token: "redirectState",
                deps: [DefineFeedService, StateService],
                resolveFn: (defineFeedService:DefineFeedService, state:StateService) => {
                    let feedId = state.transition.params().feedId;

                   return defineFeedService.loadFeed(feedId).toPromise().then((feed:Feed) => {
                       console.log("Loaded it ... lets go ",feed)
                       if(true) {//(feed.hasBeenDeployed()){
                           return FEED_OVERVIEW_STATE_NAME;
                       }
                       else {
                           return FEED_DEFINITION_SECTION_STATE_NAME+".setup-guide";
                       }
                    });
                }
            }
        ]
    },
    */
    {
        name: FEED_OVERVIEW_STATE_NAME,
        url: "/:feedId/overview",
        component: OverviewComponent
    },
    {
        name: FEED_DEFINITION_SUMMARY_STATE_NAME+".access-denied",
        url: "/access-denied",
        params:{attemptedState:null},
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            }],
        component: AccessDeniedComponent
    },
    {
        name: FEED_DEFINITION_SUMMARY_STATE_NAME+".setup-guide",
        url: "/:feedId/summary-setup-guide",
        component: SetupGuideSummaryComponent,
        params:{feedId:{type:"string"},
            loadMode:LoadMode.LATEST,
            refresh:false},
        resolve: [
            {
                token: 'showHeader',
                resolveFn: resolveTrue
            },
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            },
            {
                token: 'loadMode',
                resolveFn: resolveLoadModeLatest
            },
            {
                token: 'refresh',
                resolveFn: resolveFalse
            }
            ]
    },
    {
        name: FEED_DEFINITION_SUMMARY_STATE_NAME+".feed-activity",
        url: "/:feedId/feed-activity",
        component: FeedActivitySummaryComponent,
        params:{feedId:{type:"string"},
            loadMode:LoadMode.DEPLOYED,
            refresh:false},
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            },
            {
                token: 'loadMode',
                resolveFn: resolveLoadModeDeployed
            },
            {
                token: 'refresh',
                resolveFn: resolveFalse
            }
        ]
    },

    /**
    {
        name: FEED_DEFINITION_SECTION_STATE_NAME+".datasources",
        url: "/:feedId/source-sample",
        component: DefineFeedStepSourceSampleComponent,
        resolve: [
            {
                token: "datasources",
                deps: [CatalogService,  TdLoadingService],
                resolveFn: (catalog: CatalogService,loading: TdLoadingService) => {
                    loading.register(DefineFeedStepSourceSampleComponent.LOADER);
                    return catalog.getDataSources()
                        .pipe(finalize(() => loading.resolve(DefineFeedStepSourceSampleComponent.LOADER)))
                        .pipe(catchError((err) => {
                            console.error('Failed to load catalog', err);
                            return [];
                        }))
                        .toPromise();
                }
           },
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: (state: StateService) => state.transition.params()
            }

        ]
    },
    {
        name:FEED_DEFINITION_SECTION_STATE_NAME+".datasource",
        url:"/:feedId/source-sample/:datasourceId/:path",
        component: DefineFeedStepSourceSampleDatasourceComponent,
        params: {
            path:{value:''}
        },
        resolve: [
            {
                token: "datasource",
                deps: [CatalogService, StateService, TdLoadingService, DefineFeedService],
                resolveFn: (catalog: CatalogService, state: StateService, loading: TdLoadingService, defineFeedService:DefineFeedService) => {
                    loading.register(DefineFeedStepSourceSampleDatasourceComponent.LOADER);
                    let datasourceId = state.transition.params().datasourceId;
                    let feed = defineFeedService.getFeed();
                    if(feed && feed.sourceDataSets && feed.sourceDataSets.length >0 && feed.sourceDataSets[0].dataSource.id == datasourceId){
                        return feed.sourceDataSets[0].dataSource;
                    }
                    else {
                        return catalog.getDataSource(datasourceId)
                            .pipe(finalize(() => loading.resolve(DefineFeedStepSourceSampleDatasourceComponent.LOADER)))
                            .pipe(catchError(() => {
                                return state.go(".datasources")
                            }))
                            .toPromise();
                    }
                }
            },
            {
                token: "connectorPlugin",
                deps: [CatalogService, StateService, TdLoadingService],
                resolveFn: (catalog: CatalogService, state: StateService, loading: TdLoadingService) => {
                    let datasourceId = state.transition.params().datasourceId;
                    return catalog.getDataSourceConnectorPlugin(datasourceId)
                        .pipe(catchError(() => {
                            return state.go("catalog")
                        }))
                        .toPromise();
                }
            },
            {
                token:"params",
                deps:[StateService],
                resolveFn: (state: StateService) => {
                    let params = state.transition.params();
                    if(params && params.path) {
                        return {"path":params.path}
                    }
                    else {
                        return {};
                    }
                }

            }
        ]
    },*/
    {
        name: FEED_DEFINITION_SUMMARY_STATE_NAME+".profile",
        url: "/:feedId/profile",
        redirectTo: FEED_DEFINITION_SUMMARY_STATE_NAME+".profile.history",
        component: ProfileComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            }
        ]
    },
    {
        name: FEED_DEFINITION_SUMMARY_STATE_NAME+".profile.history",
        url: "/history",
        component: ProfileHistoryComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            },
        ]
    },
    {
        name: FEED_DEFINITION_SUMMARY_STATE_NAME+".profile.results",
        url: "/:processingdttm?t=:type",
        component: ProfileContainerComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            },
        ]
    },
    {
        name: FEED_DEFINITION_SUMMARY_STATE_NAME+".feed-lineage",
        url: "/:feedId/feed-lineage",
        component: FeedLineageComponment,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            }
        ]
    },
    {
        name: FEED_DEFINITION_SUMMARY_STATE_NAME+".sla",
        url: "/:feedId/sla",
        redirectTo: FEED_DEFINITION_SUMMARY_STATE_NAME+".sla.list",
        component: FeedSlaComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            },
            resolveFeed
        ],
        data:{
            permissionsKey:"SERVICE_LEVEL_AGREEMENTS",
            accessRedirect:"feed-definition.summary.access-denied",
        }
    },
    {
        name: FEED_DEFINITION_SUMMARY_STATE_NAME+".sla.list",
        url: "/list",
        component: SlaListComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            },
            resolveFeed
        ]
    },
    {
        name: FEED_DEFINITION_SUMMARY_STATE_NAME+".sla.new",
        url: "/new",
        component: SlaDetailsComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            },
            resolveFeed
        ]
    },
    {
        name: FEED_DEFINITION_SUMMARY_STATE_NAME+".sla.edit",
        url: "/:slaId",
        component: SlaDetailsComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            },
            resolveFeed
        ]
    },
    {
        name: FEED_DEFINITION_SUMMARY_STATE_NAME+".version-history",
        url: "/:feedId/versions",
        component: FeedVersionsComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            }
        ]
    }



];

export function loadFeed(state: StateService, feedService:DefineFeedService) {
    let feedId = state.transition.params().feedId;
    return feedService.loadFeed(feedId)
        .pipe(catchError((err: any, o: Observable<any>) => {
            console.error('Failed to load feed', err);
            return Observable.of({});
        }))
        .pipe(first()).toPromise();
}

export function resolveParams(state: StateService) {
    return state.transition.params();
}

export function resolveFalse() {
    return false;
}

export function resolveTrue() {
    return true;
}

export function resolveLoadModeDeployed() {
    return LoadMode.DEPLOYED;
}

export function resolveLoadModeLatest() {
    return LoadMode.LATEST;
}
