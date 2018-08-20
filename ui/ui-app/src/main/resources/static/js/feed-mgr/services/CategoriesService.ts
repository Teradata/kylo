import * as _ from "underscore";
import { Injectable, Inject } from '@angular/core';
import { RestUrlService } from './RestUrlService';
import { EntityAccessControlService } from '../shared/entity-access-control/EntityAccessControlService';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/Observable/of';
import { Subject } from 'rxjs/Subject';
import AccessControlService from "../../services/AccessControlService";
import { HttpClient, HttpHeaders } from "@angular/common/http";

@Injectable()
    export default class CategoriesService {

        private categoriesSubject = new Subject<any>();
        /**
         * internal cache of categories
         * @type {Array}
         */
        categories= new Array();
        /**
         * the loading request for all categories
         * @type promise
         */
        loadingRequest:any = null;

        loading = false;

        /**
         * Global category data used across directives.
         *
         * @type {CategoryModel}
         */
        model: any= {};

        modelSubject = new Subject<any>();

        loadAll () {
            if (!this.loading) {
                this.loading = true;
                this.loadingRequest = this.http.get(this.RestUrlService.CATEGORIES_URL).toPromise().then((response:any) =>{
                    this.loading = false;
                    this.loadingRequest = null;
                    this.categories = response.map((category:any)=> {
                        category._lowername = category.name.toLowerCase();
                        category.createFeed = false;
                        //if under entity access control we need to check if the user has the "CREATE_FEED" permission associated with the selected category.
                        //if the user doesnt have this permission they cannot create feeds under this category
                        if (this.accessControlService.isEntityAccessControlled()) {
                            if (this.accessControlService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.CATEGORY.CREATE_FEED, category, "category")) {
                                category.createFeed = true;
                            }
                        }
                        else {
                            category.createFeed = true;
                        }
                        return category;
                    });
                    return this.categories;
                }, (err:any) => {
                    this.loading = false;
                });
                return this.loadingRequest;
            }
            else {
                if (this.loadingRequest != null) {
                    return this.loadingRequest;
                }
                else {
                    return Promise.resolve(this.categories);
                }
            }
        }

        setModel(value: any) {
            this.modelSubject.next(value);
        }

        init() {
            this.reload();
        }

        reload (): Observable<any> {
            // return this.loadAll().then((categories:any)=>{
            //     return categories = categories;
            // }, (err:any)=> {
            // });
            this.loadAll().then((categories:any) => {
                this.categoriesSubject.next(categories);
            });
            return this.categoriesSubject.asObservable();
        }
        /**
         * Adds/updates the category back to the cached list.
         * returns true if successful, false if not
         * @param savedCategory
         * @return {boolean}
         */
        update (savedCategory:any) {
            if(typeof savedCategory.id !== 'undefined') {
                var category:any = _.find(this.categories, (category: any) => {
                    return category.id == savedCategory.id;
                });
                savedCategory._lowername = savedCategory.name.toLowerCase();
                savedCategory.createFeed = false;
                //if under entity access control we need to check if the user has the "CREATE_FEED" permission associated with the selected category.
                //if the user doesnt have this permission they cannot create feeds under this category
                if (this.accessControlService.isEntityAccessControlled()) {
                    if (this.accessControlService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.CATEGORY.CREATE_FEED, savedCategory, "category")) {
                        savedCategory.createFeed = true;
                    }
                }
                else {
                    savedCategory.createFeed = true;
                }

                if(typeof category !== 'undefined') {
                    var idx = _.indexOf(this.categories, category);
                    this.categories[idx] = savedCategory;
                }
                else {
                    this.categories.push(savedCategory);
                }
                return true;
            }
            else {
                this.reload();
            }
            return false;
        }
        delete (category:any) {
            var promise = this.http.delete(this.RestUrlService.CATEGORIES_URL + "/" + category.id).toPromise();
            return promise;
        }
        save (category:any) {
            //prepare access control changes if any
            this.entityAccessControlService.updateRoleMembershipsForSave(category.roleMemberships);
            this.entityAccessControlService.updateRoleMembershipsForSave(category.feedRoleMemberships);
            var promise = this.http.post(this.RestUrlService.CATEGORIES_URL,JSON.stringify(category),
                        {headers :new HttpHeaders({'Content-Type':'application/json; charset=utf-8'}) }).toPromise();
            return promise;
        }
        populateRelatedFeeds (category:any) {
            return new Promise((resolve,reject) => {
                this.getRelatedFeeds(category).then((response:any) => {
                    category.relatedFeedSummaries = response || [];
                    resolve(category);
                }, ()=>{
                    reject();
                })
            });
        }
        getRelatedFeeds (category:any) {
            return this.http.get(this.RestUrlService.CATEGORIES_URL + "/" + category.id + "/feeds").toPromise();
        }
        findCategory (id:any) {
            var category:any = _.find(this.categories, (category: any) => {
                return category.id == id;
            });
            return category;
        }
        findCategoryByName (name:any) {
            if (name != undefined) {
                var category:any = _.find(this.categories, (category: any) => {
                    return category.name.toLowerCase() == name.toLowerCase();
                });
                return category;
            }
            return null;
        }
        findCategoryBySystemName (systemName:any) {
            if (systemName != undefined) {
                var category:any = _.find(this.categories, (category: any) => {
                    return category.systemName == systemName;
                });
                return category;
            }
            return null;
        }
        getCategoryBySystemName (systemName:any) {
            var categoryCache = this.findCategoryBySystemName(systemName);
            return new Promise((resolve,reject) => {
                if(typeof categoryCache === 'undefined' || categoryCache === null) {
                    this.http.get(this.RestUrlService.CATEGORY_DETAILS_BY_SYSTEM_NAME_URL(systemName)).toPromise()
                        .then((response:any) => {
                            var categoryResponse = response;
                            resolve(categoryResponse);
                        });
                }
                else {
                    resolve(categoryCache);
                }
            });
        }
        getCategoryById (categoryId:any) {
            return new Promise((resolve,reject) => {
                this.http.get(this.RestUrlService.CATEGORY_DETAILS_BY_ID_URL(categoryId)).toPromise()
                .then((response:any) => {
                    var categoryResponse = response;
                    return resolve(categoryResponse);
                });
            })
        }
        querySearch (query:any) {
            return new Promise((resolve,reject) => {
                if (this.categories.length == 0) {
                    this.loadAll().then((response:any)=>{
                        this.loading = false;
                        if (query) {
                            var results = response.filter(this.createFilterFor(query))
                            resolve(results);
                        }
                        else {
                            resolve(response);
                        }
                    }, (err:any)=> {
                        this.loading = false;
                    });
                }
                else {
                    var results = query ? this.categories.filter(this.createFilterFor(query)) : this.categories;
                    resolve(results);
                }
            });
        }
        /**
         * Creates a new category model.
         *
         * @returns {CategoryModel} the new category model
         */
        newCategory () {
            let data:any = {
                id: null,
                name: null,
                description: null,
                icon: null, iconColor: null,
                userFields: [],
                userProperties: [],
                relatedFeedSummaries: [],
                securityGroups: [],
                roleMemberships: [],
                feedRoleMemberships: [],
                owner: null,
                allowIndexing: true
            }
            return data;
        }
        /**
         * Gets the user fields for a new category.
         *
         * @returns {Promise} for the user fields
         */
        getUserFields () {
            return this.http.get(this.RestUrlService.GET_CATEGORY_USER_FIELD_URL).toPromise()
                .then((response:any) => {
                    return response;
                });
        }
        /**
         * check if the user has access on an entity
         * @param permissionsToCheck an Array or a single string of a permission/action to check against this entity and current user
         * @param entity the entity to check. if its undefined it will use the current category in the model
         * @returns {*} a promise, or a true/false.  be sure to wrap this with a $q().then()
         */
        hasEntityAccess (permissionsToCheck:any, entity:any) {
            if (entity == undefined) {
                entity = this.model;
            }
            return this.accessControlService.hasEntityAccess(permissionsToCheck, entity, EntityAccessControlService.entityRoleTypes.CATEGORY);
        }

        createFilterFor (query:any) {
            var lowercaseQuery = typeof query === 'string' ? query.toLowerCase() : query;
            return (tag:any) => {
                return (tag._lowername.indexOf(lowercaseQuery) === 0);
            };
        }
       Builder () {
            var builder:any = {
                name: function (name:any) {
                    builder._name = name;
                    return this;
                },
                description: function (desc:any) {
                    builder._description = desc;
                    return this;
                },
                relatedFeeds: function (related:any) {
                    builder._relatedFeeds = related || 0;
                    return this;
                },
                icon: function (icon:any) {
                    builder._icon = icon;
                    return this;
                },
                iconColor: function (color:any) {
                    builder._iconColor = color;
                    return this;
                },
                build: function () {
                    var category:any = {};
                    category.name = this._name;
                    category.description = this._description;
                    category.icon = this._icon;
                    category.relatedFeeds = this._relatedFeeds;
                    category.iconColor = this._iconColor;
                    return category;
                }
            }
            return builder;
        }
        constructor(private RestUrlService: RestUrlService,
                    private entityAccessControlService: EntityAccessControlService,
                    private accessControlService : AccessControlService,
                    private http : HttpClient) {
                this.init();
    } // end of constructor
 }
