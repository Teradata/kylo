import {Templates} from "../../../lib/feed-mgr/services/TemplateTypes";

export class FeedDetailsProcessorRenderingHelper {

    GET_TABLE_DATA_PROCESSOR_TYPE = "com.thinkbiganalytics.nifi.GetTableData";
    GET_TABLE_DATA_PROCESSOR_TYPE2 = "com.thinkbiganalytics.nifi.v2.ingest.GetTableData";
    WATERMARK_PROCESSOR = 'com.thinkbiganalytics.nifi.v2.core.watermark.LoadHighWaterMark';
    SQOOP_PROCESSOR = 'com.thinkbiganalytics.nifi.v2.sqoop.core.ImportSqoop';

    isSqoopProcessor(processor: Templates.Processor) {
        return this.SQOOP_PROCESSOR == processor.type;
    }

    isGetTableDataProcessor(processor: Templates.Processor) {
        return this.GET_TABLE_DATA_PROCESSOR_TYPE == processor.type || this.GET_TABLE_DATA_PROCESSOR_TYPE2 == processor.type;
    }

    isWatermarkProcessor(processor: Templates.Processor) {
        return this.WATERMARK_PROCESSOR == processor.type;
    }

    isRenderProcessorGetTableDataProcessor(inputProcessor: any) {
        let render = false;
        //if the processor to check is GetTable Data it should be rendered only if it is the input, or if the input is watermark
        if (inputProcessor != undefined && (this.isGetTableDataProcessor(inputProcessor) || this.isWatermarkProcessor(inputProcessor))) {
            render = true;
        }

        return render;
    }

    isRenderSqoopProcessor(inputProcessor: Templates.Processor) {
        let render = false;
        //if the processor to check is GetTable Data it should be rendered only if it is the input, or if the input is watermark
        if (inputProcessor != undefined && (this.isSqoopProcessor(inputProcessor) || this.isWatermarkProcessor(inputProcessor))) {
            render = true;
        }

        return render;
    }

    updateGetTableDataRendering(inputProcessor: Templates.Processor, nonInputProcessors: Templates.Processor[]) {
        let renderGetData = this.isRenderProcessorGetTableDataProcessor(inputProcessor);
        let getTableDataProcessors = nonInputProcessors.filter((processor: Templates.Processor) => this.isGetTableDataProcessor(processor));
        getTableDataProcessors.forEach((processor: any) => processor.userEditable = renderGetData);
        //if the flow starts with the watermark and doesnt have a downstream getTableData then dont render the table
        if (renderGetData && this.isWatermarkProcessor(inputProcessor) && getTableDataProcessors.length == 0) {
            renderGetData = false;
        }
        return renderGetData;
    }

    updateSqoopProcessorRendering(inputProcessor: Templates.Processor, nonInputProcessors: Templates.Processor[]) {
        let render = this.isRenderSqoopProcessor(inputProcessor);
        let sqoopProcessors = nonInputProcessors.filter((processor: Templates.Processor) => this.isRenderSqoopProcessor(processor));
        sqoopProcessors.forEach((processor: any) => processor.userEditable = render);
        return render;
    }
}
