import {Ng2StateDeclaration, StateService} from "@uirouter/angular";
import {catchError} from "rxjs/operators/catchError";
import {finalize} from "rxjs/operators/finalize";

import {DefineFeedStepGeneralInfoComponent} from "./steps/general-info/define-feed-step-general-info.component";
import {FormBuilder} from "@angular/forms";
import {DefineFeedSummaryComponent} from "./summary/define-feed-summary.component";
import {DefineFeedStepSourceSampleComponent} from "./steps/source-sample/define-feed-step-source-sample.component";
import {DefineFeedStepSourceSampleDatasourceComponent} from "./steps/source-sample/define-feed-step-source-sample-datasource.component";
import {DefineFeedComponent} from "./define-feed.component";
import {DefineFeedSelectTemplateComponent} from "./select-template/define-feed-select-template.component";
import {TdLoadingService} from "@covalent/core/loading";
import {CatalogService} from "../../catalog/api/services/catalog.service";
import {DefineFeedService} from "./services/define-feed.service";
import {DefineFeedContainerComponent} from "./steps/define-feed-container/define-feed-container.component";
import {DefineFeedStepFeedDetailsComponent} from "./steps/feed-details/define-feed-step-feed-details.component";
import {DefineFeedStepFeedTargetComponent} from "./steps/feed-target/define-feed-step-feed-target.component";
import {ConnectorsComponent} from "../../catalog/connectors/connectors.component";
import {DefineFeedTableComponent} from "./steps/define-table/define-feed-table.component";
import {Observable} from "rxjs/Observable";

export const FEED_DEFINITION_STATE_NAME :string = "feed-definition";

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
        name: FEED_DEFINITION_STATE_NAME+".summary",
        url: "/:feedId/summary",
        component: DefineFeedSummaryComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: (state: StateService) => state.transition.params()
            }
        ]
    },
    {
        name: FEED_DEFINITION_STATE_NAME+".feed-step",
        url: "/feed-step",
        redirectTo: FEED_DEFINITION_STATE_NAME+".feed-step.new-feed",
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
        name: FEED_DEFINITION_STATE_NAME+".feed-step.new-feed",
        url: "/new-feed",
        component: DefineFeedStepGeneralInfoComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: (state: StateService) => state.transition.params()
            }
        ]
    },
    {
        name: FEED_DEFINITION_STATE_NAME+".feed-step.general-info",
        url: "/:feedId/general-info",
        component: DefineFeedStepGeneralInfoComponent

    },
    {
        name: FEED_DEFINITION_STATE_NAME+".feed-step.feed-details",
        url: "/:feedId/feed-details",
        component: DefineFeedStepFeedDetailsComponent
    },
    {
        name: FEED_DEFINITION_STATE_NAME+".feed-step.feed-target",
        url: "/:feedId/feed-target",
        component: DefineFeedStepFeedTargetComponent
    },
    {
        name: FEED_DEFINITION_STATE_NAME+".feed-step.feed-table",
        url: "/:feedId/feed-table",
        component: DefineFeedTableComponent
    },
    {
        name: FEED_DEFINITION_STATE_NAME+".feed-step.datasources",
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
            }
        ]
    },
    {
        name:FEED_DEFINITION_STATE_NAME+".feed-step.datasource",
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
    }



];


