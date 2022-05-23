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
package com.mastfrog.modulegrammar;

import com.mastfrog.modulegrammar.model.Export;
import com.mastfrog.modulegrammar.model.ModuleModel;
import com.mastfrog.modulegrammar.model.Opens;
import com.mastfrog.modulegrammar.model.Provides;
import com.mastfrog.modulegrammar.model.Require;
import com.mastfrog.modulegrammar.model.annotation.AnnotationModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.antlr.v4.runtime.tree.ErrorNode;

/**
 * Visitor that populates a ModuleModel. Note that this package is not opened by
 * this module.
 *
 * @author Tim Boudreau
 */
public final class ModuleModelExtractor extends ModuleInfoGrammarParserBaseVisitor<ModuleModel> {

    private final Set<String> imports = new HashSet<>();
    private final Set<String> uses = new HashSet<>();
    private final Set<Require> requires = new HashSet<>();
    private final Set<Export> exports = new HashSet<>();
    private final Set<Provides> provides = new HashSet<>();
    private final Set<Opens> opens = new HashSet<>();
    private final List<AnnotationModel> annos = new ArrayList<>();
    private String moduleName;
    private boolean open;
    private final Consumer<ErrorNode> onError;

    public ModuleModelExtractor(Consumer<ErrorNode> onError) {
        this.onError = onError;
    }

    @Override
    public ModuleModel visitErrorNode(ErrorNode node) {
        onError.accept(node);
        return super.visitErrorNode(node);
    }

    @Override
    public ModuleModel visitCompilationUnit(ModuleInfoGrammarParser.CompilationUnitContext ctx) {
        super.visitCompilationUnit(ctx);
        return new ModuleModel(open, moduleName, imports, uses, requires, provides, exports, opens, annos);
    }

    @Override
    public ModuleModel visitAnnotation(ModuleInfoGrammarParser.AnnotationContext ctx) {
        AnnotationModel anno = ctx.accept(new AnnotationModelExtractor(onError));
        annos.add(anno);
        return null;
    }

    @Override
    public ModuleModel visitImportStatement(ModuleInfoGrammarParser.ImportStatementContext ctx) {
        imports.add(ctx.classIdentifier().getText());
        return super.visitImportStatement(ctx);
    }

    @Override
    public ModuleModel visitUsesStatement(ModuleInfoGrammarParser.UsesStatementContext ctx) {
        uses.add(ctx.classIdentifier().getText());
        return super.visitUsesStatement(ctx);
    }

    @Override
    public ModuleModel visitModuleDeclaration(ModuleInfoGrammarParser.ModuleDeclarationContext ctx) {
        moduleName = ctx.moduleIdentifier().getText();
        open = ctx.Open() != null;
        return super.visitModuleDeclaration(ctx);
    }

    @Override
    public ModuleModel visitRequiresStatement(ModuleInfoGrammarParser.RequiresStatementContext ctx) {
        boolean statyc = ctx.Static() != null;
        boolean transitive = ctx.Transitive() != null;
        String what = ctx.moduleIdentifier().getText();
        requires.add(new Require(statyc, transitive, what));
        return super.visitRequiresStatement(ctx);
    }

    @Override
    public ModuleModel visitExportsStatement(ModuleInfoGrammarParser.ExportsStatementContext ctx) {
        String what = ctx.packageIdentifier().getText();
        Set<String> tos = null;
        if (ctx.toClause() != null && !ctx.toClause().moduleIdentifier().isEmpty()) {
            tos = new HashSet<>();
            for (ModuleInfoGrammarParser.ModuleIdentifierContext target : ctx.toClause().moduleIdentifier()) {
                tos.add(target.getText());
            }
        }
        exports.add(new Export(what, tos));
        return super.visitExportsStatement(ctx);
    }

    @Override
    public ModuleModel visitOpensStatement(ModuleInfoGrammarParser.OpensStatementContext ctx) {
        String what = ctx.packageIdentifier().getText();
        Set<String> tos = null;
        if (ctx.toClause() != null && !ctx.toClause().moduleIdentifier().isEmpty()) {
            tos = new HashSet<>();
            for (ModuleInfoGrammarParser.ModuleIdentifierContext target : ctx.toClause().moduleIdentifier()) {
                tos.add(target.getText());
            }
        }
        opens.add(new Opens(what, tos));
        return super.visitOpensStatement(ctx);
    }

    @Override
    public ModuleModel visitProvidesStatement(ModuleInfoGrammarParser.ProvidesStatementContext ctx) {
        String what = ctx.classIdentifier().getText();
        Set<String> impls = new HashSet<>();
        for (ModuleInfoGrammarParser.ClassIdentifierContext target : ctx.providedTypesList().classIdentifier()) {
            impls.add(target.getText());
        }
        provides.add(new Provides(what, impls));
        return super.visitProvidesStatement(ctx);
    }
}
