package com.nonapa.dsm.mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.nonapa.dsm.DSM;
import com.nonapa.dsm.DSMBuilder;
import com.nonapa.dsm.Report;
import com.nonapa.jdeps.DotToLibraryParser;
import com.nonapa.library.Library;
import com.sun.tools.jdeps.JdepsRunner;

@Mojo(name = "dsm", defaultPhase = LifecyclePhase.PACKAGE)
public class DsmMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "source", defaultValue = "")
    private File source;

    @Parameter(property = "aggressive", defaultValue = "true")
    private String aggressive;

    @Parameter(property = "variations", defaultValue = "false")
    private String variations;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        Path target = Paths.get(project.getBuild().getDirectory());
        Path dsmPath = Paths.get(target.toString(), "dsm");
        String artifactName;
        String artifactPath;
        if (source != null && source.exists()) {
            artifactName = source.getName();
            artifactPath = source.getPath();
        } else {
            artifactName = project.getBuild().getFinalName() + ".jar";
            artifactPath = Paths.get(target.toString(), artifactName).toString();
        }

        try {

            if (!Paths.get(artifactPath).toFile().exists()) {
                getLog().info("Artifact does not exist : " + artifactPath);
                return;
            }

            getLog().info("Running jdeps analysis on " + artifactPath);
            JdepsRunner.run(artifactPath, dsmPath.toString());

            Path dotPath = Paths.get(dsmPath.toString(), artifactName + ".dot");
            if (!dotPath.toFile().exists()) {
                getLog().info("JDeps output (.dot) not generated : " + dotPath);
                return;
            }

            getLog().debug("Generated jdeps .dot at " + dotPath);

            Library lib = new DotToLibraryParser(artifactName).parse(dotPath);
            Collection<DSM> dsms = new DSMBuilder().forLibrary(lib)
                                            .aggressive(Boolean.parseBoolean(aggressive))
                                            .collectVariations(Boolean.parseBoolean(variations))
                                            .build();

            getLog().debug("DSMs built for " + artifactPath);

            if (!dsms.isEmpty() && getLog().isDebugEnabled()) {
                DSM first = dsms.iterator().next();
                getLog().debug("Cycling dependecies: " + first.cycles());
                getLog().debug("Matrix:" + System.lineSeparator() + first);
            }

            Path htmlPath = Paths.get(dsmPath.toString(), artifactName + ".html");
            getLog().info("Building DSM html report for " + artifactPath);

            new Report().print(new ArrayList<>(dsms), htmlPath.toFile());
            URL url = Report.class.getResource("dsm.css");
            if (url != null) {
                try (InputStream in = url.openStream()) {
                    Files.copy(in, Paths.get(dsmPath.toString(), "dsm.css"));
                }
            } else {
                throw new FileNotFoundException("Resource dsm.css not found");
            }

            getLog().info("DSM html report successfully generated : " + htmlPath);

            getLog().debug("Cleaning jdeps resources.");
            if (Files.deleteIfExists(dotPath))
                getLog().debug("Resource deleted : " + dotPath);

            Path jdepsSummary = Paths.get(dotPath.getParent().toString(), "summary.dot");
            if (Files.deleteIfExists(jdepsSummary))
                getLog().debug("Resource deleted : " + jdepsSummary);

        } catch (IOException e) {
            getLog().error("Failed to generate DSM report", e);
        }
    }

}
