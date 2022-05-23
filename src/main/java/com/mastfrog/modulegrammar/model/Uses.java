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
 * A uses clause in a module-info.java.
 *
 * @author Tim Boudreau
 */
public final class Uses implements Iterable<String>, JsonRenderable {

    private final Set<String> usedClasses = new TreeSet<>();

    Uses(Collection<? extends String> all) {
        usedClasses.addAll(all);
    }

    @Override
    public StringBuilder renderJsonInto(StringBuilder sb) {
        return JsonUtils.append(usedClasses, sb);
    }
    
    public boolean isEmpty() {
        return usedClasses.isEmpty();
    }

    public boolean contains(String what) {
        return usedClasses.contains(what);
    }
    
    Set<String> allUses() {
        return Collections.unmodifiableSet(usedClasses);
    }

    public Uses resolve(Imports imports) {
        Set<String> nue = resolvedUses(imports);
        if (nue.equals(usedClasses)) {
            return this;
        }
        return new Uses(nue);
    }

    public Set<String> resolvedUses(Imports imports) {
        Set<String> result = new TreeSet<>();
        for (String cl : usedClasses) {
            result.add(imports.resolve(cl));
        }
        return result;
    }

    @Override
    public Iterator<String> iterator() {
        return Collections.unmodifiableSet(usedClasses).iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String type : usedClasses) {
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            sb.append("    uses ").append(type).append(';');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.usedClasses);
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
        final Uses other = (Uses) obj;
        return Objects.equals(this.usedClasses, other.usedClasses);
    }
}
