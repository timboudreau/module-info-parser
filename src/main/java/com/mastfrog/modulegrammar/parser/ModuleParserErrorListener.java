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

import org.antlr.v4.runtime.tree.ErrorNode;

/**
 * Simplified error listening interface.
 *
 * @author Tim Boudreau
 */
public interface ModuleParserErrorListener {

    /**
     * Called when the parser encounters an error node.
     *
     * @param node The node
     */
    void onParserError(ErrorNode node);

    /**
     * Called when the lexer encounters a syntax error.
     *
     * @param err The error
     */
    void onSyntaxError(ModuleSyntaxError err);

    /**
     * Combine this listener with another.
     *
     * @param l A listener
     * @return A wrapper of this and the other listener
     */
    default ModuleParserErrorListener and(ModuleParserErrorListener l) {
        return new ModuleParserErrorListener() {
            @Override
            public void onParserError(ErrorNode node) {
                ModuleParserErrorListener.this.onParserError(node);
                l.onParserError(node);
            }

            @Override
            public void onSyntaxError(ModuleSyntaxError err) {
                ModuleParserErrorListener.this.onSyntaxError(err);
                l.onSyntaxError(err);
            }
        };
    }

    /**
     * Returns the stderr listener if the passed listener is null, otherwise the
     * passed listener.
     *
     * @param listener A listener which might be null
     * @return a listener
     */
    static ModuleParserErrorListener loggingIfNull(ModuleParserErrorListener listener) {
        if (listener != null) {
            return listener;
        }
        return STDERR;
    }

    /**
     * A listener that silently ignores all errors.
     */
    public static ModuleParserErrorListener SILENT = new ModuleParserErrorListener() {
        @Override
        public void onParserError(ErrorNode node) {
            // do nothing
        }

        @Override
        public void onSyntaxError(ModuleSyntaxError err) {
            // do nothing
        }
    };

    /**
     * A listener that logs all errors to stderr.
     */
    public static ModuleParserErrorListener STDERR = new ModuleParserErrorListener() {
        @Override
        public void onParserError(ErrorNode node) {
            System.err.println("Error node encountered: " + node);
        }

        @Override
        public void onSyntaxError(ModuleSyntaxError err) {
            System.err.println("Syntax error: " + err);
        }
    };

    /**
     * A listener that throws a ModuleParserException if an error is
     * encountered.
     */
    public static ModuleParserErrorListener THROWING = new ModuleParserErrorListener() {
        @Override
        public void onParserError(ErrorNode node) {
            throw new ModuleParserException(node);
        }

        @Override
        public void onSyntaxError(ModuleSyntaxError err) {
            throw new ModuleParserException(err);
        }
    };

}
