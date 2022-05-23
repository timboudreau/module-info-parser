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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * The set of imports in a module-info.java file, which is used to qualify uses
 * and provides statements.
 *
 * @author Tim Boudreau
 */
public final class Imports implements Iterable<String>, JsonRenderable {

    private static final Set<String> JDK_IMPORTS = new HashSet<>();
    public static final Imports EMPTY = new Imports(Collections.emptyList());
    private final Set<String> importedClasses = new TreeSet<>();

    public Imports(Collection<? extends String> all) {
        importedClasses.addAll(all);
    }

    @Override
    public StringBuilder renderJsonInto(StringBuilder sb) {
        return JsonUtils.append(importedClasses, sb);
    }

    public boolean isEmpty() {
        return importedClasses.isEmpty();
    }

    Set<String> allImports() {
        return Collections.unmodifiableSet(importedClasses);
    }

    /**
     * Resolve a class simple name to a full one, if it matches the tail of one
     * in this Imports.
     *
     * @param what The string to resolve, which must not contain a '.'
     * @return A new string or the same one
     */
    public String resolve(String what) {
        if (what.indexOf('.') >= 0) {
            return what;
        }
        String testFor = '.' + what;
        for (String s : importedClasses) {
            if (s.endsWith(testFor)) {
                return s;
            }
        }
        for (String s : JDK_IMPORTS) {
            if (s.endsWith(testFor)) {
                return s;
            }
        }
        return what;
    }

    @Override
    public Iterator<String> iterator() {
        return Collections.unmodifiableSet(importedClasses).iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String type : importedClasses) {
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            sb.append("import ").append(type).append(';');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.importedClasses);
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
        final Imports other = (Imports) obj;
        return Objects.equals(this.importedClasses, other.importedClasses);
    }

    static {
        // So we can resolve these into FQNs when referenced
        JDK_IMPORTS.addAll(Arrays.asList(
                "java.lang.Object",
                "java.lang.Integer",
                "java.lang.Boolean",
                "java.lang.Long",
                "java.lang.Character",
                "java.lang.Short",
                "java.lang.Byte",
                "java.lang.String",
                "java.lang.Void",
                "java.lang.Class",
                "java.lang.Comparable",
                "java.lang.Thread",
                "java.lang.CharSequence",
                "java.lang.Cloneable",
                "java.lang.Double",
                "java.lang.Float",
                "java.lang.Enum",
                "java.lang.Exception",
                "java.lang.Error",
                "java.lang.Throwable",
                "java.lang.FunctionalInterface",
                "java.lang.Math",
                "java.lang.Module",
                "java.lang.ModuleLayer",
                "java.lang.IllegalArgumentException",
                "java.lang.IllegalStateException",
                "java.lang.AssertionError",
                "java.lang.Deprecated",
                "java.lang.Override",
                "java.lang.Number",
                "java.lang.NullPointerException",
                "java.lang.Process",
                "java.lang.ProcessBuilder",
                "java.lang.Record",
                "java.lang.Runtime",
                "java.lang.RuntimeException",
                "java.lang.SafeVarargs",
                "java.lang.StringBuffer",
                "java.lang.StringBuilder",
                "java.lang.SuppressWarnings",
                "java.lang.Thread",
                "java.lang.ThreadLocal",
                "java.lang.ThreadDeath",
                "java.lang.ThreadGroup",
                "java.lang.UnsupportedOperationException"
        ));
    }
}
