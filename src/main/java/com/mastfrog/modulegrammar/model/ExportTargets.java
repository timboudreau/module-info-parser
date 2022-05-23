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
import java.util.Set;
import java.util.TreeSet;

/**
 * The (optional) "to" targets of an exports statement in a module-info.java.
 *
 * @author Tim Boudreau
 */
public final class ExportTargets implements Iterable<String>, JsonRenderable {

    private final Set<String> tos = new TreeSet<>();

    public ExportTargets(Collection<? extends String> tos) {
        this.tos.addAll(tos);
    }

    @Override
    public StringBuilder renderJsonInto(StringBuilder sb) {
        return JsonUtils.append(tos, sb);
    }

    public boolean contains(String moduleId) {
        return tos.contains(moduleId);
    }

    @Override
    public Iterator<String> iterator() {
        return Collections.unmodifiableSet(tos).iterator();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.tos);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String to : tos) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(to);
        }
        return sb.toString();
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
        final ExportTargets other = (ExportTargets) obj;
        return Objects.equals(this.tos, other.tos);
    }

}
