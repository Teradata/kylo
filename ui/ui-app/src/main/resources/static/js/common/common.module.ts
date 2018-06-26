import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";

import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatGridListModule} from '@angular/material/grid-list';
import {MatDialogModule} from '@angular/material/dialog';

import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentMenuModule} from "@covalent/core/menu";
import {CovalentNotificationsModule} from "@covalent/core/notifications";
import {TranslateModule} from "@ngx-translate/core";

import {KyloServicesModule} from "../services/services.module";
import {AddButtonComponent} from "./add-button/add-button.component";
import {KyloIconComponent} from "./kylo-icon/kylo-icon.component";
import {NotificationMenuComponent} from "./notifications/notification-menu.component";
import {ViewTypeSelectionComponent} from "./view-type-selection/view-type-selection.component";
import {VerticalSectionLayoutComponent} from "./vertical-section-layout/vertical-section-layout-directive.component";
import {OptionsMenuComponent} from "./options-menu/OptionsMenu.component"
import {RouterBreadcrumbsComponent} from "./ui-router-breadcrumbs/ui-router-breadcrumbs.component";
import {KyloOptionsComponent} from "./kylo-options/kylo-options.component";
import {UploadFileComponent} from "./file-upload/file-upload.component";
import {CardFilterHeaderComponent} from "./card-filter-header/card-filter-header.component";
import {CardLayoutComponent} from "./card-layout/card-layout.component";

import AboutKyloDialogController from "./about-kylo/AboutKyloService";
import {IconPickerDialog} from "./icon-picker-dialog/icon-picker-dialog.component";

import {AboutKyloService} from "./about-kylo/AboutKyloService";
import { RestUrlService } from "../feed-mgr/services/RestUrlService";

@NgModule({
    declarations: [
        AddButtonComponent,
        NotificationMenuComponent,
        KyloIconComponent,
        ViewTypeSelectionComponent,
        VerticalSectionLayoutComponent,
        OptionsMenuComponent,
        RouterBreadcrumbsComponent,
        KyloOptionsComponent,
        CardFilterHeaderComponent,
        UploadFileComponent,
        CardLayoutComponent,
        IconPickerDialog,
        AboutKyloDialogController,
    ],
    entryComponents: [
        AddButtonComponent,
        NotificationMenuComponent,
        ViewTypeSelectionComponent,
        VerticalSectionLayoutComponent,
        OptionsMenuComponent,
        RouterBreadcrumbsComponent,
        KyloOptionsComponent,
        CardFilterHeaderComponent,
        UploadFileComponent,
        CardLayoutComponent,
        AboutKyloDialogController,
        IconPickerDialog
    ],
    imports: [
        CommonModule,
        CovalentLoadingModule,
        CovalentMenuModule,
        CovalentNotificationsModule,
        KyloServicesModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        MatMenuModule,
        MatInputModule,
        MatSelectModule,
        MatProgressSpinnerModule,
        MatGridListModule,
        MatDialogModule,
        FormsModule,
        HttpClientModule,
        TranslateModule
    ],
     exports: [
        KyloIconComponent,
        VerticalSectionLayoutComponent,
        CardFilterHeaderComponent
    ],
    providers: [AboutKyloService, RestUrlService]
})
export class KyloCommonModule {}
