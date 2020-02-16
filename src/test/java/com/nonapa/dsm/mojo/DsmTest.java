package com.nonapa.dsm.mojo;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import com.nonapa.dsm.DSM;
import com.nonapa.dsm.DSMBuilder;
import com.nonapa.dsm.Report;
import com.nonapa.jdeps.DotToLibraryParser;
import com.nonapa.library.Library;
import com.sun.tools.jdeps.JdepsRunner;

public class DsmTest {

    public static void main(String[] args) throws Exception {

        String workspace = "E:\\temp\\JDEPS\\tests\\";

//		String jarName = "engine.jar";
//        String jarName = "liquibase.jar";
        String jarName = "xtext.jar";

        String jar = workspace + jarName;

        JdepsRunner.run(jar, workspace);

        DotToLibraryParser parser = new DotToLibraryParser(jarName);
        Library lib = parser.parse(Paths.get(jar + ".dot"));

        Collection<DSM> dsms = new DSMBuilder().forLibrary(lib)
                                        .aggressive(true)
                                        .collectVariations(false)
                                        .build();
        DSM first = dsms.iterator().next();
        System.out.println(first.toString());
        System.out.println("Cycles = " + first.cycles());
        new Report().print(new ArrayList<>(dsms), new File(jar + ".html"));
    }
}
