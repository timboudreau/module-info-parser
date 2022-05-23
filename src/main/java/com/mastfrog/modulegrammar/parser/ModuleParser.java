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

import com.mastfrog.modulegrammar.ModuleInfoGrammarLexer;
import com.mastfrog.modulegrammar.ModuleInfoGrammarParser;
import com.mastfrog.modulegrammar.ModuleModelExtractor;
import com.mastfrog.modulegrammar.model.ModuleModel;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Parses a module-info.java file into a ModuleModel.
 *
 * @author Tim Boudreau
 */
public final class ModuleParser {

    private ModuleParser() {
        throw new AssertionError();
    }

    public static ModuleModel parse(ReadableByteChannel channel) throws IOException {
        return parse(channel, null);
    }

    public static ModuleModel parse(ReadableByteChannel channel, ModuleParserErrorListener l) throws IOException {
        return parse(CharStreams.fromChannel(channel, UTF_8), l);
    }

    public static ModuleModel parse(InputStream stream) throws IOException {
        return parse(stream, null);
    }

    public static ModuleModel parse(InputStream stream, ModuleParserErrorListener l) throws IOException {
        return parse(CharStreams.fromStream(stream, UTF_8), l);
    }

    public static ModuleModel parse(Path path) throws IOException {
        return parse(path, null);
    }

    public static ModuleModel parse(Path path, ModuleParserErrorListener l) throws IOException {
        return parse(CharStreams.fromPath(path), l);
    }

    public static ModuleModel parse(String string) {
        return parse(string, null);
    }

    public static ModuleModel parse(String string, ModuleParserErrorListener listener) {
        return parse(CharStreams.fromString(string), listener);
    }

    public static ModuleModel parse(CharStream charStream, ModuleParserErrorListener listener) {
        return parseWithListener(charStream, new AntlrErrorAdapter(ModuleParserErrorListener.loggingIfNull(listener)), listener);
    }

    public static ModuleModel parseWithListener(CharStream charStream, ANTLRErrorListener listener) {
        return parseWithListener(charStream, listener, null);
    }

    private static ModuleModel parseWithListener(CharStream charStream, ANTLRErrorListener listener, ModuleParserErrorListener errs) {
        errs = ModuleParserErrorListener.loggingIfNull(errs);
        if (listener == null) {
            listener = new AntlrErrorAdapter(errs);
        }
        ModuleInfoGrammarLexer lex = new ModuleInfoGrammarLexer(charStream);
        lex.removeErrorListeners();
        lex.addErrorListener(listener);

        CommonTokenStream cts = new CommonTokenStream(lex);
        ModuleInfoGrammarParser parser = new ModuleInfoGrammarParser(cts);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        return parser.compilationUnit().accept(new ModuleModelExtractor(errs::onParserError));
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage:  java -jar module-info-grammar.jar /path/to/module-info.java");
            System.exit(0);
        }
        Path p = Paths.get(args[0]);
        ModuleModel mdl = parse(p);
        System.out.println(mdl.toJson());
    }
}
