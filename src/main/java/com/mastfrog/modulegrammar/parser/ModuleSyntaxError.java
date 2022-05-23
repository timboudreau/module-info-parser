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
package com.mastfrog.modulegrammar.parser;

import java.util.Objects;
import java.util.Optional;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;

/**
 * Wrapper for an ANTLRErrorListener's <code>syntaxError()</code> arguments,
 * with some improvements to make token type-safe and attempt to extract the
 * character offset within the input text.
 *
 * @author Tim Boudreau
 */
public final class ModuleSyntaxError {

    /**
     * The ATN simulator used in the parse.
     */
    public final Recognizer<?, ?> rcgnzr;
    /**
     * The offending token, if present.
     */
    public final Optional<Token> offendingSymbol;
    /**
     * The character index within the character stream <i>if it is available,
     * depending on the input stream type of the recognizer</i>. This will be
     * the absolute character position the stream was on at the time the error
     * was encountered, and may be at the end of the token in question.
     * Will be -1 if the position was not available from the objects passed
     * to the original error listener's <code>syntaxError()</code> method.
     */
    public final int absoluteCharacterIndex;
    /**
     * The error message.
     */
    public final String message;
    /**
     * The line in the source text.
     */
    public final int line;
    /**
     * The character offset within the line of the source text.
     */
    public final int charPositionInLine;
    /**
     * A recognition exception, if one was thrown to initiate this syntax error.
     */
    public final Optional<RecognitionException> recognitionException;

    ModuleSyntaxError(Recognizer<?, ?> rcgnzr, Object offendingSymbol, int line, int charPositionInLine, String message, RecognitionException re) {
        this.rcgnzr = rcgnzr;
        if (offendingSymbol instanceof Token token) {
            this.offendingSymbol = Optional.of(token);
        } else {
            this.offendingSymbol = Optional.empty();
        }
        if (rcgnzr.getInputStream() instanceof TokenStream tokenStream) {
            absoluteCharacterIndex = tokenStream.getTokenSource().getInputStream().index();
        } else {
            absoluteCharacterIndex = -1;
        }
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.message = message;
        this.recognitionException = Optional.ofNullable(re);
    }
    
    public ModuleParserException toException() {
        return new ModuleParserException(this);
    }
    
    public void rethrow() throws ModuleParserException {
        throw toException();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(line).append(':').append(charPositionInLine).append('(').append(absoluteCharacterIndex).append(") ").append(message);
        offendingSymbol.ifPresent(sym -> {
            Vocabulary v = rcgnzr.getVocabulary();
            sb.append(" Token: ").append(v.getDisplayName(sym.getType())).append(" (").append(sym.getType()).append(") '").append(sym.getText()).append('\'');
        });
        recognitionException.ifPresent(re -> {
            sb.append(' ').append(re);
        });
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.message);
        hash = 47 * hash + this.line;
        hash = 47 * hash + this.charPositionInLine;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ModuleSyntaxError other = (ModuleSyntaxError) obj;
        if (this.line != other.line) {
            return false;
        }
        if (this.charPositionInLine != other.charPositionInLine) {
            return false;
        }
        return Objects.equals(this.message, other.message);
    }

}
