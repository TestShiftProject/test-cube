package org.testshift.testcube.amplify;

import com.intellij.codeInsight.daemon.*;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testshift.testcube.icons.TestCubeIcons;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

public class TestThisMethodLineMarkerProvider extends LineMarkerProviderDescriptor {

    @Override
    public @Nullable("null means disabled") @GutterName String getName() {
        return "Generate test with Test Cube";
    }

    private static class TestThisMethodGutterHandler implements GutterIconNavigationHandler<PsiElement> {

        @Override
        public void navigate(MouseEvent e, PsiElement elt) {
            if (elt == null) {
                return;
            }
            new StartTestCubeAction().actionPerformed(elt.getProject(), elt);
        }
    }

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        Function<PsiElement, String> tooltipProvider = element1 -> {
            return "Generate Test for This Method";
        };

        PsiElement parent;
        if (element instanceof PsiIdentifier && (parent = element.getParent()) instanceof PsiMethod &&
           ((PsiMethod)parent).getNameIdentifier() == element && methodToStillTest((PsiMethod)parent) ){
            return new LineMarkerInfo<>(element, element.getTextRange(),
                    TestCubeIcons.AMPLIFY_TEST, tooltipProvider, new TestThisMethodGutterHandler(),
                    GutterIconRenderer.Alignment.CENTER, () -> "Icon: Test Cube Logo");
        }
        else {
            return null;
        }
    }

    public static boolean methodToStillTest(@NotNull PsiMethod method) {
        return !AmplifyTestMarkerContributor.isTestMethod(method.getContainingClass(), method);
        // TODO use coverage analysis to exclude fully covered methods?
    }
}
