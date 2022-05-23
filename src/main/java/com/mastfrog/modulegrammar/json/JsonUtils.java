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
package com.mastfrog.modulegrammar.json;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Quick and dirty JSON.
 *
 * @author Tim Boudreau
 */
public final class JsonUtils {

    private static final ThreadLocal<Integer> DEPTH
            = ThreadLocal.withInitial(() -> 0);

    private static void descend(Runnable run) {
        int old = DEPTH.get();
        DEPTH.set(old + 1);
        try {
            run.run();
        } finally {
            DEPTH.set(old);
        }
    }

    private static StringBuilder indent(StringBuilder sb) {
        sb.append('\n');
        int d = DEPTH.get();
        if (d > 0) {
            char[] c = new char[d * 4];
            Arrays.fill(c, ' ');
            sb.append(c);
        }
        return sb;
    }

    public static StringBuilder nextItem(StringBuilder sb) {
        sb.append(',');
        return indent(sb);
    }

    public static String escapeAndQuote(CharSequence what) {
        String x = what.toString().replaceAll("\"", "\\\"");
        return '"' + x + '"';
    }

    public static StringBuilder append(String key, Object value, StringBuilder to) {
        append(value, to.append(escapeAndQuote(key)).append(" : "));
        return to;
    }

    public static StringBuilder list(StringBuilder sb, Runnable run) {
        sb.append('[');
        descend(() -> {
            indent(sb);
            run.run();
        });
        return sb.append(']');
    }

    public static StringBuilder hash(Runnable run, StringBuilder sb) {
        sb.append("{");
        descend(() -> {
            indent(sb);
            run.run();
        });
        indent(sb);
        return sb.append("}");
    }

    public static StringBuilder append(Object o, StringBuilder to) {
        if (o == null) {
            return to;
        } else if (o instanceof CharSequence seq) {
            to.append(escapeAndQuote(seq));
        } else if (o instanceof Character ch) {
            to.append(escapeAndQuote(Character.toString(ch)));
        } else if (o instanceof Number || o instanceof Boolean) {
            to.append(o.toString());
        } else if (o instanceof Enum<?> en) {
            to.append('"').append(en.name()).append('"');
        } else if (o instanceof JsonRenderable jsonRenderable) {
            jsonRenderable.renderJsonInto(to);
        } else if (o instanceof Collection<?> coll) {
            if (coll.isEmpty()) {
                to.append("[]");
                return to;
            }
            list(to, () -> {
                boolean hasPrev = false;
                for (Iterator<?> it = coll.iterator(); it.hasNext();) {
                    Object item = it.next();
                    if (item == null) {
                        continue;
                    }
                    if (hasPrev) {
                        nextItem(to);
                    }
                    append(item, to);
                    hasPrev = true;
                }
            });
        } else if (o instanceof Map<?, ?> m) {
            hash(() -> {
                int[] index = new int[1];
                m.forEach((key, val) -> {
                    if (index[0]++ > 0) {
                        nextItem(to);
                    }
                    append(key.toString(), val, to);
                });
            }, to);
        }
        return to;
    }

    private JsonUtils() {
        throw new AssertionError();
    }
}
