package com.loadtestgo.util;

import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Parse JavaScript doc files and generate comment metadata.
 */
public class JavaScriptDocParserTest {
    // /*[^*].**/                   - <comment>
    // /**[^*].**/                  - <docs markdown>
    // [a-zA-Z_$][a-zA-Z0-9_$]      - <identifier>
    // <identifier> . <identifier>  - member
    // <identifier>()               - function call
    // <lvalue> = <rvalue>          - assignment
    // <member> || <identifier>     - lvalue
    // { }
    public static class JavaScriptDocParser {
        String doc;
        int docIndex;
        int line;
        int column;
        StringBuilder currentComments;
        StringBuilder identifier;
        Object top = new Object();

        public static class Link {
        }

        public static class Example {
        }

        public static class Throws {
        }

        public static class Param {
            String name;
            String type;
            String doc;
        }

        public static class Return {
            String type;
            String doc;
        }

        public static class Function extends Object {
            // Markdown docs for function
            public String doc;

            public String name;

            public List<Param> params;
            boolean varargs;

            public Throws exceptions;
            public Return returnVal;

            public List<Example> examples;

            public List<Link> seeAlso;
        }

        public static class Object {
            public String doc;
            public Map<String,Object> members = new HashMap<>();
        }

        public JavaScriptDocParser() {

        }

        public void parse(String doc) throws ParserError {
            this.doc = doc;
            while (docIndex < doc.length()) {
                char ch = nextChar();
                if (isWhiteSpace(ch)) {
                    continue;
                } else if (ch == '/') {
                    commentStart();
                } else if (ch == '\\') {
                    unicodeStart();
                } else if (isIdStart(ch)) {
                    identifierRead(ch);
                }
            }
        }

        private void unicodeStart() throws ParserError {
            boolean unicodeStart = true;
            while (docIndex < doc.length()) {
                char ch = nextChar();
                if (unicodeStart) {
                    if (ch == 'u') {
                        unicodeStart = false;
                    } else {
                        throwParserError("Unknown escape char for identifier.");
                    }
                } else {
                    identifierRead(ch);
                    break;
                }
            }
        }

        private void identifierRead(char ch) {
            identifier = new StringBuilder();
            identifier.append(ch);
            while (docIndex < doc.length()) {
                ch = nextChar();
                if (isIdContinue(ch)) {
                    identifier.append(ch);
                } else {
                    System.out.println(identifier.toString());
                    return;
                }
            }
        }

        private boolean isIdStart(char ch) {
            return ch >= 'a' && ch <= 'z' ||
                ch >= 'A' && ch <= 'Z' ||
                ch == '$' || ch == '_';
        }

        private boolean isIdContinue(char ch) {
            return (ch >= 'a' && ch <= 'z') ||
                (ch >= 'A' && ch <= 'Z') ||
                (ch >= '0' && ch <= '9') ||
                ch == '$' || ch == '_';
        }

        private void commentStart() throws ParserError {
            char ch = nextChar();
            if (ch == '/') {
                commentLine();
            } else if (ch == '*') {
                commentMultiLine();
            } else {
                throwParserError("Expecting a comment (/* or //)");
            }
        }

        enum MultiLineState {
            BEGIN,
            NORMAL_COMMENT,
            DOC_COMMENT,
            DOC_COMMENT_NEW_LINE,
            DOC_COMMENT_STAR,
            STAR
        }

        private void commentMultiLine() throws ParserError {
            currentComments = new StringBuilder();
            MultiLineState state = MultiLineState.BEGIN;
            while (docIndex < doc.length()) {
                char ch = nextChar();
                switch (state) {
                    case BEGIN:
                        if (ch == '*') {
                            state = MultiLineState.DOC_COMMENT_STAR;
                        } else {
                            state = MultiLineState.NORMAL_COMMENT;
                        }
                        break;
                    case DOC_COMMENT_STAR:
                        if (ch == '/') {
                            return;
                        }
                        if (currentComments.length() > 0) {
                            currentComments.append('*');
                        }
                        state = docCommentParse(MultiLineState.DOC_COMMENT, ch);
                        break;
                    case STAR:
                        if (ch == '/') {
                            return;
                        }
                        state = MultiLineState.NORMAL_COMMENT;
                        break;
                    case DOC_COMMENT:
                        state = docCommentParse(state, ch);
                        break;
                    case NORMAL_COMMENT:
                        if (ch == '*') {
                            state = MultiLineState.STAR;
                        }
                        break;
                    case DOC_COMMENT_NEW_LINE:
                        if (ch == '*') {
                            state = MultiLineState.DOC_COMMENT_STAR;
                        } else if (!isWhiteSpace(ch)) {
                            currentComments.append(ch);
                        }
                        break;
                }

            }
            throwParserError("Unterminated /*");
        }

        private MultiLineState docCommentParse(MultiLineState state, char ch) {
            if (ch == '*') {
                state = MultiLineState.DOC_COMMENT_STAR;
            } else if (ch == '\n') {
                state = MultiLineState.DOC_COMMENT_NEW_LINE;
                currentComments.append(ch);
            } else {
                currentComments.append(ch);
            }
            return state;
        }

        private void commentLine() {
            while (docIndex < doc.length()) {
                char ch = nextChar();
                if (ch == '/') {
                    commentLineReadToEnd();
                    return;
                }
            }
        }

        private void commentLineReadToEnd() {
            while (docIndex < doc.length()) {
                char ch = nextChar();
                if (ch == '\n') {
                    break;
                }
            }
        }

        private boolean isWhiteSpace(char ch) {
            return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';
        }

        private char nextChar() {
            char ch = doc.charAt(docIndex++);
            if (ch == '\n') {
                line++;
                column = 0;
            } else {
                column++;
            }

            return ch;
        }

        private void throwParserError(String s) throws ParserError {
            throw new ParserError(s, line, column);
        }

        public List<Object> getTopLevelObjects() {
            return null;
        }

        private class ParserError extends Throwable {
            int line;
            int column;

            public ParserError(String msg, int line, int column) {
                super(msg);
                this.line = line;
                this.column = column;
            }

            public String getMessage() {
                return String.format("%s @ %d:%d", super.getMessage(), line, column);
            }
        }
    }

    @Ignore
    public void testDocParser() {
        String docs = "/**\n" +
            " * Main entry point for tests.\n" +
            " *\n" +
            " * Most scripts will start with a call to {@link module:pizza.open}.\n" +
            " *\n" +
            " * @example\n" +
            " * var b = pizza.open(\"loadtestgo.com\");\n" +
            " *\n" +
            " * @example\n" +
            " * var b = pizza.open();\n" +
            " * b.open(\"loadtestgo.com\");\n" +
            " *\n" +
            " * @exports pizza\n" +
            " */\n" +
            "pizza = { };\n" +
            "\n" +
            "/**\n" +
            " * Open the browser.\n" +
            " *\n" +
            " * Takes an optional URL or browser settings parameter.  See the examples below.\n" +
            " *\n" +
            " * @example\n" +
            " * // Open browser at \"http://www.mysite.com\"\n" +
            " * var b = pizza.open(\"www.mysite.com\");\n" +
            " *\n" +
            " * @example\n" +
            " * // You may want to setup the browser before navigating\n" +
            " * var b = pizza.open();\n" +
            " * ...\n" +
            " * // Navigate to \"http://www.mysite.com\"\n" +
            " * b.open(\"mysite.com\");\n" +
            " *\n" +
            " * @example\n" +
            " * // Open the browser and ignore bad certificates (e.g. unsigned certs)\n" +
            " * var b = pizza.open({ignoreCertErrors: true});\n" +
            " * b.open(\"https://my-unsigned-test-server.com\");\n" +
            " *\n" +
            " * @example\n" +
            " * // Pass a command line arg to Chrome when launching the Chrome executable.\n" +
            " * // List of switches: https://src.chromium.org/svn/trunk/src/chrome/common/chrome_switches.cc\n" +
            " * var b = pizza.open({args: [\"--force-device-scale-factor=1\"]});\n" +
            " * b.open(\"google.com\");\n" +
            " *\n" +
            " * @example\n" +
            " * // Open the browser and enable QUIC (http://en.wikipedia.org/wiki/QUIC)\n" +
            " * var b = pizza.open({enableQuic: true, forceQuic: \"my-site-that-supports-quic.com:443\"});\n" +
            " * b.open(\"https://my-site-that-supports-quic.com\");\n" +
            " *\n" +
            " * @example\n" +
            " * // Go to a 404 page but don't throw an error\n" +
            " * b.ignoreHttpErrors();\n" +
            " * b.open(\"mysite.com/404\");\n" +
            " *\n" +
            " * @params {string=|Object=} url the URL to open, or settings to use when opening the browser\n" +
            " * @return {module:Browser} the newly opened browser\n" +
            " *\n" +
            " * @see module:Browser#ignoreHttpErrors\n" +
            " */\n" +
            "pizza.open = function() {};\n" +
            "/**\n" +
            " * Take a screenshot.\n" +
            " *\n" +
            " * The format can be any of 'webp', 'png' or 'jpeg'.\n" +
            " *\n" +
            " * If on a high DPI display, the screenshot is scaled down by the devicePixelRatio.\n" +
            " *\n" +
            " * @param {String} [format='png'] The format of the image.  Can be any of 'webp', 'png'\n" +
            " * or 'jpeg'.\n" +
            " *\n" +
            " * @param {Number} [quality='1.0'] The quality of the image.  0.0 is least and smallest\n" +
            " * size, 1.0 is best quality and largest image size.\n" +
            " *\n" +
            " * @example\n" +
            " * var d = b.screenshot();\n" +
            " * // TODO: Currently no way to save data from JavaScript API\n" +
            " */\n" +
            "Browser.prototype.screenshot = function(format, quality) {};" +
            "\n" +
            "/**\n" +
            " *\n" +
            " * @type {Number}\n" +
            " */\n" +
            "Tab.prototype.height = 0;\n";

        String doc1 = "/**\n*/\n";

        JavaScriptDocParser parser = parse(doc1);

        parser = parse(docs);

        assertEquals(1, parser.getTopLevelObjects().size());
    }

    private JavaScriptDocParser parse(String docs) {
        JavaScriptDocParser parser = new JavaScriptDocParser();
        try {
            parser.parse(docs);
        } catch (JavaScriptDocParser.ParserError parserError) {
            parserError.printStackTrace();
        }
        return parser;
    }
}
