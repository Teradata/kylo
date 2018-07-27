import * as angular from 'angular';
import * as _ from "underscore";
import { Injectable, Inject } from '@angular/core';
import { RestUrlService } from './RestUrlService';
import { EntityAccessControlService } from '../shared/entity-access-control/EntityAccessControlService';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/Observable/of';
import { Subject } from 'rxjs/Subject';

@Injectable()
    export default class CategoriesService {

        private categoriesSubject = new Subject<any>();

        loadAll= ()=>  {
            if (!this.loading) {
                this.loading = true;
                this.loadingRequest = this.$injector.get("$http").get(this.RestUrlService.CATEGORIES_URL).then((response:any) =>{
                    this.loading = false;
                    this.loadingRequest = null;
                    this.categories = response.data.map((category:any)=> {
                        category._lowername = category.name.toLowerCase();
                        category.createFeed = false;
                        //if under entity access control we need to check if the user has the "CREATE_FEED" permission associated with the selected category.
                        //if the user doesnt have this permission they cannot create feeds under this category
                        if (this.$injector.get("AccessControlService").isEntityAccessControlled()) {
                            if (this.$injector.get("AccessControlService").hasEntityAccess(this.$injector.get("EntityAccessControlService").ENTITY_ACCESS.CATEGORY.CREATE_FEED, category, "category")) {
                                category.createFeed = true;
                            }
                        }
                        else {
                            category.createFeed = true;
                        }
                        return category;
                    });
                    return this.categories;
                }, (err:any)=> {
                    this.loading = false;
                });
                return this.loadingRequest;
            }
            else {
                if (this.loadingRequest != null) {
                    return this.loadingRequest;
                }
                else {
                    var defer = this.$injector.get("$q").defer();
                    defer.resolve(this.categories);
                    return defer.promise;
                }
            }
        }

        /**
         * Global category data used across directives.
         *
         * @type {CategoryModel}
         */
        model: any= {};

        modelSubject = new Subject<any>();

        setModel(value: any) {
            this.modelSubject.next(value);
        }

        init = () => {
            this.reload();
        }

        reload = (): Observable<any> => {
            // return this.loadAll().then((categories:any)=>{
            //     return categories = categories;
            // }, (err:any)=> {
            // });
            this.loadAll().then((categories:any)=>{
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
        update= (savedCategory:any) => {
            var self = this;
            if(angular.isDefined(savedCategory.id)) {
                var category:any = _.find(self.categories, (category: any)=>{
                    return category.id == savedCategory.id;
                });
                savedCategory._lowername = savedCategory.name.toLowerCase();
                savedCategory.createFeed = false;
                //if under entity access control we need to check if the user has the "CREATE_FEED" permission associated with the selected category.
                //if the user doesnt have this permission they cannot create feeds under this category
                if (this.$injector.get("AccessControlService").isEntityAccessControlled()) {
                    if (this.$injector.get("AccessControlService").hasEntityAccess(this.$injector.get("EntityAccessControlService").ENTITY_ACCESS.CATEGORY.CREATE_FEED, savedCategory, "category")) {
                        savedCategory.createFeed = true;
                    }
                }
                else {
                    savedCategory.createFeed = true;
                }

                if(angular.isDefined(category)) {
                    var idx = _.indexOf(self.categories, category);
                    self.categories[idx] = savedCategory;
                }
                else {
                    self.categories.push(savedCategory);
                }
                return true;
            }
            else {
                self.reload();
            }
            return false;
        }
        delete= (category:any) => {
            var promise = this.$injector.get("$http")({
                url: this.RestUrlService.CATEGORIES_URL + "/" + category.id,
                method: "DELETE"
            });
            return promise;
        }
        save= (category:any) => {
            //prepare access control changes if any
            this.EntityAccessControlService.updateRoleMembershipsForSave(category.roleMemberships);
            this.EntityAccessControlService.updateRoleMembershipsForSave(category.feedRoleMemberships);
            var promise = this.$injector.get("$http")({
                url: this.RestUrlService.CATEGORIES_URL,
                method: "POST",
                data: angular.toJson(category),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            });
            return promise;
        }
        populateRelatedFeeds= (category:any) => {
            var deferred = this.$injector.get("$q").defer();
            this.getRelatedFeeds(category).then((response:any)=>{
                category.relatedFeedSummaries = response.data || [];
                deferred.resolve(category);
            }, ()=>{
                deferred.reject();
            })
            return deferred.promise;
        }
        getRelatedFeeds= (category:any) => {
            return this.$injector.get("$http").get(this.RestUrlService.CATEGORIES_URL + "/" + category.id + "/feeds");
        }
        findCategory= (id:any) => {
            var self = this;
            var category:any = _.find(self.categories, (category: any)=> {
                return category.id == id;
            });
            return category;
        }
        findCategoryByName= (name:any) => {
            if (name != undefined) {
                var self = this;
                var category:any = _.find(self.categories, (category: any)=> {
                    return category.name.toLowerCase() == name.toLowerCase();
                });
                return category;
            }
            return null;
        }
        findCategoryBySystemName= (systemName:any) => {
            if (systemName != undefined) {
                var self = this;
                var category:any = _.find(self.categories, (category: any)=> {
                    return category.systemName == systemName;
                });
                return category;
            }
            return null;
        }
        getCategoryBySystemName=(systemName:any)=> {
            var self = this;
            var deferred = this.$injector.get("$q").defer();
            var categoryCache = self.findCategoryBySystemName(systemName);
            if(typeof categoryCache === 'undefined' || categoryCache === null) {
                this.$injector.get("$http").get(this.RestUrlService.CATEGORY_DETAILS_BY_SYSTEM_NAME_URL(systemName))
                    .then((response:any)=>{
                        var categoryResponse = response.data;
                        deferred.resolve(categoryResponse);
                    });
            }
            else {
                deferred.resolve(categoryCache);
            }
            return deferred.promise;
        }
        getCategoryById=(categoryId:any) => {
            var deferred = this.$injector.get("$q").defer();
            this.$injector.get("$http").get(this.RestUrlService.CATEGORY_DETAILS_BY_ID_URL(categoryId))
                .then((response:any)=>{
                    var categoryResponse = response.data;
                    return deferred.resolve(categoryResponse);
                });
            return deferred.promise;
        }
        categories= new Array();
        querySearch= (query:any) => {
            var self = this;
            var deferred = this.$injector.get("$q").defer();
            if (self.categories.length == 0) {
                this.loadAll().then((response:any)=>{
                    self.loading = false;
                    if (query) {
                        var results = response.filter(this.createFilterFor(query))
                        deferred.resolve(results);
                    }
                    else {
                        deferred.resolve(response);
                    }
                }, (err:any)=> {
                    self.loading = false;
                });
            }
            else {
                var results = query ? self.categories.filter(this.createFilterFor(query)) : self.categories;
                deferred.resolve(results);
            }
            return deferred.promise;
        }

        /**
         * Creates a new category model.
         *
         * @returns {CategoryModel} the new category model
         */
        newCategory= () => {
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
        getUserFields= () => {
            return this.$injector.get("$http").get(this.RestUrlService.GET_CATEGORY_USER_FIELD_URL)
                .then((response:any)=>{
                    return response.data;
                });
        }
        /**
         * check if the user has access on an entity
         * @param permissionsToCheck an Array or a single string of a permission/action to check against this entity and current user
         * @param entity the entity to check. if its undefined it will use the current category in the model
         * @returns {*} a promise, or a true/false.  be sure to wrap this with a $q().then()
         */
        hasEntityAccess= (permissionsToCheck:any, entity:any) => {
            if (entity == undefined) {
                entity = this.model;
            }
            return this.$injector.get("AccessControlService").hasEntityAccess(permissionsToCheck, entity, this.$injector.get("EntityAccessControlService").entityRoleTypes.CATEGORY);
        }

        /**
         * internal cache of categories
         * @type {Array}
         */
       // categories:any = [];
        /**
         * the loading request for all categories
         * @type promise
         */
        loadingRequest:any = null;
        loading = false;
        createFilterFor(query:any) {
            var lowercaseQuery = angular.lowercase(query);
            return function filterFn(tag:any) {
                return (tag._lowername.indexOf(lowercaseQuery) === 0);
            };
        }

       Builder() {
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
                    private EntityAccessControlService: EntityAccessControlService,
                    @Inject("$injector") private $injector: any) {
                this.init();
    } // end of constructor
 }