define(['angular','feed-mgr/module-name','constants/AccessConstants'], function (angular,moduleName) {
    angular.module(moduleName).factory('CategoriesService',["$q","$http","RestUrlService","AccessControlService","EntityAccessControlService", function ($q, $http, RestUrlService,AccessControlService,EntityAccessControlService) {

        /**
         * Create filter function for a query string
         */
        function createFilterFor(query) {
            var lowercaseQuery = angular.lowercase(query);
            return function filterFn(tag) {
                return (tag._lowername.indexOf(lowercaseQuery) === 0);
            };
        }

        function Builder() {
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
            }
            return builder;
        }

        function loadAll() {
            return $http.get(RestUrlService.CATEGORIES_URL).then(function (response) {
                return response.data.map(function (category) {
                    category._lowername = category.name.toLowerCase();
                    category.createFeed = false;
                    //if under entity access control we need to check if the user has the "CREATE_FEED" permission associated with the selected category.
                    //if the user doesnt have this permission they cannot create feeds under this category
                    if(AccessControlService.isEntityAccessControlled()) {
                        if (AccessControlService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.CATEGORY.CREATE_FEED, category, "category")) {
                            category.createFeed = true;
                        }
                    }
                    else {
                        category.createFeed = true;
                    }
                    return category;
                });
            });
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
                return loadAll().then(function (categories) {
                    return self.categories = categories;
                });
            },
            delete: function (category) {
                var promise = $http({
                    url: RestUrlService.CATEGORIES_URL + "/" + category.id,
                    method: "DELETE"
                });
                return promise;
            },
            save: function (category) {
                //prepare access control changes if any
                EntityAccessControlService.updateEntityForSave(category);

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
            getRelatedFeeds: function (category) {
                var promise = $http.get(RestUrlService.CATEGORIES_URL + "/" + category.id + "/feeds").then(function (response) {
                    category.relatedFeedSummaries = response.data || [];
                });
                return promise;
            },
            findCategory: function (id) {

                var self = this;
                var category = _.find(self.categories, function (category) {
                    return category.id == id;
                });
                return category;
            },
            findCategoryByName: function (name) {
                if (name != undefined) {
                    var self = this;
                    var category = _.find(self.categories, function (category) {
                        return category.name.toLowerCase() == name.toLowerCase();
                    });
                    return category;
                }
                return null;
            },
            categories: [],
            querySearch: function (query) {

                var self = this;
                var deferred = $q.defer();
                if (self.categories.length == 0) {
                    loadAll().then(function (response) {
                        if (query) {
                            var results = response.filter(createFilterFor(query))
                            deferred.resolve(results);
                        }
                        else {
                            deferred.resolve(response);
                        }
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
                return {id: null,
                        name: null,
                        description: null,
                        icon: null, iconColor: null,
                        userFields: [],
                        userProperties: [],
                        relatedFeedSummaries: [],
                        securityGroups: [],
                        roleMemberships:[],
                        owner:null};
            },

            /**
             * Gets the user fields for a new category.
             *
             * @returns {Promise} for the user fields
             */
            getUserFields: function () {
                return $http.get(RestUrlService.GET_CATEGORY_USER_FIELD_URL)
                    .then(function (response) {
                        return response.data;
                    });
            },
            /**
             * check if the user has access on an entity
             * @param permissionsToCheck an Array or a single string of a permission/action to check against this entity and current user
             * @param entity the entity to check. if its undefined it will use the current category in the model
             * @returns {*} a promise, or a true/false.  be sure to wrap this with a $q().then()
             */
            hasEntityAccess:function(permissionsToCheck,entity) {
                if(entity == undefined){
                    entity = data.model;
                }
                return  AccessControlService.hasEntityAccess(permissionsToCheck,entity,EntityAccessControlService.entityTypes.CATEGORY);
            }
        };

        //EntityAccessControlService.ENTITY_ACCESS.CHANGE_CATEGORY_PERMISSIONS
        data.init();
        return data;

    }]);
});