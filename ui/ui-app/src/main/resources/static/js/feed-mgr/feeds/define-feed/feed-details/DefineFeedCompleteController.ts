import * as angular from 'angular';
import * as _ from "underscore";
import {Transition} from "@uirouter/core";
const moduleName = require('../module-name');
import '../../module-require';
import '../module-require';


export class DefineFeedCompleteController {

    model: any;
    error: any;
    isValid: any;
    $transition$: Transition;

    static readonly $inject = ["$scope","$q","$http","$mdToast","RestUrlService","FeedService","StateService"];

    constructor(private $scope: IScope, private $q: angular.IQService, private $http: angular.IHttpService, private $mdToast: angular.material.IToastService, private RestUrlService: any, private FeedService: any, private StateService: any) {
        
        this.model = this.$transition$.params().feedModel;
        this.error = this.$transition$.params().error;

        this.isValid = this.error == null;
             
    };
 
    /**
         * Gets the feed id from the FeedService
         * @returns {*}
    */
    getFeedId() {
        var feedId = this.model != null ? this.model.id : null;
        if (feedId == null && this.FeedService.createFeedModel != null) {
            feedId = this.FeedService.createFeedModel.id;
        }
        if (feedId == null && this.FeedService.editFeedModel != null) {
            feedId = this.FeedService.editFeedModel.id;
        }
        return feedId;
    }
    /**
        * Navigate to the Feed Details first tab
    */
    onViewDetails() {
        var feedId = this.getFeedId();
        this.StateService.FeedManager().Feed().navigateToFeedDetails(feedId, 0);
    }
    gotIt() {
        this.onViewFeedsList();
    }

    /**
         * Navigate to the Feed List page
         */
    onViewFeedsList() {
        this.FeedService.resetFeed();
        this.StateService.FeedManager().Feed().navigateToFeeds();
    }

    /**
         * Navigate to the Feed Details SLA tab
    */
    onAddServiceLevelAgreement() {
        //navigate to Feed Details and move to the 3 tab (SLA)
        var feedId = this.getFeedId();
        this.StateService.FeedManager().Feed().navigateToFeedDetails(feedId, 3);
    }
}

const module = angular.module(moduleName)
    .component('thinkbigDefineFeedCompleteController', {
        bindings: {
            $transition$: '<'
        },
        templateUrl: './define-feed-complete.html',
        controllerAs: 'vm',
        controller: DefineFeedCompleteController,
    });
export default module;