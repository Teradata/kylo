

angular.module(MODULE_FEED_MGR).factory('FeedTagService', function () {


    /**
     * Create filter function for a query string
     */
    function createFilterFor(query) {
        var lowercaseQuery = angular.lowercase(query);
        return function filterFn(tag) {
           return (tag._lowername.indexOf(lowercaseQuery) === 0);
        };
    }
    var data = {
        querySearch: function (query) {
            var self = this;
        var tags = self.loadAvailableTags();
        var results = query ? tags.filter(createFilterFor(query)) : [];
        return results;
    },
    loadAvailableTags: function () {

        var data = [
            {
                'name': 'Data Ingest'
            },
            {
                'name': 'People'
            },
            {
                'name': 'HR'
            },
            {
                'name': 'Enterprise'
            }

        ];
        return data.map(function(tag){
            tag._lowername = tag.name.toLowerCase();
            return tag;
        })
    }
};
    return data;



});