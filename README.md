![CI](https://github.com/albsd/method-execution-paths/actions/workflows/ci.yml/badge.svg) 

# Method Execution Paths

## Overview

IntelliJ IDEA plugin that tracks method execution paths.

Allows the user to invoke an intention in a starting method, then to choose a target method (optionally with its signature and/or containing class). Prints all such execution paths from the starting method to the target. Allows to optionally display extra information within the path pertaining to the signatures and classes of the underlying methods. 

## Requirements 

IntelliJ IDEA 2022.1.4+


## Installation

You can download the plugin from the [Releases](https://github.com/albsd/method-execution-paths/releases) section.
Alternatively, you can follow these instructions:

1. Clone this repository:
    ```sh
    git clone https://github.com/albsd/method-execution-paths.git
    ```

2. Open the project in **IntelliJ IDEA**.

3. Build and install the plugin locally using Gradle:
    - Go to **Gradle > method-execution-paths > Tasks > intellij > buildPlugin** 
    - Or run ```./gradlew build```.
5. Take the generated .zip file from ```build/distributions```.


Finally, load the plugin into IntelliJ via **File > Settings > Plugins > Install Plugin from Disk**.

## Usage

1. Open a Java file in IntelliJ IDEA.
2. If the Method Execution Paths window isn't present in the bottom part of the editor, go to View -> Tool Windows -> Method Execution Paths
3. Place the cursor inside a method you want to analyze.
4. Open the **Intentions / Show Context Actions** menu (`Alt + Enter` or `⌥ + Enter` on macOS).
5. Select **Track method execution paths**.
6. Enter the method signature in the prompt (e.g., `Example.foo(int, String)`).
7. (Optional) Uncheck the "Output extra method information" box for a "simpler" output.
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
- If the window does not show upon running the plugin, go to View -> Tool Windows -> Method Execution Paths.
- In the case of overloaded methods, it is highly recommended to search using the method signature as well. 
- If "Output extra method information" is disabled and multiple methods with the same name are present, those are not going to be distinguishable. This is due to the format producing identical entries which are not going to be shown.
- (LINUX) If confronted with the error ```java.lang.IllegalStateException: Current thread: Thread[AWT-EventQueue-1,6,main]; expected: Thread[AWT-EventQueue-0,6,]``` when trying to build the plugin, simply retry the buildPlugin task. If still unsuccesful, try **build -> clean** and **intellij -> buildPlugin**. In case the issue persists, modify the version of IntelliJ in ```build.gradle.kts``` to "2024.3.5" and make sure IntelliJ is updated. 

## Contact
Feel free to contact me at albertsandu1@gmail.com or A.A.Sandu@student.tudelft.nl


