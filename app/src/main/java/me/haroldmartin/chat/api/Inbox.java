package me.haroldmartin.chat.api;


import me.haroldmartin.chat.firebase.MetaData;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Inbox {
    public static final String INBOX = "inbox";

    HashMap<String, InboxItem> data;
    InboxMetaData meta;

    public Inbox() {}

    public Inbox(FirebaseUser user) {
        this.meta = new InboxMetaData(user.getUid());
        this.data = new HashMap<>();
    }

    public HashMap<String, InboxItem> getData() {
        if (data == null) {
            data = new HashMap<>();
        }
        return data ;
    }

    // TODO: replace with sort query, see InboxRepo
    public List<InboxItem> getDataAsList() {
        ArrayList<InboxItem> rv = new ArrayList<>();
        if (data == null) { return rv; }
        Set<Map.Entry<String, InboxItem>> set = data.entrySet();
        for (Map.Entry<String, InboxItem> entry : set) {
            rv.add(entry.getValue());
        }
        return rv;
    }

    public InboxMetaData getMeta() {
        return meta;
    }

    public void addInboxItem(InboxItem inboxItem) {
        data.put(inboxItem.getConversationId(), inboxItem);
    }
}
