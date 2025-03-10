    package com.darksheep.sheepnote.data.service;

import com.darksheep.sheepnote.data.FlowchartData;
import com.darksheep.sheepnote.data.NoteData;

import com.darksheep.sheepnote.config.NoteDataRepository;
import com.intellij.openapi.components.Service;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

    @Service
public final class FlowchartService {

    public void saveFlowchart(@NotNull String name, @NotNull String flowchartData) {
        FlowchartData data = new FlowchartData(name, flowchartData);
        NoteDataRepository.saveFlowchart(data);
    }

    public List<FlowchartData> getAllFlowcharts() {
        return NoteDataRepository.getAllFlowcharts();
    }

    public String getFlowchartById(String id) {
        try {
            FlowchartData flowchart = NoteDataRepository.getFlowchartById(Integer.parseInt(id));
            return flowchart.getData();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<Map<String, String>> searchNotes(String keyword) {
        List<NoteData> notes = NoteDataRepository.searchNotes(keyword);
        return notes.stream().map(note -> {
            Map<String, String> result = new HashMap<>();
            result.put("id", String.valueOf(note.getId()));
            result.put("title", note.getNoteTitle().substring(0, Math.min(50, note.getNoteTitle().length())));
            result.put("path", note.getNoteFilePath());
            return result;
        }).collect(Collectors.toList());
    }
} 