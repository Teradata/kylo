import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from "@angular/material/card";
import {MatNativeDateModule, MatOptionModule} from '@angular/material/core';
import {MatDividerModule} from "@angular/material/divider";
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from "@angular/material/list";
import {MatSelectModule} from '@angular/material/select';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatTabsModule} from "@angular/material/tabs";
import {MatToolbarModule} from "@angular/material/toolbar";
import {CovalentDataTableModule} from "@covalent/core/data-table";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {CovalentLayoutModule} from "@covalent/core/layout";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentSearchModule} from "@covalent/core/search";
import {CovalentVirtualScrollModule} from '@covalent/core/virtual-scroll';
import {CovalentDynamicFormsModule} from '@covalent/dynamic-forms';
import {TranslateModule} from "@ngx-translate/core";
import {UIRouterModule} from "@uirouter/angular";

import {KyloCommonModule} from "../../common/common.module";
import {KyloFeedManagerModule} from "../feed-mgr.module";
import {CatalogApiModule} from "./api/catalog-api.module";
import {CatalogComponent} from "./catalog.component";
import {catalogStates} from "./catalog.states";
import {ConnectorComponent} from './connector/connector.component';
import {ConnectorsComponent} from './connectors/connectors.component';
import {DatasourceComponent} from "./datasource/datasource.component";
import {RemoteFilesRouterModule} from "./datasource/files/remote-files.module";
import {PreviewSchemaRouterModule} from "./datasource/preview-schema/preview-schema.module";
import {TablesRouterModule} from "./datasource/tables/tables.module";
import {DataSourcesComponent} from "./datasources/datasources.component";
import {VisibleOnMouseOverDirective} from './datasources/visible-on-mouse-over.directive';
import {CreateFeedComponent} from './datasource/create-feed/create-feed.component';
import {MatExpansionModule} from '@angular/material/expansion';
import {AdminConnectorsComponent} from "./connectors/admin-connectors.component";
import {AdminConnectorComponent} from "./connector/admin-connector.component";

@NgModule({
    declarations: [
        DataSourcesComponent,
        DatasourceComponent,
        ConnectorsComponent,
        ConnectorComponent,
        CatalogComponent,
        VisibleOnMouseOverDirective,
        CreateFeedComponent,
        AdminConnectorsComponent,
        AdminConnectorComponent
    ],
    exports: [
        DataSourcesComponent,
        DatasourceComponent,
        ConnectorsComponent,
        ConnectorComponent,
        CatalogComponent,
        AdminConnectorsComponent,
        AdminConnectorComponent
    ],
    imports: [
        CatalogApiModule,
        CommonModule,
        CovalentDynamicFormsModule,
        CovalentDataTableModule,
        CovalentDialogsModule,
        CovalentLayoutModule,
        CovalentLoadingModule,
        CovalentSearchModule,
        CovalentVirtualScrollModule,
        FlexLayoutModule,
        KyloCommonModule,
        KyloFeedManagerModule,
        MatCardModule,
        FormsModule,
        MatInputModule,
        MatIconModule,
        MatOptionModule,
        MatSelectModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatDividerModule,
        MatExpansionModule,
        MatListModule,
        MatTabsModule,
        MatToolbarModule,
        MatNativeDateModule,
        MatButtonModule,
        MatSnackBarModule,
        UIRouterModule,
        TranslateModule
    ]
})
export class CatalogModule {
}

@NgModule({
    imports: [
        CatalogModule,
        TablesRouterModule,
        PreviewSchemaRouterModule,
        UIRouterModule.forChild({states: catalogStates})
    ]
})
export class CatalogRouterModule {
}
