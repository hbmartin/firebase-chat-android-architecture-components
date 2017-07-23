package me.haroldmartin.chat.api;

import me.haroldmartin.chat.firebase.MetaData;

import java.util.HashMap;
import java.util.Map;

public class ConversationMetaData extends MetaData {
    public static final String PARTICIPANTS = "participants";
    public String name;
    public Map<String, Boolean> participants;

    public ConversationMetaData() {}

    public ConversationMetaData(String id) {
        this(id, 0, new HashMap<>());
    }

    public ConversationMetaData(String id, int total, Map<String, Boolean> participants) {
        this.id = id;
        this.total = total;
        this.participants = participants;
    }

    public Map<String, Boolean> getParticipants() {
        return participants;
    }

    public String getName() {
        return name != null ? name : id;
    }
}
