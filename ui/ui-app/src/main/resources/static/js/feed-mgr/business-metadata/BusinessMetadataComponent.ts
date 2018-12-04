import * as _ from "underscore";
import {AccessControlService} from '../../services/AccessControlService';
import { Component } from '@angular/core';
import { CloneUtil } from "../../common/utils/clone-util";
import { ObjectUtils } from "../../../lib/common/utils/object-utils";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { RestUrlService } from "../services/RestUrlService";
@Component({
    selector : 'business-metadata',
    templateUrl: './business-metadata.html',
})
export class BusinessMetadataComponent {
    /**
          * Indicates if the category fields may be edited.
          * @type {boolean}
          */
    allowCategoryEdit: boolean = false;
    /**
         * Indicates if the feed fields may be edited.
         * @type {boolean}
         */
    allowFeedEdit: boolean = false;
    /**
         * Model for editable sections.
         * @type {{categoryFields: Array, feedFields: Array}}
         */
    editModel: any = { categoryFields: [], feedFields: [] };
    /**
         * Indicates that the editable section for categories is displayed.
         * @type {boolean}
         */
    isCategoryEditable: boolean = false;

    /**
     * Indicates that the editable section for categories is valid.
     * @type {boolean}
     */
    isCategoryValid: boolean = true;
    /**
         * Indicates that the editable section for categories is displayed.
         * @type {boolean}
         */
    isFeedEditable: boolean = false;
    /**
         * Indicates that the editable section for categories is valid.
         * @type {boolean}
         */
    isFeedValid: boolean = true;
    /**
         * Indicates that the loading progress bar is displayed.
         * @type {boolean}
         */
    loading: boolean = true;
    /**
        * Model for read-only sections.
        * @type {{categoryFields: Array, feedFields: Array}}
        */
    model: any = { categoryFields: [], feedFields: [] };
    /**
     * Controller for the business metadata page.
     *
     * @constructor
     * @param $scope the application model
     * @param $http the HTTP service
     * @param {AccessControlService} AccessControlService the access control service
     * @param RestUrlService the Rest URL service
     */
    constructor(private http: HttpClient,
        private accessControlService: AccessControlService,
        private restUrlService: RestUrlService) {

        // Load the field models
        this.http.get(restUrlService.ADMIN_USER_FIELDS).toPromise().then((response: any) => {
            this.model = response;
            this.loading = false;
        });

        // Load the permissions
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowCategoryEdit = accessControlService.hasAction(AccessControlService.CATEGORIES_ADMIN, actionSet.actions);
                this.allowFeedEdit = accessControlService.hasAction(AccessControlService.FEEDS_ADMIN, actionSet.actions);
            });
    }
    /**
        * Creates a copy of the category model for editing.
        */
    onCategoryEdit () {
        this.editModel.categoryFields = CloneUtil.deepCopy(this.model.categoryFields);
    };
    /**
     * Saves the category model.
     */
    onCategorySave () {
        var model = CloneUtil.deepCopy(this.model);
        model.categoryFields = this.editModel.categoryFields;

        this.http.post(this.restUrlService.ADMIN_USER_FIELDS,ObjectUtils.toJson(model),
                    {headers : new HttpHeaders({'Content-Type':'application/json; charset=utf-8'})})
                    .toPromise().then((response : any) => {
                                                            this.model = model;
                                                          }).catch((error : any) =>{
                                                                console.log(error);
                                                            });
    };

    /**
     * Creates a copy of the feed model for editing.
     */
    onFeedEdit () {
        this.editModel.feedFields = CloneUtil.deepCopy(this.model.feedFields);
    };

    /**
     * Saves the feed model.
     */
    onFeedSave () {
        var model = CloneUtil.deepCopy(this.model);
        model.feedFields = this.editModel.feedFields;
        this.http.post(this.restUrlService.ADMIN_USER_FIELDS,ObjectUtils.toJson(model),
                    {headers : new HttpHeaders({'Content-Type':'application/json; charset=utf-8'})})
                    .toPromise().then((response : any) => {
                                                            this.model = model;
                                                          }).catch((error : any) =>{
                                                                console.log(error);
                                                             });

    };
}
