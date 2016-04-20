/**
 * ui-router service.  Controllers that link/navigate to other controllers/pages use this service.
 * See the corresponding name references in app.js
 */
angular.module(MODULE_FEED_MGR).factory('CategoriesService', function ($q,$http, RestUrlService) {


    /**
     * Create filter function for a query string
     */
    function createFilterFor(query) {
        var lowercaseQuery = angular.lowercase(query);
        return function filterFn(tag) {
            return (tag._lowername.indexOf(lowercaseQuery) === 0);
        };
    }
    function Builder(){
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
            iconColor:function(color){
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


    function  loadAll() {
       return $http.get(RestUrlService.CATEGORIES_URL).then(function (response) {
          return response.data.map(function(category){
               category._lowername = category.name.toLowerCase();
               return category;
           });
        });
    }



    var data = {
        init:function() {
           this.reload();
        },
        reload:function(){
            var self = this;
            var promise = loadAll();
            $q.when(promise).then(function(categories)
            {
                self.categories = categories;
            });
            return promise;
        },
        delete:function(category){
            var promise = $http({
                url: RestUrlService.CATEGORIES_URL+"/"+category.id,
                method: "DELETE"
            });
            return promise;
        },
        save:function(category){

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
        getRelatedFeeds:function(category){
            var promise = $http.get(RestUrlService.CATEGORIES_URL+"/"+category.id+"/feeds").then(function(response){
                category.relatedFeedSummaries = response.data || [];
            });
            return promise;
        },
        findCategory:function(id) {

            var self = this;
          var category = _.find(self.categories,function(category) {
              return category.id == id;
          });
            return category;
        },
        findCategoryByName:function(name) {
        if(name != undefined) {
            var self = this;
            var category = _.find(self.categories, function (category) {
                return category.name.toLowerCase() == name.toLowerCase();
            });
            return category;
        }
            return null;
        },
        categories:[],
        simulateQuery:false,
        querySearch: function (query) {
            var self = this;
            var results = query ? self.categories.filter( createFilterFor(query) ) : self.categories,
                deferred;
            if (self.simulateQuery) {
                deferred = $q.defer();
                $timeout(function () { deferred.resolve( results ); }, Math.random() * 1000, false);
                return deferred.promise;
            } else {
                return results;
            }
        },
        newCategory:function(){
            return {name:'',description:'',icon:'',iconColor:''};
        }

    };
    data.init();
    return data;



});