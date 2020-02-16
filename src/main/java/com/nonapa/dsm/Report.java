package com.nonapa.dsm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import com.nonapa.library.Unit;

public class Report {

    private static final int VARIATIONS_THRESHOLD = 5;
    
    public void print(DSM matrix, Path path) throws IOException {
        print(matrix, path.toFile());
    }

    public void print(DSM matrix, File file) throws IOException {
        try (PrintStream out = new PrintStream(new FileOutputStream(file), false, StandardCharsets.UTF_8.toString())) {
            print(matrix, out);
        }
    }

    public void print(DSM dsm, PrintStream out) throws IOException {
        print(Collections.singletonList(dsm), out);
    }

    public void print(List<DSM> matrixes, Path path) throws IOException {
        print(matrixes, path.toFile());
    }

    public void print(List<DSM> matrixes, File file) throws IOException {
        try (PrintStream out = new PrintStream(new FileOutputStream(file), false, StandardCharsets.UTF_8.toString())) {
            print(matrixes, out);
        }
    }

    public void print(List<DSM> matrixes, PrintStream out) throws IOException {
        head(out);
        body(matrixes, out);
        tail(out);
        out.flush();
    }

    private void head(PrintStream out) {

        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("  <meta charset=\"UTF-8\">");
        out.println("  <title>DSM</title>");
        out.println("  <link rel=\"stylesheet\" href=\"dsm.css\">");
        out.println("</head>");
    }

    private void body(List<DSM> matrixes, PrintStream out) throws IOException {
        out.println("<body>");

        if (!matrixes.isEmpty()) {
            stats(matrixes, out);
            
            for (int i = 0; i < Math.min(VARIATIONS_THRESHOLD, matrixes.size()); i++) {
                matrix(matrixes.get(i), i, out);
            }
            printDependencies(matrixes.get(0), out);
        }

        out.println("<script>");
        out.println("function openMatrix(name) {");
        out.println("var i;");
        out.println("var x = document.getElementsByClassName(\"dsmdiv\");");
        out.println("for (i = 0; i < x.length; i++) {");
        out.println("x[i].style.display = \"none\";");
        out.println("}");
        out.println("document.getElementById(name).style.display = \"block\";");
        out.println("}");
        out.println("</script>");

        out.println("</body>");
    }

    private void stats(List<DSM> matrixes, PrintStream out) {

        out.println("<div id=\"stats\">");
        out.print("<p><strong>Total packages: </strong>");
        out.println(matrixes.get(0).size() + "</p>");
        out.print("<p><strong>Cycling dependencies: </strong>");
        out.println(matrixes.get(0).cycles() + "</p>");
        
        if (matrixes.size() > 1) {
            out.print("<p><strong>Variations:</strong>");
            for (int i = 0; i < Math.min(VARIATIONS_THRESHOLD, matrixes.size()); i++) {
                out.print("<button onclick=\"openMatrix('dsm_" + i + "')\">" + i + "</button>");
            }
            out.println("</p>");
        }
        out.println("</div>");
    }

    private void matrix(DSM dsm, int id, PrintStream out) throws IOException {

        out.print("<div id=\"dsm_" + id + "\" class=\"dsmdiv\"");
        if (id != 0) {
            out.print("style=\"display:none\"");
        }
        out.println(">");
        out.println("<table id=\"m_" + id + "\" class=\"dsm\" border=\"1\">");
        out.println("  <tbody>");
        int[][] matrix = dsm.getMatrix();

        for (int i = 0; i < matrix.length; i++) {

            String pi = dsm.getPackage(i).getName();
            out.print("    <tr><th>" + pi + "</th>");

            for (int j = 0; j < matrix.length; j++) {

                if (j == i) {
                    out.print("<td class=\"same\">&ndash;</td>");
                    continue;
                }

                String pj = dsm.getPackage(j).getName();
                int value = matrix[i][j];
                if (value == 0) {
                    out.print("<td></td>");
                } else {
                    out.print("<td");
                    if (j > i) {
                        out.print(" class=\"cycle\"");
                    }
                    out.print(" title=\"" + pj + "\"><a href=\"#" + pi + "_to_" + pj + "\">" + value + "</a></td>");
                }
            }
            out.println("</tr>");
            if (out.checkError()) {
                throw new IOException("I/O error");
            }
        }

        out.println("  </tbody>");
        out.println("</table>");
        out.println("</div>");

    }

    private void printDependencies(DSM dsm, PrintStream out) {
        int[][] matrix = dsm.getMatrix();
        for (int i = 0; i < matrix.length; i++) {

            Unit pi = dsm.getPackage(i);
            for (int j = 0; j < matrix.length; j++) {

                if (matrix[i][j] == 0) {
                    continue;
                }

                Unit pj = dsm.getPackage(j);
                out.println("<table id=\"" + pi.getName() + "_to_" + pj.getName() + "\" class=\"cycles\">");
                out.println("<tr><th>" + pi.getName() + "</th><td>\u00A0\u2192\u00A0</td><th>" + pj.getName()
                        + "</th></tr>");

                for (Unit child : pi.getChildren()) {
                    for (Unit d : child.getDependencies()) {
                        if (d.getParent().equals(pj)) {
                            out.println("<tr><td>" + child.getName() + "</td><td>\u00A0\u2192\u00A0</td><td>"
                                    + d.getName() + "</td></tr>");
                        }
                    }
                }
                out.println("</table>");
            }
        }
    }

    private void tail(PrintStream out) {
        out.println("</html>");
    }

}
