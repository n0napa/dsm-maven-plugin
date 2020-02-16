package com.nonapa.jdeps;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.nonapa.jdeps.DotScanner.DependencyHandler;
import com.nonapa.library.Clazz;
import com.nonapa.library.Library;
import com.nonapa.library.Package;

public class DotToLibraryParser implements DependencyHandler {

    private static final String JDK = "JDK";
    private static final String DEFAULT_PACKAGE = "(default)";

    private Library library;

    private Map<String, Library> libCache = new HashMap<>();

    public DotToLibraryParser(String libName) {
        this.library = getLib(libName);
    }

    public Library parse(Path path) throws IOException {
        return parse(path.toFile());
    }

    public Library parse(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            return parse(in);
        }
    }

    public Library parse(InputStream in) {
        new DotScanner().scan(in, this);
        return library;
    }

    @Override
    public void nextDependency(String source, String target, String dependencyLib) {

        String sourcePackage = source.indexOf('.') == -1 ? DEFAULT_PACKAGE
                : source.substring(0, source.lastIndexOf('.'));
        String targetPackage = target.indexOf('.') == -1 ? DEFAULT_PACKAGE
                : target.substring(0, target.lastIndexOf('.'));

        Package p = (Package) library.addChild(sourcePackage);
        Clazz c = (Clazz) p.addChild(source);
        Library lib = getLib(dependencyLib);
        Package p2 = (Package) lib.addChild(targetPackage);
        Clazz c2 = (Clazz) p2.addChild(target);
        c.addDependency(c2);
    }

    private Library getLib(String name) {
        return libCache.computeIfAbsent(Optional.ofNullable(name).orElse(JDK), key -> new Library(key));
    }
}
