package com.darksheep.sheepnote.editor.gutter;

import com.darksheep.sheepnote.data.NoteData;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Supplier;

public class NoteLineMarkerInfo extends LineMarkerInfo {
    private NoteData noteData;

    public NoteData getNoteData() {
        return noteData;
    }

    public void setNoteData(NoteData noteData) {
        this.noteData = noteData;
    }

    public NoteLineMarkerInfo(@NotNull PsiElement element, @NotNull TextRange range, @NotNull Icon icon, @Nullable Function tooltipProvider, @Nullable GutterIconNavigationHandler navHandler, GutterIconRenderer.@NotNull Alignment alignment, @NotNull Supplier accessibleNameProvider) {
        super(element, range, icon, tooltipProvider, navHandler, alignment, accessibleNameProvider);
    }

    public NoteLineMarkerInfo(@NotNull PsiElement element, @NotNull TextRange range) {
        super(element, range);
    }

    public NoteLineMarkerInfo(@NotNull SmartPsiElementPointer elementRef, @NotNull TextRange range, @Nullable Icon icon, @Nullable Supplier accessibleNameProvider, @Nullable Function tooltipProvider, @Nullable GutterIconNavigationHandler navHandler, GutterIconRenderer.@NotNull Alignment alignment) {
        super(elementRef, range, icon, accessibleNameProvider, tooltipProvider, navHandler, alignment);
    }
}
