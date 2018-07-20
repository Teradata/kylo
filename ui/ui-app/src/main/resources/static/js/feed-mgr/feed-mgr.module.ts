import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
import {moduleName} from "./module-name";

import {
    categoriesServiceProvider,
    domainTypesServiceProvider,
    entityAccessControlServiceProvider, feedDetailsProcessorRenderingHelperProvider, feedInputProcessorPropertiesTemplateServiceProvider,
    feedPropertyServiceProvider,
    feedServiceProvider,
    uiComponentsServiceProvider
} from "./services/angular2";
import {DynamicFormModule} from "./shared/dynamic-form/dynamic-form.module";
import {NiFiService} from "./services/NiFiService";


@NgModule({
    imports: [
        CommonModule,
        DynamicFormModule
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
        NiFiService
    ]
})
export class KyloFeedManagerModule {
    constructor(injector: Injector) {
        console.log("Loading KyloFeedManagerModule")
       require("./module");
        injector.get("$ocLazyLoad").inject(moduleName);
        require("./module-require");

    }

}
