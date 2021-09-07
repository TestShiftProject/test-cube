package org.testshift.testcube.amplify;

import com.intellij.codeInsight.TestFrameworks;
import com.intellij.execution.TestStateStorage;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.ClassUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testIntegration.TestFramework;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testshift.testcube.icons.TestCubeIcons;

import java.util.Objects;

public class AmplifyTestMarkerContributor extends RunLineMarkerContributor {


    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        Function<PsiElement, String> tooltipProvider = element1 -> {
            return "Amplify Test";
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
                /**
                 * From {@link com.intellij.testIntegration.TestRunLineMarkerProvider#getInfo(PsiElement)}
                 */
                PsiClass containingClass = PsiTreeUtil.getParentOfType(parent, PsiClass.class);
                if (!isTestMethod(containingClass, (PsiMethod)parent)) {
                    return null;
                }
                // test method
                String testMethodName = ((PsiMethod) parent).getName();
                String testClassName = Objects.requireNonNull(((PsiMethod) parent).getContainingClass()).getQualifiedName();
//                if (testClassName == null) {
//                    return null;
//                }
                VirtualFile file = parent.getContainingFile().getVirtualFile();
                if (file != null) {
                    VirtualFile moduleRoot = ProjectFileIndex.SERVICE.getInstance(parent.getProject())
                                                                     .getContentRootForFile(file);
                    String moduleRootPath;
                    if (moduleRoot == null) {
                        moduleRootPath = parent.getProject().getBasePath();
                    } else {
                        moduleRootPath = moduleRoot.getPath();
                    }
                    return new Info(TestCubeIcons.AMPLIFY_TEST, tooltipProvider, new StartTestCubeAction(
                            "Amplify '" + element.getText() + "()'", testClassName, testMethodName, moduleRootPath));
                }
            }
        }
        return null;
    }

    /**
     * From {@link com.intellij.testIntegration.TestRunLineMarkerProvider#getInfo(PsiElement)}
     */
    private static boolean isTestMethod(PsiClass containingClass, PsiMethod method) {
        if (containingClass == null) return false;
        TestFramework framework = TestFrameworks.detectFramework(containingClass);
        return framework != null && framework.isTestMethod(method, false);
    }
}
