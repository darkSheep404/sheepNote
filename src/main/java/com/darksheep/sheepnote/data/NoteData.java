package com.darksheep.sheepnote.data;

import java.util.Date;

public class NoteData {
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
}
