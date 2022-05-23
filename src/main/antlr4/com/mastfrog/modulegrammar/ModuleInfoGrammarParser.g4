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
parser grammar ModuleInfoGrammarParser;

options { tokenVocab = ModuleInfoGrammarLexer; }

compilationUnit
    : importStatement* annotation* moduleDeclaration EOF;

moduleDeclaration
    : Open? Module moduleIdentifier LeftBrace statement* RightBrace;

moduleIdentifier
    : ( identifierComponent Dot )* identifierComponent;
// ensures that module and package names can contain
// words that are otherwise keywords
identifierComponent
    : Identifier
    | Module
    | Opens
    | Exports
    | Transitive
    | Provides
    | With;

importStatement
    : Import classIdentifier Semi;

statement
    : opensStatement Semi
    | usesStatement Semi
    | exportsStatement Semi
    | providesStatement Semi
    | requiresStatement Semi;

opensStatement
    : Opens packageIdentifier toClause?;

toClause
    : To ( moduleIdentifier Comma )* moduleIdentifier;

packageIdentifier
    : ( identifierComponent Dot )+ identifierComponent;

classIdentifier
    : ( identifierComponent Dot )* identifierComponent;

usesStatement
    : Uses classIdentifier;

exportsStatement
    : Exports packageIdentifier toClause?;

providesStatement
    : Provides classIdentifier With providedTypesList;

providedTypesList
    : ( classIdentifier Comma )* classIdentifier;

requiresStatement
    : Requires Static? Transitive? moduleIdentifier;

annotation
    : StandaloneAnnotation annotationContent?;

annotationContent
    : Annotation annotationBody? AnnotationCloseAnnotation;

annotationBody
    : ( annotationValue ( Comma annotationValue )* Comma? )
    | ( annotationElement Comma )* annotationElement;

annotationElement
    : annotationElementName AnnotationEquals annotationValue;

annotationElementName
    : AnnotationKey;

annotationValue
    : annotationArray
    | annotationEnumConstant
    | AnnotationClassReference
    | AnnotationFloat
    | AnnotationInt
    | AnnotationString
    | AnnotationBoolean
    | AnnotationChar
    | annotation;

annotationEnumConstant
    : ( Identifier Dot )* Identifier;

annotationArray
    : AnnotationOpenArray annotationArrayElements* Comma? AnnotationCloseArray;

annotationArrayElements
    : ( annotationValue Comma )* annotationValue;