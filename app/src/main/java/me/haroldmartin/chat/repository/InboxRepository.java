package me.haroldmartin.chat.repository;

import android.arch.lifecycle.LiveData;

import me.haroldmartin.chat.AppExecutors;
import me.haroldmartin.chat.api.ChatMessage;
import me.haroldmartin.chat.api.Conversation;
import me.haroldmartin.chat.api.ConversationMetaData;
import me.haroldmartin.chat.api.Inbox;
import me.haroldmartin.chat.firebase.DatabaseRouter;

import me.haroldmartin.firebaseextensions.Fire;
import me.haroldmartin.firebaseextensions.db.Resource;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import me.haroldmartin.firebaseextensions.db.FirebaseLiveData;
import me.haroldmartin.firebaseextensions.db.FirebaseStreamingLiveData;

import timber.log.Timber;

import static me.haroldmartin.chat.api.ConversationMetaData.PARTICIPANTS;
import static me.haroldmartin.chat.firebase.MetaData.DATA;
import static me.haroldmartin.chat.firebase.MetaData.META;

@Singleton
public class InboxRepository {

    private final AppExecutors appExecutors;
    private final DatabaseReference inboxRef;

    @Inject
    public InboxRepository(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        this.inboxRef = DatabaseRouter.getInboxRef();
    }

    public LiveData<ChatMessage> conversation(String id) {
        return new FirebaseStreamingLiveData<ChatMessage>(DatabaseRouter.getConversation(id), ChatMessage.class);
    }

    public LiveData<Resource<ConversationMetaData>> conversationMetaData(String id) {
        return new FirebaseLiveData<ConversationMetaData>(DatabaseRouter.getConversationMeta(id), ConversationMetaData.class);
    }

    public LiveData<Resource<Inbox>> inbox() {
        FirebaseLiveData<Inbox> inbox = new FirebaseLiveData<Inbox>(inboxRef, Inbox.class);
        return inbox;
    }

    public static void addConversation(String id, ConversationAddedListener callback) {
        DatabaseRouter.getConversationRef(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Conversation conversation = null;
                if (dataSnapshot.exists()) {
                    conversation = dataSnapshot.getValue(Conversation.class);
                    conversation.addParticipant(Fire.auth.getCurrentUserId());

                    dataSnapshot.getRef().child(META).child(PARTICIPANTS)
                            .child(Fire.auth.getCurrentUserId())
                            .setValue(true)
                            .addOnCompleteListener((result) -> { if (callback != null && result.isSuccessful()) { callback.onSuccess(id); } } );

                } else {
                    conversation = new Conversation(id);
                    conversation.addParticipant(Fire.auth.getCurrentUserId());
                    dataSnapshot.getRef()
                            .setValue(conversation)
                            .addOnCompleteListener((result) -> { if (callback != null && result.isSuccessful()) { callback.onSuccess(id); } } );
                }
                addConversationToInbox(conversation);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // this method is responsible for creating a user's inbox if they don't already have one
    public static void addConversationToInbox(Conversation conversation) {
        // Firebase Serializer chokes on InboxItem, goes into some recursive black hole and OOMs
        // InboxItem inboxItem = new InboxItem(conversation.getMeta().getId(), conversation.getMeta().getName());
        DatabaseRouter.getInboxRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Inbox inbox = new Inbox(Fire.auth.getCurrentUser());
                    dataSnapshot.getRef().child(META).setValue(inbox.getMeta());
                }
                String text = "Brand spankin new conversation";
                if (conversation.getData().size() > 0) {
                    // TODO: replace this horrible hack
                    String key = conversation.getData().keySet().toArray(new String[0])[0];
                    text = conversation.getData().get(key).getText();
                }
                dataSnapshot.getRef().child(DATA).child(conversation.getMeta().getId())
                        .setValue(new ChatMessage(text, conversation.getMeta().getId(), Fire.auth.getCurrentUser()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.e(databaseError.getDetails());
            }
        });
    }



    public static void addMessage(ChatMessage chatMessage) {
        DatabaseReference chatMsgRef = DatabaseRouter.getConversationRef(chatMessage.getConversationId()).child(DATA).push();
        chatMessage.id = chatMsgRef.getKey();
//        chatMsgRef.setValue(chatMessage);
//        ShoudBeDoneOnServer.updateChatMessageInInbox(chatMessage);

        HashMap<String, Object> childUpdates = new HashMap<>();
        String msgPath = Fire.db.getPathFromRef(chatMsgRef);
        childUpdates.put(msgPath, chatMessage);

        DatabaseReference inboxItemRef = DatabaseRouter.getInboxRef().child(DATA).child(chatMessage.getConversationId());
//        inboxItemRef.setValue(chatMessage);
        childUpdates.put(Fire.db.getPathFromRef(inboxItemRef.child("text")), chatMessage.getText());
        childUpdates.put(Fire.db.getPathFromRef(inboxItemRef.child("name")), chatMessage.getUserName());
        childUpdates.put(Fire.db.getPathFromRef(inboxItemRef.child("timestamp")), chatMessage.getTimestamp());

        Fire.db.getBaseRef().updateChildren(childUpdates);
    }

    public interface ConversationAddedListener {
        void onSuccess(String conversationId);
    }
}
