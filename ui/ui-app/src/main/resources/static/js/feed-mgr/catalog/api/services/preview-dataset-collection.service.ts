import * as _ from "underscore";
import {DatasetCollectionStatus, PreviewDataSet} from "../../datasource/preview-schema/model/preview-data-set";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";
import {Subject} from "rxjs/Subject";
import {PreviewDataSetRequest} from "../../datasource/preview-schema/model/preview-data-set-request";
import {HttpClient} from "@angular/common/http";
import {PartialObserver} from "rxjs/Observer";
import {ISubscription} from "rxjs/Subscription";



export class DatasetChangeEvent{
    public totalDatasets:number = 0;
    constructor(public dataset:PreviewDataSet, public allDataSets:PreviewDataSet[]) {
        if(allDataSets && allDataSets.length){
            this.totalDatasets = allDataSets.length;
        }
        else {
            this.totalDatasets = 0;
        }
    }

}
/**
 * This is a joined service loaded by the root /services module that both Visual Query and Catalog use
 *
 */
@Injectable()
export class PreviewDatasetCollectionService {

    /**
     * Allow other components to listen for changes to the dataSet
     * Subscribe in another component like the following:
     * this.previewDatasetCollectionService.datasets$.subscribe(this.onDataSetCollectionChanged.bind(this))
     *
     * onDataSetCollectionChanged(dataSets:PreviewDataSet[]) { ...}
     */
    public datasets$: Observable<DatasetChangeEvent>;

    /**
     * The datasets subject for listening
     */
    private datasetsSubject: Subject<DatasetChangeEvent>;


    datasets:PreviewDataSet[];

    id:string;

    constructor(private http: HttpClient){
        this.datasets = [];
        this.datasetsSubject = new Subject<DatasetChangeEvent>();
        this.datasets$ = this.datasetsSubject.asObservable();
        this.id = _.uniqueId("previewDatasetCollection-")

    }

    public subscribeToDatasetChanges(observer:PartialObserver<DatasetChangeEvent>):ISubscription{
        return this.datasetsSubject.subscribe(observer);
    }

    public reset(){

        this.datasets.forEach(dataset => dataset.collectionStatus = DatasetCollectionStatus.REMOVED);
        this.datasets = [];
    }

    /**
     * Add a dataset to the collection
     * @param {PreviewDataSet} dataset
     *
     *
     * TODO build schema out of dataset.headers.properties
     */
    public addDataSet(dataset:PreviewDataSet){
        //only add if it doesnt exist yet
        if(!this.exists(dataset)) {

            this.datasets.push(dataset);
            dataset.collectionStatus = DatasetCollectionStatus.COLLECTED;
            //notify the observers of the change
            this.datasetsSubject.next(new DatasetChangeEvent(dataset,this.datasets));
          }
          else {
            let existingDataset = this.findByKey(dataset.key);
            if(!existingDataset.hasPreview() && dataset.hasPreview()){
                //update the preview
                existingDataset.preview = dataset.preview;
                //notify???
            }
            if(existingDataset.schema == undefined && dataset.schema != undefined){
                existingDataset.schema = dataset.schema;
            }
        }
    }

    /**
     * Does the dataset already exist?
     *
     * @param {PreviewDataSet} dataset
     * @return {boolean}
     */
    public exists(dataset:PreviewDataSet):boolean{
    let key = dataset.key;
    let existingDataSets = this.datasets.filter((ds)=> ds.key == key);
    return(existingDataSets != undefined && existingDataSets.length == 1);
    }

    private findByKey(key:string){
      let items =  this.datasets.filter((ds)=> ds.key == key);
      if(items != null && items != undefined && items.length >0){
          return items[0];
      }
      return null;
    }

    public findByPath(path:string):PreviewDataSet[]{
        return this.datasets.filter((ds) => ds.getPreviewItemPath() == path);
    }

    /**
     * remove a data set from the collection
     * @param {PreviewDataSet} dataset
     */
    public remove(dataset: PreviewDataSet){
        var key = dataset.key;
        var collectedDataSet = this.findByKey(key);
        if(collectedDataSet != null) {
            var index = this.datasets.indexOf(collectedDataSet);
            if (index >= 0) {
                this.datasets.splice(index, 1)
                dataset.collectionStatus = DatasetCollectionStatus.REMOVED;
                //notify the observers of the change
                this.datasetsSubject.next(new DatasetChangeEvent(dataset,this.datasets));
            }
        }

    }

    public datasetCount():number {
        return this.datasets.length;
    }

    /**
     * get the collection back as a SparkDataSet for the Wrangler
     * @return {SparkDataSet[]}
     */
    getSparkDataSets(){
        return this.datasets.map((dataset)=> dataset.toSparkDataSet());
    }



}
