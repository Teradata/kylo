import {FileMetadata} from "./file-metadata";
import {PreviewFileDataSet} from "./preview-file-data-set";
import {Common} from '../../../../../../lib/common/CommonTypes';

/**
 *  Response object that will create new FileDataSet objects
 */
export class FileMetadataResponse {
    message:string;
    fileMetadata:FileMetadata[];
    datasets:Common.Map<PreviewFileDataSet>;

    datasetList:PreviewFileDataSet[] = [];

    public constructor(init?:Partial<FileMetadataResponse>) {
        this.message = init.message;
        this.fileMetadata = init.fileMetadata
        if(init.datasets){
            this.datasets = {};
            Object.keys(init.datasets).forEach(key => {
                let datasetData = init.datasets[key];
                datasetData.key = key;
                let dataset = new PreviewFileDataSet(datasetData);
                this.datasets[key] = dataset;
                this.datasetList.push(dataset)
            });
        }
    }
}

/**
 * Response  wrapper from the spark job that identifies the file metadata
 */
export class FileMetadataTransformResponse {

    /**
     * Error message
     */
    message: string;
    /**
     * Progress of the transformation
     */
    progress: number;

    /**
     * Result of a transformation
     */
    results: FileMetadataResponse;

    /**
     * Success status of a transformation
     */
    status: string;

    /**
     * Table name with the results
     */
    table: string;

    /**
     * Actual number of rows analyzed
     */
    actualRows: number;

    /**
     * Actual number of cols analyzed
     */
    actualCols: number;

    public constructor(init?:Partial<FileMetadataTransformResponse>) {
        Object.assign(this, init);
        if(init.results){
            this.results = new FileMetadataResponse(init.results);
        }
    }
}
