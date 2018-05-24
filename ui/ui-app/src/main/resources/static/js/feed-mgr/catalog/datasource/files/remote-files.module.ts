import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatDividerModule} from "@angular/material/divider";
import {MatListModule} from "@angular/material/list";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatSelectModule} from '@angular/material/select';
import {CovalentFileModule} from "@covalent/core/file";
import {CovalentSearchModule} from "@covalent/core/search";
import {CovalentPagingModule} from '@covalent/core/paging';
import {UIRouterModule} from "@uirouter/angular";

import {KyloCommonModule} from "../../../../common/common.module";
import {RemoteFilesComponent} from "./remote-files.component";
import {remoteFileStates} from "./remote-files.states";
import {CovalentDataTableModule} from '@covalent/core/data-table';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {CovalentNotificationsModule} from '@covalent/core/notifications';
import {MatDialogModule} from '@angular/material/dialog';
import {SelectionDialogComponent} from './dialog/selection-dialog.component';

@NgModule({
    declarations: [
        RemoteFilesComponent,
        SelectionDialogComponent
    ],
    entryComponents: [
        RemoteFilesComponent,
        SelectionDialogComponent
    ],
    imports: [
        FormsModule,
        ReactiveFormsModule,
        CovalentDataTableModule,
        CommonModule,
        CovalentFileModule,
        CovalentSearchModule,
        CovalentPagingModule,
        CovalentNotificationsModule,
        FlexLayoutModule,
        KyloCommonModule,
        MatButtonModule,
        MatCardModule,
        MatDividerModule,
        MatListModule,
        MatProgressBarModule,
        MatSelectModule,
        MatCheckboxModule,
        MatDialogModule,
        UIRouterModule.forChild({states: remoteFileStates})
    ]
})
export class RemoteFilesModule {
}
