import {Component, Injector, Input, OnInit} from "@angular/core";
import {Category, DefaultFeedModel, FeedModel, Step} from "../../model/feed.model";
import {FormControl, FormGroup} from "@angular/forms";
import {DefineFeedService} from "../../services/define-feed.service";
import {FormsModule} from '@angular/forms'
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {StateRegistry, StateService} from "@uirouter/angular";
import CategoriesService from "../../../../services/CategoriesService";
import {Observable} from "rxjs/Observable";
import {FeedStepValidator} from "../../model/feed-step-validator";
import {map, startWith,flatMap} from 'rxjs/operators';
import 'rxjs/add/observable/fromPromise';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/map';
import {FeedService} from "../../../../services/FeedService";

@Component({
    selector: "define-feed-step-general-info",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/general-info/define-feed-step-general-info.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/general-info/define-feed-step-general-info.component.html"
})
export class DefineFeedStepGeneralInfoComponent extends AbstractFeedStepComponent {

    @Input() stateParams :any;

    /**
     * Form control for categories autocomplete
     * @type {FormControl}
     */
    public categoryCtrl = new FormControl();

    public feedNameCtrl = new FormControl();
    /**
     * Aysnc autocomplete list of categories
     */
    public filteredCategories: Observable<Category[]>;
    /**
     * Angular 1 upgraged Categories service
     */
    private categoriesService:CategoriesService;

    private feedService: FeedService;


    constructor(  defineFeedService:DefineFeedService,  stateService:StateService,private $$angularInjector: Injector) {
        super(defineFeedService,stateService);
        this.categoriesService = $$angularInjector.get("CategoriesService");
        this.feedService = $$angularInjector.get("FeedService");
        this.filteredCategories = this.categoryCtrl.valueChanges.flatMap(text => {
            return <Observable<Category[]>> Observable.fromPromise(this.categoriesService.querySearch(text));
        });

        //watch for changes on the feed name to generate the system name
        this.feedNameCtrl.valueChanges.debounceTime(200).subscribe(text => this.generateSystemName());
    }


    getStepName() {
        return "General Info";
    }

    init(){
     super.init();
    }


    categoryAutocompleteDisplay(category?: Category): string | undefined {
        return category ? category.name : undefined;
    }

    generateSystemName(){
        this.feedService.getSystemName(this.feed.feedName).then((response:any) => {
            this.feed.systemName = response.data;
            //TODO add in this validation
          //  this.model.table.tableSchema.name = this.model.systemFeedName;
          //  this.validateUniqueFeedName();
          //  this.validate();
        });
    }

}

