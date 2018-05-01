import "fattable";
import {WranglerDataService} from "../services/wrangler-data.service";

export class TableCache {
    size: number = 100;
    data: object = {};
    lru_keys: string[] = [];

    constructor(size: number) {
        this.size = size;
    }

    has(k: string): boolean {
        return this.data.hasOwnProperty(k);
    }

    get(k: string): any {
        return this.data[k];
    }

    clear(): void {
        this.data = {};
        this.lru_keys = [];
    }

    set(k: string, v: any) {
        let idx: number, removeKey: string;
        idx = this.lru_keys.indexOf(k);
        if (idx >= 0) {
            this.lru_keys.splice(idx, 1);
        }
        this.lru_keys.push(k);
        if (this.lru_keys.length >= this.size) {
            removeKey = this.lru_keys.shift();
            delete this.data[removeKey];
        }
        return this.data[k] = v;
    };
}


export class WranglerTableModel extends fattable.TableModel {

    cacheSize: number = 100;
    pageCache: TableCache;
    headerPageCache: TableCache;
    headerFetchCallbacks: any;
    fetchCallbacks: any;

    constructor(private data: WranglerDataService) {
        super();
        this.pageCache = new TableCache(this.cacheSize);
        this.headerPageCache = new TableCache(this.cacheSize);
        this.fetchCallbacks = {};
        this.headerFetchCallbacks = {};
    }

    hasCell(i: number, j: number): boolean {
        var pageName: string;
        pageName = this.cellPageName(i, j);
        return this.pageCache.has(pageName);
    };

    getCell(i: number, j: number, cb: any) {
        var pageName: string;
        var self = this;
        if (cb == null) {
            cb = (function (data: any) {
            });
        }
        pageName = this.cellPageName(i, j);
        if (this.pageCache.has(pageName)) {
            return cb(this.pageCache.get(pageName)(i, j));
        } else if (this.fetchCallbacks[pageName] != null) {
            return this.fetchCallbacks[pageName].push([i, j, cb]);
        } else {
            this.fetchCallbacks[pageName] = [[i, j, cb]];
            return this.fetchCellPage(pageName, (function (self) {
                return function (page: any) {
                    var len, n, ref, ref1;
                    self.pageCache.set(pageName, page);
                    ref = self.fetchCallbacks[pageName];
                    for (n = 0, len = ref.length; n < len; n++) {
                        ref1 = ref[n], i = ref1[0], j = ref1[1], cb = ref1[2];
                        cb(page(i, j));
                    }
                    return delete self.fetchCallbacks[pageName];
                };
            })(this));
        }
    };

    getHeader(j: number, cb: any): void {
        cb(this.data.getHeader(j));
    };

    cellPageName(i: number, j: number): string {
        return this.data.cellPageName(i, j);
    }

    fetchCellPage(pageName: string, cb: any): void {
        this.data.fetchCellPage(pageName, cb);
    }

    headerPageName(j: number): string {
        return this.data.headerPageName(j);
    };


}