package com.albsd.methodexecutionpaths.actions;

import com.albsd.methodexecutionpaths.util.MethodPath;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class FindExecutionPathsTest extends LightJavaCodeInsightFixtureTestCase {

    private FindExecutionPaths plugin;

    private static final String PATH_MISMATCH_ERR = "Incorrect resulting paths!";

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/";
    }



    @Override
    protected void setUp() throws Exception {
        super.setUp();
        plugin = new FindExecutionPaths();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } catch (Throwable e) {
            addSuppressedException(e);
        }
    }


    @Test
    public void depthTest() throws Exception {
        boolean showClasses = false;
        String fileName = "BlockDepth.java";
        String expectedOutput = """
                start -> a -> target
                start -> a -> b_2 -> c -> target
                start -> a -> b_1 -> c -> target
                start -> lambda -> d -> target
                start -> for_loop -> target
                start -> lambda -> c -> target
                start -> a -> truef -> target
                start -> a -> b_1 -> d -> target
                start -> a -> b_2 -> d -> target
                """;

        testFindExecutionPaths(fileName, "start", "target", expectedOutput, showClasses);
    }

    @Test
    public void testMultipleClasses() throws Exception {
        boolean showClasses = true;
        String fileName = "MultipleClasses.java";
        String expectedOutput = "ExampleA.void a() -> ExampleB.void a() -> ExampleC.void a() -> ExampleC.void target()\n";

        testFindExecutionPaths(fileName, "a", "target", expectedOutput, showClasses);
    }


    // For this and the other recursive tests in general it is important to NOT get repetitions
    @Test
    public void testCyclicRecursive() throws Exception {
        boolean showClasses = false;
        String fileName = "CyclicRecursive.java";

        /*
            For instance, a different execution path resulting from the program would be:
            recursiveA -> recursiveA -> recursiveB
            This should NOT be shown as it's already included within recursiveA -> recursiveB

            On the other hand (as long as it is its own target),
            recursiveA -> recursiveA
            is fine, as it shows that the function is recursive
        */
        String expectedOutputOne = "recursiveA -> recursiveB\n";
        testFindExecutionPaths(fileName, "recursiveA", "recursiveB", expectedOutputOne, showClasses);

        String expectedOutputTwo = "recursiveB -> recursiveA -> recursiveB\n";
        testFindExecutionPaths(fileName, "recursiveB", "recursiveB", expectedOutputTwo, showClasses);

        String expectedOutputThree = "base -> recursiveA\n" +
                "base -> recursiveB -> recursiveA\n";
        testFindExecutionPaths(fileName, "base", "recursiveA", expectedOutputThree, showClasses);
    }

    @Test
    public void testNoClass() throws Exception {
        boolean showClasses = false;
        String fileName = "NoClass.java";
        String expectedOutput = "c -> a\nc -> b -> a\n";

        testFindExecutionPaths(fileName, "c", "a", expectedOutput, showClasses);
    }

    // This one specifically should NOT result in a buffer overflow
    @Test
    public void testRecursiveNoAnswer() throws Exception {
        boolean showClasses = false;
        String fileName = "CyclicRecursive.java";

        String expectedOutputOne = "No execution paths found!";
        testFindExecutionPaths(fileName, "recursiveA", "UNREACHABLE", expectedOutputOne, showClasses);
    }

    @Test
    public void testSimpleRecursive() throws Exception {
        boolean showClasses = false;
        String fileName = "SimpleRecursive.java";
        String expectedOutputOne = "recursive -> recursive\n";

        testFindExecutionPaths("SimpleRecursive.java", "recursive", "recursive", expectedOutputOne, showClasses);

        String expectedOutputTwo = "firstBase -> recursive\nfirstBase -> secondBase -> recursive\n";

        testFindExecutionPaths(fileName, "firstBase", "recursive", expectedOutputTwo, showClasses);
    }

    @Test
    public void testSimpleExample() throws Exception {
        boolean showClasses = false;
        String expectedOutput = "foo -> bar -> baz -> interestingMethod\n";

        testFindExecutionPaths("SimpleExample.java", "foo", "interestingMethod", expectedOutput, showClasses);
    }

    @Test
    public void testFindExecutionPaths(String fileName, String startingMethodName, String targetMethodName, String expectedOutput, boolean showClasses) throws Exception {
        String filePath = getTestDataPath() + fileName;

        String testCode = new String(Files.readAllBytes(Paths.get(filePath)));

        VirtualFile virtualFile = myFixture.findFileInTempDir(fileName);
        PsiFile file;

        if (virtualFile != null) {
            file = PsiManager.getInstance(getProject()).findFile(virtualFile);
        } else {
            file = myFixture.addFileToProject(fileName, testCode);
        }

        myFixture.configureByFile(fileName);

        PsiMethod startingMethod = findMethodByName(file, startingMethodName);

        MethodPath startingPath;

        assertNotNull(startingMethod);

        /*
        Due to some time constraints, part of the logic of the plugin is emulated within the setup for the test.
        Normally (inevitably in the future) more fine-grained plugin logic would be extracted in methods which
        could be called directly here, which should also allow for more "robust" tests down the line.
         */
        if (showClasses) {
            startingPath = new MethodPath(plugin.getIdentifier(startingMethod));
        } else {
            startingPath = new MethodPath(startingMethodName);
            startingPath.methodSet = new HashSet<>();
            startingPath.methodSet.add(plugin.getIdentifier(startingMethod));
        }

        plugin.setOutputPathMode(showClasses);
        Set<String> paths = plugin.findExecutionPaths(startingMethod, targetMethodName, null, startingPath, null);

        //System.out.println(findExecutionPaths.pathsToString(paths));

        assertNotNull(paths);
        assertEquals(PATH_MISMATCH_ERR, expectedOutput, plugin.pathsToString(paths));
    }

    private PsiMethod findMethodByName(PsiFile file, String methodName) {
        PsiClass[] classes = ((PsiJavaFile) file).getClasses();
        for (PsiClass psiClass : classes) {
            for (PsiMethod method : psiClass.getMethods()) {
                if (method.getName().equals(methodName)) {
                    return method;
                }
            }
        }
        return null;
    }
}
