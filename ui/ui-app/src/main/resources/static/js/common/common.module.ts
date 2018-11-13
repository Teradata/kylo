import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentMenuModule} from "@covalent/core/menu";
import {CovalentNotificationsModule} from "@covalent/core/notifications";

import {KyloServicesModule} from "../services/services.module";
import {AddButtonComponent} from "./add-button/add-button.component";
import {BrowserHeight} from "./browser-height/browser-height";
import {KyloIconComponent} from "./kylo-icon/kylo-icon.component";
import {NotificationMenuComponent} from "./notifications/notification-menu.component";
import {TranslateModule} from "@ngx-translate/core";
import {FileUploadComponent} from "./file-upload/file-upload.component";
import {CovalentFileModule} from "@covalent/core/file";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {FormsModule} from "@angular/forms";
import {FlexLayoutModule} from "@angular/flex-layout";
import {LocalStorageService} from "./local-storage/local-storage.service";
import {KyloVisNetworkComponent} from "./kylo-vis-network/kylo-vis-network.component";
import {BaseDraggableDirective} from "./draggable-ng2/base-draggable.directive";
import {KyloDraggableDirective} from "./draggable-ng2/kylo-draggable.directive";
import {KyloTimerDirective} from "./timer/kylo-timer.component";
import {DateTimeService} from "./utils/date-time.service";
import {FilteredPaginatedTableViewComponent} from "./filtered-paginated-table-view/filtered-paginated-table-view.component";
import {CovalentDataTableModule} from "@covalent/core/data-table";
import {CovalentPagingModule} from "@covalent/core/paging";
import {MatCardModule} from "@angular/material/card";
import {MatSelectModule} from "@angular/material/select";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {CovalentSearchModule} from "@covalent/core/search";
import {SafeHtmlPipe} from "./safe-html/safe-html.pipe";
import {AccessDeniedComponent} from "./access-denied/access-denied.component";
@NgModule({
    declarations: [
        AddButtonComponent,
        BrowserHeight,
        KyloIconComponent,
        NotificationMenuComponent,
        FileUploadComponent,
        KyloVisNetworkComponent,
        BaseDraggableDirective,
        KyloDraggableDirective,
        KyloTimerDirective,
        FilteredPaginatedTableViewComponent,
        SafeHtmlPipe,
        AccessDeniedComponent
    ],
    entryComponents: [
        AddButtonComponent,
        NotificationMenuComponent
    ],
    providers:[
        LocalStorageService,
        DateTimeService
    ],
    imports: [
        CommonModule,
        CovalentLoadingModule,
        CovalentMenuModule,
        CovalentNotificationsModule,
        CovalentDataTableModule,
        CovalentPagingModule,
        CovalentFileModule,
        CovalentSearchModule,
        KyloServicesModule,
        FormsModule,
        MatFormFieldModule,
        MatCardModule,
        MatSelectModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        MatMenuModule,
        MatToolbarModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        FlexLayoutModule,
        TranslateModule
    ],
    exports: [
        BrowserHeight,
        KyloIconComponent,
        FileUploadComponent,
        KyloVisNetworkComponent,
        BaseDraggableDirective,
        KyloDraggableDirective,
        KyloTimerDirective,
        FilteredPaginatedTableViewComponent,
        SafeHtmlPipe,
        AccessDeniedComponent
    ]
})
export class KyloCommonModule {
}
