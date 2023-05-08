package com.darksheep.sheepnote.config;

import com.darksheep.sheepnote.data.NoteData;
import com.intellij.util.messages.Topic;

public interface AddNoteEventListener {
    Topic<AddNoteEventListener> ADD_NOTE_TOPIC = Topic.create("Add Note Event", AddNoteEventListener.class);

    void onAddNoteEvent(NoteData noteData);
}
