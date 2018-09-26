(function (window) {
    window.__env = window.__env || {};
    var themes = {
        "primaryPalette":{name:'teal',details:{ 'default':'700'}},
        "accentPalette": {name:'orange',details:{'default':'800','hue-1':'A100','hue-2': 'A400'}},
        "warnPalette": {name:'red',details:{'default':'600'}},
        "backgroundPalette": { }
    }
    window.__env.themes = themes;

}(this));