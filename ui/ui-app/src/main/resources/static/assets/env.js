(function (window) {
    window.__env = window.__env || {};
    var themes = {
        "definePalette":[ {name:'primaryBlue',details:{ '500': '3483BA',
                '900':'2B6C9A'},extend:"blue"},
            {name:'accentOrange',details:{'A200': 'F08C38'},extend:"orange"},
        ],
        "primaryPalette": {
            name:"primaryBlue",
            details:{ 'hue-2':'900'}
        },
        "accentPalette": {
            name:"accentOrange"
        },
        //,
        "warnPalette": {
        },
        "backgroundPalette": { }
    }
    window.__env.themes = themes;

}(this));