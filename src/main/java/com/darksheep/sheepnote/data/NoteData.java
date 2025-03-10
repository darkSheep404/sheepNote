package com.darksheep.sheepnote.data;

import java.util.Date;

public class NoteData {

    public Integer id;
    public String noteTitle;
    public String noteFilePath;
    public int noteLineNumber;
    public String selectCode;

    public Date createTime;

    public Date updateTime;

    public NoteData() {
    }

    public NoteData(String noteTitle, String noteFilePath, int noteLineNumber, String selectCode) {
        this.noteTitle = noteTitle;
        this.noteFilePath = noteFilePath;
        this.noteLineNumber = noteLineNumber;
        this.selectCode = selectCode;
    }
    public static NoteData buildExampleNote(){
        return new NoteData("this note will auto disappear after you take first note and restart idea", "D:/sheepnote/data.sqllite", 1, "this note will auto disappear after you take first note and restart idea");
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public String getNoteFilePath() {
        return noteFilePath;
    }

    public int getNoteLineNumber() {
        return noteLineNumber;
    }

    public String getSelectCode() {
        return selectCode;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }
}
