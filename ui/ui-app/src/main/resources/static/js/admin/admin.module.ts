import { CommonModule } from "@angular/common";
import { UIRouterModule } from "@uirouter/angular";
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatDividerModule} from '@angular/material/divider';
import {MatListModule} from '@angular/material/list';
import {MatIconModule} from '@angular/material/icon';
import { NgModule } from "@angular/core";
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {MatSelectModule} from '@angular/material/select';
import {MatToolbarModule} from '@angular/material/toolbar';
import { CovalentDialogsModule } from '@covalent/core/dialogs';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatChipsModule} from '@angular/material/chips';
import {MatInputModule} from '@angular/material/input';
import {FlexLayoutModule} from "@angular/flex-layout";
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import * as angular from "angular";
import { JcrQueryComponent } from "./jcr/jcr-query.component";
import {ClusterComponent} from './cluster/cluster.component';
import { states } from "./admin.states";
import { MatButtonModule } from "@angular/material/button";
import {MatTabsModule} from '@angular/material/tabs';
import {CodemirrorModule} from "ng2-codemirror";
import { CovalentDataTableModule } from '@covalent/core/data-table';
import { KyloCommonModule } from "../common/common.module";
import { MatCardModule } from "@angular/material/card";
import { TranslateModule } from "@ngx-translate/core";

@NgModule({
    declarations: [
        JcrQueryComponent,
        ClusterComponent
    ],
    imports: [
        CommonModule,
        KyloCommonModule,
        MatProgressBarModule,
        MatDividerModule,
        MatListModule,
        MatIconModule,
        MatSelectModule,
        MatToolbarModule,
        CovalentDialogsModule,
        MatFormFieldModule,
        MatChipsModule,
        MatInputModule,
        MatSnackBarModule,
        FlexLayoutModule,
        FormsModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatTabsModule,
        MatCardModule,
        CodemirrorModule,
        CovalentDataTableModule,
        TranslateModule,
        UIRouterModule.forChild({states: states})
    ],
    providers : [
        {provide: "$injector", useFactory: () => angular.element(document.body).injector()},
    ],
    schemas : [NO_ERRORS_SCHEMA]
})
export class AdminModule {
}