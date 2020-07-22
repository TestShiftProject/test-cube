package org.testshift.testcube.amplify;

import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.testIntegration.TestFailedLineManager;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testshift.testcube.icons.TestCubeIcons;

import java.util.Objects;

public class AmplifyTestMarkerContributor extends RunLineMarkerContributor {


    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        Function<PsiElement, String> tooltipProvider = element1 -> {
            return "Amplify unit test " + element1.getText();
        };
        if (element instanceof PsiIdentifier) {
            PsiElement parent = element.getParent();
//            if (parent instanceof PsiClass) {
//                TestFramework framework = TestFrameworks.detectFramework((PsiClass)parent);
//                if (framework != null && framework.isTestClass(parent)) {
//                    // test class
//                    return new Info(AllIcons.Actions.Colors, tooltipProvider, new StartTestCubeAction("Amplify test case"));
//                }
//            }
            if (parent instanceof PsiMethod) {
                TestFailedLineManager.TestInfo testInfo = TestFailedLineManager.getInstance(parent.getProject()).getTestInfo((PsiMethod)parent);
                // test method
                String testMethodName = ((PsiMethod) parent).getName();
                String testClassName = Objects.requireNonNull(((PsiMethod) parent).getContainingClass()).getQualifiedName();
//                if (testClassName == null) {
//                    return null;
//                }
                return testInfo == null ? null : new Info(TestCubeIcons.AMPLIFY_TEST, tooltipProvider, new StartTestCubeAction("Amplify '" + element.getText() + "'", testClassName, testMethodName));
            }
        }
        return null;
    }

}
