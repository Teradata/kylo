import {DefineFeedTableValidator} from "../steps/define-table/define-feed-table-validator";
import {Step} from "../../../model/feed/feed-step.model";
import {FeedStepConstants} from "../../../model/feed/feed-step-constants";
import {DefineFeedStepSourceValidator} from "../steps/source/define-feed-step-source-validator";
import {FeedStepBuilder, StepBuilder} from "../../../model/feed/feed-step-builder";
import {TranslateService} from "@ngx-translate/core";
import {DefineFeedPropertiesValidator} from "../steps/properties/define-feed-properties-validator";
import {DefineFeedPropertiesRequiredCheck} from "../steps/properties/define-feed-properties-required-check";


export class FeedStepBuilderUtil {


    builder:FeedStepBuilder;

    constructor(private _translateService: TranslateService){
        this.builder = new FeedStepBuilder();
    }

    add(stepBuilder:StepBuilder){
       this.builder.addStep(stepBuilder);
       return this;
    }

    build(){
        return this.builder.build();
    }

    feedDetailsStep():StepBuilder {
        let name = this._translateService.instant("FeedDefinition.Steps.AdditionalOptions.Name")
        let desc = this._translateService.instant("FeedDefinition.Steps.AdditionalOptions.Description")
        return new StepBuilder().setName(name).setIcon("speaker_notes").setSystemName(FeedStepConstants.STEP_FEED_DETAILS).addDependsUpon(FeedStepConstants.STEP_FEED_SOURCE).setDescription(desc).setSref("feed-details").setRequired(true);
    }

    permissionStep():StepBuilder {
        let name =  this._translateService.instant("FeedDefinition.Steps.Permissions.Name")
        return  new StepBuilder().setName(name).setIcon("person").setSystemName(FeedStepConstants.STEP_PERMISSIONS).setDescription("Assign who can read and edit this feed").setSref("feed-permissions");
    }

    propertiesStep():StepBuilder {
        let name =  this._translateService.instant("FeedDefinition.Steps.Properties.Name")
        return  new StepBuilder().setName(name).setIcon("assignment").setSystemName(FeedStepConstants.STEP_PROPERTIES).setDescription("Define and set extra properties for this feed").setSref("feed-properties").setValidator(new DefineFeedPropertiesValidator()).setFeedStepRequiredCheck(new DefineFeedPropertiesRequiredCheck());
    }

    targetStep():StepBuilder {
        let name =  this._translateService.instant("FeedDefinition.Steps.Target.Name")
        return  new StepBuilder().setIcon("grid_on").setName(name).setSystemName(FeedStepConstants.STEP_FEED_TARGET).setDescription("Define target table").addDependsUpon(FeedStepConstants.STEP_WRANGLER).setSref("feed-table").setRequired(true).setValidator(new DefineFeedTableValidator());
    }

    sourceStep(required:boolean = true):StepBuilder {
        let name =  this._translateService.instant("FeedDefinition.Steps.Source.Name")
        return  new StepBuilder().setName(name).setIcon("work").setSystemName(FeedStepConstants.STEP_FEED_SOURCE).setDescription("Define source options").setSref("datasources").setRequired(required).setValidator(new DefineFeedStepSourceValidator());
    }

    wranglerStep():StepBuilder {
        let name =  this._translateService.instant("FeedDefinition.Steps.Wrangler.Name")
        return  new StepBuilder().setName(name).setIcon("blur_linear").setSystemName(FeedStepConstants.STEP_WRANGLER).setDescription("Select data and apply transformations").setSref("wrangler").setRequired(true).setFullscreen(true);
    }

     defineTableFeedSteps() :Step[] {

        return this.add(this.sourceStep())
            .add(this.targetStep())
            .add(this.feedDetailsStep())
            .add(this.propertiesStep())
            .add(this.permissionStep())
            .build();
    }

     dataTransformationSteps() :Step[] {

         return this.add(this.sourceStep())
             .add(this.wranglerStep())
             .add(this.targetStep())
             .add(this.feedDetailsStep())
             .add(this.propertiesStep())
             .add(this.permissionStep())
             .build()
    }


     simpleFeedSteps() :Step[] {
         return this.add(this.sourceStep())
             .add(this.feedDetailsStep())
             .add(this.propertiesStep())
             .add(this.permissionStep())
             .build()
    }

}

