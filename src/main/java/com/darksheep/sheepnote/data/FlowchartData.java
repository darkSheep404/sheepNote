package com.darksheep.sheepnote.data;

import org.jetbrains.annotations.NotNull;

public class FlowchartData {
    private int id;
    private String name;
    private String data;
    private long createTime;

    public FlowchartData(@NotNull String name, @NotNull String data) {
        this.name = name;
        this.data = data;
        this.createTime = System.currentTimeMillis();
    }

    public FlowchartData() {
    }

    public FlowchartData(int id, @NotNull String name, @NotNull String data, long createTime) {
        this.id = id;
        this.name = name;
        this.data = data;
        this.createTime = createTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public String getData() {
        return data;
    }

    public void setData(@NotNull String data) {
        this.data = data;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
} 