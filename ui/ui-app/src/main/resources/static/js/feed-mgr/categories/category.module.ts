import {NgModule, CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";

import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatGridListModule} from '@angular/material/grid-list';
import {MatDialogModule} from '@angular/material/dialog';

import { FormsModule } from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatCardModule} from '@angular/material/card';

import {BrowserModule} from "@angular/platform-browser";

import { CovalentCommonModule } from '@covalent/core/common';
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentMenuModule} from "@covalent/core/menu";
import {CovalentNotificationsModule} from "@covalent/core/notifications";

import {TranslateModule} from "@ngx-translate/core";

import {KyloServicesModule} from "../../services/services.module";
import {KyloCommonModule} from "../../common/common.module";

import {CategoryDefinitionController} from "./details/category-definition.component";
import {CategoryPropertiesController} from "./details/category-properties.component";
import { CategoryFeedPropertiesController } from "./details/category-feed-properties.component";
import { CategoryAccessControlController } from "./details/category-access-control.component";
import { CategoryFeedsController } from "./details/category-feeds.component";

import {CategoriesControllerComponent} from "./CategoriesController.component";
import {CategoryDetailsController} from "./category-details.component";

import {PropertiesAdminController} from "../shared/properties-admin/properties-admin.component";
import {PropertiesAdminEditorController} from "../shared/properties-admin/properties-admin-editor.component";

import { UIRouterModule } from "@uirouter/angular";
import {categoriesStates} from "./categories.states";

import CategoriesService from "../services/CategoriesService";
import { RestUrlService } from "../services/RestUrlService";
import { FeedSecurityGroups } from "../services/FeedSecurityGroupsService";
import { EntityAccessControlService } from "../shared/entity-access-control/EntityAccessControlService";

@NgModule({
    declarations: [
        CategoriesControllerComponent,
        CategoryDefinitionController,
        CategoryPropertiesController,
        CategoryFeedPropertiesController,
        CategoryAccessControlController,
        CategoryFeedsController,
        CategoryDetailsController,
        PropertiesAdminController,
        PropertiesAdminEditorController
    ],
    entryComponents: [
        CategoriesControllerComponent,
        CategoryDefinitionController,
        CategoryPropertiesController,
        CategoryFeedPropertiesController,
        CategoryAccessControlController,
        CategoryFeedsController,
        CategoryDetailsController,
        PropertiesAdminController,
        PropertiesAdminEditorController
    ],
    imports: [
        BrowserModule,
        CovalentCommonModule,
        CovalentLoadingModule,
        CovalentMenuModule,
        CovalentNotificationsModule,
        KyloServicesModule,
        KyloCommonModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        MatMenuModule,
        MatInputModule,
        MatSelectModule,
        MatProgressSpinnerModule,
        MatGridListModule,
        MatDialogModule,
        FormsModule,
        TranslateModule,
        MatFormFieldModule,
        MatCardModule,
        UIRouterModule.forChild({states: categoriesStates})
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers: [CategoriesService, RestUrlService, FeedSecurityGroups, EntityAccessControlService]
})
export class CategoryModule {
}
