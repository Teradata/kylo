import {Subject} from "rxjs/Subject";
import {DataSource} from "../../catalog/api/models/datasource";
import {PartialObserver} from "rxjs/Observer";

export class DataSourceChangedEvent {
    constructor(public dataSource:DataSource,public params:any){}
}

export class DatasetPreviewStepperService{


    public dataSource$ = new Subject<DataSourceChangedEvent>();
    public dataSource:DataSource;
    public dataSourceParams:any;
    public stepIndex:number;

    public stepChanged$ = new Subject<number>();

    public setDataSource(dataSource:DataSource, params?:any) {
     this.dataSource = dataSource;
     this.dataSourceParams = params;
     this.dataSource$.next(new DataSourceChangedEvent(dataSource,params));
    }

    public subscribeToDataSourceChanges(o:PartialObserver<DataSourceChangedEvent>){
        return this.dataSource$.subscribe(o);
    }

    public subscribeToStepChanges(o:PartialObserver<number>){
        return this.stepChanged$.subscribe(o);
    }

    public setStepIndex(index:number){
        if(this.stepIndex == undefined || this.stepIndex != index){
            this.stepIndex = index;
            this.stepChanged$.next(index);
        }
    }




}