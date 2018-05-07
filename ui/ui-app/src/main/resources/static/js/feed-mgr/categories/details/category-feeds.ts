import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/categories/module-name');

    /**
     * Creates a directive for the Related Feeds section.
     *
     * @returns {Object} the directive
     */
    function thinkbigCategoryFeeds() {
        return {
            controller: "CategoryFeedsController",
            controllerAs: "vm",
            restrict: "E",
            scope: {},
            templateUrl: "js/feed-mgr/categories/details/category-feeds.html"
        };
    }


export class CategoryFeedsController {

    model:any;
    onFeedClick:any;
     /**
     * Manages the Related Feeds section of the Category Details page.
     *
     * @constructor
     * @param $scope the application model
     * @param CategoriesService the category service
     * @param StateService the URL service
     */
    constructor(private $scope:any, private CategoriesService:any, private StateService:any) {
        var self = this;

        /**
         * Category data.
         * @type {CategoryModel}
         */
        self.model = CategoriesService.model;

        /**
         * Navigates to the specified feed.
         *
         * @param {Object} feed the feed to navigate to
         */
        self.onFeedClick = function(feed:any) {
            StateService.FeedManager().Feed().navigateToFeedDetails(feed.id);
        };
    }


}

angular.module(moduleName).controller('CategoryFeedsController', ["$scope","CategoriesService","StateService",CategoryFeedsController]);
angular.module(moduleName).directive('thinkbigCategoryFeeds', thinkbigCategoryFeeds);
