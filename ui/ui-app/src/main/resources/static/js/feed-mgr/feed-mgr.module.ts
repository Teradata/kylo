import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
import {moduleName} from "./module-name"

import {
    categoriesServiceProvider,
    domainTypesServiceProvider,
    entityAccessControlServiceProvider, feedDetailsProcessorRenderingHelperProvider, feedInputProcessorPropertiesTemplateServiceProvider,
    feedPropertyServiceProvider,
    feedServiceProvider,
    uiComponentsServiceProvider
} from "./services/angular2";
import {PolicyInputFormService} from "./shared/policy-input-form/PolicyInputFormService";
import {RestUrlService} from "./services/RestUrlService";
import {CategoriesService} from "./services/CategoriesService";
import {FeedSecurityGroups} from "./services/FeedSecurityGroups";
import {KyloCommonModule} from "../common/common.module";
import {UpgradeModule} from "@angular/upgrade/static";


@NgModule({
    imports: [
        CommonModule,
        UpgradeModule,
        KyloCommonModule
    ],
    providers: [
        categoriesServiceProvider,
        entityAccessControlServiceProvider,
        feedServiceProvider,
        domainTypesServiceProvider,
        feedPropertyServiceProvider,
        uiComponentsServiceProvider,
        feedInputProcessorPropertiesTemplateServiceProvider,
        feedDetailsProcessorRenderingHelperProvider,
        PolicyInputFormService,
        RestUrlService
    ],
    exports:[
    ]
})
export class KyloFeedManagerModule {
    constructor(injector: Injector) {
        console.log("Loading KyloFeedManagerModule")
        require("./module");
     //   injector.get("$ocLazyLoad").inject(moduleName);
        require("./module-require");

    }

}
