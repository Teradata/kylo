import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
import {MatDialogModule} from "@angular/material/dialog";
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
import {PropertyListComponent} from "./shared/property-list/property-list.component";
import {PropertyListModule} from "./shared/property-list/property-list.module";
import {DomainTypeConflictDialogComponent} from "./shared/domain-type/domain-type-conflict/domain-type-conflict-dialog.component";
import {ApplyDomainTypeDialogComponent} from "./shared/domain-type/apply-domain-type/apply-domain-type-dialog.component";


@NgModule({
    imports: [
        CommonModule,
        DynamicFormModule,
        PropertyListModule,
        MatDialogModule
    ],
    declarations:[
        DomainTypeConflictDialogComponent,
        ApplyDomainTypeDialogComponent
    ],
    entryComponents:[
        DomainTypeConflictDialogComponent,
        ApplyDomainTypeDialogComponent
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
