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

    public void saveFlowchart(@NotNull String name, @NotNull String flowchartData) {
        // 解析流程图内容
        JsonObject flowchartContent = gson.fromJson(flowchartData, JsonObject.class);
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
                    note.addTag(name);
                    notesToUpdate.add(note);
                }
            }
        }
        
        // 保存流程图
        FlowchartData data = new FlowchartData(name, flowchartData);
        NoteDataRepository.saveFlowchart(data);
        
        // 更新笔记的标签
        for (NoteData note : notesToUpdate) {
            NoteDataRepository.updateNoteTags(note);
        }
    }

    public List<Map<String, String>> getFlowcharts() {
        List<FlowchartData> flowcharts = NoteDataRepository.getAllFlowcharts();
        return flowcharts.stream()
            .map(f -> {
                Map<String, String> map = new HashMap<>();
                map.put("id", String.valueOf(f.getId()));
                map.put("name", f.getName());
                return map;
            })
            .collect(Collectors.toList());
    }

    public String getFlowchartById(String id) {
        try {
            FlowchartData flowchart = NoteDataRepository.getFlowchartById(Integer.parseInt(id));
            return flowchart != null ? flowchart.getData() : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<NoteData> searchNotes(String keyword) {
        return NoteDataRepository.searchNotes(keyword);
    }
} 