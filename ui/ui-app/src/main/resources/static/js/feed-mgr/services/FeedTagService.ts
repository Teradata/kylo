import { Injectable } from "@angular/core";

@Injectable()
export class FeedTagService {

    constructor() {

    }
    /**
     * Create filter function for a query string
     */
    createFilterFor(query: any) {
        var lowercaseQuery = typeof query === 'string' ? query.toLowerCase() : query;
        return (tag: any) => {
            return (tag._lowername.indexOf(lowercaseQuery) === 0);
        };
    }

    querySearch(query: any) {
        var self = this;
        var tags = self.loadAvailableTags();
        var results = query ? tags.filter(this.createFilterFor(query)) : [];
        return results;
    };
    loadAvailableTags() {

        var data: any = [];
        return data.map(function (tag: any) {
            tag._lowername = tag.name.toLowerCase();
            return tag;
        })
    }

}
