import {Ng2StateDeclaration, StateService} from "@uirouter/angular";
import {catchError} from "rxjs/operators/catchError";
import {finalize} from "rxjs/operators/finalize";

import {DefineFeedStepGeneralInfoComponent} from "./steps/general-info/define-feed-step-general-info.component";
import {DefineFeedStepSourceSampleComponent} from "./steps/source-sample/define-feed-step-source-sample.component";
import {DefineFeedComponent} from "./define-feed.component";
import {DefineFeedSelectTemplateComponent} from "./select-template/define-feed-select-template.component";
import {TdLoadingService} from "@covalent/core/loading";
import {CatalogService} from "../../catalog/api/services/catalog.service";
import {DefineFeedService} from "./services/define-feed.service";
import {DefineFeedContainerComponent} from "./steps/define-feed-container/define-feed-container.component";
import {DefineFeedStepFeedDetailsComponent} from "./steps/feed-details/define-feed-step-feed-details.component";
import {DefineFeedTableComponent} from "./steps/define-table/define-feed-table.component";
import {FEED_DEFINITION_SECTION_STATE_NAME, FEED_DEFINITION_STATE_NAME} from "../../model/feed/feed-constants";
import {DefineFeedStepWranglerComponent} from "./steps/wrangler/define-feed-step-wrangler.component";
import {ProfileComponent} from './summary/profile/profile.component';
import {OverviewComponent} from './summary/overview/overview.component';
import {FeedLineageComponment} from "./summary/feed-lineage/feed-lineage.componment";
import {ProfileContainerComponent} from './summary/profile/container/profile-container.component';
import {ProfileHistoryComponent} from './summary/profile/history/profile-history.component';
import {DefineFeedPermissionsComponent} from "./steps/permissions/define-feed-permissions.component";
import {DefineFeedPropertiesComponent} from "./steps/properties/define-feed-properties.component";
import {DefineFeedStepSourceComponent} from "./steps/source-sample/define-feed-step-source.component";


const resolveFeed :any =
    {
        token: 'feed',
            deps: [StateService, DefineFeedService],
        resolveFn: (state: StateService, feedService:DefineFeedService) => {
        let feedId = state.transition.params().feedId;
        feedService.loadFeed(feedId)
                .toPromise();
    }
    }



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
            displayName: "Define Feed"
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
        redirectTo: FEED_DEFINITION_SECTION_STATE_NAME+".overview",
        component: DefineFeedContainerComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: (state: StateService) => state.transition.params()
            }
        ]
    },
    {
        name: FEED_DEFINITION_SECTION_STATE_NAME+".overview",
        url: "/:feedId/overview",
        component: OverviewComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: (state: StateService) => state.transition.params()
            }
        ]
    },   
    {
        name: FEED_DEFINITION_SECTION_STATE_NAME+".general-info",
        url: "/:feedId/general-info",
        component: DefineFeedStepGeneralInfoComponent,


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
        component: DefineFeedStepWranglerComponent
    },
    {
        name: FEED_DEFINITION_SECTION_STATE_NAME + ".datasources",
        url: "/:feedId/source-sample",
        component: DefineFeedStepSourceComponent,
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
        name: FEED_DEFINITION_SECTION_STATE_NAME+".profile",
        url: "/:feedId/profile",
        redirectTo: FEED_DEFINITION_SECTION_STATE_NAME+".profile.history",
        component: ProfileComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: (state: StateService) => state.transition.params()
            }
        ]
    },
    {
        name: FEED_DEFINITION_SECTION_STATE_NAME+".profile.history",
        url: "/history",
        component: ProfileHistoryComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: (state: StateService) => state.transition.params()
            },
        ]
    },
    {
        name: FEED_DEFINITION_SECTION_STATE_NAME+".profile.results",
        url: "/:processingdttm?t=:type",
        component: ProfileContainerComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: (state: StateService) => state.transition.params()
            },
        ]
    },
    {
        name: FEED_DEFINITION_SECTION_STATE_NAME+".feed-lineage",
        url: "/:feedId/feed-lineage",
        component: FeedLineageComponment,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: (state: StateService) => state.transition.params()
            }
        ]
    }



];


