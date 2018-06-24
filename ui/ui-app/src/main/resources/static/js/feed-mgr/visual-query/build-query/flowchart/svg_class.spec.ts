describe('svg_class', ()=>{
    var nullVar: any= null;
    it('removeClassSVG returns false when there is no classes attr', ()=> {
        var mockElement = {
            attr: function() {
                return nullVar;
            },
        };
        var testClass: any = 'foo';
        expect(removeClassSVG(mockElement, testClass)).toBe(false);
    });
    it('removeClassSVG returns false when the element doesnt already have the class', ()=>{
        var mockElement: any = {
            attr: function () {
                return 'smeg';
            },
        };
        var testClass: any = 'foo';
        expect(removeClassSVG(mockElement, testClass)).toBe(false);
    });
    xit('removeClassSVG returns true and removes the class when the element does have the class', ()=>{
        var testClass: any = 'foo';
        var mockElement: any = {
            attr: ()=> {
                return testClass;
            },
        };
        spyOn(mockElement, 'attr').and.callThrough();
        expect(removeClassSVG(mockElement, testClass)).toBe(true);
        expect(mockElement.attr).toHaveBeenCalledWith('class', '');
    });
    it('hasClassSVG returns false when attr returns null', ()=> {
        var mockElement: any = {
            attr: ()=> {
                return nullVar;
            },
        };
        var testClass: any = 'foo';
        expect(hasClassSVG(mockElement, testClass)).toBe(false);
    });
    it('hasClassSVG returns false when element has no class', ()=>{
        var mockElement: any = {
            attr: ()=> {
                return '';
            },
        };
        var testClass: any = 'foo';
        expect(hasClassSVG(mockElement, testClass)).toBe(false);
    });
    it('hasClassSVG returns false when element has wrong class', ()=> {
        var mockElement: any = {
            attr: ()=>{
                return 'smeg';
            },
        };
        var testClass: any = 'foo';
        expect(hasClassSVG(mockElement, testClass)).toBe(false);
    });
    it('hasClassSVG returns true when element has correct class', ()=>{
        var testClass: any = 'foo';
        var mockElement: any = {
            attr: ()=>{
                return testClass;
            },
        };
        expect(hasClassSVG(mockElement, testClass)).toBe(true);
    });    
    it('hasClassSVG returns true when element 1 correct class of many ',()=>{
        var testClass: any = 'foo';
        var mockElement: any = {
            attr: ()=>{
                return "whar " + testClass + " smeg";
            },
        };
        expect(hasClassSVG(mockElement, testClass)).toBe(true);
    });        
});