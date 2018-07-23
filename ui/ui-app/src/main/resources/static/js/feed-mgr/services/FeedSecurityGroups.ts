
export class FeedSecurityGroups {

 static $inject = ["$http","$q","RestUrlService"]
constructor (private $http:any, private $q:any, private restUrlService:any) { }

    /**
     * Create filter function for a query string
     */
    private createFilterFor(query:string) {
        var lowercaseQuery = query ? query.toLowerCase() : '';
        return function filterFn(tag:any) {
            return (tag._lowername.indexOf(lowercaseQuery) === 0);
        };
    }

        querySearch (query:any) {
            return this.loadAvailableGroups(query);
        }
        loadAvailableGroups(query:string) {

            var securityGroups = this.$http.get(this.restUrlService.HADOOP_SECURITY_GROUPS)
                .then(function (dataResult:any) {
                        let lowerGroups = dataResult.data.map(function (tag:any) {
                            tag._lowername = tag.name.toLowerCase();
                            return tag;
                        });
                        var results = query ? lowerGroups.filter(this.createFilterFor(query)) : [];
                        return results;
                    },
                    function (error:any) {
                        console.log('Error retrieving hadoop authorization groups');
                    });
            return securityGroups;
        }
        isEnabled() {
            var isEnabled = this.$http.get(this.restUrlService.HADOOP_AUTHORIZATATION_BASE_URL + "/enabled")
                .then(function (dataResult:any) {
                        return dataResult.data[0].enabled;
                    },
                    function (error:any) {
                        console.log('Error retrieving hadoop authorization groups');
                    });
            return isEnabled;
        }
 }