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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 *
 * @author Tim Boudreau
 */
public final class AnnotationModel implements JsonRenderable {

    private final Map<String, AnnotationValue<?>> pairs = new TreeMap<>();
    private final String annotationName;

    public AnnotationModel(String annotationName, Map<String, AnnotationValue<?>> pairs) {
        this.annotationName = annotationName;
        this.pairs.putAll(pairs);
    }

    @Override
    public StringBuilder renderJsonInto(StringBuilder sb) {
        return JsonUtils.hash(() -> {
            JsonUtils.append("annotation", annotationName, sb);
            if (!pairs.isEmpty()) {
                JsonUtils.nextItem(sb);
                JsonUtils.append("properties", pairs, sb);
            }
        }, sb);
    }

    public AnnotationModel resolve(Imports imports) {
        String resolvedName = imports.resolve(annotationName);
        Map<String, AnnotationValue<?>> newPairs = new HashMap<>();
        boolean anyChanged = false;
        for (Map.Entry<String, AnnotationValue<?>> e : pairs.entrySet()) {
            AnnotationValue nue = e.getValue().resolve(imports);
            anyChanged |= nue != e.getValue();
            newPairs.put(e.getKey(), nue);
        }
        if (!anyChanged) {
            return this;
        }
        return new AnnotationModel(resolvedName, newPairs);
    }

    public String name() {
        return annotationName;
    }

    public boolean visitValues(AnnotationModelVisitor v) {
        return visitValues(v, 0);
    }

    public Optional<AnnotationValue<?>> getProperty(String propertyName) {
        return Optional.ofNullable(pairs.get(propertyName));
    }

    public Optional<Object> getValue(String name) {
        return getProperty(name).map(AnnotationValue::value);
    }

    @SuppressWarnings("unchecked")
    boolean visitValues(AnnotationModelVisitor v, int depth) {
        if (!v.onEnterAnnotationModel(this, depth)) {
            return false;
        }
        boolean result = true;
        for (Map.Entry<String, AnnotationValue<?>> e : pairs.entrySet()) {
            Object o = e.getValue().value();
            if (o instanceof List<?> l) {
                List<AnnotationValue<?>> avs = (List<AnnotationValue<?>>) l;
                for (AnnotationValue<?> av : avs) {
                    result = v.visitValue(this, depth, e.getKey(), av, true);
                    if (!result) {
                        break;
                    }
                    Object o1 = av.value();
                    if (o1 instanceof AnnotationModel am) {
                        result = am.visitValues(v, depth + 1);
                        if (!result) {
                            break;
                        }
                    }
                }
            } else {
                result = v.visitValue(this, depth, e.getKey(), e.getValue(), false);
            }
            if (!result) {
                break;
            }
            if (o instanceof AnnotationModel child) {
                result = child.visitValues(v, depth + 1);
                if (!result) {
                    break;
                }
            }
        }
        result &= v.onExitAnnotationModel(this, depth);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(192).append('@').append(annotationName);
        if (!pairs.isEmpty()) {
            sb.append('(');
            for (Iterator<Map.Entry<String, AnnotationValue<?>>> it = pairs.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, AnnotationValue<?>> e = it.next();
                sb.append(e.getKey()).append(" = ").append(e.getValue());
                if (it.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(')');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.pairs);
        hash = 41 * hash + Objects.hashCode(this.annotationName);
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
        final AnnotationModel other = (AnnotationModel) obj;
        if (!Objects.equals(this.annotationName, other.annotationName)) {
            return false;
        }
        return Objects.equals(this.pairs, other.pairs);
    }
}
