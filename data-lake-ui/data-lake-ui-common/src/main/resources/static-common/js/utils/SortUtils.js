

/*
 * Copyright (c) 2015.
 */

var SortUtils = (function () {
    function SortUtils() {
    }


    SortUtils.sort = function(arr,attr, desc) {

        var retObj = arr.sort(function (a, b) {
            //Sort numeric values
            if (typeof (a[attr]) === "number") {

                return parseInt(a[attr]) > parseInt(b[attr]) ? 1 : -1;
            }
            //sort string values
            else {
                return a[attr] > b[attr] ? 1 : -1;
            }
        });
        //If sort is descending
        if (desc) {
            return retObj.reverse();
        }
        else {
            return retObj;
        }
    }

    return SortUtils;
})();


