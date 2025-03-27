package com.albsd.methodexecutionpaths.actions;

import com.albsd.methodexecutionpaths.toolwindow.CustomToolWindowFactory;
import com.albsd.methodexecutionpaths.util.MethodPath;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindExecutionPaths implements IntentionAction {

    @NotNull
    @Override
    public String getText() {
        return "Track method usages and execution paths";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Method tracker";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiFile file) {
        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        return PsiTreeUtil.getParentOfType(element, PsiMethod.class) != null;
    }

    private static final String ARROW_STR = " -> ";

    private static final String ERR_STR = "No execution paths found!";

    private static final String EXAMPLE_STR =
            """
            
            Maybe the format of the input is incorrect? Examples:
            
            foo
            foo(int, String)
            Example.foo
            int foo()
            int foo(int, String)
            Example.foo(int, String)
            Example.int foo(int, String)
            Example.int foo()
            """;

    public String log = "LOG:\n";

    private boolean OUTPUT_FULL_PATH = false;

    private boolean LOG_OUTPUT = false;

    private boolean MATCH_PARAMETERS = false;

    private boolean MATCH_RETURN_TYPE = false;

    public String targetMethodName = "";
    public String targetClassName = "";
    public String targetReturnType = "";
    public String[] targetParams = new String[0];


    // TODO: Error messages, tests, class-mode

    public void setOutputPathMode(boolean b) {
        OUTPUT_FULL_PATH = b;
    }

    public static Pair<String, Boolean> showDialogAndGetInput() {
        return Messages.showInputDialogWithCheckBox(
                "Enter the method name to track:",
                "Track Method Execution Paths",
                "Output extra path method information (class, signatures, return types)",
                true,
                true,
                Messages.getQuestionIcon(),
                "",
                null
        );
    }

    public boolean parseInput(String inputString) {
        String methodName = null;
        String className = null;
        String returnType = null;
        String[] paramTypes = new String[0];

        String[] classSplit = inputString.split("\\.");
        String methodPart = classSplit.length == 2 ? classSplit[1] : classSplit[0];

        if (classSplit.length == 2) {
            className = classSplit[0];
        }

        Pattern signaturePattern = Pattern.compile("(?:([\\w<>]+)\\s+)?([a-zA-Z_][a-zA-Z0-9_]*)\\((.*)\\)");
        Matcher matcher = signaturePattern.matcher(methodPart);

        if (matcher.matches()) {

            returnType = matcher.group(1);

            if (returnType != null && !returnType.isEmpty()) {
                this.MATCH_RETURN_TYPE = true;
            }

            methodName = matcher.group(2);
            String paramList = matcher.group(3);

            if (paramList.isEmpty()) {
                paramTypes = new String[0];
            } else {
                this.MATCH_PARAMETERS = true;
                paramTypes = paramList.split("\\s*,\\s*");
            }

        } else {

            String[] parts = methodPart.trim().split(" ");
            if (parts.length == 2) {
                this.MATCH_RETURN_TYPE = true;
                returnType = parts[0];
                methodName = parts[1];
            } else if (parts.length == 1) {
                methodName = parts[0];
            } else {
                return false;
            }
        }

        this.targetMethodName = methodName;
        this.targetClassName = className;
        this.targetReturnType = returnType;
        this.targetParams = paramTypes;

        return true;
    }

    private void cleanVariables() {
        this.targetMethodName = null;
        this.targetClassName = null;
        this.targetReturnType = null;
        this.targetParams = new String[0];
        this.log = "LOG:\n";
        this.OUTPUT_FULL_PATH = false;
        this.MATCH_PARAMETERS = false;
        this.MATCH_RETURN_TYPE = false;
        this.LOG_OUTPUT = false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiFile file) {
        cleanVariables();

        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());

        PsiMethod startingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (startingMethod == null) {
            return;
        }

        Pair<String, Boolean> result = showDialogAndGetInput();
        String inputString = result.getFirst();
        setOutputPathMode(result.getSecond());

        if (inputString == null || inputString.trim().isEmpty()) {
            return;
        }

        boolean parseSuccess = parseInput(inputString);

        if (!parseSuccess) {
            Messages.showErrorDialog("Invalid format!\n" + EXAMPLE_STR, "Error");
            return;
        }

        log += "Initial params: "
                + "target method name = '" + this.targetMethodName + "'; "
                + "target class name = '" + this.targetClassName + "'; "
                + "target method parameters = '" + Arrays.toString(this.targetParams) + "'; "
                + "target return type = '" + this.targetReturnType + "'\n\n";

        Set<String> paths;


        String startingMethodIdentifier = getIdentifier(startingMethod);

        try {
            MethodPath startingPath;
            if (OUTPUT_FULL_PATH) {
                startingPath = new MethodPath(startingMethodIdentifier);
            } else {
                startingPath = new MethodPath(startingMethod.getName());
                startingPath.methodSet = new HashSet<>();
                startingPath.methodSet.add(startingMethodIdentifier);
            }

            paths = findExecutionPaths(startingMethod, this.targetMethodName, this.targetClassName, startingPath, this.targetParams);
        } catch (Exception e) {
            log += e.getMessage() + "\n" + e.toString();
            CustomToolWindowFactory.showSimpleToolWindow(project, "ERROR:\n" + log);
            return;
        }

        String infoMessage = getInfoMessage(this.targetMethodName, startingMethodIdentifier);
        String pathsMessage = pathsToString(paths);

        if (pathsMessage.equals(ERR_STR)) {
            pathsMessage += "\n" + EXAMPLE_STR;
        }

        String fullMessage = infoMessage + pathsMessage;

        if (LOG_OUTPUT) {
            fullMessage += "\n\n " + log;
        } else {
            log = "";
        }

        CustomToolWindowFactory.showSimpleToolWindow(project, fullMessage);
    }

    private String getInfoMessage(String targetMethodName, String startingMethodName) {
        return "Usages of {" + targetMethodName + "} within execution paths of {" + startingMethodName + "}:\n";
    }

    public String pathsToString(Set<String> paths) {
        if (paths.size() == 0) {
            return ERR_STR;
        }

        StringBuilder pathsMessage = new StringBuilder();

        for (String path : paths) {
            pathsMessage.append(path).append("\n");
        }

        return pathsMessage.toString();

    }

    public Set<String> findExecutionPaths(PsiMethod startingMethod, String targetMethodName, String targetClassName, MethodPath currentPath, String[] params) {
        PsiCodeBlock methodBody = startingMethod.getBody();
        if (methodBody == null) return Set.of();

        Set<String> result = new HashSet<>();

        PsiTreeUtil.processElements(methodBody, element -> {
            if (element instanceof PsiMethodCallExpression methodCall) {
                PsiReferenceExpression methodExpression = methodCall.getMethodExpression();
                String methodName = methodExpression.getReferenceName();
                PsiMethod callee = (PsiMethod) methodExpression.resolve();

                if (callee != null && methodName != null) {
                    PsiClass calleeClass = callee.getContainingClass();;


                    // extra conditions
                    boolean classCondition = true;
                    classCondition = targetClassName == null || (calleeClass != null && calleeClass.getName() != null && calleeClass.getName().equals(targetClassName));

                    boolean signatureCondition = true;
                    PsiParameterList calleeParams = callee.getParameterList();
                    if (MATCH_PARAMETERS) {
                        signatureCondition = compareMethodParameters(calleeParams, params);
                    }


                    boolean returnCondition = true;
                    String calleeReturnType = callee.getReturnType() != null ? callee.getReturnType().getPresentableText() : "void";
                    if (MATCH_RETURN_TYPE) {
                        returnCondition = this.targetReturnType == null || this.targetReturnType.equals(calleeReturnType);
                    }

                    String methodIdentifier = getIdentifier(calleeClass, methodName, calleeParams, calleeReturnType);

                    /*
                        MethodPath holds a Set of methods we visited and the full "message". Having such a set and
                        copying it is necessary to address both cases of recursion / cycles and paths "intersecting"
                        in a specific method.

                        Refer to BlockDepth.java in src/test/testData for an actual example.
                     */
                    MethodPath newPath = new MethodPath(currentPath);
                    if (OUTPUT_FULL_PATH) {
                        newPath.pathString += ARROW_STR + methodIdentifier;
                    } else {
                        newPath.pathString += ARROW_STR + methodName;
                    }


                    // Keep track of visited through methodIdentifier (signature, name, class)
                    if (methodName.equals(targetMethodName) && classCondition && signatureCondition && returnCondition ) {
                        result.add(newPath.pathString);
                    } else if (newPath.methodSet.add(methodIdentifier)) {
                        log += "METHODNAME: " + methodName + " TARGETNAME: " + targetMethodName + " signature test: "+ signatureCondition + " return test: " + returnCondition + " class test: " + classCondition+  " method identifier: " + methodIdentifier + "\n";
                        Set<String> temp = findExecutionPaths(callee, targetMethodName, targetClassName, newPath, params);
                        result.addAll(temp);
                    }
                }
            }
            return true;
        });

        return result;
    }

    public String getIdentifier(PsiMethod method) {
        PsiType startingReturnType = method.getReturnType();
        String startingReturnTypeText = startingReturnType == null ? null : startingReturnType.getPresentableText();
        return getIdentifier(method.getContainingClass(), method.getName(), method.getParameterList(), startingReturnTypeText);
    }

    public String getIdentifier(PsiClass calleeClass, String methodName, PsiParameterList calleeParams, String calleeReturnType) {
        StringBuilder sb = new StringBuilder();
        if (calleeClass != null) {
            sb.append(calleeClass.getName());
            sb.append(".");
        }

        if (calleeReturnType != null) {
            sb.append(calleeReturnType);
            sb.append(" ");
        } else {
            sb.append("void");
            sb.append(" ");
        }

        sb.append(methodName);

        sb.append("(");

        if (calleeParams != null) {
            PsiParameter[] psiParams = calleeParams.getParameters();
            for (int i = 0; i < psiParams.length; i++) {
                sb.append(psiParams[i].getType().getPresentableText());

                if (i < psiParams.length - 1) {
                    sb.append(", ");
                }
            }
        }

        sb.append(")");
        return sb.toString();
    }

    private boolean compareMethodParameters(PsiParameterList calleeParams, String[] expectedParamTypes) {
        if (calleeParams.isEmpty() && (expectedParamTypes == null || expectedParamTypes.length == 0)) {
            return true;
        }
        PsiParameter[] parameters = calleeParams.getParameters();

        //log += Arrays.toString(parameters) + " ";

        if (parameters.length != expectedParamTypes.length) {
            return false;
        }

        for (int i = 0; i < parameters.length; i++) {
            String actualType = parameters[i].getType().getPresentableText();
            //log += "actual type: " + actualType + " ";
            if (!actualType.equals(expectedParamTypes[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}