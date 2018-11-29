import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatDialogModule} from "@angular/material/dialog";
import {CovalentChipsModule} from "@covalent/core/chips";
import {CovalentDataTableModule} from "@covalent/core/data-table";

import {KyloCommonModule} from "../common/common.module";
import {moduleName} from "./module-name";
import {
    accessControlServiceProvider,
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
    userGroupServiceProvider,
    visualQueryServiceProvider
} from "./services/angular2";
import {NiFiService} from "./services/NiFiService";
import {ApplyDomainTypeDialogComponent} from "./shared/domain-type/apply-domain-type/apply-domain-type-dialog.component";
import {ApplyDomainTypesDialogComponent} from "./shared/domain-type/apply-domain-types/apply-domain-types-dialog.component";
import {DomainTypeConflictDialogComponent} from "./shared/domain-type/domain-type-conflict/domain-type-conflict-dialog.component";
import {DynamicFormModule} from "../../lib/dynamic-form/dynamic-form.module";
import {EntityAccessControlComponent} from "./shared/entity-access-control/entity-access-control.component";
import {PropertyListModule} from "./shared/property-list/property-list.module";
import {SqlEditorModule} from "./shared/sql-editor/sql-editor.module";
import {TranslateModule} from "@ngx-translate/core";
import {SlaService} from "./services/sla.service";
import {FeedLoadingService} from './feeds/define-feed-ng2/services/feed-loading-service';
import {DefineFeedService} from './feeds/define-feed-ng2/services/define-feed.service';
import {FeedAccessControlService} from './feeds/define-feed-ng2/services/feed-access-control.service';


@NgModule({
    imports: [
        CommonModule,
        CovalentDataTableModule,
        CovalentChipsModule,
        DynamicFormModule,
        FlexLayoutModule,
        FormsModule,
        KyloCommonModule,
        MatButtonModule,
        MatDialogModule,
        PropertyListModule,
        ReactiveFormsModule,
        SqlEditorModule,
        TranslateModule
    ],
    declarations: [
        DomainTypeConflictDialogComponent,
        ApplyDomainTypeDialogComponent,
        ApplyDomainTypesDialogComponent,
        EntityAccessControlComponent
    ],
    entryComponents: [
        DomainTypeConflictDialogComponent,
        ApplyDomainTypeDialogComponent,
        ApplyDomainTypesDialogComponent
    ],
    exports: [
        EntityAccessControlComponent
    ],
    providers: [
        FeedLoadingService,
        DefineFeedService,
        FeedAccessControlService,
        categoriesServiceProvider,
        entityAccessControlServiceProvider,
        userGroupServiceProvider,
        accessControlServiceProvider,
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
        SlaService
    ]
})
export class KyloFeedManagerModule {
    constructor(injector: Injector) {

        require("./module");
        injector.get("$ocLazyLoad").inject(moduleName);
        require("./module-require");

    }
}
