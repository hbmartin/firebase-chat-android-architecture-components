package me.haroldmartin.chat.firebase;

import android.support.annotation.WorkerThread;

import me.haroldmartin.chat.api.ChatMessage;
import me.haroldmartin.chat.api.Conversation;
import me.haroldmartin.chat.api.Inbox;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import me.haroldmartin.firebaseextensions.FBX;
import timber.log.Timber;


import static me.haroldmartin.chat.firebase.MetaData.DATA;
import static me.haroldmartin.chat.firebase.MetaData.META;

public class ShoudBeDoneOnServer {
    @WorkerThread
    public static void addConversationToInbox(Conversation conversation) {
        // Firebase Serializer chokes on InboxItem, goes into some recursive black hole and OOMs
        // InboxItem inboxItem = new InboxItem(conversation.getMeta().getId(), conversation.getMeta().getName());
        DatabaseRouter.getInboxRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Inbox inbox = new Inbox(FBX.auth.getCurrentUser());
                    dataSnapshot.getRef().child(META).setValue(inbox.getMeta());
                }

                dataSnapshot.getRef().child(DATA).child(conversation.getMeta().getId())
                        .setValue(new ChatMessage("Brand spankin new conversation", conversation.getMeta().getId(), FBX.auth.getCurrentUser()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.e(databaseError.getDetails());
            }
        });
    }

    @WorkerThread
    public static void updateChatMessageInInbox(final ChatMessage chatMessage) {
//        DatabaseReference inboxItemRef = DatabaseRouter.getInboxRef().child(DATA).child(chatMessage.getConversationId());
//        inboxItemRef.setValue(chatMessage);
        // TODO: something about updating other users' inboxes as well
    }

    // TODO save user to /people/ when they signin
}
