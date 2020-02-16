package com.nonapa.library;

public class Package extends Unit {

    public Package(String name, Library library) {
        super(name, library);
    }

    @Override
    protected Unit newChild(String name) {
        return new Clazz(name, this);
    }
}
