import {Dataset} from '../../api/models/dataset';
import {ItemSaveResponse} from '../../../shared/info-item/item-save-response';

export class DatasetSaveResponse extends ItemSaveResponse {
    constructor(public dataset: Dataset, private isSuccess: boolean, msg: string) {
        super(isSuccess, msg);
    }

}
