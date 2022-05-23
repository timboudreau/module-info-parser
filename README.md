Java Module Grammar
===================

An ANTLR grammar for parsing `module-info.java` files based on the specification, which
also handles modelling Java annotations (which modules may have) and their contents.

Usage
-----

```java
ModuleModel model = ModuleParser.parse(Paths.get("/path/to/module-info.java"), 
   ModuleParserErrorListener.THROWING);
```

`ModuleModel` then lets you list or query `requires`, `opens`, `provides`, `uses` or `exports` statements
in the module file, walk any annotations on the module, search for annotations by name, etc.

_Note:_ `ModuleModel.resolve()` will use the `import` statements from the module to create a _resolved copy_
of the model, in which all annotation names and class names of all elements of the model have been replaced
by fully qualified class names with a leading package (this is assuming the import statements are complete).
Elements from `java.lang` are also automatically fully qualified.

Query methods will try both unresolved and resolved versions of their input and what is tested, but the
`resolve()` method guarantees everything is consistent and uses fully qualified class names where it was possible
to determine them (if they were not, the input java file was probably missing imports).

Note that resolving does not work if wildcard imports are used in the `module-info.java` - this library will
not guess.  In that case, if you have access to the package, you could supply an augmented `Imports` instance
that fills in explicit types (or enum constants).

As an Executable
----------------

For consumption by _whatever_, `ModuleModel` and all of its child objects have a simple
`toJson()` method which will return a straightforward JSON representation of the complete
contents of a `module-info.java` file (modulo comments).

In addition to the usual artifacts, the Maven build creates an executable _fat jar_ which
can be run with `java -jar` and passed the path to a module-info file, and will output that
JSON format. E.g.,

```sh
java -jar target/module-info-grammar.jar src/test/java/module-info.java
```

```json
{
    "name" : "module.info.grammar",
    "open" : false,
    "requires" : [
        {
            "module" : "com.fasterxml.jackson.databind", "transitive" : true, "static" : false
        },
        {
            "module" : "org.antlr.antlr4.runtime", "transitive" : false, "static" : false
        },
        {
            "module" : "org.junit.jupiter.api", "transitive" : false, "static" : false
        },
        {
            "module" : "org.junit.jupiter.engine", "transitive" : false, "static" : false
        }],
    "opens" : [
        {
            "package" : "com.mastfrog.modulegrammar.model"
        },
        {
            "package" : "com.mastfrog.modulegrammar.model.annotation"
        },
        {
            "package" : "com.mastfrog.modulegrammar.parser"
        }]
}
```