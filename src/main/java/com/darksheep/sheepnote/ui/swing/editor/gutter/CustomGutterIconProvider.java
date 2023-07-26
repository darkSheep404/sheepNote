package com.darksheep.sheepnote.ui.swing.editor.gutter;

import com.darksheep.sheepnote.config.NoteDataRepository;
import com.darksheep.sheepnote.data.NoteData;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import java.util.*;

public class CustomGutterIconProvider implements LineMarkerProvider {
    private  List<NoteData> notes = new ArrayList<>(); // 笔记列表，来自您的数据源

    public CustomGutterIconProvider(List<NoteData> notes) {
        this.notes = notes;
    }

    public CustomGutterIconProvider() {
    }

    {
        try {
            this.notes = NoteDataRepository.getAllNoteData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements, @NotNull Collection<? super LineMarkerInfo<?>> result) {
        HashSet<String> lineAndFilePath = new HashSet<>();
        for (PsiElement element : elements) {
            // 仅处理每行的第一个非空白 PsiElement
            PsiElement firstVisibleLeaf = extractFirstVisibleLeaf(element);
            if (firstVisibleLeaf==null||firstVisibleLeaf.getPrevSibling() != null || !(firstVisibleLeaf.getParent().getFirstChild() == element)) {
                continue;
            }

            int lineNumber = element.getContainingFile()
                    .getViewProvider()
                    .getDocument()
                    .getLineNumber(element.getTextRange().getStartOffset());
            String filePath = element.getContainingFile().getVirtualFile().getPath();

            List<NoteData> matchingNotes = getMatchingNotes(lineNumber,filePath);
            if (matchingNotes.isEmpty()) {
                continue;
            }
            if(lineAndFilePath.contains(StringUtils.join(filePath,lineNumber+1))){
                continue;
            }
            int iconOffset = 0;
            for (NoteData noteData:matchingNotes) { // 为每个匹配的笔记创建一个 LineMarkerInfo
                Icon icon = AllIcons.Actions.IntentionBulb;
                lineAndFilePath.add(StringUtils.join(noteData.getNoteFilePath(),noteData.noteTitle));
                LineMarkerInfo<?> lineMarkerInfo = new NoteLineMarkerInfo(
                        element,
                        element.getTextRange().shiftRight(iconOffset),
                        icon,
                        null,
                        (event, elt) -> {
                            showPopup(event,noteData);
                        },
                        GutterIconRenderer.Alignment.LEFT,
                        () -> "Note"
                );
                //lineMarkerInfo.setNoteData(noteData);
                result.add(lineMarkerInfo);
                iconOffset += icon.getIconWidth();
            }
        }
    }

    /**
     * 获取与给定行匹配的所有笔记 笔记行号需要减一 与实际行号对比 实际行号从0开始 笔记行号从1开始
     * @param lineNumber
     * @param filePath
     * @return
     */
    private List<NoteData> getMatchingNotes(int lineNumber,String filePath) {
        List<NoteData> matchingNotes = new ArrayList<>();
        for (NoteData noteData : notes) {
            if (noteData.getNoteLineNumber()-1 == lineNumber&& StringUtils.equals(filePath,noteData.noteFilePath)) {
                matchingNotes.add(noteData);
            }
        }
        return matchingNotes;
    }
    private void showPopup(MouseEvent event, NoteData noteData) {
        Project currentProject = IdeFocusManager.getGlobalInstance().getLastFocusedFrame().getProject();
        NoteDetailsDialog noteDetailsDialog = new NoteDetailsDialog(currentProject,noteData);
        noteDetailsDialog.pack();
        noteDetailsDialog.getWindow().setLocationRelativeTo(event.getComponent());
        noteDetailsDialog.show();
    }
    @Nullable
    private static PsiElement extractFirstVisibleLeaf(@NotNull PsiElement element) {
        if (element.getPrevSibling() != null) {
            return null;
        }
        PsiElement firstVisibleElement = null;
        PsiElement parent = element.getParent();
        if (parent != null) {
            PsiElement child = parent.getFirstChild();
            while (child != null) {
                if (!child.getText().trim().isEmpty()) {
                    firstVisibleElement = child;
                    break;
                }
                child = child.getNextSibling();
            }
        }
        return firstVisibleElement;
    }
}