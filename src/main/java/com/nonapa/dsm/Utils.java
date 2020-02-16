package com.nonapa.dsm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Utils {

    public static final int THRESHOLD = 9;

    private Utils() {
    }

    public static long factorial(int n) {
        if (n < 0 || n > THRESHOLD)
            throw new IllegalArgumentException(n + " is out of range");
        return (n == 0) ? 1 : n * factorial(n - 1);
    }

    public static <T> List<T> permutation(long no, List<T> items) {
        return permutation(no, new LinkedList<>(Objects.requireNonNull(items)), new ArrayList<>());
    }

    private static <T> List<T> permutation(long no, LinkedList<T> in, List<T> out) {
        if (in.isEmpty())
            return out;
        long subFactorial = factorial(in.size() - 1);
        out.add(in.remove((int) (no / subFactorial)));
        return permutation((int) (no % subFactorial), in, out);
    }
}
