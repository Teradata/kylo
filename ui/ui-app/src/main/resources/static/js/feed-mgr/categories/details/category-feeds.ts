import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('../module-name');

export class CategoryFeedsController {

    model:any;
     /**
     * Manages the Related Feeds section of the Category Details page.
     *
     * @constructor
     * @param $scope the application model
     * @param CategoriesService the category service
     * @param StateService the URL service
     */
    constructor(private $scope:IScope, private CategoriesService:any, private StateService:any) {
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
    onFeedClick (feed:any) {
        this.StateService.FeedManager().Feed().navigateToFeedDetails(feed.id);
    }
}
angular.module(moduleName).component('thinkbigCategoryFeeds',{
    controller: CategoryFeedsController,
    controllerAs: "vm",
    templateUrl: "./category-feeds.html"
});