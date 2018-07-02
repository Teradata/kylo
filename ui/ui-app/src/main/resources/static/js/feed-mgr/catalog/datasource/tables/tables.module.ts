import {NgModule} from "@angular/core";
import {UIRouterModule} from "@uirouter/angular";
import {TablesComponent} from "./tables.component";
import {tablesStates} from "./tables.states";
import {CovalentNotificationsModule} from '@covalent/core/notifications';
import {MatDialogModule} from '@angular/material/dialog';
import {CovalentFileModule} from '@covalent/core/file';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatDividerModule} from '@angular/material/divider';
import {MatCardModule} from '@angular/material/card';
import {MatListModule} from '@angular/material/list';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {CovalentDataTableModule} from '@covalent/core/data-table';
import {CovalentSearchModule} from '@covalent/core/search';
import {KyloCommonModule} from '../../../../common/common.module';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {CovalentPagingModule} from '@covalent/core/paging';
import {CommonModule} from '@angular/common';
import {BrowserModule} from '../api/browser.module';
import {CovalentLoadingModule} from '@covalent/core/loading';

@NgModule({
    declarations: [
        TablesComponent,
    ],
    entryComponents: [
        TablesComponent,
    ],
    imports: [
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        CovalentDataTableModule,
        CommonModule,
        CovalentFileModule,
        CovalentSearchModule,
        CovalentPagingModule,
        CovalentNotificationsModule,
        CovalentLoadingModule,
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
        UIRouterModule.forChild({states: tablesStates})
    ]
})
export class TablesModule {
}
