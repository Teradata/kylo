import * as angular from 'angular';
import * as _ from "underscore";
import AccessControlService from '../../../../services/AccessControlService';
import {DefaultFeedModel, FeedModel} from "../model/feed.model";
import {Component, Injector, Input, OnInit} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {DefineFeedService} from "../services/define-feed.service";
import {StateRegistry, StateService} from "@uirouter/angular";
import {FEED_DEFINITION_STATE_NAME} from "../define-feed-states"

@Component({
    selector: "define-feed-select-template",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/select-template/define-feed-select-template.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/select-template/define-feed-select-template.component.html"
})
export class DefineFeedSelectTemplateComponent implements OnInit {

    /**
     * Name of the template, resolved via state transition in the define-feed-states.ts
     */
    @Input()
    templateName:string;

    /**
     * String id of the template, resolved via the state transition in the define-feed-states.ts
     */
    @Input()
    templateId:string;


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
        model:FeedModel


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



    constructor ( private http:HttpClient,private stateService: StateService, private defineFeedService:DefineFeedService,private $$angularInjector: Injector) {

        this.model = new DefaultFeedModel();
        /**
         * The total number of steps to deisplay and render for the feed stepper
         * @type {null}
         */
        this.model.totalSteps = null;

        this.allTemplates = [];

        this.firstTemplates = [];

        this.displayMoreLink = false;

        let sideNavService = $$angularInjector.get("SideNavService");
        sideNavService.showSideNav();
    };




        /**
         * Click the more link to show all the template cards
         */
        more() {
            this.layout = 'all';
            this.displayMoreLink = false;
        };

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

            this.defineFeedService.initializeFeedSteps(this.model);

            // Load table option
            if (this.model.templateTableOption !== "NO_TABLE") {



                /*
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
                    */
            } else {
                this.model.totalSteps = 5;
            }
            this.defineFeedService.setFeed(this.model)
            this.stateService.go(FEED_DEFINITION_STATE_NAME+".feed-step.new-feed")
        };


        /**
         * Return a list of the Registered Templates in the system
         * @returns {HttpPromise}
         */
        getRegisteredTemplates() {
            var successFn =  (response:any) => {

                if (response) {

                    var data = _.chain(response).filter((template) => {
                        return template.state === 'ENABLED'
                    }).sortBy('order')
                        .value();

                    if (data.length > 1) {
                        this.displayMoreLink = true;
                    }
                    this.allTemplates = data;
                    this.firstTemplates = _.first(data, 3);
                }

            };
            var errorFn = (err:any) => {

            };
            var promise = this.http.get("/proxy/v1/feedmgr/templates/registered");
            promise.subscribe(successFn,errorFn);
            return promise;
        }

                /**
         * initialize the controller
         */
        ngOnInit() {

            this.getRegisteredTemplates().subscribe((response:any) =>{
                if(angular.isDefined(this.templateName) && this.templateName != ''){
                    var match = _.find(this.allTemplates,(template:any) => {
                        return template.templateName == this.templateName || template.id == this.templateId;
                    });
                    if(angular.isDefined(match)) {
                        this.selectTemplate(match);
                    }
                }
            });
                    /*
            // Fetch the allowed actions
            this.accessControlService.getUserAllowedActions()
                .then( (actionSet:any) => {
                    this.allowImport = this.accessControlService.hasAction(AccessControlService.FEEDS_IMPORT, actionSet.actions);
                });
                */

        }




}
