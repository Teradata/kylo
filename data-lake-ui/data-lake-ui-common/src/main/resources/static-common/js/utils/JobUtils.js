

var JobUtils = (function () {
    function JobUtils() {
    }
    JobUtils.filterStatusMap = {'All':'','Active':'STARTED,RUNNING,FAILED','Running':'STARTED','Waiting':'STARTING','Completed':'COMPLETED','Failed':'FAILED','Stopped':'STOPPED','Abandoned':'ABANDONED'};
    JobUtils.statusFilterMap = function() {
        var map = {};
        $.each(JobUtils.filterStatusMap,function(filter,status){
            var arr = status.split(',');
            if(arr.length ==1){
                map[status] = filter;
            }
        });
        return map;
    }
    return JobUtils;
})();
