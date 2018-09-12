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

@NgModule({
    declarations: [
        AddButtonComponent,
        BrowserHeight,
        KyloIconComponent,
        NotificationMenuComponent,
        FileUploadComponent,
        KyloVisNetworkComponent,
        BaseDraggableDirective,
        KyloDraggableDirective
    ],
    entryComponents: [
        AddButtonComponent,
        NotificationMenuComponent
    ],
    providers:[
        LocalStorageService
    ],
    imports: [
        CommonModule,
        CovalentLoadingModule,
        CovalentMenuModule,
        CovalentNotificationsModule,
        CovalentFileModule,
        KyloServicesModule,
        FormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        MatMenuModule,
        MatProgressSpinnerModule,
        FlexLayoutModule
    ],
    exports: [
        BrowserHeight,
        KyloIconComponent,
        FileUploadComponent,
        KyloVisNetworkComponent,
        BaseDraggableDirective,
        KyloDraggableDirective
    ]
})
export class KyloCommonModule {
}
