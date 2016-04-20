

angular.module(MODULE_FEED_MGR).factory('FeedSecurityGroups', function () {


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
        var groups = self.loadAvailableGroups();
        var results = query ? groups.filter(createFilterFor(query)) : [];
        return results;
    },
        loadAvailableGroups: function () {

        var data = [
            {
                'name': 'root'
            },
            {
                'name': 'admin'
            },
            {
                'name': 'hive'
            },
            {
                'name': 'Marketing'
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