# Test Cube Development Setup
Here we describe the development setup for Test Cube.

If you will work on the test amplification itself, you need to setup `test-runner` and `DSpot`, too 🙂

## Test Cube

1. Install IntelliJ together with the `gradle-intellij-plugin` plugin. (https://jetbrains.org/intellij/sdk/docs/tutorials/build_system.html)
2. Clone this repository
3. Open the root in IntelliJ! 😄
After opening the project in IntelliJ, you can execute Test Cube with the gradle task `runIde`.

To test and run Test Cube / DSpot you need:
- an installation of Java 8 (including the path to your Java Home)
- an installation of Maven (including the path to your Maven Home)
- an example project based on Java 8 and Maven

## Example Project

You’ll need an example project to try out DSpot/Test Cube on 🙂

Currently, DSpot supports projects that use Maven and Java 8, potentially Java 11 (They say, I didn’t check)

One project that Test Cube works well on is jsoup: [https://github.com/jhy/jsoup](https://github.com/jhy/jsoup)

Fell free to try it on any other projects you’re interested in! you can find a few more that DSpot should run on here → [https://github.com/STAMP-project/dspot-experiments/blob/master/dataset/dataset.json](https://github.com/STAMP-project/dspot-experiments/blob/master/dataset/dataset.json)

## test-runner
Our version of DSpot relies on a custom version of the test-runner. To build DSpot you’ll need to setup test-runner as follows:

Clone from: [https://github.com/STAMP-project/test-runner](https://github.com/STAMP-project/test-runner)

Build with

```
mvn package
```

Install with

```
mvn install:install-file -Dfile=target/test-runner-<version>-jar-with-dependencies.jar -DpomFile=pom.xml -Dclassifier=jar-with-dependencies
```

```
mvn install:install-file -Dfile=target/test-runner-2.3.0-SNAPSHOT-jar-with-dependencies.jar -DpomFile=pom.xml -Dclassifier=jar-with-dependencies
```

## DSpot

Clone from: [https://github.com/TestShiftProject/dspot](https://github.com/TestShiftProject/dspot/tree/test-cube)

Checkout the branch `test-cube`

Check in pom.xml that the test-runner is set to the same version you installed above

Build with

```
mvn package
```

If you get this error:

`Execution default-test of goal org.apache.maven.plugins:maven-surefire-plugin:2.12.4:test failed: The forked VM terminated without saying properly goodbye. VM crash or System.exit called ?`

solve it with an `mvn clean` and another `mvn package`

----

To include a new version within Test Cube, locate the built jar with dependencies in the `target` folder  and replace the jar in `test-cube/src/main/resources/dspot`.
