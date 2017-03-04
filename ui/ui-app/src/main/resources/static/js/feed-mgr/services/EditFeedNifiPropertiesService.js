/**
 * Used to store temporary state of the Edit Feed Nifi Properties
 * when a user clicks the Edit link for the Feed Details so the object can be passed to the template factory
 *
 */
define(['angular','feed-mgr/module-name'], function (angular,moduleName) {
    angular.module(moduleName).service('EditFeedNifiPropertiesService', function () {

        var self = this;
        this.editFeedModel = {};

    });
});