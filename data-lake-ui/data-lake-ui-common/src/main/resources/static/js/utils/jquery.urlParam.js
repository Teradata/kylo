/**
 * get params from url address bar
 * example.com?param1=name&param2=&id=6

 $.urlParam('param1'); // name
 $.urlParam('id');        // 6
 $.urlParam('param2');   // null

 * @param name
 * @returns {*}
 */
(function ($) {
$.urlParam = function(name){
    var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
    if (results==null){
        return null;
    }
    else{
        return results[1] || 0;
    }
}
})(jQuery);