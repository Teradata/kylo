import * as _ from "underscore";
import AccessControlService from '../../../services/AccessControlService';
import { EntityAccessControlService } from '../../shared/entity-access-control/EntityAccessControlService';
import { Component } from '@angular/core';
import CategoriesService from '../../services/CategoriesService';
import { TdDialogService } from '@covalent/core/dialogs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ObjectUtils } from '../../../common/utils/object-utils';
import { CloneUtil } from '../../../common/utils/clone-util';
import { TranslateService } from "@ngx-translate/core";

@Component({
    selector: 'thinkbig-category-access-control',
    templateUrl: 'js/feed-mgr/categories/details/category-access-control.html'
})
export class CategoryAccessControl {

    model: any;
    categoryAccessControlForm: any = {};
    /**
     * Indicates if the properties may be edited.
     */
    allowEdit: boolean = false;

    /**
     * Category data used in "edit" mode.
     * @type {CategoryModel}
     */
    editModel: any;

    /**
     * Indicates if the view is in "edit" mode.
     * @type {boolean} {@code true} if in "edit" mode or {@code false} if in "normal" mode
     */
    isEditable: any= false;

    /**
     * Indicates of the category is new.
     * @type {boolean}
     */
    isNew: boolean = true;

    ngOnInit() {
        this.model = this.CategoriesService.model;

        if (this.CategoriesService.model.roleMemberships == undefined) {
            this.CategoriesService.model.roleMemberships = this.model.roleMemberships = [];
            this.CategoriesService.setModel(this.CategoriesService.model);
        }

        if (this.CategoriesService.model.feedRoleMemberships == undefined) {
            this.CategoriesService.model.feedRoleMemberships = this.model.feedRoleMemberships = [];
            this.CategoriesService.setModel(this.CategoriesService.model);
        }

        /**
         * Category data used in "edit" mode.
         * @type {CategoryModel}
         */
        this.editModel = this.CategoriesService.newCategory();

        /**
         * Category data used in "normal" mode.
         * @type {CategoryModel}
         */
        this.model = this.CategoriesService.model;


        //Apply the entity access permissions
        this.accessControlService.hasPermission(AccessControlService.CATEGORIES_EDIT, this.model,
                                                         AccessControlService.ENTITY_ACCESS.CATEGORY.CHANGE_CATEGORY_PERMISSIONS)
                                                                                                    .then((access: any) => {
            this.allowEdit = access;
        });

        this.CategoriesService.modelSubject.subscribe((newValue: any) => {
            this.isNew = !ObjectUtils.isString(newValue.id)
        });
    }
    constructor(private CategoriesService: CategoriesService,
                private accessControlService: AccessControlService,
                private entityAccessControlService: EntityAccessControlService,
                private _tdDialogService : TdDialogService,
                private snackBar : MatSnackBar,
                private translate : TranslateService) {

    }

    /**
         * Switches to "edit" mode.
         */
    onEdit () {
        this.editModel = CloneUtil.deepCopy(this.model);
    };
    /**
         * Saves the category .
         */
    onSave () {
        var model = CloneUtil.deepCopy(this.CategoriesService.model);
        model.roleMemberships = this.editModel.roleMemberships;
        model.feedRoleMemberships = this.editModel.feedRoleMemberships;
        model.owner = this.editModel.owner;
        model.allowIndexing = this.editModel.allowIndexing;
        this.entityAccessControlService.updateRoleMembershipsForSave(model.roleMemberships);
        this.entityAccessControlService.updateRoleMembershipsForSave(model.feedRoleMemberships);

        //TODO Open a Dialog showing Category is Saving progress
        this.CategoriesService.save(model).then((response: any) => {
            this.model = this.CategoriesService.model = response;
            //set the editable flag to false after the save is complete.
            //this will flip the directive to read only mode and call the entity-access#init() method to requery the accesss control for this entity
            this.isEditable = false;
            this.CategoriesService.update(response);
            this.snackBar.open(this.translate.instant('FEEDMGR.category.saved'), this.translate.instant('view.main.ok'), {duration : 3000});
        }, (err: any) => {
            //keep editable active if an error occurred
            this.isEditable = true;
            this._tdDialogService.openAlert({
                title : this.translate.instant('views.common.save.failed.title'),
                message : this.translate.instant('FEEDMGR.category.dialog.save.failed.message',{entity: model.name, message: err.message}),
                ariaLabel : this.translate.instant('views.common.save.failed',{entity:'Category'}),
                closeButton : this.translate.instant('views.common.dialog.gotIt'),
                disableClose : false 
            });
        });
    };

}

