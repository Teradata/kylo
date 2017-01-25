/*-
 * #%L
 * thinkbig-ui-common
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
