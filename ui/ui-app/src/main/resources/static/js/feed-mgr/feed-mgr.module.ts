import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";

import {
    domainTypesServiceProvider,
    entityAccessControlServiceProvider, feedDetailsProcessorRenderingHelperProvider, feedInputProcessorPropertiesTemplateServiceProvider,
    feedPropertyServiceProvider,
    feedServiceProvider,
    uiComponentsServiceProvider
} from "./services/angular2";
import {PolicyInputFormService} from "./shared/policy-input-form/PolicyInputFormService";
import {RestUrlService} from "./services/RestUrlService";
import CategoriesService from "./services/CategoriesService";
import {KyloCommonModule} from "../common/common.module";
import {UpgradeModule} from "@angular/upgrade/static";
import {FeedSecurityGroups} from "./services/FeedSecurityGroups";

@NgModule({
    imports: [
        CommonModule,
        UpgradeModule,
        KyloCommonModule
    ],
    providers: [
        CategoriesService,
        entityAccessControlServiceProvider,
        feedServiceProvider,
        domainTypesServiceProvider,
        feedPropertyServiceProvider,
        uiComponentsServiceProvider,
        feedInputProcessorPropertiesTemplateServiceProvider,
        feedDetailsProcessorRenderingHelperProvider,
        PolicyInputFormService,
        RestUrlService,
        FeedSecurityGroups
    ],
    exports:[
    ]
})
export class KyloFeedManagerModule {
    constructor() {
        console.log("Loading KyloFeedManagerModule")
    }

}
