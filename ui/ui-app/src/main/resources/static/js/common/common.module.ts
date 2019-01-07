import {CommonModule} from "@angular/common";
import {HttpClientModule} from '@angular/common/http';
import {NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from '@angular/material/card';
import {MatDialogModule} from '@angular/material/dialog';
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatGridListModule} from '@angular/material/grid-list';
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatSelectModule} from '@angular/material/select';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatTooltipModule} from "@angular/material/tooltip";
import {CovalentDataTableModule} from '@covalent/core/data-table';
import {CovalentFileModule} from "@covalent/core/file";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentMenuModule} from "@covalent/core/menu";
import {CovalentNotificationsModule} from "@covalent/core/notifications";
import {CovalentPagingModule} from '@covalent/core/paging';
import {CovalentSearchModule} from '@covalent/core/search';
import {TranslateModule} from "@ngx-translate/core";
import {UIRouterModule} from "@uirouter/angular";

import {RestUrlService} from "../feed-mgr/services/RestUrlService";
import {AboutKyloDialogController, AboutKyloService} from "./about-kylo/AboutKyloService";
import {AccessDeniedComponent} from "./access-denied/access-denied.component";
import {AccordianMenuComponent} from "./accordion-menu/accordionMenuComponent";
import {AccordionMenuService} from "./accordion-menu/AccordionMenuService";
import {MenuToggleComponent} from "./accordion-menu/menuToggleComponent";
import {AddButtonComponent} from "./add-button/add-button.component";
import {BrowserHeight} from "./browser-height/browser-height";
import {CardFilterHeaderComponent} from "./card-filter-header/card-filter-header.component";
import {CardLayoutComponent} from "./card-layout/card-layout.component";
import {BaseDraggableDirective} from "./draggable-ng2/base-draggable.directive";
import {KyloDraggableDirective} from "./draggable-ng2/kylo-draggable.directive";
import {UploadFileComponent} from "./file-upload/file-upload.component";
import {FilteredPaginatedTableViewComponent} from "./filtered-paginated-table-view/filtered-paginated-table-view.component";
import {IconPickerDialog} from "./icon-picker-dialog/icon-picker-dialog.component";
import {KyloIconComponent} from "./kylo-icon/kylo-icon.component";
import {KyloOptionsComponent} from "./kylo-options/kylo-options.component";
import {KyloVisNetworkComponent} from "./kylo-vis-network/kylo-vis-network.component";
import {LocalStorageService} from "./local-storage/local-storage.service";
import {NotificationMenuComponent} from "./notifications/notification-menu.component";
import {OptionsMenuComponent} from "./options-menu/OptionsMenu.component"
import {SafeHtmlPipe} from "./safe-html/safe-html.pipe";
import {KyloTimerDirective} from "./timer/kylo-timer.component";
import {RouterBreadcrumbsComponent} from "./ui-router-breadcrumbs/ui-router-breadcrumbs.component";
import {DateTimeService} from "./utils/date-time.service";
import {VerticalSectionLayoutComponent} from "./vertical-section-layout/vertical-section-layout.component";
import {ViewTypeSelectionComponent} from "./view-type-selection/view-type-selection.component";
import { menuLinkComponent } from "./accordion-menu/menuLinkComponent";
import { VisualQueryTableHeader } from "../feed-mgr/visual-query/transform-data/visual-query-table/visual-query-table-header.component";
import { CellMenuComponent } from "../feed-mgr/visual-query/transform-data/visual-query-table/cell-menu.component";
import { SampleDialog } from '../feed-mgr/visual-query/transform-data/main-dialogs/sample-dialog';
import { QuickCleanDialog } from '../feed-mgr/visual-query/transform-data/main-dialogs/quick-clean-dialog';
import { MatCheckboxModule, MatRadioModule } from '@angular/material';
import { SchemaLayoutDialog } from '../feed-mgr/visual-query/transform-data/main-dialogs/schema-layout-dialog';
import { DndListModule } from 'ngx-drag-and-drop-lists';
import { QuickColumnsDialog } from '../feed-mgr/visual-query/transform-data/main-dialogs/quick-columns-dialog';
import { MiniCategoricalComponent, MiniHistogramComponent } from '../feed-mgr/visual-query/transform-data/main-dialogs/quick-column-components';
import { NvD3Module } from 'ng2-nvd3';
import { ConnectionDialog } from '../feed-mgr/visual-query/build-query/connection-dialog/connection-dialog.component';

@NgModule({
    declarations: [
        AboutKyloDialogController,
        AccessDeniedComponent,
        AccordianMenuComponent,
        AddButtonComponent,
        BaseDraggableDirective,
        BrowserHeight,
        CardFilterHeaderComponent,
        CardLayoutComponent,
        FilteredPaginatedTableViewComponent,
        IconPickerDialog,
        KyloDraggableDirective,
        KyloIconComponent,
        KyloOptionsComponent,
        KyloTimerDirective,
        KyloVisNetworkComponent,
        menuLinkComponent,
        MenuToggleComponent,
        NotificationMenuComponent,
        OptionsMenuComponent,
        RouterBreadcrumbsComponent,
        SafeHtmlPipe,
        UploadFileComponent,
        VerticalSectionLayoutComponent,
        ViewTypeSelectionComponent,
        menuLinkComponent,
        VisualQueryTableHeader,
        CellMenuComponent
    ],
    entryComponents: [
        AboutKyloDialogController,
        AccordianMenuComponent,
        AddButtonComponent,
        CardFilterHeaderComponent,
        CardLayoutComponent,
        FilteredPaginatedTableViewComponent,
        IconPickerDialog,
        KyloOptionsComponent,
        menuLinkComponent,
        MenuToggleComponent,
        NotificationMenuComponent,
        OptionsMenuComponent,
        RouterBreadcrumbsComponent,
        UploadFileComponent,
        VerticalSectionLayoutComponent,
        ViewTypeSelectionComponent,
        VisualQueryTableHeader,
        CellMenuComponent,
    ],
    providers: [
        AboutKyloService,
        AccordionMenuService,
        DateTimeService,
        LocalStorageService,
        RestUrlService
    ],
    imports: [
        CommonModule,
        CovalentDataTableModule,
        CovalentFileModule,
        CovalentLoadingModule,
        CovalentMenuModule,
        CovalentNotificationsModule,
        CovalentPagingModule,
        CovalentSearchModule,
        FlexLayoutModule,
        FormsModule,
        ReactiveFormsModule,
        HttpClientModule,
        MatButtonModule,
        MatCardModule,
        MatDialogModule,
        MatFormFieldModule,
        MatGridListModule,
        MatIconModule,
        MatInputModule,
        MatListModule,
        MatMenuModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        MatSelectModule,
        MatToolbarModule,
        MatCheckboxModule,
        MatTooltipModule,
        MatRadioModule,
        TranslateModule,
        DndListModule,
        UIRouterModule,
        NvD3Module
    ],
    exports: [
        AccessDeniedComponent,
        AccordianMenuComponent,
        AddButtonComponent,
        BaseDraggableDirective,
        BrowserHeight,
        CardFilterHeaderComponent,
        CovalentDataTableModule,
        FilteredPaginatedTableViewComponent,
        IconPickerDialog,
        KyloDraggableDirective,
        KyloIconComponent,
        KyloOptionsComponent,
        KyloTimerDirective,
        KyloVisNetworkComponent,
        MenuToggleComponent,
        NotificationMenuComponent,
        RouterBreadcrumbsComponent,
        SafeHtmlPipe,
        UploadFileComponent,
        VerticalSectionLayoutComponent,
    ]
})
export class KyloCommonModule {
}
