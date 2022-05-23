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

import java.util.List;

/**
 * Kinds of values that annotation properties can have.
 *
 * @author Tim Boudreau
 */
public enum AnnotationValueKind {
    /**
     * A Class property - values are returned as Strings (no ".class" appended).
     */
    CLASS,
    /**
     * An Enum property - values are returned as Strings.
     */
    ENUM,
    /**
     * A Float property - values are either Float or Double, depending on
     * whether the original value was suffixed with 'F' or 'D' and/or what they
     * can be parsed accurately as. Cast to Number for type safety.
     */
    FLOAT,
    /**
     * An Integer property - values are either Long or Integer, depending on
     * whether the original was suffixed with 'L' and/or what they can be parsed
     * as. Cast to Number for type safety.
     */
    INT,
    /**
     * A String property - values are instances of String, and do <i>not</i>
     * have leading and trailing quotes.
     */
    STRING,
    /**
     * A Character property - values are Character.
     */
    CHAR,
    /**
     * A Boolean property - values are Boolean.
     */
    BOOLEAN,
    /**
     * An array property; values are AnnotationValue instances.
     */
    ARRAY,
    /**
     * An annotation property; values are AnnotationModel instances.
     */
    ANNOTATION;

    /**
     * Get the type values for this kind will have.
     *
     * @return A type
     */
    public Class<?> javaValueType() {
        switch (this) {
            case INT:
            case FLOAT:
                return Number.class;
            case CHAR:
                return Character.class;
            case ARRAY:
                return List.class;
            case ANNOTATION:
                return AnnotationModel.class;
            case BOOLEAN:
                return Boolean.class;
            case CLASS:
            case ENUM:
            case STRING:
                return String.class;
            default:
                throw new AssertionError(this);
        }
    }
}
