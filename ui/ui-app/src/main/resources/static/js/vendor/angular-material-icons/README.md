This is not using a bower include because a slight modification was needed to add a callback to the svg morph function to clear the fill style from the svg path.

Before:

try {
        //ADDED callback to morph to allow for svg to clear the fill style
        // this block will succeed if SVGMorpheus is available
        new SVGMorpheus(element.children()[0]).to(newicon, options, function () {
            element.find('path[fill="rgba(-1,-1,-1,0)"]').attr('fill', '')
        });
    }

After:

try {
        // this block will succeed if SVGMorpheus is available
        new SVGMorpheus(element.children()[0]).to(newicon, options);
    }