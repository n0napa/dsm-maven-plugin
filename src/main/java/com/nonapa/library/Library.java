package com.nonapa.library;

public class Library extends Unit {

    public Library(String name) {
        super(name, null);
    }

    @Override
    protected Unit newChild(String name) {
        return new Package(name, this);
    }

}
