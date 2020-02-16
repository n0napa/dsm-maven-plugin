package com.nonapa.library;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public abstract class Unit {

    private String name;
    private Unit parent;
    private Set<Unit> children;
    private Map<Unit, Integer> dependencies;

    public Unit(String name, Unit parent) {
        this.name = name;
        this.parent = parent;
        this.children = new HashSet<>();
        this.dependencies = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Unit getParent() {
        return parent;
    }

    public Set<Unit> getChildren() {
        return children;
    }

    public Unit addChild(String name) {
        for (Unit c : getChildren()) {
            if (name.equals(c.getName())) {
                return c;
            }
        }
        Unit child = newChild(name);
        getChildren().add(child);
        return child;
    }

    protected abstract Unit newChild(String name);

    void addDependency(Unit other) {
        if (this.equals(other))
            return;
        dependencies.merge(other, 1, Integer::sum);
    }

    Stream<Unit> siblings() {
        if (getParent() == null)
            return Stream.empty();

        return getParent().getChildren().stream().filter(c -> c != this);
    }

    /**
     * Calculates the overall dependency count of a unit to all the other units in
     * the same parent.
     *
     * @param p
     * @return
     */
    public int dependencyCount() {
        return siblings().map(s -> dependenciesTo(s)).reduce(0, Integer::sum);
    }

    /**
     * Returns the number of sibling units that the this one depends on.
     *
     * @param p
     * @return
     */
    public int siblingDependenciesCount() {
        return (int) siblings().filter(s -> dependsOn(s)).count();
    }

    //TODO remove this
    public Set<Unit> getDependencies() {
        return dependencies.keySet();
    }

    public int dependenciesTo(Unit other) {
        if (this.equals(other))
            return 0;

        return Optional.ofNullable(dependencies.get(other)).orElse(0);
    }

    boolean dependsOn(Unit other) {
        return dependenciesTo(other) != 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        Unit other = ((Unit) obj);
        return Objects.equals(getName(), other.getName()) && Objects.equals(getParent(), other.getParent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getParent());
    }

    @Override
    public String toString() {
        return getName();
    }
}
