import {NgModule, CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";

import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatListModule} from "@angular/material/list";
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatCardModule} from '@angular/material/card';
import { CovalentDialogsModule } from '@covalent/core/dialogs';
import {BrowserModule} from "@angular/platform-browser";

import { CovalentCommonModule } from '@covalent/core/common';
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentMenuModule} from "@covalent/core/menu";

import {TranslateModule} from "@ngx-translate/core";

import { UIRouterModule } from "@uirouter/angular";
import {searchStates} from "./search.states";

import { MatTooltipModule } from '@angular/material/tooltip';
import { CovalentDataTableModule } from '@covalent/core/data-table';
import { CovalentSearchModule } from '@covalent/core/search';
import { CovalentPagingModule } from '@covalent/core/paging';
import { FormsModule, ReactiveFormsModule, FormControlDirective } from '@angular/forms';
import * as angular from "angular";

import { KyloCommonModule } from "../common/common.module";
import { KyloServicesModule } from "../services/services.module";

import { SearchComponent } from "./common/SearchComponent";
import { KyloFeedManagerModule } from "../feed-mgr/feed-mgr.module";

@NgModule({
    declarations: [
        SearchComponent
    ],
    entryComponents: [
        SearchComponent
    ],
    imports: [
        CovalentCommonModule,
        CovalentLoadingModule,
        CovalentDataTableModule,
        CovalentSearchModule,
        CovalentPagingModule,
        CovalentMenuModule,
        KyloServicesModule,
        KyloFeedManagerModule,
        KyloCommonModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        MatProgressBarModule,
        MatGridListModule,
        MatTooltipModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        MatCardModule,
        CovalentDialogsModule,
        UIRouterModule.forChild({states: searchStates})
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers: [ 
        {provide: "$injector", useFactory: () => angular.element(document.body).injector()},
    ]
})
export class SearchModule {
    constructor() {
    }
}