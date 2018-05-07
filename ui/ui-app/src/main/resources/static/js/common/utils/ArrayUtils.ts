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



var ArrayUtils: any = (function () {
    function ArrayUtils() {
    }
    (ArrayUtils as any).sum = (arr: any)=>{
      return [].reduce.call(arr,
          function(total: any, num: any){ return total + num; }
          , 0);
    }
    (ArrayUtils as any).avg =(arr: any)=>{
        var sum = (ArrayUtils as any).sum(arr);
        return sum / arr.length;
    }
    (ArrayUtils as any).min = (arr: any)=>{
        return Math.min.apply(null,arr);
    }
    (ArrayUtils as any).max = (arr: any)=>{
        return Math.max.apply(null,arr);
    }
    (ArrayUtils as any).first = (arr: any)=>{
        return arr[0];
    }
    (ArrayUtils as any).last = (arr: any) =>{
        return arr[arr.length-1];
    }
    (ArrayUtils as any).aggregrate = (arr: any,fn: any) =>{
        if(arr === undefined){
            arr = [];
        }
         fn = fn.toLowerCase();
        if(fn == 'max'){
            return (ArrayUtils as any).max(arr);
        }
        else  if(fn == 'min'){
            return (ArrayUtils as any).min(arr);
        }
        else if(fn == 'sum'){
            return (ArrayUtils as any).sum(arr);
        }
        else if(fn == 'avg'){
            return (ArrayUtils as any).avg(arr);
        }
        else  if(fn == 'first'){
            return (ArrayUtils as any).first(arr);
        }
        else  if(fn == 'last'){
            return (ArrayUtils as any).last(arr);
        }
        else {
            return undefined;
        }
    }


    return ArrayUtils;
})();
