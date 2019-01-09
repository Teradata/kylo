import {ItemSaveResponse} from '../../../../../shared/info-item/item-save-response';
import {SparkDataSet} from '../../../../../model/spark-data-set.model';

export class DatasetSaveResponse extends ItemSaveResponse {
    constructor(public dataset: SparkDataSet, private isSuccess: boolean, msg: string) {
        super(isSuccess, msg);
    }

}
