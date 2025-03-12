package com.darksheep.sheepnote.data;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class FlowchartData {
    private Integer id;
    private String name;
    private String data;
    public Date createTime;

    public Date updateTime;

    public FlowchartData(@NotNull String name, @NotNull String data) {
        this.name = name;
        this.data = data;
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    public FlowchartData() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}