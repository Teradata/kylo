import * as angular from 'angular';
import * as _ from "underscore";
import {AccessControlService} from '../../../../services/AccessControlService';
import {Feed} from "../../../model/feed/feed.model";
import {Component, Injector, Input, OnInit} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {DefineFeedService} from "../services/define-feed.service";
import {StateService} from "../../../../services/StateService";
import {
    FEED_DEFINITION_SECTION_STATE_NAME,
    FEED_DEFINITION_STATE_NAME,
    FEED_DEFINITION_SUMMARY_STATE_NAME,
    FEED_OVERVIEW_STATE_NAME,
    FEED_SETUP_GUIDE_STATE_NAME
} from "../../../model/feed/feed-constants";
import {TdDialogService} from "@covalent/core/dialogs";
import {DateFormatDialog} from "../../../visual-query/wrangler/core/columns/date-format.component";
import {DateFormatConfig, DateFormatResponse} from "../../../visual-query/wrangler/api/services/dialog.service";
import {Observable} from "rxjs/Observable";
import {NewFeedDialogComponent, NewFeedDialogData, NewFeedDialogResponse} from "../new-feed-dialog/new-feed-dialog.component";
import {Template} from "../../../model/template-models";
import {SaveFeedResponse} from "../model/save-feed-response.model";
import {LocalStorageService} from "../../../../common/local-storage/local-storage.service";
import {FeedLoadingService} from "../services/feed-loading-service";
import {finalize} from 'rxjs/operators/finalize';
import {KyloRouterService} from "../../../../services/kylo-router.service";


@Component({
    selector: "define-feed-select-template",
    styleUrls: ["./define-feed-select-template.component.scss"],
    templateUrl: "./define-feed-select-template.component.html"
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
        model:Feed


        /**
         * All the templates available
         * @type {Array}
         */
        allTemplates:Array<any> = [];
        

        /**
         * Array of the first n templates to be displayed prior to the 'more' link
         * @type {Array}
         */
        favorites:Array<any> = [];

        favoriteTemplateNames:string[];

        /**
         * flag to indicate we need to display the 'more templates' link
         * @type {boolean}
         */
        displayMoreLink:boolean = false;

        /**
         * The number of templates to display iniitally
         * @type {number}
         */
        FAVORITE_TEMPLATES = 5;

        LOCAL_STORAGE_KEY = "KYLO_FAVORITE_TEMPLATES";



    constructor ( private http:HttpClient,private stateService: StateService, private defineFeedService:DefineFeedService,private dialog: TdDialogService, private localStorageService:LocalStorageService,
                  private $$angularInjector: Injector,
                  protected feedLoadingService:FeedLoadingService,
                  private kyloRouterService:KyloRouterService) {

        this.model = new Feed();
        /**
         * The total number of steps to deisplay and render for the feed stepper
         * @type {null}
         */
        this.model.totalSteps = null;

        let sideNavService = $$angularInjector.get("SideNavService");
        sideNavService.hideSideNav();

        //TODO change with Entity Access Control!
        this.allowImport = true;


    };

        /**
         * Go back to prev state
         */
        goBack(){
            this.kyloRouterService.back("feeds");
        }

        /**
         * Navigate to the feed import page
         */
        gotoImportFeed(){
            this.stateService.FeedManager().Feed().navigateToFeedImport();
        }


        /**
         * Click the more link to show all the template cards
         */
        more() {
            this.layout = 'all';
            this.displayMoreLink = false;
        };


        selectTemplate(template:Template){
            this.openNewFeedDialog(new NewFeedDialogData(template)).subscribe((response:NewFeedDialogResponse) => {
                this.createFeed(response)
            })
        }
        /**
         * Select a template
         * @param template
         */
        createFeed(newFeedData:NewFeedDialogResponse) {
            this.feedLoadingService.registerLoading()
            let template = newFeedData.template;
            let feedName = newFeedData.feedName;
            let systemFeedName = newFeedData.systemFeedName;
            this.model = new Feed();
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

            this.model.registeredTemplate = template;
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
            this.model.feedName = feedName;
            this.model.systemFeedName = systemFeedName
            this.model.category = newFeedData.category;
            this.defineFeedService.saveFeed(this.model).subscribe((response :SaveFeedResponse)=> {
                this.feedLoadingService.resolveLoading()
                this.stateService.go(FEED_SETUP_GUIDE_STATE_NAME, {"feedId": response.feed.id,"newFeed":true})
            }, error1 => {
                this.feedLoadingService.resolveLoading()
                //TODO ERROR unable to create feed!!!
            });

        };

        public makeFavorite($event:MouseEvent,template:Template){
            $event.preventDefault();
            $event.stopPropagation();
            //push the template to the front
            //if remove it from the fav array and add it back in
            this.removeFavorite($event,template);
            this.favoriteTemplateNames.unshift(template.templateName);
            this.favorites.unshift(template);

            this.localStorageService.setItem(this.LOCAL_STORAGE_KEY, this.favoriteTemplateNames);
        }

        public removeFavorite($event:MouseEvent,template:Template) {
            $event.preventDefault();
            $event.stopPropagation();
            let idx = this.favoriteTemplateNames.indexOf(template.templateName);
            if(idx >=0){
                //remove it
                this.favoriteTemplateNames.splice(idx,1)
            }
            idx = this.favorites.indexOf(template);
            if(idx >=0){
                //remove it
                this.favorites.splice(idx,1)
            }
            this.localStorageService.setItem(this.LOCAL_STORAGE_KEY, this.favoriteTemplateNames);

        }


        /**
         * Return a list of the Registered Templates in the system
         * @returns {HttpPromise}
         */
        getRegisteredTemplates() {
            var successFn =  (response:any) => {

                if (response) {

                    var data = _.chain(response).filter((template:any) => {
                        return template.state === 'ENABLED'
                    }).sortBy('order')
                        .value();

                    if (data.length > 1) {
                        this.displayMoreLink = true;
                    }
                    this.allTemplates = data.sort((a:Template,b:Template)=>{
                        return (a.templateName.localeCompare(b.templateName));
                    });
                    let favoriteTemplates:string[] = <string[]>this.localStorageService.getItem(this.LOCAL_STORAGE_KEY);
                    if(favoriteTemplates) {
                        let favoriteTemplateObjects = _.filter(data, ((template:Template) => {
                            return _.find(favoriteTemplates, (templateName) => templateName == template.templateName) != undefined;
                        }));
                        this.favorites = _.first(favoriteTemplateObjects,this.FAVORITE_TEMPLATES);
                        this.favoriteTemplateNames = favoriteTemplates
                    }
                    else {
                        this.favoriteTemplateNames = [];
                    }
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
            this.feedLoadingService.registerLoading();
            this.getRegisteredTemplates().pipe(
                finalize(() => this.feedLoadingService.resolveLoading())
            ).subscribe((response:any) =>{
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


    /**
     * Opens a modal dialog for the user to input a date format string.
     *
     * @param config - dialog configuration
     * @returns the date format string
     */
    openNewFeedDialog(config: NewFeedDialogData): Observable<NewFeedDialogResponse> {
        return this.dialog.open(NewFeedDialogComponent, {data: config,disableClose:true})
            .afterClosed()
            .filter(value => typeof value !== "undefined");
    }



}
