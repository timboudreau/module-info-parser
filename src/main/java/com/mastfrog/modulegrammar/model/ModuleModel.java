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
import com.mastfrog.modulegrammar.model.annotation.AnnotationModel;
import com.mastfrog.modulegrammar.model.annotation.AnnotationModelVisitor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Models a <code>module-info.java</code> file's contents. Note that the
 * contents are as-is, though query methods will attempt to resolve unqualified
 * names using imports if present. Call <code>resolved()</code> to create a copy
 * where all class names are resolved to fully-qualified java package+class
 * names in all child elements.
 * <p>
 * The <code>toString()</code> representation is a usable, parsable copy of the
 * module's contents (not comment- or whitespace-preserving).
 *
 * @author Tim Boudreau
 */
public final class ModuleModel implements JsonRenderable {

    private final boolean open;
    private final String name;
    private final Imports imports;
    private final Uses uses;
    private final Set<Require> requires;
    private final Set<Provides> provides;
    private final Set<Export> exports;
    private final Set<Opens> opens;
    private final List<AnnotationModel> annotations;

    public ModuleModel(boolean open, String name,
            Collection<? extends String> imports,
            Collection<? extends String> uses,
            Set<Require> requires,
            Collection<? extends Provides> provides,
            Collection<? extends Export> exports,
            Collection<? extends Opens> opens,
            Collection<? extends AnnotationModel> annotations) {
        this.open = open;
        this.name = name;
        this.imports = new Imports(imports);
        this.uses = new Uses(uses);
        this.requires = new TreeSet<>(requires);
        this.provides = new TreeSet<>(provides);
        this.exports = new TreeSet<>(exports);
        this.opens = new TreeSet<>(opens);
        this.annotations = new ArrayList<>(annotations);
    }

    public boolean requires(String what) {
        for (Require req : requires) {
            if (req.moduleName().equals(what)) {
                return true;
            }
        }
        return false;
    }

    public boolean requiresTransitive(String what) {
        for (Require req : requires) {
            if (req.isTransitive() && req.moduleName().equals(what)) {
                return true;
            }
        }
        return false;
    }

    public boolean requiresStatic(String what) {
        for (Require req : requires) {
            if (req.isStatic() && req.moduleName().equals(what)) {
                return true;
            }
        }
        return false;
    }

    public boolean uses(String what) {
        String resolved = imports.resolve(what);
        for (String s : uses) {
            if (s.equals(what) || s.equals(resolved) || imports.resolve(s).equals(resolved)) {
                return true;
            }
        }
        return false;
    }

    public boolean provides(String what) {
        for (Provides p : provides) {
            if (p.provided().equals(what)) {
                return true;
            } else if (p.provided().indexOf('.') < 0 && imports.resolve(p.provided()).equals(what)) {
                return true;
            }
        }
        return false;
    }

    public boolean provides(String what, String with) {
        for (Provides p : provides) {
            if (p.provided().equals(what)) {
                for (String s : p) {
                    if (what.equals(s)) {
                        return true;
                    } else if (!s.contains(".") && imports.resolve(s).equals(with)) {
                        return true;
                    } else if (imports.resolve(with).equals(s)) {
                        return true;
                    } else if (imports.resolve(with).equals(imports.resolve(s))) {
                        return true;
                    }
                }
            } else if (p.provided().indexOf('.') < 0 && (imports.resolve(p.provided()).equals(what) || imports.resolve(p.provided()).equals(imports.resolve(what)))) {
                for (String s : p) {
                    if (with.equals(s)) {
                        return true;
                    } else if (!s.contains(".") && !with.contains(".") && imports.resolve(s).equals(imports.resolve(with))) {
                        return true;
                    } else if (!s.contains(".") && imports.resolve(s).equals(with)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Traverse all of the module's annotations and their values.
     *
     * @param visitor A visitor
     * @return false if the visitor returned false at some point in traversal to
     * abort traversal
     */
    public boolean visitAnnotations(AnnotationModelVisitor visitor) {
        for (AnnotationModel mdl : annotations) {
            if (!mdl.visitValues(visitor)) {
                return false;
            }
        }
        return true;
    }

    public boolean opens(String pkg, String to) {
        if (isOpen()) {
            return true;
        }
        for (Opens o : opens) {
            if (pkg.equals(o.opened())) {
                return o.isOpenedTo(to);
            }
        }
        return false;
    }

    /**
     * Resolve all class names in the model to instances with fully qualified
     * class names.
     *
     * @return A new ModuleModel if that results in any differences, otherwise
     * this
     */
    public ModuleModel resolved() {
        Set<Provides> newProvides = new TreeSet<>();
        boolean anyChanged = false;
        for (Provides p : provides) {
            Provides res = p.resolve(imports);
            if (res != p) {
                anyChanged = true;
            }
            newProvides.add(res);
        }
        Uses newUses = uses.resolve(imports);
        anyChanged |= newUses != uses;
        List<AnnotationModel> newAnnos = new ArrayList<>(annotations.size());
        for (AnnotationModel m : annotations) {
            AnnotationModel newAnno = m.resolve(imports);
            newAnnos.add(newAnno);
            if (newAnno != m) {
                anyChanged = true;
            }
        }
        if (!anyChanged) {
            return this;
        }
        return new ModuleModel(open, name, Collections.emptySet(),
                newUses.allUses(), requires, newProvides, exports, opens,
                newAnnos);
    }

    /**
     * Get the set of annotations on this Module.
     *
     * @return A collection of annotations.
     */
    public List<? extends AnnotationModel> annotations() {
        return Collections.unmodifiableList(annotations);
    }

    /**
     * Get the set of modules that are mentioned in require statements.
     *
     * @return A set of modules
     */
    public Set<String> requiredModuleNames() {
        Set<String> result = new HashSet<>();
        for (Require req : requires) {
            result.add(req.moduleName());
        }
        return result;
    }

    /**
     * True if this is an open module.
     *
     * @return True if open
     */
    public boolean isOpen() {
        return open;
    }

    public Set<? extends Opens> opens() {
        return Collections.unmodifiableSet(opens);
    }

    public String moduleName() {
        return name;
    }

    public Imports imports() {
        return imports;
    }

    public Uses uses() {
        return uses;
    }

    /**
     * Get the set of fully qualified class names used in use statements,
     * resolved using imports where possible.
     *
     * @return A set of class anmes.
     */
    public Set<? extends String> usedClasses() {
        return uses.resolve(imports).resolvedUses(imports);
    }

    /**
     * Get pairs of fully qualified (if they can be qualified using imports or
     * already were) provider/provides classes declared in this module.
     *
     * @param specAndImplConsumer A consumer which receives the provider
     * interface type and the implementation type
     */
    public void visitProvidedClasses(BiConsumer<String, String> specAndImplConsumer) {
        for (Provides p : provides) {
            p.resolve(imports()).visitProvides(imports, specAndImplConsumer);
        }
    }

    public Set<Require> requires(Predicate<? super Require> test) {
        Set<Require> result = new HashSet<>();
        for (Require req : requires) {
            if (test.test(req)) {
                result.add(req);
            }
        }
        return result;
    }

    public Set<? extends Require> requires() {
        return Collections.unmodifiableSet(requires);
    }

    public Set<? extends Provides> provides() {
        return Collections.unmodifiableSet(provides);
    }

    public Set<? extends Export> exports() {
        return Collections.unmodifiableSet(exports);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!imports.isEmpty()) {
            sb.append(imports).append('\n');
        }
        if (!annotations.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            for (AnnotationModel am : annotations) {
                sb.append(am).append('\n');
            }
        }
        if (open) {
            sb.append("open ");
        }
        sb.append("module ").append(name).append(" {\n");
        for (Require r : requires) {
            sb.append('\n').append(r);
        }
        for (String use : uses) {
            sb.append("\n    uses ").append(use).append(';');
        }
        for (Provides p : provides) {
            sb.append('\n').append(p);
        }
        for (Export e : exports) {
            sb.append('\n').append(e);
        }
        for (Opens o : opens) {
            sb.append('\n').append(o);
        }
        return sb.append("\n}\n").toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.open ? 1 : 0);
        hash = 13 * hash + Objects.hashCode(this.name);
        hash = 13 * hash + Objects.hashCode(this.imports);
        hash = 13 * hash + Objects.hashCode(this.uses);
        hash = 13 * hash + Objects.hashCode(this.requires);
        hash = 13 * hash + Objects.hashCode(this.provides);
        hash = 13 * hash + Objects.hashCode(this.exports);
        hash = 13 * hash + Objects.hashCode(this.opens);
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
        final ModuleModel other = (ModuleModel) obj;
        if (this.open != other.open) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.imports, other.imports)) {
            return false;
        }
        if (!Objects.equals(this.annotations, other.annotations)) {
            return false;
        }
        if (!Objects.equals(this.uses, other.uses)) {
            return false;
        }
        if (!Objects.equals(this.requires, other.requires)) {
            return false;
        }
        if (!Objects.equals(this.provides, other.provides)) {
            return false;
        }
        if (!Objects.equals(this.exports, other.exports)) {
            return false;
        }
        return Objects.equals(this.opens, other.opens);
    }

    public Optional<AnnotationModel> findAnnotation(String annotationClassName) {
        String resolved = imports.resolve(annotationClassName);
        for (AnnotationModel mdl : annotations) {
            if (mdl.name().equals(annotationClassName) || imports.resolve(mdl.name()).equals(resolved)) {
                return Optional.of(mdl);
            }
        }
        return Optional.empty();
    }

    @Override
    public StringBuilder renderJsonInto(StringBuilder sb) {
        return JsonUtils.hash(() -> {
            JsonUtils.append("name", name, sb);
            JsonUtils.nextItem(sb);
            JsonUtils.append("open", open, sb);
            if (!imports.isEmpty()) {
                JsonUtils.nextItem(sb);
                JsonUtils.append("imports", imports, sb);
            }
            if (!uses.isEmpty()) {
                JsonUtils.nextItem(sb);
                JsonUtils.append("uses", uses, sb);
            }
            if (!requires.isEmpty()) {
                JsonUtils.nextItem(sb);
                JsonUtils.append("requires", requires, sb);
            }
            if (!provides.isEmpty()) {
                JsonUtils.nextItem(sb);
                JsonUtils.append("provides", provides, sb);
            }
            if (!exports.isEmpty()) {
                JsonUtils.nextItem(sb);
                JsonUtils.append("exports", exports, sb);
            }
            if (!opens.isEmpty()) {
                JsonUtils.nextItem(sb);
                JsonUtils.append("opens", opens, sb);
            }
            if (!annotations.isEmpty()) {
                JsonUtils.nextItem(sb);
                JsonUtils.append("annotations", annotations, sb);
            }
        }, sb);
    }
}
