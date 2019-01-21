import * as angular from 'angular';
import * as _ from "underscore";
import {Component, Injector, Input, OnInit, ViewChild} from "@angular/core";
import { Templates } from "../../../../../../lib/feed-mgr/services/TemplateTypes";
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
import {FieldConfig} from "../../../../../../lib/dynamic-form/model/FieldConfig";
import {InputText, InputType} from "../../../../../../lib/dynamic-form/model/InputText";
import {Select} from "../../../../../../lib/dynamic-form/model/Select";
import {Checkbox} from "../../../../../../lib/dynamic-form/model/Checkbox";
import {DynamicFormService} from "../../../../../../lib/dynamic-form/services/dynamic-form.service";
import {MatRadioChange} from "@angular/material";
import {RegisterTemplatePropertyService} from "../../../../services/RegisterTemplatePropertyService";
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import "rxjs/add/observable/empty";
import "rxjs/add/observable/of";
import "rxjs/add/observable/from";
import 'rxjs/add/observable/forkJoin'
import {SectionHeader} from "../../../../../../lib/dynamic-form/model/SectionHeader";
import {RestUrlConstants} from "../../../../services/RestUrlConstants";
import {UiComponentsService} from "../../../../services/UiComponentsService";
import {RadioButton} from "../../../../../../lib/dynamic-form/model/RadioButton";
import {Textarea} from "../../../../../../lib/dynamic-form/model/Textarea";
import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import {FieldGroup} from "../../../../../../lib/dynamic-form/model/FieldGroup";
import {DynamicFormBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-builder";
import {DynamicFormFieldGroupBuilder} from "../../../../../../lib/dynamic-form/services/dynamic-form-field-group-builder";
import {ConfigurationFieldBuilder, FieldConfigBuilder} from "../../../../../../lib/dynamic-form/services/field-config-builder";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {TdDialogService} from "@covalent/core/dialogs";
import {FeedSideNavService} from "../../services/feed-side-nav.service";
import {FeedNifiPropertiesComponent, NiFiPropertiesProcessorsChangeEvent} from "./feed-nifi-properties.component";
import {FormGroupUtil} from "../../../../../services/form-group-util";




@Component({
    selector: "define-feed-step-feed-details",
    styleUrls: ["./define-feed-step-feed-details.component.css"],
    templateUrl: "./define-feed-step-feed-details.component.html"
})
export class DefineFeedStepFeedDetailsComponent extends AbstractFeedStepComponent {


    @ViewChild("feedNifiPropertiesComponent")
    feedPropertyNiFiComponent:FeedNifiPropertiesComponent

    private loading: boolean = true;

    public form :FormGroup;

    public displayEditActions:boolean = false;

    public noPropertiesExist:boolean = false;

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

    ngAfterViewInit() {
        this.subscribeToFormChanges(this.form);
    }

    onProcessorsChange(event:NiFiPropertiesProcessorsChangeEvent){
        let prevRequiredValue = this.step.required;
        this.noPropertiesExist = event.noPropertiesExist;
        if(event.noPropertiesExist){
         this.displayEditActions = false;
         // mark this step as being optional
         this.step.required = false;
        }
        else {
            this.step.required = true;
            this.displayEditActions = true;
        }
        if(prevRequiredValue != this.step.required){
            //re validate
            this.step.validate(this.feed);
            this.defineFeedService.updateStepState(this.feed, this.step);
        }
    }

    onNiFiPropertiesFormUpdated(){
        if(this.loading){
            this.loading = false;
            this.displayEditActions = true;
        }
    }

    /**
     * called before saving
     */
    protected applyUpdatesToFeed():(Observable<any>| boolean | null){


        if(this.form.invalid){
            this.step.validator.hasFormErrors = true;
            //show the errors
            FormGroupUtil.touchFormControls(this.form);
            return false;
        }
        else {
            this.step.validator.hasFormErrors = false;
            this.feedPropertyNiFiComponent.applyUpdatesToFeed()
            return true;
        }


    }


}
