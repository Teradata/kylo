import {NgModule, CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";

import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatGridListModule} from '@angular/material/grid-list';
import { CovalentDialogsModule } from '@covalent/core/dialogs';

import { FormsModule } from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatCardModule} from '@angular/material/card';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatSnackBarModule} from '@angular/material/snack-bar';

import {BrowserModule} from "@angular/platform-browser";

import { CovalentCommonModule } from '@covalent/core/common';
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentMenuModule} from "@covalent/core/menu";
import {CovalentNotificationsModule} from "@covalent/core/notifications";

import {TranslateModule} from "@ngx-translate/core";

import {KyloCommonModule} from "../../common/common.module";

import { UIRouterModule } from "@uirouter/angular";
import {domainTypeStates} from "./domain-types.states";

import {KyloFeedManagerModule} from "../feed-mgr.module";
import {CommonModule} from "@angular/common";
import * as angular from "angular";
import { CovalentSearchModule } from "@covalent/core/search";
import {CovalentDataTableModule} from "@covalent/core/data-table";
import { ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from "../shared/shared.modules";
import { DomainTypesComponent } from "./DomainTypesController";
import { DomainTypeDetailsComponent } from "./details/details.component";
import { DomainTypeRulesDetailsComponent } from "./details/rules/rules.component";
import { DomainTypeDetailsService } from "./services/details.service";
import { DomainTypeMetadataDetailsComponent } from "./details/metadata/metadata.component";
import { RegExpEditorComponent } from "./details/matchers/regexp-editor.component";

import {CodemirrorModule} from "ng2-codemirror";
import { DomainTypeMatchersDetailsComponent } from "./details/matchers/matchers.component";
import { MatChipsModule } from "@angular/material/chips";
import { FeedTagService } from "../services/FeedTagService";
import { CovalentChipsModule } from "@covalent/core/chips";

import "./codemirror-regex.css";
import "./details/matchers/regexp-editor.component.scss";

@NgModule({
    declarations: [
        DomainTypesComponent,
        DomainTypeDetailsComponent,
        DomainTypeRulesDetailsComponent,
        DomainTypeMetadataDetailsComponent,
        RegExpEditorComponent,
        DomainTypeMatchersDetailsComponent
    ],
    entryComponents: [
        DomainTypesComponent,
        DomainTypeDetailsComponent,
        DomainTypeRulesDetailsComponent,
        DomainTypeMetadataDetailsComponent,
        RegExpEditorComponent,
        DomainTypeMatchersDetailsComponent
    ],
    imports: [
        CodemirrorModule,
        CovalentCommonModule,
        CovalentLoadingModule,
        CovalentMenuModule,
        CovalentNotificationsModule,
        KyloCommonModule,
        KyloFeedManagerModule,
        SharedModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        MatMenuModule,
        MatInputModule,
        MatSelectModule,
        MatProgressSpinnerModule,
        MatGridListModule,
        CovalentDialogsModule,
        MatSnackBarModule,
        CovalentSearchModule,
        CovalentDataTableModule,
        FormsModule,
        TranslateModule.forChild(),
        MatFormFieldModule,
        MatCardModule,
        MatCheckboxModule,
        ReactiveFormsModule,
        MatChipsModule,
        CovalentChipsModule,
        UIRouterModule.forChild({states: domainTypeStates})
    ],
    exports: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers: [ DomainTypeDetailsService, FeedTagService,
        {provide: "$injector", useFactory: () => angular.element(document.body).injector()}
    ]
})
export class DomainTypesModule {}
