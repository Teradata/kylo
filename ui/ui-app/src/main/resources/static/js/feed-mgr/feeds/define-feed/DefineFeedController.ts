import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/feeds/define-feed/module-name');

export class DefineFeedController implements ng.IComponentController {



        requestedTemplate:any = this.$transition$.params().templateName || '';
        requestedTemplateId:any = this.$transition$.params().templateId || '';


        /**
         * Indicates if feeds may be imported from an archive.
         * @type {boolean}
         */
        allowImport:boolean = false;

        /**
         * the layout choosen.  Either 'first', or 'all'  changed via the 'more' link
         * @type {string}
         */
        layout:string = 'first';

        /**
         * The selected template
         * @type {null}
         */
        template:any = null;

        /**
         * The model for creating the feed
         * @type {*}
         */
        model:any;


        /**
         * All the templates available
         * @type {Array}
         */
        allTemplates:Array<any>;
        

        /**
         * Array of the first n templates to be displayed prior to the 'more' link
         * @type {Array}
         */
        firstTemplates:Array<any>;

        /**
         * flag to indicate we need to display the 'more templates' link
         * @type {boolean}
         */
        displayMoreLink:boolean = false;

        /**
         * Flag to indicate we are cloning
         * This is set via a $transition$ param in the init() method
         * @type {boolean}
         */
        cloning:boolean = false;
        
        
        
        /**
         * After the stepper is initialized this will get called to setup access control
         * @param stepper
         */
        onStepperInitialized = (function(stepper:any) {
            var accessChecks = {entityAccess: this.AccessControlService.checkEntityAccessControlled(), securityGroups: this.FeedSecurityGroups.isEnabled()};
            this.$q.all(accessChecks).then( (response:any) => {
                var entityAccess = this.AccessControlService.isEntityAccessControlled();
                var securityGroupsAccess = response.securityGroups;
                //disable the access control step
                if (!entityAccess && !securityGroupsAccess) {
                    //Access Control is second to last step 0 based array indexc
                    stepper.deactivateStep(this.model.totalSteps - 2);
                }
                if (this.cloning) {
                    this.hideCloningDialog();
                }
            });

        }).bind(this);
        /**
         * hide the cloning dialog
         */
        hideCloningDialog() {
            this.$mdDialog.hide();
        }

        /**
         * Click the more link to show all the template cards
         */
        more() {
            this.layout = 'all';
        };

        /**
         * Navigate to the import feed screen
         */
        gotoImportFeed = (function() {
            this.StateService.FeedManager().Feed().navigatetoImportFeed();
        }).bind(this);

        /**
         * Select a template
         * @param template
         */
        selectTemplate(template:any) {
            this.model.templateId = template.id;
            this.model.templateName = template.templateName;
            //setup some initial data points for the template
            this.model.defineTable = template.defineTable;
            this.model.allowPreconditions = template.allowPreconditions;
            this.model.dataTransformationFeed = template.dataTransformation;

            // Determine table option
            if (template.templateTableOption) {
                this.model.templateTableOption = template.templateTableOption;
            } else if (template.defineTable) {
                this.model.templateTableOption = "DEFINE_TABLE";
            } else if (template.dataTransformation) {
                this.model.templateTableOption = "DATA_TRANSFORMATION";
            } else {
                this.model.templateTableOption = "NO_TABLE";
            }

            //set the total pre-steps for this feed to be 0. They will be taken from the templateTableOption
            this.model.totalPreSteps = 0;
            //When rendering the pre-step we place a temp tab/step in the front for the initial steps to transclude into and then remove it.
            //set this render flag to false initially
            this.model.renderTemporaryPreStep = false;

            // Load table option
            if (this.model.templateTableOption !== "NO_TABLE") {
                this.UiComponentsService.getTemplateTableOption(this.model.templateTableOption)
                    .then( (tableOption:any) => {
                        //if we have a pre-stepper configured set the properties
                        if(angular.isDefined(tableOption.preStepperTemplateUrl) && tableOption.preStepperTemplateUrl != null){
                            this.model.totalPreSteps = tableOption.totalPreSteps
                            this.model.renderTemporaryPreStep = true;
                        }
                        //signal the service that we should track rendering the table template
                        //We want to run our initializer when both the Pre Steps and the Feed Steps have completed.
                        //this flag will be picked up in the TableOptionsStepperDirective.js
                        this.UiComponentsService.startStepperTemplateRender(tableOption);

                        //add the template steps + 5 (general, feedDetails, properties, access, schedule)
                        this.model.totalSteps = tableOption.totalSteps +  5;
                    },  () => {
                        this.$mdDialog.show(
                            this.$mdDialog.alert()
                                .clickOutsideToClose(true)
                                .title("Create Failed")
                                .textContent("The template table option could not be loaded.")
                                .ariaLabel("Failed to create feed")
                                .ok("Got it!")
                        );
                        this.StateService.FeedManager().Feed().navigateToFeeds();
                    });
            } else {
                this.model.totalSteps = 5;
            }
        };

        /**
         * Cancel the stepper
         */
        cancelStepper() {
            this.FeedService.resetFeed();
            this.model.totalSteps = null;
        };

        /**
         * Return a list of the Registered Templates in the system
         * @returns {HttpPromise}
         */
        getRegisteredTemplates() {
            var successFn =  (response:any) => {

                if (response.data) {

                    var data = _.chain(response.data).filter((template) => {
                        return template.state === 'ENABLED'
                    }).sortBy('order')
                        .value();

                    if (data.length > 1) {
                        this.displayMoreLink = true;
                    }
                    this.allTemplates = data;
                    this.firstTemplates = _.first(data, 3);
                    if (this.cloning) {
                        this.model = this.FeedService.cloneFeed();
                        var registeredTemplate = this.model.registeredTemplate;
                        var templateObj = {
                            id: registeredTemplate.id,
                            templateName: registeredTemplate.templateName,
                            defineTable: registeredTemplate.defineTable,
                            allowPreconditions: registeredTemplate.allowPreconditions,
                            dataTransformation: registeredTemplate.dataTransformation,
                            templateTableOption: registeredTemplate.templateTableOption
                        }
                        this.selectTemplate(templateObj);
                    }

                }

            };
            var errorFn = (err:any) => {

            };
            var promise = this.$http.get(this.RestUrlService.GET_REGISTERED_TEMPLATES_URL);
            promise.then(successFn, errorFn);
            return promise;
        }

                /**
         * initialize the controller
         */
        init() {
            
            var isCloning = this.$transition$.params().bcExclude_cloning;
            var cloneFeedName = this.$transition$.params().bcExclude_cloneFeedName;
            this.cloning = angular.isUndefined(isCloning) ? false : isCloning;
            var feedDescriptor = this.$transition$.params().feedDescriptor || '';
            this.model.feedDescriptor = feedDescriptor;

            this.getRegisteredTemplates().then((response:any) =>{
                if(angular.isDefined(this.requestedTemplate) && this.requestedTemplate != ''){
                    var match = _.find(this.allTemplates,(template:any) => {
                        return template.templateName == this.requestedTemplate || template.id == this.requestedTemplateId;
                    });
                    if(angular.isDefined(match)) {
                        this.FeedService.resetFeed();
                        if(angular.isDefined(feedDescriptor) && feedDescriptor != ''){
                            this.model.feedDescriptor =feedDescriptor;
                        }
                        this.selectTemplate(match);
                    }
                }
            });

            if (this.cloning) {
                this.showCloningDialog(cloneFeedName);
            }
            // Fetch the allowed actions
            this.AccessControlService.getUserAllowedActions()
                .then( (actionSet:any) => {
                    this.allowImport = this.AccessControlService.hasAction(this.AccessControlService.FEEDS_IMPORT, actionSet.actions);
                });

        }


        /**
         * Show a dialog while the cloning is setting up the stepper with the data
         * @param cloneFeedName
         */
        showCloningDialog(cloneFeedName:any) {
            if (angular.isUndefined(cloneFeedName)) {
                cloneFeedName = "the feed";
            }
            this.$mdDialog.show({
                templateUrl: 'js/feed-mgr/feeds/define-feed/clone-feed-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: true,
                locals: {
                    feedName: cloneFeedName
                },
                controller: CloningDialogController,
                fullscreen: true
            });
            function CloningDialogController($scope:any, $mdDialog:any, feedName:any) {
                $scope.feedName = feedName;
                $scope.closeDialog = function () {
                    $mdDialog.hide();
                }
            }
        }


    constructor (private $scope:any, private $http:any, private $mdDialog:any, private $q:any, private $transition$:any
        , private AccessControlService:any, private FeedService:any, private FeedSecurityGroups:any, private RestUrlService:any, private StateService:any
        , private UiComponentsService:any) {

        var self = this;

        this.model = FeedService.createFeedModel;

        if(angular.isUndefined(this.model)){
            FeedService.resetFeed();
        }
        /**
         * The total number of steps to deisplay and render for the feed stepper
         * @type {null}
         */
        this.model.totalSteps = null;

        /**
         * The stepper url.
         *
         * @type {string}
         */
        this.model.stepperTemplateUrl= 'js/feed-mgr/feeds/define-feed/define-feed-stepper.html'


        this.requestedTemplate = $transition$.params().templateName || '';
        this.requestedTemplateId = $transition$.params().templateId || '';
        this.allTemplates = [];

        this.firstTemplates = [];

        this.displayMoreLink = false;
        this.cloning = false;





        //initialize the controller
        this.init();

    };

}
angular.module(moduleName).controller('DefineFeedController',
    ["$scope", "$http", "$mdDialog", "$q", "$transition$", "AccessControlService", "FeedService", "FeedSecurityGroups", "RestUrlService", "StateService",
     "UiComponentsService", DefineFeedController]);
