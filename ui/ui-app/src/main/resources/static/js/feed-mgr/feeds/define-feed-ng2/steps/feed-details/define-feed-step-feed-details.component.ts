import * as angular from 'angular';
import * as _ from "underscore";
import {Component, Injector, Input, OnInit, ViewChild} from "@angular/core";
import { Templates } from "../../../../services/TemplateTypes";
import {Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";
import {FormControl, FormGroup} from "@angular/forms";
import {DefineFeedService} from "../../services/define-feed.service";
import {FormsModule} from '@angular/forms'
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {StateRegistry, StateService} from "@uirouter/angular";
import {FeedService} from "../../../../services/FeedService";
import {ITdDynamicElementConfig, TdDynamicElement} from "@covalent/dynamic-forms";
import {TdDynamicType} from "@covalent/dynamic-forms/services/dynamic-forms.service";
import {FieldConfig} from "../../../../shared/dynamic-form/model/FieldConfig";
import {InputText, InputType} from "../../../../shared/dynamic-form/model/InputText";
import {Select} from "../../../../shared/dynamic-form/model/Select";
import {Checkbox} from "../../../../shared/dynamic-form/model/Checkbox";
import {DynamicFormService} from "../../../../shared/dynamic-form/services/dynamic-form.service";
import {MatRadioChange} from "@angular/material";
import {RegisterTemplatePropertyService} from "../../../../services/RegisterTemplatePropertyService";
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import "rxjs/add/observable/empty";
import "rxjs/add/observable/of";
import "rxjs/add/observable/from";
import 'rxjs/add/observable/forkJoin'
import {SectionHeader} from "../../../../shared/dynamic-form/model/SectionHeader";
import {RestUrlConstants} from "../../../../services/RestUrlConstants";
import {UiComponentsService} from "../../../../services/UiComponentsService";
import {RadioButton} from "../../../../shared/dynamic-form/model/RadioButton";
import {Textarea} from "../../../../shared/dynamic-form/model/Textarea";
import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import {FieldGroup} from "../../../../shared/dynamic-form/model/FieldGroup";
import {DynamicFormBuilder} from "../../../../shared/dynamic-form/services/dynamic-form-builder";
import {DynamicFormFieldGroupBuilder} from "../../../../shared/dynamic-form/services/dynamic-form-field-group-builder";
import {ConfigurationFieldBuilder, FieldConfigBuilder} from "../../../../shared/dynamic-form/services/field-config-builder";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {TdDialogService} from "@covalent/core/dialogs";
import {FeedSideNavService} from "../../shared/feed-side-nav.service";
import {FeedNifiPropertiesComponent} from "./feed-nifi-properties.component";




@Component({
    selector: "define-feed-step-feed-details",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/feed-details/define-feed-step-feed-details.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/feed-details/define-feed-step-feed-details.component.html"
})
export class DefineFeedStepFeedDetailsComponent extends AbstractFeedStepComponent {


    @ViewChild("feedNifiPropertiesComponent")
    feedPropertyNiFiComponent:FeedNifiPropertiesComponent

    private loading: boolean;

    public form :FormGroup;

    constructor(  defineFeedService:DefineFeedService,  stateService:StateService, private http:HttpClient,
                  private dynamicFormService:DynamicFormService, feedLoadingService:FeedLoadingService,
                  dialogService: TdDialogService, feedSideNavService:FeedSideNavService) {
        super(defineFeedService,stateService, feedLoadingService,dialogService, feedSideNavService);
        this.form = new FormGroup({});

    }

    getStepName() {
        return FeedStepConstants.STEP_FEED_DETAILS;
    }

    init(){
        //listen when the form is valid or invalid


    }

    onFormInitialized(){
        this.subscribeToFormChanges(this.form);
    }

    /**
     * called before saving
     */
    protected applyUpdatesToFeed():(Observable<any>| null){
        this.feedPropertyNiFiComponent.applyUpdatesToFeed()
        return null;
    }


}