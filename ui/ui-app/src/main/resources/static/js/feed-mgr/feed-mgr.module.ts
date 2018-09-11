import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatButtonModule} from "@angular/material/button";
import {MatDialogModule} from "@angular/material/dialog";
import {CovalentDataTableModule} from "@covalent/core/data-table";

import {KyloCommonModule} from "../common/common.module";
import {moduleName} from "./module-name";
import {
    categoriesServiceProvider,
    datasourcesServiceProvider,
    domainTypesServiceProvider,
    entityAccessControlServiceProvider,
    feedDetailsProcessorRenderingHelperProvider,
    feedInputProcessorPropertiesTemplateServiceProvider,
    feedPropertyServiceProvider,
    feedServiceProvider,
    hiveServiceProvider,
    uiComponentsServiceProvider,
    visualQueryServiceProvider
} from "./services/angular2";
import {NiFiService} from "./services/NiFiService";
import {ApplyDomainTypeDialogComponent} from "./shared/domain-type/apply-domain-type/apply-domain-type-dialog.component";
import {ApplyDomainTypesDialogComponent} from "./shared/domain-type/apply-domain-types/apply-domain-types-dialog.component";
import {DomainTypeConflictDialogComponent} from "./shared/domain-type/domain-type-conflict/domain-type-conflict-dialog.component";
import {DynamicFormModule} from "./shared/dynamic-form/dynamic-form.module";
import {PropertyListModule} from "./shared/property-list/property-list.module";
import {SqlEditorModule} from "./shared/sql-editor/sql-editor.module";
import {CatalogDatasetPreviewModule} from "./catalog-dataset-preview/catalog-dataset-preview.module";


@NgModule({
    imports: [
        CommonModule,
        DynamicFormModule,
        PropertyListModule,
        MatDialogModule,
        FlexLayoutModule,
        KyloCommonModule,
        MatButtonModule,
        CovalentDataTableModule,
        SqlEditorModule
    ],
    declarations: [
        DomainTypeConflictDialogComponent,
        ApplyDomainTypeDialogComponent,
        ApplyDomainTypesDialogComponent
    ],
    entryComponents: [
        DomainTypeConflictDialogComponent,
        ApplyDomainTypeDialogComponent,
        ApplyDomainTypesDialogComponent
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
        hiveServiceProvider,
        visualQueryServiceProvider,
        datasourcesServiceProvider,
        NiFiService,

    ]
})
export class KyloFeedManagerModule {
    constructor(injector: Injector) {
        console.log("Loading KyloFeedManagerModule");
        require("./module");
        injector.get("$ocLazyLoad").inject(moduleName);
        require("./module-require");

    }
}
