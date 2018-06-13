import * as angular from 'angular';
import * as _ from "underscore";
import AccessControlService from '../../../services/AccessControlService';
import { RegisterTemplateServiceFactory } from '../../services/RegisterTemplateServiceFactory';
import { EntityAccessControlService } from '../../shared/entity-access-control/EntityAccessControlService';
import { EntityAccessControlDialogService } from '../../shared/entity-access-control/EntityAccessControlDialogService';
import { Transition } from '@uirouter/core';
const moduleName = require('feed-mgr/feeds/edit-feed/module-name');


var FeedUploadFileDialogController = function ($scope:any, $mdDialog:any, $http:any, RestUrlService:any
    , FileUpload:any, feedId:any){
    var self = this;
    $scope.uploading = false;
    $scope.uploadFiles = null;
    /**
     * Upload file
     */
    $scope.doUpload = function() {
 
        $scope.uploading = true;
        $scope.errorMessage = '';
        var uploadUrl = RestUrlService.UPLOAD_FILE_FEED_URL(feedId);
        var params = {};
        var successFn = function (response:any) {
            $scope.uploading = false;
            $mdDialog.hide('Upload successfully submitted.');
        }
        var errorFn = function (data:any) {
            $scope.uploading = false;
            $scope.errorMessage = 'Failed to submit file.';
        }
        FileUpload.uploadFileToUrl($scope.uploadFiles, uploadUrl, successFn, errorFn, params);
    };
    $scope.hide = function() {
        $mdDialog.hide();
    };
 
    $scope.cancel = function() {
        $mdDialog.cancel();
    };

}




const SLA_INDEX = 3;

export class FeedDetailsController {
    
    /**
    * Flag to indicate style of page
    * if true it will fit the card to the 980px width
    * if false it will make it go 100% width
    * @type {boolean}
    */
    cardWidth : boolean = true;
    /**
    * Indicates if admin operations are allowed.
    * @type {boolean}
    */
    allowAdmin : boolean = false;
    /**
    * Allow the Changing of this feeds permissions
    * @type {boolean}
    */
    allowChangePermissions : boolean = false;
    /**
    * Indicates if edit operations are allowed.
    * @type {boolean}
    */
    allowEdit : boolean = false;
    /**
    * Indicates if export operations are allowed.
    * @type {boolean}
    */
    allowExport : boolean = false;
    /**
    \* Indicates if starting a feed is allowed.
    * @type {boolean}
    */
    allowStart : boolean = false;
    /**
    * Alow user to access the sla tab
    * @type {boolean}
    */
    allowSlaAccess : boolean = false;
    feedId: any = null;
    selectedTabIndex: number = 0;
    loadingFeedData: boolean = false;
    model: any;
    loadMessage: string = '';
    uploadFile: any = null;
    uploading: boolean = false;
    uploadAllowed: boolean = false;
    /**
    * flag to indicate the feed could not be loaded
    * @type {boolean}
    */
    errorLoadingFeed: boolean = false;
    /** flag to indicate if we get a valid connection back from NiFi.  Initially to true. it will be rechecked on load **/
    isNiFiRunning : boolean = true;
    /**
    * flag to indicate if the SLA page should be set to empty new form rather than the list
    * Used for when the "Add SLA" button is clicked
    * @type {boolean}
    */
    newSla : boolean = false;
    disabling: any;
    enabling: any;
    exportFeedUrl: any;
    feedNavigationLinks: any;
    startingFeed: boolean;
    requestedTabIndex : any;
    $transition$: Transition;

    static readonly $inject = ["$scope", "$q", "$mdDialog", "$mdToast", "$http", "$state", 
    "AccessControlService", "RestUrlService", "FeedService", "RegisterTemplateService", "StateService",
    "SideNavService", "FileUpload", "ConfigurationService", "EntityAccessControlDialogService", 
    "EntityAccessControlService", "UiComponentsService", "AngularModuleExtensionService", "DatasourcesService"];

    /**
     * 
     * Displays the details for a feed.
     *
     * @param $scope
     * @param $q
     * @param $transition$.params()
     * @param $mdDialog
     * @param $mdToast
     * @param $http
     * @param $state
     * @param {AccessControlService} AccessControlService the access control service
     * @param RestUrlService
     * @param FeedService
     * @param RegisterTemplateService
     * @param StateService
     */
    constructor(private $scope: any, private $q: angular.IQService, private $mdDialog: angular.material.IDialogService, private $mdToast: angular.material.IToastService
        , private $http: any, private $state: any, private accessControlService: AccessControlService, private RestUrlService: any
        , private FeedService: any, private registerTemplateService: RegisterTemplateServiceFactory, private StateService: any
        , private SideNavService: any, private FileUpload: any, private ConfigurationService: any
        , private entityAccessControlDialogService: EntityAccessControlDialogService, private entityAccessControlService: EntityAccessControlService, private UiComponentsService: any
        , private AngularModuleExtensionService: any, private DatasourcesService: any) {


        this.model = FeedService.editFeedModel = {};
        this.model.loaded = false;

        this.feedNavigationLinks = AngularModuleExtensionService.getFeedNavigation();

        this.requestedTabIndex = this.$transition$.params().tabIndex;

        $scope.$watch( () => {
            return this.selectedTabIndex;
        }, (newVal: any) => {
            //reset display of feed versions
            FeedService.resetVersionFeedModel();

            //Make the Lineage tab fit without side nav
            //open side nav if we are not navigating between lineage links
            if (newVal == 2 || (this.requestedTabIndex != undefined && this.requestedTabIndex == 2)) {
                SideNavService.hideSideNav();
                this.cardWidth = false;
                this.requestedTabIndex = 0;
            }
            else {
                SideNavService.showSideNav();
                this.cardWidth = true;
            }

        })

        this.init();
    };
    init = () => {
        this.feedId = this.$transition$.params().feedId;

        this.exportFeedUrl = this.RestUrlService.ADMIN_EXPORT_FEED_URL + "/" + this.feedId

        this.loadFeed(this.requestedTabIndex);
        this.nifiRunningCheck();
    };

    cloneFeed = () => {
        this.StateService.FeedManager().Feed().navigateToCloneFeed(this.model.feedName);
    }

    /**
     * Displays a confirmation dialog for deleting the feed.
     */
    confirmDeleteFeed = () => {
        if (this.allowAdmin) {
            // Verify there are no dependent feeds
            if (angular.isArray(this.model.usedByFeeds) && this.model.usedByFeeds.length > 0) {
                var list = "<ul>";
                list += _.map(this.model.usedByFeeds, (feed: any) => {
                    return "<li>" + _.escape(feed.feedName) + "</li>";
                });
                list += "</ul>";

                var alert = this.$mdDialog.alert()
                    .parent($("body"))
                    .clickOutsideToClose(true)
                    .title("Feed is referenced")
                    .htmlContent("This feed is referenced by other feeds and cannot be deleted. The following feeds should be deleted first: " + list)
                    .ariaLabel("feed is referenced")
                    .ok("Got it!");
                    this.$mdDialog.show(alert);

                return;
            }

            // Display delete dialog
            var $dialogScope = this.$scope.$new();
            $dialogScope.dialog = this.$mdDialog;
            $dialogScope.vm = self;

            this.$mdDialog.show({
                escapeToClose: false,
                fullscreen: true,
                parent: angular.element(document.body),
                scope: $dialogScope,
                templateUrl: "js/feed-mgr/feeds/edit-feed/feed-details-delete-dialog.html"
            });
        }
    };

    /**
     * Permanently deletes this feed.
     */
    deleteFeed = () => {
        // Update model state
        this.model.state = "DELETED";

        // Delete the feed
        var successFn = () => {
            this.$state.go("feeds");
        };
        var errorFn = (response: any) => {
            // Update model state
            this.model.state = "DISABLED";

            // Display error message
            var msg = "<p>The feed cannot be deleted at this time.</p><p>";
            msg += angular.isString(response.data.message) ? _.escape(response.data.message) : "Please try again later.";
            msg += "</p>";

            this.$mdDialog.hide();
            this.$mdDialog.show(
                this.$mdDialog.alert()
                    .ariaLabel("Error deleting feed")
                    .clickOutsideToClose(true)
                    .htmlContent(msg)
                    .ok("Got it!")
                    .parent(document.body)
                    .title("Error deleting feed")
            );
        };

        this.$http.delete(this.RestUrlService.GET_FEEDS_URL + "/" + this.feedId).then(successFn, errorFn);
    };

    feedNavigationLink = (link: any) => {
        var feedId = this.feedId;
        var feedName = this.model.systemCategoryName + "." + this.model.systemFeedName;
        this.$state.go(link.sref, { feedId: feedId, feedName: feedName, model: this.model });
    }

    showFeedUploadDialog = () => {
        this.$mdDialog.show({
            controller: 'FeedUploadFileDialogController',
            escapeToClose: false,
            fullscreen: true,
            parent: angular.element(document.body),
            templateUrl: "js/feed-mgr/feeds/edit-feed/feed-details-upload-dialog.html",
            locals: { feedId: this.feedId }
        }).then((msg: any) => {
            this.$mdToast.show(
                this.$mdToast.simple()
                    .textContent('File uploaded.')
                    .hideDelay(3000)
            );
        });
    }

    showAccessControlDialog = () => {

        var onCancel = () => {

        };

        var onSave = () => {
        };

        this.entityAccessControlDialogService.showAccessControlDialog(this.model, "feed", this.model.feedName, onSave, onCancel);

    }


    openFeedMenu = ($mdOpenMenu: any, ev: any) =>{
        $mdOpenMenu(ev);
    };


    /**
     * Enables this feed.
     */
    enableFeed = () => {
        if (!this.enabling && this.allowEdit) {
            this.enabling = true;
            this.$http.post(this.RestUrlService.ENABLE_FEED_URL(this.feedId)).then( (response: any) => {
                this.model.state = response.data.state;
                this.FeedService.updateEditModelStateIcon();
                this.enabling = false;
            }, () => {
                this.$mdDialog.show(
                    this.$mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("NiFi Error")
                        .textContent("The feed could not be enabled.")
                        .ariaLabel("Cannot enable feed.")
                        .ok("OK")
                );
                this.enabling = false;
            });
        }
    };

    /**
     * Disables this feed.
     */
    disableFeed = () => {
        if (!this.disabling && this.allowEdit) {
            this.disabling = true;
            this.$http.post(this.RestUrlService.DISABLE_FEED_URL(this.feedId)).then((response: any) => {
                this.model.state = response.data.state;
                this.FeedService.updateEditModelStateIcon();
                this.disabling = false;
            }, () => {
                this.$mdDialog.show(
                    this.$mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("NiFi Error")
                        .textContent("The feed could not be disabled.")
                        .ariaLabel("Cannot disable feed.")
                        .ok("OK")
                );
                this.disabling = false;
            });
        }
    };

    /**
     * Starts this feed.
     */
    startFeed = () => {
        if (!this.startingFeed && this.allowStart) {
            this.startingFeed = true;
            this.$http.post(this.RestUrlService.START_FEED_URL(this.feedId)).then((response: any) => {
                let msg = "Feed started";
                if (response && response.data && response.data.message) {
                    msg = response.data.message;
                }
                this.$mdToast.show(
                    this.$mdToast.simple()
                        .textContent(msg)
                        .hideDelay(3000)
                );
                this.startingFeed = false;
            }, (response: any) => {
                let msg = "The feed could not be started.";
                if (response && response.data && response.data.message) {
                    msg += "<br/><br/>" + response.data.message;
                }
                console.error("Unable to start the feed ", response);
                this.$mdDialog.show(
                    this.$mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Error starting the feed")
                        .htmlContent(msg)
                        .ariaLabel("Cannot start feed.")
                        .ok("OK")
                );
                this.startingFeed = false;
            });
        }
    };

    mergeTemplateProperties = (feed: any) => {
        var successFn = (response: any) => {
            return response;
        }
        var errorFn = (err: any) => {

        }

        var promise = this.$http({
            url: this.RestUrlService.MERGE_FEED_WITH_TEMPLATE(feed.id),
            method: "POST",
            data: angular.toJson(feed),
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }
        }).then(successFn, errorFn);

        return promise;
    }

    /**
     * Navigates to the category details page for this feed's category.
     *
     * An error is displayed if the user does not have permissions to access categories.
     */
    onCategoryClick = () => {
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                if (this.accessControlService.hasAction(AccessControlService.CATEGORIES_ACCESS, actionSet.actions)) {
                    this.StateService.FeedManager().Category().navigateToCategoryDetails(this.model.category.id);
                } else {
                    this.$mdDialog.show(
                        this.$mdDialog.alert()
                            .clickOutsideToClose(true)
                            .title("Access Denied")
                            .textContent("You do not have permissions to access categories.")
                            .ariaLabel("Access denied for categories")
                            .ok("OK")
                    );
                }
            });
    };

    onTableClick = () => {
        this.StateService.FeedManager().Table().navigateToTable(this.DatasourcesService.getHiveDatasource().id, this.model.category.systemName, this.model.table.tableSchema.name);
    }

    addSla = () => {
        this.selectedTabIndex = SLA_INDEX;
        this.newSla = true;
    }

    updateMenuOptions = () => {
        this.uploadAllowed = false;
        var model = this.model;
        if (model && model.inputProcessor && model.inputProcessor.allProperties.length > 0) {
            angular.forEach(model.inputProcessor.allProperties, (property) => {
                if (property.processorType == 'org.apache.nifi.processors.standard.GetFile') {
                    this.uploadAllowed = true;
                    return;
                }
            });
        }
    }

    loadFeed = (tabIndex: any) => {
        this.errorLoadingFeed = false;
        this.loadingFeedData = true;
        this.model.loaded = false;
        this.loadMessage = '';
        var successFn = (response: any) => {
            if (response.data) {
                var promises = {
                    feedPromise: this.mergeTemplateProperties(response.data),
                    processorTemplatesPromise: this.UiComponentsService.getProcessorTemplates()
                };

                this.$q.all(promises).then((result: any) => {


                    //deal with the feed data
                    var updatedFeedResponse = result.feedPromise;
                    //merge in the template properties
                    //this will update teh this.model as they point to the same object
                    if (updatedFeedResponse == undefined || updatedFeedResponse.data == undefined) {
                        this.loadingFeedData = false;
                        var loadMessage = 'Unable to load Feed Details.  Please ensure that Apache Nifi is up and running and then refresh this page.';
                        this.loadMessage = loadMessage;
                        this.$mdDialog.show(
                            this.$mdDialog.alert()
                                //   .parent(angular.element(document.querySelector('#popupContainer')))
                                .clickOutsideToClose(true)
                                .title('Unable to load Feed Details')
                                .textContent(loadMessage)
                                .ariaLabel('Unable to load Feed Details')
                                .ok('Got it!')
                        );
                    } else {
                        this.model.loaded = true;
                        this.FeedService.updateFeed(updatedFeedResponse.data);
                        if (tabIndex != null && tabIndex != undefined && tabIndex != this.selectedTabIndex) {
                            this.selectedTabIndex = tabIndex;
                        }

                        this.registerTemplateService.initializeProperties(updatedFeedResponse.data.registeredTemplate, 'edit');
                        this.model.inputProcessors = this.registerTemplateService.removeNonUserEditableProperties(updatedFeedResponse.data.registeredTemplate.inputProcessors, true);
                        //sort them by name
                        this.model.inputProcessors = _.sortBy(this.model.inputProcessors, 'name')

                        this.model.inputProcessor = _.find(this.model.inputProcessors, (processor) => {
                            return angular.isDefined(this.model.inputProcessorName) && this.model.inputProcessorName != null ? this.model.inputProcessorType == processor.type && this.model.inputProcessorName.toLowerCase() == processor.name.toLowerCase() : this.model.inputProcessorType == processor.type;
                        });

                        if (angular.isUndefined(this.model.inputProcessor)) {
                            this.model.inputProcessor = _.find(this.model.inputProcessors, (processor) => {
                                return this.model.inputProcessorType == processor.type;
                            });
                        }
                        this.model.nonInputProcessors = this.registerTemplateService.removeNonUserEditableProperties(updatedFeedResponse.data.registeredTemplate.nonInputProcessors, false);
                        this.updateMenuOptions();
                        this.loadingFeedData = false;
                        this.model.isStream = updatedFeedResponse.data.registeredTemplate.stream;
                        this.FeedService.updateEditModelStateIcon();

                        var entityAccessControlled = this.accessControlService.isEntityAccessControlled();
                        //Apply the entity access permissions
                        var requests = {
                            entityEditAccess: !entityAccessControlled || this.FeedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS, this.model),
                            entityExportAccess: !entityAccessControlled || this.FeedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.EXPORT, this.model),
                            entityStartAccess: !entityAccessControlled || this.FeedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.START, this.model),
                            entityPermissionAccess: !entityAccessControlled || this.FeedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.CHANGE_FEED_PERMISSIONS, this.model),
                            functionalAccess: this.accessControlService.getUserAllowedActions()
                        };
                        this.$q.all(requests).then((response: any) => {
                            var allowEditAccess = this.accessControlService.hasAction(AccessControlService.FEEDS_EDIT, response.functionalAccess.actions);
                            var allowAdminAccess = this.accessControlService.hasAction(AccessControlService.FEEDS_ADMIN, response.functionalAccess.actions);
                            var slaAccess = this.accessControlService.hasAction(AccessControlService.SLA_ACCESS, response.functionalAccess.actions);
                            var allowExport = this.accessControlService.hasAction(AccessControlService.FEEDS_EXPORT, response.functionalAccess.actions);
                            var allowStart = this.accessControlService.hasAction(AccessControlService.FEEDS_EDIT, response.functionalAccess.actions);

                            this.allowEdit = response.entityEditAccess && allowEditAccess;
                            this.allowChangePermissions = entityAccessControlled && response.entityPermissionAccess && allowEditAccess;
                            this.allowAdmin = allowAdminAccess;
                            this.allowSlaAccess = slaAccess;
                            this.allowExport = response.entityExportAccess && allowExport;
                            this.allowStart = response.entityStartAccess && allowStart;
                        });
                    }
                }, (err: any) => {
                    //handle err
                    this.loadingFeedData = false;
                });
            }
            else {
                errorFn(" The feed was not found.")
            }
        }
        var errorFn = (err: any) => {
            this.loadingFeedData = false;
            this.errorLoadingFeed = true;
            var message = angular.isDefined(err) && angular.isString(err) ? err : '';
            this.$mdDialog.show(
                this.$mdDialog.alert()
                    .parent(angular.element(document.querySelector('body')))
                    .clickOutsideToClose(true)
                    .title('Error loading feed')
                    .textContent('Error loading feed. ' + message)
                    .ariaLabel('Error loading feed')
                    .ok('Got it!')
                //.targetEvent(ev)
            );

        }
        var promise = this.$http.get(this.RestUrlService.GET_FEEDS_URL + "/" + this.feedId);
        promise.then(successFn, errorFn);
        return promise;
    }

    nifiRunningCheck = () => {
        var promise = this.$http.get(this.RestUrlService.IS_NIFI_RUNNING_URL);
        promise.then((response: any) => {
            this.isNiFiRunning = response.data;
        }, (err: any) => {
            this.isNiFiRunning = false;
        });
    }

    gotoFeedStats = (ev: any) => {
        ev.preventDefault();
        ev.stopPropagation();
        var feedName = this.model.systemCategoryName + "." + this.model.systemFeedName;
        this.StateService.OpsManager().Feed().navigateToFeedStats(feedName);
    };

    gotoFeedDetails = (ev: any) => {
        ev.preventDefault();
        ev.stopPropagation();
        var feedName = this.model.systemCategoryName + "." + this.model.systemFeedName;
        this.StateService.OpsManager().Feed().navigateToFeedDetails(feedName);
    };

    shouldIndexingOptionsBeDisabled = () => {
        return ((this.model.historyReindexingStatus === 'IN_PROGRESS') || (this.model.historyReindexingStatus === 'DIRTY'));
    };

    shouldIndexingOptionsBeEnabled = () => {
        return !this.shouldIndexingOptionsBeDisabled();
    };

    findAndReplaceString = (str: any, findStr: any, replacementStr: any) => {
        var i = 0;
        if (angular.isUndefined(str) || angular.isUndefined(findStr)) {
            return '';
        }
        else {
            var strLength = str.length;
            for (i; i < strLength; i++) {
                str = str.replace(findStr, replacementStr);
            }
            return str;
        }
    };


}

angular.module(moduleName).filter('capitalizeFirstLetterOfEachWord', () => {
    return (str: any) => {
        return (!!str) ?
            str.split(' ')
                .map((word: any) => {
                    return word.charAt(0).toUpperCase() + word.substr(1).toLowerCase();
                })
                .join(' ')
            : '';
    }
});

angular.module(moduleName).component('feedDetailsController', {
    templateUrl: 'js/feed-mgr/feeds/edit-feed/feed-details.html',
    controller: FeedDetailsController,
    controllerAs: 'vm',
    bindings: {
        $transition$: '<'
    },
});

angular.module(moduleName).controller('FeedUploadFileDialogController',["$scope","$mdDialog","$http","RestUrlService","FileUpload","feedId",FeedUploadFileDialogController]);
