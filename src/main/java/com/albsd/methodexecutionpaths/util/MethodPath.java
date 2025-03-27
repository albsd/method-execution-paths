package com.albsd.methodexecutionpaths.util;

import java.util.HashSet;
import java.util.Set;

public class MethodPath {
    public String pathString;
    public Set<String> methodSet;

    public MethodPath(String pathString) {
        this.methodSet = new HashSet<>();
        if (pathString != null) {
            this.pathString = pathString;
            this.methodSet.add(pathString);
        }

    }

    public MethodPath(MethodPath other) {
        this.pathString = other.pathString;
        this.methodSet = new HashSet<>();
        this.methodSet.addAll(other.methodSet);
    }
}
