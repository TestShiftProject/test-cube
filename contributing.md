# Test Cube Development Setup
Here we describe the development setup for Test Cube.

1. Install IntelliJ together with the `gradle-intellij-plugin` plugin. (https://jetbrains.org/intellij/sdk/docs/tutorials/build_system.html)
2. Clone this repository
3. Open the root in IntelliJ! 😄

To test and run Test Cube / DSpot you need:
- an installation of Java 8 (including the path to your Java Home)
- an installation of Maven (including the path to your Maven Home)
- an example project based on Java 8 and Maven

## Dependency Setup
Test Cube is dependent on snapshot-versions of [DSpot](https://github.com/lacinoire/dspot) and [TestRunner](https://github.com/lacinoire/test-runner).
Test Cube comes with a complete jar of DSpot, checked into the repository. If you want to make changes in DSpot, follow these instructions:

### Test Runner
1. Clone the (forked!) test-runner repository
2. Go to the repository root and build the maven package:
```mvn package```
3. Install the package in your local maven repository:
```mvn install:install-file -Dfile=target/test-runner-2.1.2-SNAPSHOT-jar-with-dependencies.jar -DpomFile=pom.xml -Dclassifier=jar-with-dependencies```

### DSpot
1. Check out the (forked!) DSpot repository
2. Go to the root and checkout the `test-cube` branch
3. Build the maven package:
```mvn package```
4. Copy the jar over to your Test Cube repository:
```mv <link-to-your-dspot-repo-root>/dspot/target/dspot-3.1.1-SNAPSHOT-jar-with-dependencies.jar <link-to-your-testcube-repo-root>/src/main/resources/dspot/dspot-3.1.1-SNAPSHOT-jar-with-dependencies.jar```
