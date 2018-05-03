define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/categories/module-name');
    var CategoryFeedsController = /** @class */ (function () {
        /**
        * Manages the Related Feeds section of the Category Details page.
        *
        * @constructor
        * @param $scope the application model
        * @param CategoriesService the category service
        * @param StateService the URL service
        */
        function CategoryFeedsController($scope, CategoriesService, StateService) {
            this.$scope = $scope;
            this.CategoriesService = CategoriesService;
            this.StateService = StateService;
            /**
             * Category data.
             * @type {CategoryModel}
             */
            this.model = CategoriesService.model;
        }
        /**
        * Navigates to the specified feed.
        *
        * @param {Object} feed the feed to navigate to
        */
        CategoryFeedsController.prototype.onFeedClick = function (feed) {
            this.StateService.FeedManager().Feed().navigateToFeedDetails(feed.id);
        };
        return CategoryFeedsController;
    }());
    exports.CategoryFeedsController = CategoryFeedsController;
    angular.module(moduleName).component('thinkbigCategoryFeeds', {
        controller: CategoryFeedsController,
        controllerAs: "vm",
        templateUrl: "js/feed-mgr/categories/details/category-feeds.html"
    });
});
//# sourceMappingURL=category-feeds.js.map