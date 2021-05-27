# Welcome to the TestImpactGraph extension of Test Cube!
You can follow these instructions to create more `.json` files for the [TestImpactGraph](https://github.com/TestShiftProject/test-impact-graph).

First, follow the development setup instructions in the [contributing guide](contributing.md).
It is sufficient to just setup TestCube, without the dependencies.
Unfortunately, TestCube runs only on projects that support Java 8 and Maven.
Two example projects you could use are [jsoup](https://github.com/jhy/jsoup) or [javapoet](https://github.com/square/javapoet).

After successfully amplifying a test case you picked (sometimes TestCube can't find any test cases, just try a different base test case then! 😊), select "Inspect Amplification Result".
In the opened tool window, click the button "Explore Test Case!".

Now you'll be presented with a new white tool window 😉
The TestImpactGraph is not fully integrated into TestCube yet, so you will have to manually copy over the `.json` containing all the relevant data.
You can find it in the `target` folder within the `test-cube` project.
Check out the [instructions for the TestImpactGraph](https://github.com/TestShiftProject/test-impact-graph#generating-and-inspecting-an-amplified-test-case-from-testcube) to learn where to copy the `.json` to! 😊
