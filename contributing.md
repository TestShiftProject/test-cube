# Test Cube Development Setup
Here we describe the development setup for Test Cube.

If you will work on the test amplification itself, you need to setup `test-runner` and `DSpot`, too 🙂

## Test Cube

1. Install IntelliJ together with the `gradle-intellij-plugin` plugin. (https://jetbrains.org/intellij/sdk/docs/tutorials/build_system.html)
2. Clone this repository
3. Open the root in IntelliJ! 😄
After opening the project in IntelliJ, you can execute Test Cube with the gradle task `runIde`.

To test and run Test Cube / DSpot you need:
- an installation of Java 11 (including the path to your Java Home)
- an installation of Maven (including the path to your Maven Home)
- an example project based on Java 11 and Maven

## Example Project

You’ll need an example project to try out DSpot/Test Cube on 🙂

Currently, DSpot supports projects that use Maven and Java 8 and Java 11 (with caveats also Java 16)

One project that Test Cube works well on is jsoup: [https://github.com/jhy/jsoup](https://github.com/jhy/jsoup)

Fell free to try it on any other projects you’re interested in! you can find a few more that DSpot should run on here → [https://github.com/STAMP-project/dspot-experiments/blob/master/dataset/dataset.json](https://github.com/STAMP-project/dspot-experiments/blob/master/dataset/dataset.json)

## DSpot
If you will work on the amplification process itself, you will likely do this by modifying DSpot, the test amplification tool TestCube relies on.
TestCube contains a packaged jar of DSpot, so if you do changes to DSpot: repackage it and copy it over to the test-cube repository :)

Clone from: [https://github.com/TestShiftProject/dspot](https://github.com/TestShiftProject/dspot/tree/test-cube)

Checkout the branch `master`

Build with

```
mvn package
```

If you get this error:

`Execution default-test of goal org.apache.maven.plugins:maven-surefire-plugin:2.12.4:test failed: The forked VM terminated without saying properly goodbye. VM crash or System.exit called ?`

solve it with an `mvn clean` and another `mvn package`

To include a new version of DSpot within Test Cube, locate the built jar with dependencies in the `target` folder  and replace the jar in `test-cube/src/main/resources/dspot`.
