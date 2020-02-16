package com.nonapa.dsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.nonapa.library.Library;
import com.nonapa.library.Unit;

public class DSMBuilder {

    private List<Unit> packages = Collections.emptyList();
    private boolean collectVariations;
    private boolean aggressive;
    private int cycles = Integer.MAX_VALUE;
    private Set<List<Unit>> variations = new HashSet<>();

    public DSMBuilder forLibrary(Library lib) {
        this.packages = lib.getChildren().stream().collect(Collectors.toList());
        return this;
    }

    public DSMBuilder collectVariations(boolean collectVariations) {
        this.collectVariations = collectVariations;
        return this;
    }

    public DSMBuilder aggressive(boolean aggressive) {
        this.aggressive = aggressive;
        return this;
    }

    public Collection<DSM> build() {

        // for small amount of packages use permutations for best results
        if (packages.size() <= Utils.THRESHOLD) {
            cycles = permute();
        } else {
            nonapaSort(aggressive);
        }

        if (collectVariations) {
            List<DSM> dsms = new ArrayList<>();
            for (List<Unit> packs : variations) {
                dsms.add(new DSM(packs));
            }
            return dsms;
        } else {
            return Collections.singleton(new DSM(packages));
        }
    }

    private int permute() {
        int tmpCycles = Integer.MAX_VALUE;
        for (int i = 0; i < Utils.factorial(packages.size()); i++) {
            List<Unit> permutation = Utils.permutation(i, packages);
            int countCycles = DSM.cycles(permutation);

            if (countCycles < tmpCycles) {
                tmpCycles = countCycles;
                packages = permutation;

                if (collectVariations) {
                    variations.clear();
                    variations.add(permutation);
                }
            } else if (countCycles == tmpCycles) {
                if (collectVariations) {
                    variations.add(permutation);
                }
            }
        }
        return tmpCycles;
    }

    /**
     * Sort following the method of mathematical guessing + addition to get better
     * answer
     */
    private void nonapaSort(boolean aggressive) {

        cycles = initialSort();
        cycles = diagonalReflectionSort();

        if (aggressive) {
            int tmp = -1;
            while (tmp != cycles) {
                tmp = cycles;
                diagonalReflectionSort();
            }
        }
    }

    private int initialSort() {

        Collections.sort(packages, (p1, p2) -> {
           
            // check overall package dependency count -> how many
            // classes from other packages this package depends on.
            int diff = p1.dependencyCount() - p2.dependencyCount();
            if (diff == 0) {
                diff = p1.dependenciesTo(p2) - p2.dependenciesTo(p1);
                if (diff == 0) {
                    // last resort
                    return p1.getName().compareTo(p2.getName());
                }
            }
            return diff;
        });

        return DSM.cycles(packages);
    }

    /**
     * Compare packages that are symmetrical to the matrix diagonal. Re-arrange
     * them by checking their dependencies to each other.
     * Always compare packages in couples, try to swap their positions and check the 
     * resulting cycles (brute-force). Comparing and swapping is done in stages.
     * First compare packages that are next to each other ->when looking at the matrix 
     * we compare cell values that are next to each other on the two sides of the diagonal;
     * Then compare packages with offset 2 then 3 and so on.
     * 
     * <table border = "1">
     * <tr><th>a</th><td> x </td><td> ab </td><td> ac </td><td> ad </td></tr>
     * <tr><th>b</th><td> ba </td><td> x </td><td> bc </td><td> bd </td></tr>
     * <tr><th>c</th><td> ca </td><td> cb </td><td> x </td><td> cd </td></tr>
     * <tr><th>d</th><td> da </td><td> db </td><td> dc </td><td> x </td></tr>
     * </table>
     * In the example matrix we would first compare <b>ab</b> and <b>ba</b> and potentially swap 
     * <b>a</b> and <b>b</b> if <b>ab</b> > <b>ba</b>. If not swaped then we 
     * compare <b>bc</b> and <b>cb</b>, then <b>cd</b> and <b>dc</b> etc..
     * Then we do the same with step 2 - meaning we compare 
     * packages <b>a</b> and <b>c</b>, <b>b</b> and <b>d</b>.
     * .... 
     */
    private int diagonalReflectionSort() {

        rearrange(false);
        rearrange(true);
        return cycles;
    }

    private void rearrange(boolean optimistic) {

        for (int i = 1; i < packages.size() - 1; i++) {
            int distance = i;

            for (int j = 0; j < packages.size() - distance; j++) {

                Unit p1 = packages.get(j);
                Unit p2 = packages.get(j + distance);
                int d1 = p1.dependenciesTo(p2);
                int d2 = p2.dependenciesTo(p1);

                if (d1 >= d2) {
                    // try to swap elements
                    trySwap(j, j + distance, optimistic);
                }
            }
        }
    }

    private boolean trySwap(int i, int j, boolean optimistic) {
        List<Unit> temp = new ArrayList<>(packages);
        Collections.swap(temp, i, j);
        int tmp = DSM.cycles(temp);

        if (tmp < cycles) {
            cycles = tmp;
            packages = temp;

            if (collectVariations) {
                variations.clear();
                variations.add(temp);
            }
            return true;
        } else if (optimistic && tmp == cycles) {
            cycles = tmp;
            packages = temp;

            if (collectVariations) {
                variations.add(temp);
            }

            return true;
        }

        return false;
    }
}
