import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatCardModule} from "@angular/material/card";
import {MatDividerModule} from "@angular/material/divider";
import {MatListModule} from "@angular/material/list";
import {MatTabsModule} from "@angular/material/tabs";
import {MatToolbarModule} from "@angular/material/toolbar";
import {CovalentDataTableModule} from "@covalent/core/data-table";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {CovalentLayoutModule} from "@covalent/core/layout";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentSearchModule} from "@covalent/core/search";
import {TranslateModule} from "@ngx-translate/core";

import {KyloCommonModule} from "../../common/common.module";
import {CatalogApiModule} from "./api/catalog-api.module";
import {DataSourcesComponent} from "./datasources/datasources.component";
import {DatasourceComponent} from "./datasource/datasource.component";
import {CatalogComponent} from "./catalog.component";
import {ConnectorsComponent} from './connectors/connectors.component';
import {ConnectorComponent} from './connector/connector.component';
import {CovalentDynamicFormsModule} from '@covalent/dynamic-forms';
import {MatNativeDateModule, MatOptionModule} from '@angular/material/core';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatIconModule} from '@angular/material/icon';
import {VisibleOnMouseOverDirective} from './datasources/visible-on-mouse-over.directive';
import {UIRouterModule} from "@uirouter/angular";
import {catalogStates} from "./catalog.states";
import {RemoteFilesRouterModule} from "./datasource/files/remote-files.module";
import {TablesRouterModule} from "./datasource/tables/tables.module";
import {PreviewSchemaRouterModule} from "./datasource/preview-schema/preview-schema.module";
import { CovalentVirtualScrollModule } from '@covalent/core/virtual-scroll';
@NgModule({
    declarations: [
        DataSourcesComponent,
        DatasourceComponent,
        ConnectorsComponent,
        ConnectorComponent,
        CatalogComponent,
        VisibleOnMouseOverDirective
    ],
    exports:[
        DataSourcesComponent,
        DatasourceComponent,
        ConnectorsComponent,
        ConnectorComponent,
        CatalogComponent
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
        MatCardModule,
        FormsModule,
        MatInputModule,
        MatIconModule,
        MatOptionModule,
        MatSelectModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatDividerModule,
        MatListModule,
        MatTabsModule,
        MatToolbarModule,
        MatNativeDateModule,
        MatButtonModule,
        MatSnackBarModule,
        UIRouterModule.forChild(),
        TranslateModule
    ]
})
export class CatalogModule {
}

@NgModule({
    imports: [
        CatalogModule,
        RemoteFilesRouterModule,
        TablesRouterModule,
        PreviewSchemaRouterModule,
        UIRouterModule.forChild({states: catalogStates})
    ]
})
export class CatalogRouterModule {
}
