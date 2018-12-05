import {CommonModule} from "@angular/common";
import {HttpClientModule} from '@angular/common/http';
import {NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from '@angular/material/card';
import {MatDialogModule} from '@angular/material/dialog';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatSelectModule} from '@angular/material/select';
import {MatToolbarModule} from '@angular/material/toolbar';
import {CovalentDataTableModule} from '@covalent/core/data-table';
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentMenuModule} from "@covalent/core/menu";
import {CovalentNotificationsModule} from "@covalent/core/notifications";
import {CovalentPagingModule} from '@covalent/core/paging';
import {CovalentSearchModule} from '@covalent/core/search';
import {TranslateModule} from "@ngx-translate/core";

import {RestUrlService} from "../feed-mgr/services/RestUrlService";
import {KyloServicesModule} from "../services/services.module";
import AboutKyloDialogController, {AboutKyloService} from "./about-kylo/AboutKyloService";
import {AddButtonComponent} from "./add-button/add-button.component";
import {CardFilterHeaderComponent} from "./card-filter-header/card-filter-header.component";
import {CardLayoutComponent} from "./card-layout/card-layout.component";
import {UploadFileComponent} from "./file-upload/file-upload.component";
import {FilteredPaginatedTableViewComponent} from "./filtered-paginated-table-view/filteredPaginatedTableView.component";
import {IconPickerDialog} from "./icon-picker-dialog/icon-picker-dialog.component";
import {KyloIconComponent} from "./kylo-icon/kylo-icon.component";
import {KyloOptionsComponent} from "./kylo-options/kylo-options.component";
import {NotificationMenuComponent} from "./notifications/notification-menu.component";
import {OptionsMenuComponent} from "./options-menu/OptionsMenu.component"
import {KyloTimerDirective} from "./timer/kylo-timer.component";
import {RouterBreadcrumbsComponent} from "./ui-router-breadcrumbs/ui-router-breadcrumbs.component";
import {VerticalSectionLayoutComponent} from "./vertical-section-layout/vertical-section-layout.component";
import {ViewTypeSelectionComponent} from "./view-type-selection/view-type-selection.component";
import { AccordianMenuComponent } from "./accordion-menu/accordionMenuComponent";
import { MenuToggleComponent } from "./accordion-menu/menuToggleComponent";
import { menuLinkComponent } from "./accordion-menu/menuLinkComponent";
// import { IfPermissionDirective } from "./ng-if-permission/ng-if-permission.directive";
import { AccordionMenuService } from "./accordion-menu/AccordionMenuService";
import { MatTooltipModule } from "@angular/material/tooltip";
import { UIRouterModule } from "@uirouter/angular";
import { VisualQueryTableHeader } from "../feed-mgr/visual-query/transform-data/visual-query-table/visual-query-table-header.component";
import { CellMenuComponent } from "../feed-mgr/visual-query/transform-data/visual-query-table/call-menu.component";

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
        FilteredPaginatedTableViewComponent,
        KyloTimerDirective,
        // IfPermissionDirective,
        AccordianMenuComponent,
        MenuToggleComponent,
        menuLinkComponent,
        VisualQueryTableHeader,
        CellMenuComponent
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
        IconPickerDialog,
        FilteredPaginatedTableViewComponent,
        AccordianMenuComponent,
        MenuToggleComponent,
        menuLinkComponent,
        menuLinkComponent,
        VisualQueryTableHeader,
        CellMenuComponent
    ],
    imports: [
        CommonModule,
        CovalentLoadingModule,
        CovalentMenuModule,
        CovalentNotificationsModule,
        CovalentDataTableModule,
        CovalentPagingModule,
        CovalentSearchModule,
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
        MatToolbarModule,
        MatCardModule,
        FormsModule,
        FlexLayoutModule,
        HttpClientModule,
        TranslateModule,
        MatProgressBarModule,
        MatTooltipModule,
        UIRouterModule.forChild()
    ],
    exports: [
        KyloIconComponent,
        VerticalSectionLayoutComponent,
        CardFilterHeaderComponent,
        IconPickerDialog,
        FilteredPaginatedTableViewComponent,
        UploadFileComponent,
        CovalentDataTableModule,
        KyloTimerDirective,
        // IfPermissionDirective,
        AccordianMenuComponent,
        MenuToggleComponent,
        AddButtonComponent,
        RouterBreadcrumbsComponent,
        NotificationMenuComponent,
        KyloOptionsComponent
    ],
    providers: [
        AboutKyloService,
        RestUrlService,
        AccordionMenuService
    ]
})
export class KyloCommonModule {
}
