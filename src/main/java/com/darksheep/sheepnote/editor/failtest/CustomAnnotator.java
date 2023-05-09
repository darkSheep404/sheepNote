package com.darksheep.sheepnote.editor.failtest;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * 可以初始化 并调用方法打印
 * 但是未画出图案和文本
 */
public class CustomAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        System.out.println("enter CustomAnnotator 12345");
        if (element.getContainingFile() == null || !element.getContainingFile().isValid()) {
            return;
        }

        final PsiFile psiFile = element.getContainingFile();
        final Document document = FileDocumentManager.getInstance().getDocument(psiFile.getVirtualFile());

        if (document == null) {
            return;
        }

        final int targetLineNumber = 19; // 20 - 1 since line numbers are zero-indexed
        final int totalLines = document.getLineCount();

        if (targetLineNumber >= totalLines) {
            return;
        }

        int lineStartOffset = document.getLineStartOffset(targetLineNumber);
        int lineEndOffset = document.getLineEndOffset(targetLineNumber);
        TextRange range = new TextRange(lineStartOffset, lineEndOffset);

        /**
         * com.intellij.util.IncorrectOperationException: 'AnnotationHolder.createInfoAnnotation()' method (the call to which was found in class com.darksheep.sheepnote.editor.failtest.CustomAnnotator) is slow, non-incremental and thus can cause unexpected behaviour (e.g. annoying blinking),
         * is deprecated and will be removed soon. Please use `newAnnotation().create()` instead
         */
        if (range.contains(element.getTextRange())) {
            Annotation annotation = holder.createInfoAnnotation(range, "//HelloWorld");
            annotation.setNeedsUpdateOnTyping(true);
            annotation.setAfterEndOfLine(true);
        }
    }
}
