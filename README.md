![CI](https://github.com/albsd/method-execution-paths/actions/workflows/ci.yml/badge.svg) 

# Method Execution Paths

## Overview

IntelliJ IDEA plugin that tracks method execution paths.

Allows the user to invoke an intention in a starting method, then to choose a target method (optionally with its signature and/or containing class). Prints all such execution paths from the starting method to the target. Allows to optionally display extra information within the path pertaining to the signatures and classes of the underlying methods. 


## Installation

1. Clone this repository:
    ```sh
    git clone https://github.com/albsd/method-execution-paths.git
    ```

2. Open the project in **IntelliJ IDEA**.

3. Build and install the plugin locally using Gradle:
    ```sh
    ./gradlew build
    ```

4. Load the plugin into IntelliJ via **File > Settings > Plugins > Install Plugin from Disk**.

## Usage

1. Open a Java file in IntelliJ IDEA.
2. Place the cursor inside a method you want to analyze.
3. Open the **Intentions Menu** (`Alt + Enter` or `⌥ + Enter` on macOS).
4. Select **Track method usages and execution paths**.
5. Enter the method signature in the prompt (e.g., `Example.foo(int, String)`).
6. (Optional) Uncheck the "Output extra method information" box for a "simpler" output.
6. View execution paths in the **Method Execution Paths** window.

## Input Format Examples

Valid method input formats include:

- `foo`
- `foo(int, String)`
- `Example.foo`
- `int foo()`
- `int foo(int, String)`
- `Example.foo(int, String)`
- `Example.int foo(int, String)`
- `Example.int foo()`

## Potential issues
- If the window does not show up upon running the plugin, then go to View -> Tool Windows -> Method Execution Paths.

