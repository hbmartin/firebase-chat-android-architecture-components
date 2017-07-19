package me.haroldmartin.chat.api;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Conversation {

    HashMap<String, ChatMessage> data;
    ConversationMetaData meta;

    public Conversation() {}

    public Conversation(ConversationMetaData meta, HashMap<String, ChatMessage> data) {
        this.meta = meta;
        this.data = data;
    }

    public Conversation(String id) {
        this.meta = new ConversationMetaData(id);
        this.data = new HashMap<>();
    }

    public HashMap<String, ChatMessage> getData() {
        return data;
    }

    public List<ChatMessage> getDataAsList() {  // TODO: replace with sort query
        ArrayList<ChatMessage> rv = new ArrayList<>();
        if (data == null) { return rv; }
        Set<Map.Entry<String, ChatMessage>> set = data.entrySet();
        for (Map.Entry<String, ChatMessage> entry : set) {
            rv.add(entry.getValue());
        }
        return rv;
    }

    public ConversationMetaData getMeta() {
        return meta;
    }

    public Conversation addParticipant(String id) {
        if (this.meta.participants == null) {
            this.meta.participants = new HashMap<>();
        }

        this.meta.participants.put(id, true);

        return this;
    }

    public void addChatMessage(ChatMessage chatMessage) {
        if (data == null) {
            data = new HashMap<>();
        }

    }
}
