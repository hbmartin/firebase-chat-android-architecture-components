package me.haroldmartin.chat.firebase;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import me.haroldmartin.firebaseextensions.Fire;

import static me.haroldmartin.firebaseextensions.Fire.db.ref;

public class DatabaseRouter {

    public static DatabaseReference getPostsRef() {
        return ref("posts");
    }

    public static DatabaseReference getPeopleRef() {
        return ref("people");
    }

    public static DatabaseReference getCommentsRef() {
        return ref("comments");
    }

    public static DatabaseReference getFeedRef() {
        return ref("feed");
    }

    public static DatabaseReference getInboxRef() {
        return ref("inbox").child(Fire.auth.getCurrentUserId());
    }

    public static DatabaseReference getConversationRef(String id) {
        return ref("conversations").child(id);
    }

    public static Query getConversation(String id) {
        return getConversationRef(id).child("data").orderByChild("timestamp");
    }

    public static DatabaseReference getConversationMeta(String id) {
        return getConversationRef(id).child("meta");
    }
}
