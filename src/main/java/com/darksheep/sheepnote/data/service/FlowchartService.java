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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.*;

@Service
public final class FlowchartService {
    private final Gson gson;

    public FlowchartService() {
        this.gson = new Gson();
    }

    public Integer saveFlowchart(FlowchartData flowchartData) {
        // 解析流程图内容
        JsonObject flowchartContent = gson.fromJson(flowchartData.getData(), JsonObject.class);
        JsonArray nodes = flowchartContent.getAsJsonArray("nodes");
        // 收集需要更新标签的笔记
        Set<NoteData> notesToUpdate = new HashSet<>();
        
        // 遍历所有节点，找出包含 noteData 的节点
        for (JsonElement node : nodes) {
            JsonObject nodeObj = node.getAsJsonObject();
            if (nodeObj.has("noteData")) {
                JsonObject noteData = nodeObj.getAsJsonObject("noteData");
                Integer noteId = noteData.get("id").getAsInt();
                
                // 获取笔记并更新标签
                NoteData note = NoteDataRepository.getNoteById(noteId);
                if (note != null) {
                    note.addTag(flowchartData.getName());
                    notesToUpdate.add(note);
                }
            }
        }
        Integer Id = NoteDataRepository.saveFlowchart(flowchartData);
        // 更新笔记的标签
        for (NoteData note : notesToUpdate) {
            NoteDataRepository.updateNoteTags(note);
        }
        return Id;
    }

    public void deleteFlowchart(int id) {
        NoteDataRepository.deleteFlowchart(id);
    }

    public List<FlowchartData> getFlowcharts() {
        List<FlowchartData> flowcharts = NoteDataRepository.getAllFlowcharts();
        return flowcharts;
    }

    public String getFlowchartById(String id) {
        try {
            FlowchartData flowchart = NoteDataRepository.getFlowchartById(Integer.parseInt(id));
            return gson.toJson(flowchart);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<NoteData> searchNotes(String keyword) {
        return NoteDataRepository.searchNotes(keyword);
    }
} 