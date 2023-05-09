package com.darksheep.sheepnote.editor.failtest;

import com.darksheep.sheepnote.icon.PluginIcons;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * 未显示图片和文本
 * Gutter Icon：我们可以通过创建一个 Gutter Icon（边缘图标）表达自定义文本。虽然这种方法不能实际在行尾插入文本，
 * 但它可以在编辑器的 gutter 栏中显示自定义文本提示。
 * 这是一个简单的 GutterIcon实现示例
 */
public class CustomGutterIconProvider implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof PsiWhiteSpace)) {
            return null;
        }

        int lineNumber = element.getContainingFile()
                .getViewProvider()
                .getDocument()
                .getLineNumber(element.getTextRange().getStartOffset());
        if (lineNumber != 19) {
            return null;
        }
        System.out.println("获取到第二十行");
        return new LineMarkerInfo<>(
                element,
                element.getTextRange(),
                createInvisibleIcon(),
                8, // This will make sure the tooltip is shown even when there is no real icon.
                this::createTooltip,
                this::handleClick,
                GutterIconRenderer.Alignment.RIGHT
        );
    }

    private Icon createInvisibleIcon() {
        return PluginIcons.INVISIBLE_ICON; // You can also use an empty icon, any 1x1 transparent image will work.
    }

    private String createTooltip(PsiElement element) {
        return "//HelloWorld";
    }

    private void handleClick(MouseEvent event, PsiElement element) {
        System.out.println("be clicked");
    }
}
