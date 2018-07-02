
import * as angular from 'angular';
import * as _ from "underscore";
import "../../codemirror-require/module";
declare const CodeMirror: any;

var CodeMirrorRegex: any = (function () {
  function codeMirrorRegexFn(){
            var g: any = 0;
            var tokenBase: any = (stream: any)=> {
                var ch: any = stream.next();
                if (ch == "\\" && stream.match(/./, false)) {
                    if (stream.match(/u\w{4}/)) return "regex-esc";
                    if (stream.match(/u/)) return "regex-error";

                    if (stream.match(/x\w{2}/)) return "regex-esc";
                    if (stream.match(/x/)) return "regex-error";

                    if (stream.match(/[wWdDsS]/)) return "regex-charclass";
                    if (stream.match(/b/)) return "regex-anchor";

                    if (stream.match(/./)) return "regex-esc";

                    return "regex-esc";
                }

                if (ch == "+" || ch == "?" || ch == "*") {
                    return "regex-quant";
                }
                if (ch == "{") {
                    if (stream.match(/(\d|\d,\d?)\}/)) return "regex-quant";
                }

                if (ch == "[" && stream.match(/[^\]]+\]/)) {
                    return "regex-set";
                }

                if (ch == "|") {
                    return "regex-alt";
                }

                if (ch == "(") {
                    stream.match(/[\?\!\:]+/);
                    ++g;
                    return "regex-group";
                }
                if (ch == ")") {
                    if (g - 1 < 0) return "regex-error";
                    --g;
                    return "regex-group";
                }

                if (ch == "^" || ch == "$") {
                    return "regex-anchor";
                }

                if (ch == ".") {
                    return "regex-charclass"
                }

                if (ch == "/") {
                    return (stream.column() === 0 || stream.match(/[iu]*$/)) ? "regex-decorator" : "regex-error";
                }
            };

            return {
                startState:(base: any)=> { g = 0;},
                token: tokenBase
            };
        }

    CodeMirror.defineMode('regex', this.codeMirrorRegexFn());
    CodeMirror.defineMIME("text/x-regex", "regex");
});