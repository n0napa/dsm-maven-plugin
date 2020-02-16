package com.nonapa.dsm;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.nonapa.library.Unit;

public class DSM {

    private int[][] matrix;
    private List<Unit> packages;

    DSM(List<Unit> packages) {
        this.packages = packages;
        this.matrix = toMatrix(packages);
    }

    private int[][] toMatrix(List<Unit> packages) {

        int[][] m = new int[packages.size()][packages.size()];
        for (int i = 0; i < packages.size(); i++) {
            for (int j = 0; j < packages.size(); j++) {
                m[i][j] = packages.get(i).dependenciesTo(packages.get(j));
            }
        }
        return m;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public int cycles() {
        return cycles(packages);
    }

    static int cycles(List<Unit> units) {

        int result = 0;
        for (int i = 0; i < units.size(); i++) {
            for (int j = i + 1; j < units.size(); j++) {
                result += units.get(i).dependenciesTo(units.get(j));
            }
        }
        return result;
    }

    public int size() {
        return packages.size();
    }

    public Unit getPackage(int index) {
        return packages.get(index);
    }

    @Override
    public String toString() {

        // TODO overkill
        int longestName = packages.stream().map(Unit::getName).max(Comparator.comparingInt(String::length)).get()
                .length();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < matrix.length; i++) {
            builder.append(packages.get(i).getName());
            for (int j = 0; j < longestName - packages.get(i).getName().length(); j++) {
                builder.append(" ");
            }
            builder.append(Arrays.toString(matrix[i])).append("\n");
        }
        return builder.toString();
    }

}
