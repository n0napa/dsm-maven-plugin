package com.nonapa.library;

public class Clazz extends Unit {

    public Clazz(String name, Package parent) {
        super(name, parent);
    }

    @Override
    protected Unit newChild(String name) {
        throw new UnsupportedOperationException();
    }

    public void addDependency(Clazz other) {

        super.addDependency(other); // add dependency to other class (at most 1)
        if (!getParent().equals(other.getParent())) {
            super.addDependency(other.getParent()); // add dependency to other package
            getParent().addDependency(other); // add dependency from current package to the other class
            getParent().addDependency(other.getParent()); // add dependency from current package to other's package
        }
    }
}
