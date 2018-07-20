import * as angular from 'angular';
import * as _ from "underscore";
import { Component } from '@angular/core';
import {CategoriesService} from '../../services/CategoriesService';
import StateService from '../../../services/StateService';
// const moduleName = require('feed-mgr/categories/module-name');

@Component({
    selector: 'thinkbig-category-feeds',
    templateUrl: 'js/feed-mgr/categories/details/category-feeds.html'
})
export class CategoryFeedsController {

    model:any;

    ngOnInit() {
        /**
         * Category data.
         * @type {CategoryModel}
         */
        this.model = this.CategoriesService.model;
    }
     /**
     * Manages the Related Feeds section of the Category Details page.
     *
     * @constructor
     * @param CategoriesService the category service
     * @param StateService the URL service
     */
    constructor(private CategoriesService:CategoriesService, 
                private StateService:StateService) {}

    /**
    * Navigates to the specified feed.
    *
    * @param {Object} feed the feed to navigate to
    */
    onFeedClick = (feed:any) => {
        this.StateService.FeedManager().Feed().navigateToFeedDetails(feed.id);
    }
}