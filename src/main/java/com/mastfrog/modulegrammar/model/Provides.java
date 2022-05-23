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
import java.util.function.BiConsumer;

/**
 * A provides clause in a module-info.java.
 *
 * @author Tim Boudreau
 */
public final class Provides implements Iterable<String>, Comparable<Provides>, JsonRenderable {

    private final String provided;
    private final Set<String> providers = new TreeSet<>();

    public Provides(String provided, Collection<? extends String> providers) {
        this.providers.addAll(providers);
        this.provided = provided;
    }

    @Override
    public StringBuilder renderJsonInto(StringBuilder sb) {
        return JsonUtils.hash(() -> {
            JsonUtils.append("type", provided, sb);
            JsonUtils.nextItem(sb);
            JsonUtils.append("with", providers, sb);
        }, sb);
    }
    
    @SuppressWarnings("StringEquality")
    public Provides resolve(Imports imports) {
        String pname = imports.resolve(provided);
        Set<String> nue = new TreeSet<>();
        for (String provider : providers) {
            nue.add(imports.resolve(provider));
        }
        if (pname == provided && nue.equals(providers)) {
            return this;
        }
        return new Provides(pname, nue);
    }

    @Override
    public int compareTo(Provides o) {
        return provided.compareTo(o.provided);
    }

    public String provided() {
        return provided;
    }

    public void visitProvides(Imports resolver, BiConsumer<String, String> pairConsumer) {
        for (String provider : providers) {
            pairConsumer.accept(provided, resolver.resolve(provider));
        }
    }

    @Override
    public Iterator<String> iterator() {
        return Collections.unmodifiableSet(providers).iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("    provides ").append(provided).append(" with ");
        for (Iterator<String> it = providers.iterator(); it.hasNext();) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.append(';').toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.provided);
        hash = 41 * hash + Objects.hashCode(this.providers);
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
        final Provides other = (Provides) obj;
        if (!Objects.equals(this.provided, other.provided)) {
            return false;
        }
        return Objects.equals(this.providers, other.providers);
    }

}
