package com.darksheep.sheepnote.editor.gutter;

import com.darksheep.sheepnote.data.NoteData;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public  class NoteGutterIconLineMarkerProvider implements LineMarkerProvider {

    private  int lineNumber;
    private  String noteTitle;

    public NoteGutterIconLineMarkerProvider() {
    }

    public NoteGutterIconLineMarkerProvider(NoteData noteData) {
        this.lineNumber = noteData.noteLineNumber;
        this.noteTitle = noteData.noteTitle;
    }

    public NoteGutterIconLineMarkerProvider(int lineNumber, String noteTitle) {
        this.lineNumber = lineNumber;
        this.noteTitle = noteTitle;
    }

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PsiFile) {
            int elementLineNumber = getLineNumber(element);
            if (elementLineNumber == this.lineNumber) {
                Icon icon = new Icon() {
                    @Override
                    public void paintIcon(Component c, Graphics g, int x, int y) {
                        g.setColor(JBColor.BLUE);
                        g.fillOval(x, y, getIconWidth(), getIconHeight());
                    }

                    @Override
                    public int getIconWidth() {
                        return 12;
                    }

                    @Override
                    public int getIconHeight() {
                        return 12;
                    }
                };

                TextRange range = new TextRange(element.getTextOffset(), element.getTextOffset() + 1);
                Function<PsiElement, String> tooltipProvider = (__1) -> noteTitle;
                GutterIconNavigationHandler<PsiElement> navHandler = (__1, __2) -> {};
                GutterIconRenderer.Alignment alignment = GutterIconRenderer.Alignment.RIGHT;
                Supplier<String> accessibleNameProvider = () -> noteTitle;
                LineMarkerInfo<?> lineMarkerInfo = new LineMarkerInfo<PsiElement>((PsiElement) element, range, icon,
                        (com.intellij.util.Function<? super PsiElement, String>) tooltipProvider, navHandler, alignment, accessibleNameProvider);
                return lineMarkerInfo;
            }
        }
        return null;
    }

    private int getLineNumber(PsiElement element) {
        Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(element.getContainingFile());
        if (document != null) {
            return document.getLineNumber(element.getTextOffset()) + 1;
        }
        return -1;
    }
}
