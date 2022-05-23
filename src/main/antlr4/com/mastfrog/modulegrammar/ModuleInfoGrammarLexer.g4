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
lexer grammar ModuleInfoGrammarLexer;

import moduleinfogrammar;

@members {
    int lastTokenType = -1;
    public void emit(Token token) {
        switch(token.getType()) {
            case Whitespace :
            case Comment :
            case LineComment :
                break;
            default : 
                lastTokenType = token.getType();
                break;
        }
        super.emit(token);
    }

    boolean nextNonWhitespace(char c, boolean match) {
        for (int i = 1;; i++) {
            int val = _input.LA(i);
            if (val == CharStream.EOF) {
                return false;
            }
            char curr = (char) val;
            if (!Character.isWhitespace(curr)) {
                return match ? c == curr : c != curr;
            }
        }
    }
}

Open
    : 'open';

Module
    : 'module';

Uses
    : 'uses';

Provides
    : 'provides';

With
    : 'with';

Import
    : 'import';

To
    : 'to';

Static
    : 'static';

Opens
    : 'opens';

Transitive
    : 'transitive';

Exports
    : 'exports';

Requires
    : 'requires';

Identifier
    : IDENTIFIER;

StandaloneAnnotation
    : OPEN_ANNOTATION IDENTIFIER ( DOT IDENTIFIER )*;

Annotation
    : { lastTokenType == StandaloneAnnotation }? LEFT_PAREN -> pushMode ( Annotations );

LeftBrace
    : '{';

RightBrace
    : '}';

Dot
    : DOT;

Comma
    : COMMA;

Semi
    : ';';

LineComment
    : LINE_COMMENT_OPEN .*? LINE_END -> channel ( 2 );

Whitespace
    : WHITESPACE+ -> channel ( 1 );

Comment
    : '/*' .*? '*/' -> channel ( 2 );


mode Annotations;

AnnotationStandaloneAnnotation
    : OPEN_ANNOTATION IDENTIFIER ( DOT IDENTIFIER )* -> type ( StandaloneAnnotation );

AnnotationChildAnnotation
    : { lastTokenType == StandaloneAnnotation }? LEFT_PAREN -> type ( Annotation ), pushMode ( Annotations );

AnnotationClassReference
    : ( IDENTIFIER DOT )* IDENTIFIER DOT CLASS;

AnnotationCloseAnnotation
    : ')' -> popMode;

AnnotationFloat
    : ( DIGIT+ DOT DIGIT+
      | '.' DIGIT+
      | DIGIT+ DOT )( 'F'
                     | 'D'
                     | 'f'
                     | 'd' )?;

AnnotationInt
    : DIGIT+ ( 'L'
             | 'l' )?;

AnnotationBoolean
    : 'true'
    | 'false';

AnnotationEquals
    : '=';

AnnotationString
    : STRING;

AnnotationChar
    : CHAR;

AnnotationComma
    : COMMA -> type ( Comma );

AnnotationOpenArray
    : '{';

AnnotationCloseArray
    : '}';

AnnotationDot
    : DOT -> type ( Dot );
// XXX this will miss if there is a line comment or block comment
// between the key name and the =
AnnotationKey
    : IDENTIFIER { nextNonWhitespace('=', true ) }?;

AnnotationIdentifier
    : IDENTIFIER -> type ( Identifier );

AnnotationLineComment
    : LINE_COMMENT_OPEN .*? LINE_END -> type ( LineComment ), channel ( 2 );

AnnotationComment
    : '/*' .*? '*/' -> type ( Comment ), channel ( 2 );

AnnotationWhitespace
    : WHITESPACE+ -> type ( Whitespace ), channel ( 2 );