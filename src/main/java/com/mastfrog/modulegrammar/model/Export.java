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
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

/**
 * An exports clause in a module-info.java.
 *
 * @author Tim Boudreau
 */
public final class Export implements Comparable<Export>, JsonRenderable {

    private final ExportTargets targets;
    public final String exportedPackage;

    public Export(String exportedPackage, Collection<? extends String> targets) {
        this.exportedPackage = exportedPackage;
        this.targets = targets == null || targets.isEmpty() ? null : new ExportTargets(targets);
    }

    public Export(String exportedPackage, ExportTargets targets) {
        this.exportedPackage = exportedPackage;
        this.targets = targets;
    }

    @Override
    public StringBuilder renderJsonInto(StringBuilder sb) {
        return JsonUtils.hash(() -> {
            JsonUtils.append("exportedPackage", exportedPackage, sb);
            JsonUtils.nextItem(sb);
            JsonUtils.append("to", targets, sb);
        }, sb);
    }
    
    @Override
    public int compareTo(Export o) {
        return exportedPackage.compareTo(o.exportedPackage);
    }
    
    public boolean isExportedTo(String what) {
        if (targets == null) {
            return true;
        }
        return targets.contains(what);
    }

    public Optional<ExportTargets> targets() {
        return Optional.ofNullable(targets);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("    exports ").append(exportedPackage);
        if (targets != null) {
            sb.append(" to");
            for (Iterator<String> it = targets.iterator(); it.hasNext();) {
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
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.targets);
        hash = 97 * hash + Objects.hashCode(this.exportedPackage);
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
        final Export other = (Export) obj;
        if (!Objects.equals(this.exportedPackage, other.exportedPackage)) {
            return false;
        }
        return Objects.equals(this.targets, other.targets);
    }
}
