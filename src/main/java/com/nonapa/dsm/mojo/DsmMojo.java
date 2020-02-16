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

            JdepsRunner.run(artifactPath, dsmPath.toString());

            Path dotPath = Paths.get(dsmPath.toString(), artifactName + ".dot");

            DotToLibraryParser parser = new DotToLibraryParser(artifactName);
            Library lib = parser.parse(dotPath);
            Collection<DSM> dsms = new DSMBuilder().forLibrary(lib)
                                            .aggressive(Boolean.parseBoolean(aggressive))
                                            .collectVariations(Boolean.parseBoolean(variations))
                                            .build();

            Path htmlPath = Paths.get(dsmPath.toString(), artifactName + ".html");
            new Report().print(new ArrayList<>(dsms), htmlPath.toFile());
            
            URL url = Report.class.getResource("dsm.css");
            if (url != null) {
                try (InputStream in = url.openStream()) {
                    Files.copy(in, Paths.get(dsmPath.toString(), "dsm.css"));
                }
            } else {
                throw new FileNotFoundException("Resource dsm.css not found");
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
