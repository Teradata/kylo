import {ProfileOutputRow} from "..";

/**
 * Helper class for working with profile statistics
 */
export class ProfileHelper {

    data: Array<any> = [];
    private _profile: any;
    private _field: string;
    private _totalCount: string;
    private _unique: string;
    private _maxLen: string;
    private _minLen: string;
    private _percUnique: number;
    private _percEmpty: number;
    private _emptyCount: string;
    private _columnDataType: string;
    private _nullCount: string;
    private _percNull: number;
    private _max: string;
    private _min: string;
    private _sum: string;
    private _mean: string;
    private _stddev: string;
    private _variance: string;
    private _histo: object;
    private _validCount: number;
    private _invalidCount: string;
    private _showValid: boolean = false;

    constructor(fieldName:string, profileData:ProfileOutputRow[]) {
        this._profile = profileData;
        this._field = fieldName;
        this.initializeStats();
    }

    private initializeStats(): void {
        var self = this;
        for (let value of self._profile) {
            if (value.columnName == self._field) {

                switch (value.metricType) {
                    case 'TOP_N_VALUES':
                        let values = value.metricValue.split("^B");
                        for (let item in values) {
                            if (item != '') {
                                let e = item.split("^A");
                                self.data.push({domain: e[1], count: parseInt(e[2])});
                            }
                        };
                        break;
                    case 'TOTAL_COUNT':
                        self._totalCount = value.metricValue;
                        break;
                    case 'UNIQUE_COUNT':
                        self._unique = value.metricValue;
                        break;
                    case 'EMPTY_COUNT':
                        self._emptyCount = value.metricValue;
                        break;
                    case 'NULL_COUNT':
                        self._nullCount = value.metricValue;
                        break;
                    case 'COLUMN_DATATYPE':
                        self._columnDataType = value.metricValue;
                        break;
                    case 'MAX_LENGTH':
                        self._maxLen = value.metricValue;
                        break;
                    case 'MIN_LENGTH':
                        self._minLen = value.metricValue;
                        break;
                    case 'MAX':
                        self._max = value.metricValue;
                        break;
                    case 'MIN':
                        self._min = value.metricValue;
                        break;
                    case 'SUM':
                        self._sum = value.metricValue;
                        break;
                    case 'MEAN':
                        self._mean = value.metricValue;
                        break;
                    case 'STDDEV':
                        self._stddev = value.metricValue;
                        break;
                    case 'VARIANCE':
                        self._variance = value.metricValue;
                        break;
                    case 'HISTO':
                        self._histo = JSON.parse(value.metricValue);
                        break;
                    case 'VALID_COUNT':
                        self._validCount = value.metricValue;
                        self._showValid = true;
                        break;
                    case 'INVALID_COUNT':
                        self._invalidCount = value.metricValue;
                        self._showValid = true;
                        break;

                }
            }
        };
        if (this._unique != null) {
            this._percUnique = (parseInt(this._unique) / parseInt(this._totalCount))
        }
        if (this._emptyCount != null) {
            this._percEmpty = (parseInt(this._emptyCount) / parseInt(this._totalCount));
        }
        if (this._nullCount != null) {
            this._percNull = (parseInt(this._nullCount) / parseInt(this._totalCount));
        }
        if (this._showValid) {
            this._validCount = (parseInt(this._totalCount) - parseInt(this._invalidCount));
        }

    }

    getTopN(): Array<any> {
        return this.data;
    }

    get profile(): any {
        return this._profile;
    }

    get field(): string {
        return this._field;
    }

    get totalCount(): string {
        return this._totalCount;
    }

    get unique(): string {
        return this._unique;
    }

    get maxLen(): string {
        return this._maxLen;
    }

    get minLen(): string {
        return this._minLen;
    }

    get percUnique(): number {
        return this._percUnique;
    }

    get percEmpty(): number {
        return this._percEmpty;
    }

    get emptyCount(): string {
        return this._emptyCount;
    }

    get columnDataType(): string {
        return this._columnDataType;
    }

    get nullCount(): string {
        return this._nullCount;
    }

    get percNull(): number {
        return this._percNull;
    }

    get max(): string {
        return this._max;
    }

    get min(): string {
        return this._min;
    }

    get sum(): string {
        return this._sum;
    }

    get mean(): string {
        return this._mean;
    }

    get stddev(): string {
        return this._stddev;
    }

    get variance(): string {
        return this._variance;
    }

    get histo(): object {
        return this._histo;
    }

    get validCount(): number {
        return this._validCount;
    }

    get invalidCount(): string {
        return this._invalidCount;
    }

    get showValid(): boolean {
        return this._showValid;
    }
}