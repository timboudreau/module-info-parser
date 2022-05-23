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
package com.mastfrog.modulegrammar;

import com.mastfrog.modulegrammar.model.annotation.AnnotationModel;
import com.mastfrog.modulegrammar.model.annotation.AnnotationValue;
import com.mastfrog.modulegrammar.model.annotation.AnnotationValueKind;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.antlr.v4.runtime.tree.ErrorNode;

/**
 * Builds trees for annotations.
 *
 * @author Tim Boudreau
 */
final class AnnotationModelExtractor extends ModuleInfoGrammarParserBaseVisitor<AnnotationModel> {

    private String currentKeyName;
    private List<AnnotationValue<?>> currentArray;
    private String annotationName;
    private final Map<String, AnnotationValue<?>> values = new HashMap<>(16);
    private int depth;
    private final Consumer<ErrorNode> onError;

    AnnotationModelExtractor(Consumer<ErrorNode> onError) {
        this.onError = onError;
    }

    @Override
    public AnnotationModel visitErrorNode(ErrorNode node) {
        onError.accept(node);
        return super.visitErrorNode(node);
    }

    private void setAnnotationName(String text) {
        if (annotationName != null) {
            throw new IllegalStateException("Already have annotation name '"
                    + annotationName + "' cannot change it to '" + text + "'");
        }
        if (text.startsWith("@")) {
            text = text.substring(1);
        }
        annotationName = text;
    }

    private <T> void onKeyValuePair(String name, AnnotationValue<T> value) {
        values.put(name, value);
    }

    private void onKeyName(String name) {
        currentKeyName = name;
    }

    private <T> void onValue(AnnotationValueKind kind, T value) {
        AnnotationValue<T> av = new AnnotationValue<>(kind, value);
        if (currentArray != null) {
            currentArray.add(av);
        } else {
            if (currentKeyName != null) {
                onKeyValuePair(currentKeyName, av);
            } else {
                onKeyValuePair("value", av);
            }
        }
    }

    private Number parseInt(String text) {
        text = text.replaceAll("_", "");
        if (text.charAt(text.length() - 1) == 'L') {
            return Long.parseLong(text.substring(0, text.length() - 1));
        } else {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ex) {
                return Long.parseLong(text);
            }
        }
    }

    private Number parseFloat(String text) {
        text = text.replaceAll("_", "");
        char last = text.charAt(text.length() - 1);
        boolean explicitFloat = last == 'F' || last == 'f';
        boolean explicitDouble = last == 'D' || last == 'd';
        if (explicitDouble || explicitFloat) {
            text = text.substring(0, text.length() - 1);
        }
        if (explicitFloat) {
            return Float.parseFloat(text);
        }
        return Double.parseDouble(text);
    }

    private String stripString(String what) {
        return what.substring(1, what.length() - 1);
    }

    private <T> T inArray(Supplier<T> runner) {
        List<AnnotationValue<?>> old = currentArray;
        List<AnnotationValue<?>> nue = new ArrayList<>();
        String keyName = currentKeyName;
        try {
            currentArray = nue;
            return runner.get();
        } finally {
            currentArray = old;
            currentKeyName = keyName;
            onValue(AnnotationValueKind.ARRAY, nue);
        }
    }

    private String stripDotClass(String val) {
        if (val.endsWith(".class")) {
            val = val.substring(0, val.length() - ".class".length());
        }
        return val;
    }

    @Override
    public AnnotationModel visitAnnotationValue(ModuleInfoGrammarParser.AnnotationValueContext ctx) {
        if (ctx.AnnotationBoolean() != null) {
            onValue(AnnotationValueKind.BOOLEAN, "true".equals(ctx.AnnotationBoolean().getText()));
        } else if (ctx.AnnotationInt() != null) {
            onValue(AnnotationValueKind.INT, parseInt(ctx.AnnotationInt().getText()));
        } else if (ctx.AnnotationClassReference() != null) {
            onValue(AnnotationValueKind.CLASS, stripDotClass(ctx.AnnotationClassReference().getText()));
        } else if (ctx.AnnotationChar() != null) {
            onValue(AnnotationValueKind.CHAR, ctx.AnnotationChar().getText().charAt(1));
        } else if (ctx.AnnotationFloat() != null) {
            onValue(AnnotationValueKind.FLOAT, parseFloat(ctx.AnnotationFloat().getText()));
        } else if (ctx.AnnotationString() != null) {
            onValue(AnnotationValueKind.STRING, stripString(ctx.AnnotationString().getText()));
        } else if (ctx.annotationArray() != null) {
            return inArray(() -> super.visitAnnotationValue(ctx));
        } else if (ctx.annotationEnumConstant() != null) {
            onValue(AnnotationValueKind.ENUM, ctx.getText());
        } else if (ctx.annotation() != null && currentKeyName != null) {
            AnnotationModelExtractor child = new AnnotationModelExtractor(onError);
            onValue(AnnotationValueKind.ANNOTATION, ctx.accept(child));
            return null;
        }
        return super.visitAnnotationValue(ctx);
    }

    @Override
    public AnnotationModel visitAnnotationElementName(ModuleInfoGrammarParser.AnnotationElementNameContext ctx) {
        onKeyName(ctx.AnnotationKey().getText());
        return super.visitAnnotationElementName(ctx);
    }

    @Override
    public AnnotationModel visitAnnotation(ModuleInfoGrammarParser.AnnotationContext ctx) {
        depth++;
        try {
            boolean isOutermost = depth == 1;
            if (isOutermost) {
                setAnnotationName(ctx.StandaloneAnnotation().getText());
                super.visitAnnotation(ctx);
                return new AnnotationModel(this.annotationName, this.values);
            } else {
                return super.visitAnnotation(ctx);
            }
        } finally {
            depth--;
        }
    }

    @Override
    public AnnotationModel visitAnnotationElement(ModuleInfoGrammarParser.AnnotationElementContext ctx) {
        String kn = currentKeyName;
        currentKeyName = ctx.annotationElementName().AnnotationKey().getText();
        try {
            return super.visitAnnotationElement(ctx);
        } finally {
            currentKeyName = kn;
        }
    }
}
