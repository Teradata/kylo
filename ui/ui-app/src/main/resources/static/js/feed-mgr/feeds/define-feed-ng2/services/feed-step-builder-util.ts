import {DefineFeedTableValidator} from "../steps/define-table/define-feed-table-validator";
import {Step} from "../../../model/feed/feed-step.model";
import {DefineFeedStepGeneralInfoValidator} from "../steps/general-info/define-feed-step-general-info-validator";
import {FeedStepConstants} from "../../../model/feed/feed-step-constants";
import {DefineFeedStepSourceSampleValidator} from "../steps/source-sample/define-feed-step-source-sample-validator";
import {FeedStepBuilder, StepBuilder} from "../../../model/feed/feed-step-builder";
import {TranslateService} from "@ngx-translate/core";
import {DefineFeedPropertiesValidator} from "../steps/properties/define-feed-properties-validator";


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
        let name = this._translateService.instant("views.define-feed-stepper.FeedDetails")
        return new StepBuilder().setName(name).setIcon("speaker_notes").setSystemName(FeedStepConstants.STEP_FEED_DETAILS).setDescription("Update NiFi processor settings").setSref("feed-details").setRequired(true);
    }

    permissionStep():StepBuilder {
        let name = FeedStepConstants.STEP_PERMISSIONS;//this._translateService.instant("views.define-feed-stepper.FeedDetails")
        return  new StepBuilder().setName(name).setIcon("person").setSystemName(FeedStepConstants.STEP_PERMISSIONS).setDescription("Assign who can read and edit this feed").setSref("feed-permissions");
    }

    propertiesStep():StepBuilder {
        let name = FeedStepConstants.STEP_PROPERTIES;//this._translateService.instant("views.define-feed-stepper.FeedDetails")
        return  new StepBuilder().setName(name).setIcon("assignment").setSystemName(FeedStepConstants.STEP_PROPERTIES).setDescription("Define and set extra properties for this feed").setSref("feed-properties").setValidator(new DefineFeedPropertiesValidator());
    }

    targetTableStep():StepBuilder {
        let name = FeedStepConstants.STEP_FEED_TARGET;
        return  new StepBuilder().setIcon("table_chart").setName(name).setSystemName(FeedStepConstants.STEP_FEED_TARGET).setDescription("Define target table").addDependsUpon(FeedStepConstants.STEP_SOURCE_SAMPLE).addDependsUpon(FeedStepConstants.STEP_WRANGLER).setSref("feed-table").setRequired(true).setValidator(new DefineFeedTableValidator());
    }

    sourceSampleStep():StepBuilder {
        let name = FeedStepConstants.STEP_SOURCE_SAMPLE;
        return  new StepBuilder().setName(name).setIcon("work").setSystemName(FeedStepConstants.STEP_SOURCE_SAMPLE).setDescription("Browse catalog for sample").setSref("datasources").setRequired(true);
    }

    wranglerStep():StepBuilder {
        let name = FeedStepConstants.STEP_WRANGLER;
        return  new StepBuilder().setName(name).setIcon("blur_linear").setSystemName(FeedStepConstants.STEP_WRANGLER).setDescription("Data Wrangler").addDependsUpon(FeedStepConstants.STEP_SOURCE_SAMPLE).setSref("wrangler").setRequired(true);
    }

     defineTableFeedSteps() :Step[] {

        return this.add(this.propertiesStep())
            .add(this.permissionStep())
            .add(this.sourceSampleStep())
            .add(this.targetTableStep())
            .add(this.feedDetailsStep())
            .build();
    }

     dataTransformationSteps() :Step[] {

         return this.add(this.propertiesStep())
             .add(this.permissionStep())
             .add(this.sourceSampleStep())
             .add(this.wranglerStep())
             .add(this.targetTableStep())
             .add(this.feedDetailsStep())
             .build()
    }


     simpleFeedSteps() :Step[] {
         return this.add(this.propertiesStep())
             .add(this.permissionStep())
             .add(this.feedDetailsStep())
             .build()
    }

}

