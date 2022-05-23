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
import java.util.Objects;

/**
 * A requiresclause in a module-info.java.
 *
 * @author Tim Boudreau
 */
public final class Require implements Comparable<Require>, JsonRenderable {

    private final boolean requireStatic;
    private final boolean transitive;
    private final String requiredModule;

    public Require(boolean requireStatic, boolean transitive,
            String requiredModule) {
        this.requireStatic = requireStatic;
        this.transitive = transitive;
        this.requiredModule = requiredModule;
    }

    @Override
    public StringBuilder renderJsonInto(StringBuilder sb) {
        return JsonUtils.hash(() -> {
            JsonUtils.append("module", requiredModule, sb);
            sb.append(", ");
            JsonUtils.append("transitive", transitive, sb);
            sb.append(", ");
            JsonUtils.append("static", requireStatic, sb);
        }, sb);
    }

    @Override
    public int compareTo(Require o) {
        return requiredModule.compareTo(o.requiredModule);
    }

    public boolean isTransitive() {
        return transitive;
    }

    public boolean isStatic() {
        return requireStatic;
    }

    public String moduleName() {
        return requiredModule;
    }

    @Override
    public String toString() {
        return "    requires " + (requireStatic ? "static " : "")
                + (transitive ? "transitive " : "") + requiredModule + ";";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.requireStatic ? 1 : 0);
        hash = 67 * hash + (this.transitive ? 1 : 0);
        hash = 67 * hash + Objects.hashCode(this.requiredModule);
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
        final Require other = (Require) obj;
        if (this.requireStatic != other.requireStatic) {
            return false;
        }
        if (this.transitive != other.transitive) {
            return false;
        }
        return Objects.equals(this.requiredModule, other.requiredModule);
    }

}
