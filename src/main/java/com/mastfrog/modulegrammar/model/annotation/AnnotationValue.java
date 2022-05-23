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
package com.mastfrog.modulegrammar.model.annotation;

import com.mastfrog.modulegrammar.json.JsonRenderable;
import com.mastfrog.modulegrammar.json.JsonUtils;
import com.mastfrog.modulegrammar.model.Imports;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * The value of a property of an annotation - note that this class cannot know
 * what the type in the annotation declaration is, only what it looks like when
 * it is encountered (so <code>foo = 5</code> is interpreted as an int even
 * though the annotation might declare <code>float foo()</code>).
 *
 * @author Tim Boudreau
 */
public final class AnnotationValue<T> implements JsonRenderable {

    private final AnnotationValueKind kind;
    private final T value;

    /**
     * Create a new annotation values.
     *
     * @param kind The kind, non-null
     * @param value The value, non-null, which must be an instance of the return
     * value of <code>javaValueType()</code>.
     */
    public AnnotationValue(AnnotationValueKind kind, T value) {
        if (kind == null) {
            throw new IllegalArgumentException("Kind may not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value may not be null");
        }
        if (!kind.javaValueType().isInstance(value)) {
            throw new IllegalArgumentException(kind + " is typed on " + kind.javaValueType()
                    + " but passed an instance of " + value.getClass().getName() + ": " + value);
        }
        this.kind = kind;
        this.value = value;
    }

    @Override
    public StringBuilder renderJsonInto(StringBuilder sb) {
        return JsonUtils.hash(() -> {
            JsonUtils.append("kind", kind, sb);
            JsonUtils.nextItem(sb);
            JsonUtils.append("value", value, sb);
        }, sb);
    }

    /**
     * Resolve this value using the set of imports, ensuring that class and
     * annotation names that can be fully qualified using the imports are.
     *
     * @param imports An import set
     * @return A new AnnotationValue if attributing types changes anything, or
     * this if not
     */
    @SuppressWarnings({"StringEquality", "unchecked"})
    public AnnotationValue<?> resolve(Imports imports) {
        switch (kind) {
            case ANNOTATION:
                AnnotationModel val = (AnnotationModel) value;
                return new AnnotationValue<>(kind, val.resolve(imports));
            case CLASS:
                String className = (String) value;
                String newName = imports.resolve(className);
                if (newName != className) {
                    return new AnnotationValue<>(kind, newName);
                }
                return this;
            case ARRAY:
                List<AnnotationValue<?>> l = (List<AnnotationValue<?>>) value;
                List<AnnotationValue<?>> newVs = new ArrayList<>(l.size());
                boolean anyChanged = false;
                for (AnnotationValue<?> v : l) {
                    AnnotationValue nue = v.resolve(imports);
                    newVs.add(nue);
                    if (nue != v) {
                        anyChanged = true;
                    }
                }
                if (!anyChanged) {
                    return this;
                } else {
                    return new AnnotationValue<>(AnnotationValueKind.ARRAY, newVs);
                }
            case ENUM:
                String ev = (String) value;
                int dotIx = ev.indexOf('.');
                if (dotIx > 0 && ev.lastIndexOf('.') == dotIx) {
                    // If we have, e.g. SomeEnum.SOME_ITEM, strip the string to SomeEnum
                    // and see if we can resolve on that
                    String head = ev.substring(0, dotIx);
                    String resolvedHead = imports.resolve(head);
                    if (resolvedHead != head) {
                        String tail = ev.substring(dotIx + 1);
                        return new AnnotationValue<>(AnnotationValueKind.ENUM, resolvedHead + "." + tail);
                    }
                } else {
                    String resolved = imports.resolve(ev);
                    if (resolved != ev) {
                        return new AnnotationValue<>(AnnotationValueKind.ENUM, resolved);
                    }
                }
                return this;
            default:
                return this;
        }
    }

    public T value() {
        return value;
    }

    public AnnotationValueKind kind() {
        return kind;
    }

    public String stringValue() {
        return Objects.toString(value);
    }

    @Override
    public String toString() {
        String baseValue = value.toString();
        switch (kind) {
            case ANNOTATION:
                return baseValue;
            case CLASS:
                return baseValue + ".class";
            case STRING:
                return '"' + baseValue + '"';
            case INT:
                return value instanceof Long ? baseValue + "L" : baseValue;
            case FLOAT:
                return value instanceof Float ? baseValue + "F" : baseValue + "D";
            case ARRAY:
                StringBuilder sb = new StringBuilder();
                sb.append('{');
                List<AnnotationValue<?>> l = (List<AnnotationValue<?>>) value;
                for (Iterator<AnnotationValue<?>> it = l.iterator(); it.hasNext();) {
                    sb.append(it.next());
                    if (it.hasNext()) {
                        sb.append(", ");
                    }
                }
                return sb.append('}').toString();
            case CHAR:
                return "'" + baseValue + "'";
            default:
                return baseValue;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + Objects.hashCode(this.value);
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
        final AnnotationValue<?> other = (AnnotationValue<?>) obj;
        return Objects.equals(this.value, other.value);
    }
}
