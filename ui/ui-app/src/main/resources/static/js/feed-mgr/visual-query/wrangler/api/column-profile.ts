import {ProfileOutputRow} from "..";
import * as _ from "underscore";

export class ColumnProfileHelper {

    static createColumnProfiles(profileData: ProfileOutputRow[]): ColumnProfile[] {
        let result: ColumnProfile[] = [];
        let profileColumns = _.groupBy(profileData, (v:ProfileOutputRow)=>{
            return v.columnName;
        });

        _.each(profileColumns, function(values:ProfileOutputRow[], key:string) {
            if (key != '(ALL)') {
                result.push(new ColumnProfile(key, values));
            }
        });
        return result;
    }
}

/**
 * Helper class for working with profile statistics
 */
export class ColumnProfile {

    /**
     * Sorted top n values
     */
    topN: Array<any> = [];

    /**
     * Raw profile array
     */
    private _profile: any;

    field: string;
    totalCount: string;
    unique: string;
    maxLen: string;
    minLen: string;
    percUnique: number;
    percEmpty: number;
    emptyCount: string;
    columnDataType: string;
    columnDataTypePlain: string;
    nullCount: string;
    percNull: number;
    max: string;
    min: string;
    sum: string;
    mean: string;
    stddev: string;
    variance: string;
    histo: object;
    validCount: number;
    invalidCount: string;
    showValid: boolean = false;

    constructor(fieldName: string, profileData: ProfileOutputRow[]) {
        this._profile = profileData;
        this.field = fieldName;
        this.initializeStats();
    }

    private initializeStats(): void {
        var self = this;
        for (let value of self._profile) {
            if (value.columnName == self.field) {

                switch (value.metricType) {
                    case 'TOP_N_VALUES':
                        let values = value.metricValue.split("^B");
                        for (let item of values) {
                            if (item != '') {
                                let e = item.split("^A");
                                self.topN.push({domain: e[1], count: parseInt(e[2])});
                            }
                        }
                        ;
                        break;
                    case 'TOTAL_COUNT':
                        self.totalCount = value.metricValue;
                        break;
                    case 'UNIQUE_COUNT':
                        self.unique = value.metricValue;
                        break;
                    case 'EMPTY_COUNT':
                        self.emptyCount = value.metricValue;
                        break;
                    case 'NULL_COUNT':
                        self.nullCount = value.metricValue;
                        break;
                    case 'COLUMN_DATATYPE':
                        self.columnDataType = value.metricValue + "";
                        self.columnDataTypePlain = self.columnDataType.replace("Type", "").toLowerCase();
                        break;
                    case 'MAX_LENGTH':
                        self.maxLen = value.metricValue;
                        break;
                    case 'MIN_LENGTH':
                        self.minLen = value.metricValue;
                        break;
                    case 'MAX':
                        self.max = value.metricValue;
                        break;
                    case 'MIN':
                        self.min = value.metricValue;
                        break;
                    case 'SUM':
                        self.sum = value.metricValue;
                        break;
                    case 'MEAN':
                        self.mean = value.metricValue;
                        break;
                    case 'STDDEV':
                        self.stddev = value.metricValue;
                        break;
                    case 'VARIANCE':
                        self.variance = value.metricValue;
                        break;
                    case 'HISTO':
                        self.histo = JSON.parse(value.metricValue);
                        break;
                    case 'VALID_COUNT':
                        self.validCount = value.metricValue;
                        self.showValid = true;
                        break;
                    case 'INVALID_COUNT':
                        self.invalidCount = value.metricValue;
                        self.showValid = true;
                        break;

                }
            }
        }
        ;
        if (this.unique != null) {
            this.percUnique = (parseInt(this.unique) / parseInt(this.totalCount)) * 100;
        }
        if (this.emptyCount != null) {
            this.percEmpty = (parseInt(this.emptyCount) / parseInt(this.totalCount)) * 100;
        }
        if (this.nullCount != null) {
            this.percNull = (parseInt(this.nullCount) / parseInt(this.totalCount)) * 100;
        }
        if (this.showValid) {
            this.validCount = (parseInt(this.totalCount) - parseInt(this.invalidCount));
        }

        // populate metrics
        if (this.topN && this.topN.length > 0) {
            this.topN.sort(this.compare);
        }
    }

    /**
     * Comparator function for model reverse sort
     */
    compare(a: any, b: any): number {
        if (a.count < b.count)
            return 1;
        if (a.count > b.count)
            return -1;
        return 0;
    }


}