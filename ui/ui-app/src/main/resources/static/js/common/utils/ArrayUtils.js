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



var ArrayUtils = (function () {
    function ArrayUtils() {
    }
    ArrayUtils.sum = function(arr) {
      return arr.reduce(
          function(total, num){ return total + num }
          , 0);
    }
    ArrayUtils.avg = function(arr){
        var sum = ArrayUtils.sum(arr);
        return sum / arr.length;
    }
    ArrayUtils.min = function(arr){
        return Math.min.apply(null,arr);
    }
    ArrayUtils.max = function(arr){
        return Math.max.apply(null,arr);
    }
    ArrayUtils.first = function(arr){
        return arr[0];
    }
    ArrayUtils.last = function(arr){
        return arr[arr.length-1];
    }
    ArrayUtils.aggregrate = function(arr,fn){
        if(arr === undefined){
            arr = [];
        }
         fn = fn.toLowerCase();
        if(fn == 'max'){
            return ArrayUtils.max(arr);
        }
        else  if(fn == 'min'){
            return ArrayUtils.min(arr);
        }
        else if(fn == 'sum'){
            return ArrayUtils.sum(arr);
        }
        else if(fn == 'avg'){
            return ArrayUtils.avg(arr);
        }
        else  if(fn == 'first'){
            return ArrayUtils.first(arr);
        }
        else  if(fn == 'last'){
            return ArrayUtils.last(arr);
        }
        else {
            return undefined;
        }
    }


    return ArrayUtils;
})();
