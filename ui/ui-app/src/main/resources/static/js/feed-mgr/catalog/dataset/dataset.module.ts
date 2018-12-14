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
import {datasetStates} from './dataset.states';
import {DatasetComponent} from './dataset.component';
import {KyloCommonModule} from '../../../common/common.module';
import {KyloFeedManagerModule} from '../../feed-mgr.module';
import {MatExpansionModule} from '@angular/material/expansion';
import {DatasetInfoDescriptionComponent} from './dataset-info/dataset-info-description/dataset-info-description.component';
import {DatasetLoadingService} from './dataset-loading-service';
import {DatasetService} from './dataset-service';
import {DatasetInfoTitleComponent} from './dataset-info/dataset-info-title/dataset-info-title.component';
import {DatasetInfoTagsComponent} from './dataset-info/dataset-info-tags/dataset-info-tags.component';
import {CovalentChipsModule} from '@covalent/core/chips';
import {DatasetInfoComponent} from './dataset-info/dataset-info.component';
import {DatasetOverviewComponent} from './dataset-overview/dataset-overview.component';
import {DatasetUsageComponent} from './dataset-usage/dataset-usage.component';
import {DatasetColumnProfileComponent} from './dataset-column-profile/dataset-column-profile.component';
import {DatasetSampleContentComponent} from './dataset-sample-content/dataset-sample-content.component';

@NgModule({
    declarations: [
        DatasetComponent,
        DatasetOverviewComponent,
        DatasetInfoDescriptionComponent,
        DatasetInfoTitleComponent,
        DatasetInfoTagsComponent,
        DatasetInfoComponent,
        DatasetUsageComponent,
        DatasetColumnProfileComponent,
        DatasetSampleContentComponent,
    ],
    exports: [
    ],
    imports: [
        CommonModule,
        CovalentDynamicFormsModule,
        CovalentDataTableModule,
        CovalentDialogsModule,
        CovalentLayoutModule,
        CovalentLoadingModule,
        CovalentSearchModule,
        CovalentVirtualScrollModule,
        CovalentChipsModule,
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
    ],
    providers:[
        DatasetLoadingService,
        DatasetService,
    ]})
export class DatasetModule {
}

@NgModule({
    imports: [
        DatasetModule,
        UIRouterModule.forChild({states: datasetStates})
    ]
})
export class DatasetRouterModule {
}
