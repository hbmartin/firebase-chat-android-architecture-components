package me.haroldmartin.chat.api;

import me.haroldmartin.chat.firebase.MetaData;

public class InboxMetaData extends MetaData {
    public InboxMetaData() {}

    public InboxMetaData(String uid) {
        super(uid, 0);
    }
}
