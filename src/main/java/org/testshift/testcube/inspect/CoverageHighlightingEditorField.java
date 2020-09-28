package org.testshift.testcube.inspect;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.ui.LanguageTextField;

public class CoverageHighlightingEditorField extends LanguageTextField {

    public CoverageHighlightingEditorField() {
        super(JavaLanguage.INSTANCE, null, "placeholder value");
        setOneLineMode(false);
        setAutoscrolls(true);
    }

    @Override
    protected EditorEx createEditor() {
        EditorEx editor = super.createEditor();
        editor.setVerticalScrollbarVisible(true);
        editor.setHorizontalScrollbarVisible(true);
        editor.setColorsScheme(EditorColorsManager.getInstance().getGlobalScheme());
        editor.setCaretEnabled(true);
        editor.setCaretVisible(true);
        editor.getScrollPane().setBackground(editor.getBackgroundColor());

        EditorSettings settings = editor.getSettings();
        settings.setLineNumbersShown(true);
        settings.setAdditionalPageAtBottom(true);
        settings.setLineMarkerAreaShown(true);

        return editor;
    }
}
