
import * as angular from 'angular';
import * as _ from "underscore";
import {AccessControlService} from '../../services/AccessControlService';
import { EntityAccessControlService } from '../shared/entity-access-control/EntityAccessControlService';
import {moduleName} from '../module-name';

//import "../module"; // ensure module is loaded first

// export class CategoriesService {
    export  class CategoriesService {
          loadAll= () => {
            if (!this.loading) {
                this.loading = true;
                this.loadingRequest = this.$http.get(this.RestUrlService.CATEGORIES_URL).then((response:any) =>{
                    this.loading = false;
                    this.loadingRequest = null;
                    this.categories = response.data.map((category:any)=> {
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
                    var defer = this.$q.defer();
                    defer.resolve(this.categories);
                    return defer.promise;
                }
            }
        }
        /**
         * Create filter function for a query string
         */
      //  data: any= {
                /**
                 * Global category data used across directives.
                 *
                 * @type {CategoryModel}
                 */
                model: any= {};
                init= () => {
                    this.reload();
                }
                reload=() => {
                    //var self = this;
                    return this.loadAll().then((categories:any)=>{
                        return categories = categories;
                    }, (err:any)=> {
                    });
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
                        if (this.accessControlService.isEntityAccessControlled()) {
                            if (this.accessControlService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.CATEGORY.CREATE_FEED, savedCategory, "category")) {
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
                    var promise = this.$http({
                        url: this.RestUrlService.CATEGORIES_URL + "/" + category.id,
                        method: "DELETE"
                    });
                    return promise;
                }
                save= (category:any) => {
                    //prepare access control changes if any
                    this.entityAccessControlService.updateRoleMembershipsForSave(category.roleMemberships);
                    this.entityAccessControlService.updateRoleMembershipsForSave(category.feedRoleMemberships);
                    var promise = this.$http({
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
                    var deferred = this.$q.defer();
                    this.getRelatedFeeds(category).then((response:any)=>{
                        category.relatedFeedSummaries = response.data || [];
                        deferred.resolve(category);
                    }, ()=>{
                        deferred.reject();
                    })
                    return deferred.promise;
                }
                getRelatedFeeds= (category:any) => {
                    return this.$http.get(this.RestUrlService.CATEGORIES_URL + "/" + category.id + "/feeds");
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
                    var deferred = this.$q.defer();
                    var categoryCache = self.findCategoryBySystemName(systemName);
                    if(typeof categoryCache === 'undefined' || categoryCache === null) {
                        this.$http.get(this.RestUrlService.CATEGORY_DETAILS_BY_SYSTEM_NAME_URL(systemName))
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
                    var deferred = this.$q.defer();
                    this.$http.get(this.RestUrlService.CATEGORY_DETAILS_BY_ID_URL(categoryId))
                        .then((response:any)=>{
                            var categoryResponse = response.data;
                            return deferred.resolve(categoryResponse);
                        });
                    return deferred.promise;
                }
                categories= new Array();
                querySearch= (query:any) => {
                    var self = this;
                    var deferred = this.$q.defer();
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
                    return this.$http.get(this.RestUrlService.GET_CATEGORY_USER_FIELD_URL)
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
                    return this.accessControlService.hasEntityAccess(permissionsToCheck, entity, EntityAccessControlService.entityRoleTypes.CATEGORY);
                }
           // };

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
        static readonly $inject = ["$q","$http","RestUrlService","AccessControlService","EntityAccessControlService"];
        constructor(private $q:any, 
                    private $http:any, 
                    private RestUrlService:any,
                    private accessControlService:AccessControlService, 
                    private entityAccessControlService:EntityAccessControlService) {
                this.init();
    } // end of constructor
 }

