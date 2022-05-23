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

/**
 * Walks the tree of annotations, visiting child values of any annotation values
 * which are themselves annotations. Any of the methods may return false to
 * abort further traversal, so the tree can be searched efficiently. If
 * traversal is aborted due to <code>visitValue()</code> returning false,
 * <code>onExitAnnotationModel()</code> will still be called for each annotation
 * that has been entered, but the return value is ignored.
 */
@FunctionalInterface
public interface AnnotationModelVisitor {

    /**
     * Optional method, called when entering an annotation.
     *
     * @param model The annotation model being visited
     * @param depth The depth, zero for top-level
     * @return true if traversal should continue
     */
    default boolean onEnterAnnotationModel(AnnotationModel model, int depth) {
        return true;
    }

    /**
     * Visit one annotation parameter name/value pair.
     *
     * @param owner The annotation owning this name/value pair
     * @param depth The depth of that owner (0 means this is a top-level
     * annotation, not an annotation value of some child object)
     * @param name The name of the property
     * @param value The value of the property
     * @param isArrayElement True if the value is one element of an array
     * @return true to continue traversal, false to abort
     */
    boolean visitValue(AnnotationModel owner, int depth, String name, AnnotationValue<?> value, boolean isArrayElement);

    /**
     * Called when all properties of an annotation have been visited. If
     * visitValue() has already returned false to abort traversal, this method
     * will still be called symmetrically with calls to
     * <code>onEnterAnnotationModel</code> but the result will be ignored, and
     * no further annotations will be entered.
     *
     * @param model The annotation being exited
     * @param depth The depth - 0 = top-level
     * @return true to continue traversal (if any), false to abort
     */
    default boolean onExitAnnotationModel(AnnotationModel model, int depth) {
        return true;
    }

}
