package com.nonapa.jdeps;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DotScanner {

    private static final String IDENTIFIER = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
    // pattern for fully qualified class names
    private static final Pattern FQCN = Pattern.compile(IDENTIFIER + "(\\." + IDENTIFIER + ")*");

    // noop dependency handler
    private static final DependencyHandler DEFAULT_HANDLER = (s, t, l) -> {};

    public void scan(Path path, DependencyHandler handler) throws IOException {
        scan(path.toFile(), handler);
    }

    public void scan(File file, DependencyHandler handler) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            scan(in, handler);
        }
    }

    public void scan(InputStream in, DependencyHandler handler) {
        try (Scanner scanner = new Scanner(in)) {
            scan(scanner, handler);
        }
    }

    private void scan(Scanner scanner, DependencyHandler handler) {
        // TODO revisit this
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] split = line.split("->");
            if (split.length != 2) {
                // TODO handle error line - create an error diagnostic
                continue;
            }

            Matcher m1 = FQCN.matcher(split[0]);
            if (!m1.find()) {
                // TODO handle error line - create an error diagnostic
                continue;
            }

            Matcher m2 = FQCN.matcher(split[1]);
            if (!m2.find()) {
                // TODO handle error line - create an error diagnostic
                continue;
            }

            String dependencyLib = null;
            if (line.contains("(") && line.contains(")")) {
                dependencyLib = line.substring(line.indexOf('(') + 1, line.indexOf(')'));
            }

            Optional.ofNullable(handler).orElse(DEFAULT_HANDLER)
                        .nextDependency(m1.group(0), m2.group(0), dependencyLib);
        }
    }

    interface DependencyHandler {
        void nextDependency(String source, String target, String dependencyLib);
    }
}
