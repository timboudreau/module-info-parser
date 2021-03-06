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

import java.util.Optional;
import org.antlr.v4.runtime.tree.ErrorNode;

/**
 *
 * @author Tim Boudreau
 */
public final class ModuleParserException extends IllegalStateException {

    private final ErrorNode errorNode;
    private final ModuleSyntaxError syntaxError;

    ModuleParserException(ErrorNode err) {
        super("Error node encountered: " + err);
        this.errorNode = err;
        this.syntaxError = null;
    }

    ModuleParserException(ModuleSyntaxError err) {
        super(err.toString());
        this.syntaxError = err;
        this.errorNode = null;
    }

    public boolean isSyntaxError() {
        return syntaxError != null;
    }

    public boolean isErrorNode() {
        return errorNode != null;
    }

    public Optional<ErrorNode> errorNode() {
        return Optional.ofNullable(errorNode);
    }

    public Optional<ModuleSyntaxError> syntaxError() {
        return Optional.ofNullable(syntaxError);
    }

    public Object getError() {
        return errorNode == null ? syntaxError : errorNode;
    }
}
