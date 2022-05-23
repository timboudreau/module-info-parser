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
package com.mastfrog.modulegrammar.model;

import com.mastfrog.modulegrammar.json.JsonRenderable;
import com.mastfrog.modulegrammar.json.JsonUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Models an <code>opens</code> statement in a <code>module-info.java</code>.
 *
 * @author Tim Boudreau
 */
public final class Opens implements Comparable<Opens>, JsonRenderable {

    private final Set<String> tos;
    private final String what;

    public Opens(String what, Collection<? extends String> tos) {
        this.tos = tos == null ? null : new TreeSet<>(tos);
        this.what = what;
    }

    @Override
    public StringBuilder renderJsonInto(StringBuilder sb) {
        return JsonUtils.hash(() -> {
            JsonUtils.append("package", what, sb);
            if (tos != null && !tos.isEmpty()) {
                JsonUtils.nextItem(sb);
                JsonUtils.append("to", tos, sb);
            }
        }, sb);
    }

    @Override
    public int compareTo(Opens o) {
        return what.compareTo(o.what);
    }

    public String opened() {
        return what;
    }

    public boolean isOpenedTo(String what) {
        if (tos == null) {
            return true;
        }
        return tos.contains(what);
    }

    public Optional<Set<? extends String>> to() {
        if (tos == null) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableSet(tos));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("    opens ").append(what);
        if (tos != null) {
            sb.append(" to");
            for (Iterator<String> it = tos.iterator(); it.hasNext();) {
                sb.append(' ').append(it.next());
                if (it.hasNext()) {
                    sb.append(',');
                }
            }
        }
        return sb.append(';').toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.tos);
        hash = 29 * hash + Objects.hashCode(this.what);
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
        final Opens other = (Opens) obj;
        if (!Objects.equals(this.what, other.what)) {
            return false;
        }
        return Objects.equals(this.tos, other.tos);
    }
}
