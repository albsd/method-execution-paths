package com.albsd.methodexecutionpaths.actions;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FindExecutionPathsParserTest {

    private FindExecutionPaths plugin = new FindExecutionPaths();

    @Test
    public void parserTestMethodNameOnly() {
        plugin.parseInput("foo");
        assertNull(plugin.targetClassName);
        assertEquals("foo", plugin.targetMethodName);
        assertNull(plugin.targetReturnType);
        assertEquals(0, plugin.targetParams.length);
    }

    @Test
    public void parserTestMethodWithParameters() {
        plugin.parseInput("foo(int, String)");
        assertNull(plugin.targetClassName);
        assertEquals("foo", plugin.targetMethodName);
        assertNull(plugin.targetReturnType);
        assertArrayEquals(new String[]{"int", "String"}, plugin.targetParams);
    }

    @Test
    public void parserTestClassAndMethodName() {
        plugin.parseInput("Example.foo");
        assertEquals("Example", plugin.targetClassName);
        assertEquals("foo", plugin.targetMethodName);
        assertNull(plugin.targetReturnType);
        assertEquals(0, plugin.targetParams.length);
    }

    @Test
    public void parserTestClassMethodAndParameters() {
        plugin.parseInput("Example.foo(int, String)");
        assertEquals("Example", plugin.targetClassName);
        assertEquals("foo", plugin.targetMethodName);
        assertNull(plugin.targetReturnType);
        assertArrayEquals(new String[]{"int", "String"}, plugin.targetParams);
    }

    @Test
    public void parserTestReturnTypeAndMethodName() {
        plugin.parseInput("int foo(int, String)");
        assertNull(plugin.targetClassName);
        assertEquals("foo", plugin.targetMethodName);
        assertEquals("int", plugin.targetReturnType);
        assertArrayEquals(new String[]{"int", "String"}, plugin.targetParams);
    }

    @Test
    public void parserTestClassReturnTypeMethodAndParams() {
        plugin.parseInput("Example.int foo(int, String)");
        assertEquals("Example", plugin.targetClassName);
        assertEquals("foo", plugin.targetMethodName);
        assertEquals("int", plugin.targetReturnType);
        assertArrayEquals(new String[]{"int", "String"}, plugin.targetParams);
    }

    @Test
    public void parserTestNoParameters() throws Exception {
        plugin.parseInput("Example.void foo()");
        assertEquals("Example", plugin.targetClassName);
        assertEquals("foo", plugin.targetMethodName);
        assertEquals("void", plugin.targetReturnType);
        assertEquals(0, plugin.targetParams.length);
    }

    @Test
    public void parserTestNoClassNoParameters() throws Exception {
        plugin.parseInput("void foo()");
        assertNull(plugin.targetClassName);
        assertEquals("foo", plugin.targetMethodName);
        assertEquals("void", plugin.targetReturnType);
        assertEquals(0, plugin.targetParams.length);
    }
}
