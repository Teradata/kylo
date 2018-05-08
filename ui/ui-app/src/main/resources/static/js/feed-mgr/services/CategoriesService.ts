
import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');

// export class CategoriesService {
     function CategoriesService ($q:any, $http:any, RestUrlService:any
        ,AccessControlService:any, EntityAccessControlService:any) {

        /**
         * internal cache of categories
         * @type {Array}
         */
        var categories:any = [];
        /**
         * the loading request for all categories
         * @type promise
         */
        var loadingRequest:any = null;

        var loading = false;
        /**
         * Create filter function for a query string
         */
        function createFilterFor(query:any) {
            var lowercaseQuery = angular.lowercase(query);
            return function filterFn(tag:any) {
                return (tag._lowername.indexOf(lowercaseQuery) === 0);
            };
        }

        function Builder() {
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

        function loadAll() {
            if (!loading) {
                loading = true;
                loadingRequest = $http.get(RestUrlService.CATEGORIES_URL).then(function (response:any) {
                    loading = false;
                    loadingRequest = null;
                    categories = response.data.map(function (category:any) {
                        category._lowername = category.name.toLowerCase();
                        category.createFeed = false;
                        //if under entity access control we need to check if the user has the "CREATE_FEED" permission associated with the selected category.
                        //if the user doesnt have this permission they cannot create feeds under this category
                        if (AccessControlService.isEntityAccessControlled()) {
                            if (AccessControlService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.CATEGORY.CREATE_FEED, category, "category")) {
                                category.createFeed = true;
                            }
                        }
                        else {
                            category.createFeed = true;
                        }
                        return category;
                    });
                    return categories;
                }, function (err:any) {
                    loading = false;
                });
                return loadingRequest;
            }
            else {
                if (loadingRequest != null) {
                    return loadingRequest;
                }
                else {
                    var defer = $q.defer();
                    defer.resolve(categories);
                    return defer.promise;
                }
            }
        }

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

            /**
             * Utility functions for managing categories.
             *
             * @type {Object}
             */
            var data = {
                /**
                 * Global category data used across directives.
                 *
                 * @type {CategoryModel}
                 */
                model: {},

                init: function () {
                    this.reload();
                },
                reload: function () {
                    var self = this;
                    return loadAll().then(function (categories:any) {
                        return self.categories = categories;
                    }, function (err:any) {
                    });
                },
                /**
                 * Adds/updates the category back to the cached list.
                 * returns true if successful, false if not
                 * @param savedCategory
                 * @return {boolean}
                 */
                update:function(savedCategory:any){
                    var self = this;
                    if(angular.isDefined(savedCategory.id)) {
                        var category:any = _.find(self.categories, function (category) {
                            return category.id == savedCategory.id;
                        });
                        savedCategory._lowername = savedCategory.name.toLowerCase();
                        savedCategory.createFeed = false;
                        //if under entity access control we need to check if the user has the "CREATE_FEED" permission associated with the selected category.
                        //if the user doesnt have this permission they cannot create feeds under this category
                        if (AccessControlService.isEntityAccessControlled()) {
                            if (AccessControlService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.CATEGORY.CREATE_FEED, savedCategory, "category")) {
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
                },
                delete: function (category:any) {
                    var promise = $http({
                        url: RestUrlService.CATEGORIES_URL + "/" + category.id,
                        method: "DELETE"
                    });
                    return promise;
                },
                save: function (category:any) {
                    //prepare access control changes if any
                    EntityAccessControlService.updateRoleMembershipsForSave(category.roleMemberships);
                    EntityAccessControlService.updateRoleMembershipsForSave(category.feedRoleMemberships);

                    var promise = $http({
                        url: RestUrlService.CATEGORIES_URL,
                        method: "POST",
                        data: angular.toJson(category),
                        headers: {
                            'Content-Type': 'application/json; charset=UTF-8'
                        }
                    });
                    return promise;
                },
                populateRelatedFeeds: function (category:any) {
                    var deferred = $q.defer();
                    this.getRelatedFeeds(category).then(function(response:any) {
                        category.relatedFeedSummaries = response.data || [];
                        deferred.resolve(category);
                    }, function(){
                        deferred.reject();
                    })
                    return deferred.promise;
                },
                getRelatedFeeds: function (category:any) {
                    return $http.get(RestUrlService.CATEGORIES_URL + "/" + category.id + "/feeds");
                },
                findCategory: function (id:any) {

                    var self = this;
                    var category:any = _.find(self.categories, function (category) {
                        return category.id == id;
                    });
                    return category;
                },
                findCategoryByName: function (name:any) {
                    if (name != undefined) {
                        var self = this;
                        var category:any = _.find(self.categories, function (category) {
                            return category.name.toLowerCase() == name.toLowerCase();
                        });
                        return category;
                    }
                    return null;
                },
                findCategoryBySystemName: function (systemName:any) {
                  if (systemName != undefined) {
                      var self = this;
                      var category:any = _.find(self.categories, function (category) {
                          return category.systemName == systemName;
                      });
                      return category;
                  }
                  return null;
                },
                getCategoryBySystemName:function(systemName:any){
                    var self = this;
                    var deferred = $q.defer();
                    var categoryCache = self.findCategoryBySystemName(systemName);
                    if(typeof categoryCache === 'undefined' || categoryCache === null) {
                        $http.get(RestUrlService.CATEGORY_DETAILS_BY_SYSTEM_NAME_URL(systemName))
                            .then(function (response:any) {
                                var categoryResponse = response.data;
                                deferred.resolve(categoryResponse);
                            });
                    }
                    else {
                        deferred.resolve(categoryCache);
                    }
                    return deferred.promise;
                },
                getCategoryById:function(categoryId:any) {
                    var deferred = $q.defer();
                    $http.get(RestUrlService.CATEGORY_DETAILS_BY_ID_URL(categoryId))
                        .then(function(response:any) {
                            var categoryResponse = response.data;
                            return deferred.resolve(categoryResponse);
                        });
                    return deferred.promise;
                },
                categories: new Array(),
                querySearch: function (query:any) {
                    var self = this;
                    var deferred = $q.defer();
                    if (self.categories.length == 0) {
                        loadAll().then(function (response:any) {
                            self.loading = false;
                            if (query) {
                                var results = response.filter(createFilterFor(query))
                                deferred.resolve(results);
                            }
                            else {
                                deferred.resolve(response);
                            }
                        }, function (err:any) {
                            self.loading = false;
                        });
                    }
                    else {
                        var results = query ? self.categories.filter(createFilterFor(query)) : self.categories;
                        deferred.resolve(results);
                    }
                    return deferred.promise;

                },

                /**
                 * Creates a new category model.
                 *
                 * @returns {CategoryModel} the new category model
                 */
                newCategory: function () {
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
                },

                /**
                 * Gets the user fields for a new category.
                 *
                 * @returns {Promise} for the user fields
                 */
                getUserFields: function () {
                    return $http.get(RestUrlService.GET_CATEGORY_USER_FIELD_URL)
                        .then(function (response:any) {
                            return response.data;
                        });
                },
                /**
                 * check if the user has access on an entity
                 * @param permissionsToCheck an Array or a single string of a permission/action to check against this entity and current user
                 * @param entity the entity to check. if its undefined it will use the current category in the model
                 * @returns {*} a promise, or a true/false.  be sure to wrap this with a $q().then()
                 */
                hasEntityAccess: function (permissionsToCheck:any, entity:any) {
                    if (entity == undefined) {
                        entity = data.model;
                    }
                    return AccessControlService.hasEntityAccess(permissionsToCheck, entity, EntityAccessControlService.entityRoleTypes.CATEGORY);
                }
            };

            //EntityAccessControlService.ENTITY_ACCESS.CHANGE_CATEGORY_PERMISSIONS
            data.init();
            return data;

    }
// }

angular.module(moduleName).factory('CategoriesService',["$q","$http","RestUrlService","AccessControlService","EntityAccessControlService",CategoriesService]);