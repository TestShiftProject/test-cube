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
            new StartTestCubeAction().actionPerformed(elt.getProject());
        }
    }

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        Function<PsiElement, String> tooltipProvider = element1 -> {
            return "Generate Test";
        };

        PsiElement parent;
        if (element instanceof PsiIdentifier && (parent = element.getParent()) instanceof PsiMethod &&
           ((PsiMethod)parent).getNameIdentifier() == element) {
            return new LineMarkerInfo<>(element, element
                    .getTextRange(), TestCubeIcons.AMPLIFY_TEST, tooltipProvider, null,
                    GutterIconRenderer.Alignment.CENTER, () -> "Icon: Test Cube Logo");
        }
        else {
            return null;
        }
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements, @NotNull Collection<?
            super LineMarkerInfo<?>> result) {
        super.collectSlowLineMarkers(elements, result);
    }
}
