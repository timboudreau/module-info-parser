/* 
 * The MIT License
 *
 * Copyright 2022 Tim Boudreau.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
lexer grammar moduleinfogrammar;

fragment CLASS
    : 'class';

fragment COMMA
    : ',';

fragment LEFT_PAREN
    : '(';

fragment DOT
    : '.';

fragment BLOCK_COMMENT_OPEN
    : '/*';

fragment BLOCK_COMMENT_CLOSE
    : '*/';

fragment LINE_COMMENT_OPEN
    : '//';

fragment LINE_END
    : '\r'? '\n';

fragment OPEN_ANNOTATION
    : '@';

fragment IDENTIFIER
    : [a-zA-Z_] [a-zA-Z0-9_]*;

fragment DIGIT
    : [0-9];

fragment STRING
    : '"' ( DQUOTE_ESCAPE
          | . )*? '"';

fragment CHAR
    : '\'' ( SQUOTE_ESCAPE
           | . ) '\'';

fragment DQUOTE_ESCAPE
    : '\\"'
    | '\\\\';

fragment SQUOTE_ESCAPE
    : '\\\''
    | '\\\\';

fragment WHITESPACE
    : [ \r\n\t];

fragment NON_WHITESPACE
    : ~[\r\n\t ];