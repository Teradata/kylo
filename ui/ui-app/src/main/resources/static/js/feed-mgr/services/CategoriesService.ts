
import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');

// export class CategoriesService {
    export default class CategoriesService {
          loadAll= function() {
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
                        if (this.AccessControlService.isEntityAccessControlled()) {
                            if (this.AccessControlService.hasEntityAccess(this.EntityAccessControlService.ENTITY_ACCESS.CATEGORY.CREATE_FEED, category, "category")) {
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
                init= function() {
                    this.reload();
                }
                reload=function() {
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
                update= function(savedCategory:any){
                    var self = this;
                    if(angular.isDefined(savedCategory.id)) {
                        var category:any = _.find(self.categories, (category: any)=>{
                            return category.id == savedCategory.id;
                        });
                        savedCategory._lowername = savedCategory.name.toLowerCase();
                        savedCategory.createFeed = false;
                        //if under entity access control we need to check if the user has the "CREATE_FEED" permission associated with the selected category.
                        //if the user doesnt have this permission they cannot create feeds under this category
                        if (this.AccessControlService.isEntityAccessControlled()) {
                            if (this.AccessControlService.hasEntityAccess(this.EntityAccessControlService.ENTITY_ACCESS.CATEGORY.CREATE_FEED, savedCategory, "category")) {
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
                delete= function (category:any) {
                    var promise = this.$http({
                        url: this.RestUrlService.CATEGORIES_URL + "/" + category.id,
                        method: "DELETE"
                    });
                    return promise;
                }
                save= function (category:any) {
                    //prepare access control changes if any
                    this.EntityAccessControlService.updateRoleMembershipsForSave(category.roleMemberships);
                    this.EntityAccessControlService.updateRoleMembershipsForSave(category.feedRoleMemberships);
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
                populateRelatedFeeds= function (category:any) {
                    var deferred = this.$q.defer();
                    this.getRelatedFeeds(category).then((response:any)=>{
                        category.relatedFeedSummaries = response.data || [];
                        deferred.resolve(category);
                    }, ()=>{
                        deferred.reject();
                    })
                    return deferred.promise;
                }
                getRelatedFeeds= function (category:any) {
                    return this.$http.get(this.RestUrlService.CATEGORIES_URL + "/" + category.id + "/feeds");
                }
                findCategory= function (id:any) {
                    var self = this;
                    var category:any = _.find(self.categories, (category: any)=> {
                        return category.id == id;
                    });
                    return category;
                }
                findCategoryByName= function (name:any) {
                    if (name != undefined) {
                        var self = this;
                        var category:any = _.find(self.categories, (category: any)=> {
                            return category.name.toLowerCase() == name.toLowerCase();
                        });
                        return category;
                    }
                    return null;
                }
                findCategoryBySystemName= function (systemName:any) {
                  if (systemName != undefined) {
                      var self = this;
                      var category:any = _.find(self.categories, (category: any)=> {
                          return category.systemName == systemName;
                      });
                      return category;
                  }
                  return null;
                }
                getCategoryBySystemName=function(systemName:any){
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
                getCategoryById=function(categoryId:any) {
                    var deferred = this.$q.defer();
                    this.$http.get(this.RestUrlService.CATEGORY_DETAILS_BY_ID_URL(categoryId))
                        .then((response:any)=>{
                            var categoryResponse = response.data;
                            return deferred.resolve(categoryResponse);
                        });
                    return deferred.promise;
                }
                categories= new Array();
                querySearch= function (query:any) {
                    var self = this;
                    var deferred = this.$q.defer();
                    if (self.categories.length == 0) {
                        this.loadAll().then((response:any)=>{
                            this.loading = false;
                            if (query) {
                                var results = response.filter(this.createFilterFor(query))
                                deferred.resolve(results);
                            }
                            else {
                                deferred.resolve(response);
                            }
                        }, (err:any)=> {
                            this.loading = false;
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
                newCategory= function () {
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
                getUserFields= function () {
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
                hasEntityAccess= function (permissionsToCheck:any, entity:any) {
                    if (entity == undefined) {
                        entity = this.data.model;
                    }
                    return this.AccessControlService.hasEntityAccess(permissionsToCheck, entity, this.EntityAccessControlService.entityRoleTypes.CATEGORY);
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
    //return this.data;
      
        constructor(private $q:any, 
                    private $http:any, 
                    private RestUrlService:any,
                    private AccessControlService:any, 
                    private EntityAccessControlService:any) {

            //EntityAccessControlService.ENTITY_ACCESS.CHANGE_CATEGORY_PERMISSIONS
            /**
             * Utility functions for managing categories.
             *
             * @type {Object}
             */
            /**
             * A category for grouping similar feeds.
             *
             * @typedef {Object} CategoryModel
             * @property {string|null} id the unique identifier
             * @property {string|null} name a human-readable name
             * @property {string|null} description a sentence describing the category
             * @property {string|null} icon the name of a Material Design icon
             * @property {string|null} iconColor the color of the icon
             * @property {Object.<string,string>} userProperties map of user-defined property name to value
             * @property {Array<Object>} relatedFeedSummaries the feeds within this category
             */

           this.init();
    } // end of constructor
 }

angular.module(moduleName).factory('CategoriesService',["$q","$http","RestUrlService","AccessControlService","EntityAccessControlService",
                                    ($q: any,$http: any, 
                                    RestUrlService: any,
                                    AccessControlService: any , 
                                    EntityAccessControlService: any)=> new CategoriesService($q, 
                                    $http, 
                                    RestUrlService,
                                    AccessControlService, 
                                    EntityAccessControlService)]);