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
package com.mastfrog.modulegrammar.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastfrog.modulegrammar.model.ModuleModel;
import com.mastfrog.modulegrammar.model.annotation.AnnotationModel;
import com.mastfrog.modulegrammar.model.annotation.AnnotationValue;
import com.mastfrog.modulegrammar.model.annotation.AnnotationValueKind;
import static com.mastfrog.modulegrammar.parser.ModuleParser.parse;
import static com.mastfrog.modulegrammar.parser.ModuleParserErrorListener.THROWING;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.tree.ErrorNode;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ModuleParserTest {

    @Test
    public void testParseWithAnnotations() {
        ModuleModel model = parse(TEST_WITH_ANNOS, ModuleParserErrorListener.THROWING);
        assertNotNull(model);
        assertEquals("build.thing", model.moduleName());
        assertFalse(model.isOpen());
        assertEquals("com.mastfrog.module.annotations.Maven", model.imports().resolve("Maven"));
        assertEquals("java.lang.Long", model.imports().resolve("Long"));

        assertFalse(model.isOpen());
        assertTrue(model.opens("wurg.gwee", "argle.bargle"));
        assertTrue(model.opens("wurg.gwee", "wumble.squmble"));
        assertFalse(model.opens("blah.gwee", "wumble.squmble"));
        assertFalse(model.opens("wurg.gwee", "other.package"));

        assertTrue(model.provides("Processor"));
        assertTrue(model.provides("javax.annotation.processing.Processor"));
        assertFalse(model.provides("fnord.fnord.Processor"));

        assertTrue(model.provides("Processor", "DefaultsAnnotationProcessor"));
        assertTrue(model.provides("javax.annotation.processing.Processor", "com.mastfrog.giulius.annotation.processors.DefaultsAnnotationProcessor"));
        assertTrue(model.provides("javax.annotation.processing.Processor", "DefaultsAnnotationProcessor"));
        assertTrue(model.provides("javax.annotation.processing.Processor", "com.mastfrog.giulius.annotation.processors.NamespaceAnnotationProcessor"));

        assertTrue(model.requires("fnords.are.invisible"));
        assertTrue(model.requiresTransitive("fnords.are.invisible"));
        assertFalse(model.requiresStatic("fnords.are.invisible"));

        assertTrue(model.requires("module.annotations"));
        assertTrue(model.requiresStatic("module.annotations"));
        assertFalse(model.requiresTransitive("module.annotations"));

        assertTrue(model.requires("module.info.grammar"));
        assertFalse(model.requiresStatic("module.info.grammar"));
        assertFalse(model.requiresTransitive("module.info.grammar"));

        assertTrue(model.uses("AbstractWoogle"), "AbstractWoogle missing from " + model.usedClasses());
        assertTrue(model.usedClasses().contains("com.mastfrog.util.service.AbstractWoogle"), "com.mastfrog.util.service.AbstractWoogle missing from " + model.usedClasses());
        assertTrue(model.resolved().usedClasses().contains("com.mastfrog.util.service.AbstractWoogle"));

        assertTrue(model.findAnnotation("Slarg").isPresent());
        assertTrue(model.findAnnotation("Noog").isPresent());
        assertTrue(model.findAnnotation("com.mastfrog.modxule.annotations.Blarg").isPresent());
        assertTrue(model.findAnnotation("Blarg").isPresent());
        assertTrue(model.findAnnotation("Glark").isPresent(), "No Glark in " + model.annotations());
        assertFalse(model.findAnnotation("NotAThing").isPresent());

        AnnotationModel anno = model.findAnnotation("Slarg").get();
        assertTrue(anno.getProperty("mub").isPresent());
        assertTrue(anno.getProperty("gug").isPresent());
        assertTrue(anno.getProperty("wig").isPresent());

        assertTrue(anno.getValue("mub").isPresent());
        assertTrue(anno.getValue("gug").isPresent());
        assertTrue(anno.getValue("wig").isPresent());

        assertSame(AnnotationValueKind.CHAR, anno.getProperty("mub").get().kind());
        assertSame(AnnotationValueKind.INT, anno.getProperty("gug").get().kind());
        assertSame(AnnotationValueKind.FLOAT, anno.getProperty("wig").get().kind());

        assertTrue(anno.getValue("mub").get() instanceof Character);
        assertTrue(anno.getValue("gug").get() instanceof Long);
        assertTrue(anno.getValue("wig").get() instanceof Double);

        anno = model.findAnnotation("Poob").get();
        assertSame(AnnotationValueKind.ENUM, anno.getProperty("bargle").get().kind());
        assertSame(AnnotationValueKind.BOOLEAN, anno.getProperty("bug").get().kind());
        assertSame(AnnotationValueKind.INT, anno.getProperty("gug").get().kind());
        assertTrue(anno.getValue("bargle").get() instanceof String);
        assertTrue(anno.getValue("bug").get() instanceof Boolean);
        assertTrue(anno.getValue("gug").get() instanceof Integer);

        anno = model.findAnnotation("Noog").get();
        assertTrue(anno.getProperty("value").isPresent());
        assertSame(AnnotationValueKind.ARRAY, anno.getProperty("value").get().kind());
        assertTrue(anno.getProperty("value").get().value() instanceof List<?>);

        List<AnnotationValue<?>> avs = (List<AnnotationValue<?>>) anno.getProperty("value").get().value();
        assertEquals(2, avs.size(), "Should have two but have one: " + avs);
        assertEquals("BUG", avs.get(0).value());
        assertEquals("WUG", avs.get(1).value());
        assertSame(AnnotationValueKind.ENUM, avs.get(0).kind());
        assertSame(AnnotationValueKind.ENUM, avs.get(0).kind());

        anno = model.findAnnotation("Foo").get();
        assertSame(AnnotationValueKind.CLASS, anno.getProperty("value").get().kind());

        anno = model.findAnnotation("Maven").get();
        assertSame(AnnotationValueKind.ARRAY, anno.getProperty("value").get().kind());
        assertTrue(anno.getProperty("value").get().value() instanceof List<?>);
        avs = (List<AnnotationValue<?>>) anno.getProperty("value").get().value();
        assertEquals(1, avs.size());
        assertTrue(avs.get(0).value() instanceof AnnotationModel);
        anno = (AnnotationModel) avs.get(0).value();
        assertEquals("Artifact", anno.name());
        assertEquals("com.mastfrog:util-preconditions:2.8.1", anno.getValue("is").get());
        assertEquals("util.preconditions", anno.getValue("javaModule").get());
    }

    @Test
    public void testOpenModuleDetection() {
        ModuleModel model = parse(TEST_WITH_ANNOS.replaceAll("module build.thing ",
                "open module build.thing "), ModuleParserErrorListener.THROWING);
        assertTrue(model.isOpen());
    }

    @Test
    public void testParseWithParseErrors() {
        ErrorChecker check = new ErrorChecker();
        String erroneous = "module some.stuff {\n requires foo.bar;\n }\n" + TEST_WITH_ANNOS;
        parse(erroneous, check);
        check.assertErrorNode();
    }

    @Test
    public void testParseWithSyntaxErrors() {
        String erroneous = TEST_WITH_ANNOS.replace("requires", "r#quires");
        ErrorChecker check = new ErrorChecker();
        parse(erroneous, check);
        check.assertSyntaxError();
    }

    @Test
    public void testResolvableEnumConstants() {
        ModuleModel mm = parse(RESOLVABLE_ENUM_CONSTANTS, THROWING);
        assertNotNull(mm);
        ModuleModel r = mm.resolved();
        assertTrue(r.toString().contains("@com.foo.SomeAnno(value = {com.foo.SomeEnum.ONE, com.foo.SomeEnum.TWO, com.foo.SomeEnum.THREE})"));
        assertTrue(r.toString().contains("@com.foo.OtherAnno(value = {com.foo.Oe.FIRST, com.foo.Oe.SECOND, com.foo.Oe.THIRD})"));
    }

    @Test
    public void testNonLossySmall() {
        ModuleModel mm = parse(RESOLVABLE_ENUM_CONSTANTS, THROWING);
        ModuleModel reparsed = parse(mm.toString(), THROWING);
        assertEquals(mm, reparsed);
    }

    @Test
    public void testNonLossyComplete() {
        ModuleModel mm = parse(TEST_WITH_ANNOS, THROWING);
        ModuleModel reparsed = parse(mm.toString(), THROWING);

        assertEquals(mm, reparsed);
    }
    
    @Test
    public void testToJson() throws JsonProcessingException {
        ModuleModel mm = parse(TEST_WITH_ANNOS, THROWING);
        String json = mm.resolved().toJson();
        System.out.println("JSON:\n" + json);
        // Simply make sure that we got valid json
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> map = mapper.readValue(json, Map.class);
    }

    static class ErrorChecker implements ModuleParserErrorListener {

        private ErrorNode err;
        private ModuleSyntaxError serr;

        public ErrorNode assertErrorNode() {
            assertNotNull(err, "No error node encountered");
            ErrorNode result = err;
            err = null;
            return result;
        }

        public ModuleSyntaxError assertSyntaxError() {
            assertNotNull(serr, "No syntax error encountered");
            ModuleSyntaxError result = serr;
            serr = null;
            return result;
        }

        @Override
        public void onParserError(ErrorNode node) {
            err = node;
        }

        @Override
        public void onSyntaxError(ModuleSyntaxError err) {
            this.serr = err;
        }
    }

    private static final String RESOLVABLE_ENUM_CONSTANTS = "import com.foo.SomeAnno;\n"
            + "import com.foo.SomeEnum.ONE;\n"
            + "import com.foo.SomeEnum.TWO;\n"
            + "import com.foo.SomeEnum.THREE;\n"
            + "import com.foo.OtherAnno;\n"
            + "import com.foo.Oe;\n\n"
            + "@SomeAnno({ ONE, TWO, THREE})\n"
            + "@OtherAnno({ Oe.FIRST, Oe.SECOND, Oe.THIRD})\n"
            + "module poodle.farb {\n"
            + "    requires transitive something;\n"
            + "}\n";

    private static final String TEST_WITH_ANNOS = "import com.mastfrog.modxule.annotations.Artifact;\n"
            + "import com.mastfrog.module.annotations.Maven;\n"
            + "import com.mastfrog.modxule.annotations.Foo;\n"
            + "import com.mastfrog.modxule.annotations.Blarg;\n"
            + "import com.mastfrog.modxule.annotations.Boob;\n"
            + "import com.mastfrog.modxule.annotations.Arg;\n"
            + "import com.mastfrog.util.service.AbstractWoogle;\n"
            + "import javax.annotation.processing.Processor;\n"
            + "import com.mastfrog.giulius.annotation.processors.DefaultsAnnotationProcessor;\n"
            + "import foo.bar.Workg;\n"
            + "\n"
            + "@Maven( value = {\n"
            + "    @Artifact(javaModule=\"util.preconditions\", is=\"com.mastfrog:util-preconditions:2.8.1\")\n"
            + "})\n"
            + "@Workg({ String.class, Integer.class, Thread.class })\n"
            + "@Workg({ Arg.class, Blarg.class, })\n"
            + "\n"
            + "@Foo( Xd.class )\n"
            + "\n"
            + "@Slarg( mub = 'g', gug = 524324L, wig = 0.432D)\n"
            + "\n"
            + "@Blarg({ WIG, WUG })\n"
            + "\n"
            + "@Noog( { BUG, WUG } )\n"
            + "@Glark\n"
            + "\n"
            + "@Poob(bargle = WOOB, \n"
            + "      bug = true, \n"
            + "      gug = 23\n"
            + ")\n"
            + "\n"
            + "module build.thing {\n"
            + "    requires static module.annotations;\n"
            + "    requires module.info.grammar;\n"
            + "    requires transitive fnords.are.invisible;\n"
            + "    requires static java.compiler;\n"
            + "    opens wurg.gwee to wumble.squmble, argle.bargle;\n"
            + "    uses AbstractWoogle;\n"
            + "    provides Processor with\n"
            + "       DefaultsAnnotationProcessor,\n"
            + "       com.mastfrog.giulius.annotation.processors.NamespaceAnnotationProcessor;\n"
            + "}";

}
