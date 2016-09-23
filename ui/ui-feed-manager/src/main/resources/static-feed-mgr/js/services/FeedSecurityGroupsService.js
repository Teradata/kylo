

angular.module(MODULE_FEED_MGR).factory('FeedSecurityGroups', function ($http, $q, RestUrlService) {


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
        var groups = self.loadAvailableGroups(query);
        return groups;
    },
        loadAvailableGroups: function (query) {

        var securityGroups = $http.get(RestUrlService.HADOOP_SECURITY_GROUPS)
            .then(function(dataResult) {
                lowerGroups = dataResult.data.map(function(tag){
                    tag._lowername = tag.name.toLowerCase();
                    return tag;
                });
                var results = query ? lowerGroups.filter(createFilterFor(query)) : [];
                return results;
            },
            function(error) {
                console.log('Error retrieving hadoop authorization groups');
            });
        return securityGroups;
    }
};
    return data;



});