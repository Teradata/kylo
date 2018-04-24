define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    // export class CategoriesService {
    var CategoriesService = /** @class */ (function () {
        //return this.data;
        function CategoriesService($q, $http, RestUrlService, AccessControlService, EntityAccessControlService) {
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
            this.$q = $q;
            this.$http = $http;
            this.RestUrlService = RestUrlService;
            this.AccessControlService = AccessControlService;
            this.EntityAccessControlService = EntityAccessControlService;
            this.loadAll = function () {
                var _this = this;
                if (!this.loading) {
                    this.loading = true;
                    this.loadingRequest = this.$http.get(this.RestUrlService.CATEGORIES_URL).then(function (response) {
                        _this.loading = false;
                        _this.loadingRequest = null;
                        _this.categories = response.data.map(function (category) {
                            category._lowername = category.name.toLowerCase();
                            category.createFeed = false;
                            //if under entity access control we need to check if the user has the "CREATE_FEED" permission associated with the selected category.
                            //if the user doesnt have this permission they cannot create feeds under this category
                            if (_this.AccessControlService.isEntityAccessControlled()) {
                                if (_this.AccessControlService.hasEntityAccess(_this.EntityAccessControlService.ENTITY_ACCESS.CATEGORY.CREATE_FEED, category, "category")) {
                                    category.createFeed = true;
                                }
                            }
                            else {
                                category.createFeed = true;
                            }
                            return category;
                        });
                        return _this.categories;
                    }, function (err) {
                        _this.loading = false;
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
            };
            /**
             * Create filter function for a query string
             */
            //  data: any= {
            /**
             * Global category data used across directives.
             *
             * @type {CategoryModel}
             */
            this.model = {};
            this.init = function () {
                this.reload();
            };
            this.reload = function () {
                //var self = this;
                return this.loadAll().then(function (categories) {
                    return categories = categories;
                }, function (err) {
                });
            };
            /**
             * Adds/updates the category back to the cached list.
             * returns true if successful, false if not
             * @param savedCategory
             * @return {boolean}
             */
            this.update = function (savedCategory) {
                var self = this;
                if (angular.isDefined(savedCategory.id)) {
                    var category = _.find(self.categories, function (category) {
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
                    if (angular.isDefined(category)) {
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
            };
            this.delete = function (category) {
                var promise = this.$http({
                    url: this.RestUrlService.CATEGORIES_URL + "/" + category.id,
                    method: "DELETE"
                });
                return promise;
            };
            this.save = function (category) {
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
            };
            this.populateRelatedFeeds = function (category) {
                var deferred = this.$q.defer();
                this.getRelatedFeeds(category).then(function (response) {
                    category.relatedFeedSummaries = response.data || [];
                    deferred.resolve(category);
                }, function () {
                    deferred.reject();
                });
                return deferred.promise;
            };
            this.getRelatedFeeds = function (category) {
                return this.$http.get(this.RestUrlService.CATEGORIES_URL + "/" + category.id + "/feeds");
            };
            this.findCategory = function (id) {
                var self = this;
                var category = _.find(self.categories, function (category) {
                    return category.id == id;
                });
                return category;
            };
            this.findCategoryByName = function (name) {
                if (name != undefined) {
                    var self = this;
                    var category = _.find(self.categories, function (category) {
                        return category.name.toLowerCase() == name.toLowerCase();
                    });
                    return category;
                }
                return null;
            };
            this.findCategoryBySystemName = function (systemName) {
                if (systemName != undefined) {
                    var self = this;
                    var category = _.find(self.categories, function (category) {
                        return category.systemName == systemName;
                    });
                    return category;
                }
                return null;
            };
            this.getCategoryBySystemName = function (systemName) {
                var self = this;
                var deferred = this.$q.defer();
                var categoryCache = self.findCategoryBySystemName(systemName);
                if (typeof categoryCache === 'undefined' || categoryCache === null) {
                    this.$http.get(this.RestUrlService.CATEGORY_DETAILS_BY_SYSTEM_NAME_URL(systemName))
                        .then(function (response) {
                        var categoryResponse = response.data;
                        deferred.resolve(categoryResponse);
                    });
                }
                else {
                    deferred.resolve(categoryCache);
                }
                return deferred.promise;
            };
            this.getCategoryById = function (categoryId) {
                var deferred = this.$q.defer();
                this.$http.get(this.RestUrlService.CATEGORY_DETAILS_BY_ID_URL(categoryId))
                    .then(function (response) {
                    var categoryResponse = response.data;
                    return deferred.resolve(categoryResponse);
                });
                return deferred.promise;
            };
            this.categories = new Array();
            this.querySearch = function (query) {
                var _this = this;
                var self = this;
                var deferred = this.$q.defer();
                if (self.categories.length == 0) {
                    this.loadAll().then(function (response) {
                        _this.loading = false;
                        if (query) {
                            var results = response.filter(_this.createFilterFor(query));
                            deferred.resolve(results);
                        }
                        else {
                            deferred.resolve(response);
                        }
                    }, function (err) {
                        _this.loading = false;
                    });
                }
                else {
                    var results = query ? self.categories.filter(this.createFilterFor(query)) : self.categories;
                    deferred.resolve(results);
                }
                return deferred.promise;
            };
            /**
             * Creates a new category model.
             *
             * @returns {CategoryModel} the new category model
             */
            this.newCategory = function () {
                var data = {
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
                };
                return data;
            };
            /**
             * Gets the user fields for a new category.
             *
             * @returns {Promise} for the user fields
             */
            this.getUserFields = function () {
                return this.$http.get(this.RestUrlService.GET_CATEGORY_USER_FIELD_URL)
                    .then(function (response) {
                    return response.data;
                });
            };
            /**
             * check if the user has access on an entity
             * @param permissionsToCheck an Array or a single string of a permission/action to check against this entity and current user
             * @param entity the entity to check. if its undefined it will use the current category in the model
             * @returns {*} a promise, or a true/false.  be sure to wrap this with a $q().then()
             */
            this.hasEntityAccess = function (permissionsToCheck, entity) {
                if (entity == undefined) {
                    entity = this.data.model;
                }
                return this.AccessControlService.hasEntityAccess(permissionsToCheck, entity, this.EntityAccessControlService.entityRoleTypes.CATEGORY);
            };
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
            this.loadingRequest = null;
            this.loading = false;
            this.init();
        } // end of constructor
        CategoriesService.prototype.createFilterFor = function (query) {
            var lowercaseQuery = angular.lowercase(query);
            return function filterFn(tag) {
                return (tag._lowername.indexOf(lowercaseQuery) === 0);
            };
        };
        CategoriesService.prototype.Builder = function () {
            var builder = {
                name: function (name) {
                    builder._name = name;
                    return this;
                },
                description: function (desc) {
                    builder._description = desc;
                    return this;
                },
                relatedFeeds: function (related) {
                    builder._relatedFeeds = related || 0;
                    return this;
                },
                icon: function (icon) {
                    builder._icon = icon;
                    return this;
                },
                iconColor: function (color) {
                    builder._iconColor = color;
                    return this;
                },
                build: function () {
                    var category = {};
                    category.name = this._name;
                    category.description = this._description;
                    category.icon = this._icon;
                    category.relatedFeeds = this._relatedFeeds;
                    category.iconColor = this._iconColor;
                    return category;
                }
            };
            return builder;
        };
        return CategoriesService;
    }());
    exports.default = CategoriesService;
    angular.module(moduleName).factory('CategoriesService', ["$q", "$http", "RestUrlService", "AccessControlService", "EntityAccessControlService",
        function ($q, $http, RestUrlService, AccessControlService, EntityAccessControlService) { return new CategoriesService($q, $http, RestUrlService, AccessControlService, EntityAccessControlService); }]);
});
//# sourceMappingURL=CategoriesService.js.map