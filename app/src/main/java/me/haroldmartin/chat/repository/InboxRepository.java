package me.haroldmartin.chat.repository;

import android.arch.lifecycle.LiveData;

import me.haroldmartin.chat.AppExecutors;
import me.haroldmartin.chat.api.ChatMessage;
import me.haroldmartin.chat.api.Conversation;
import me.haroldmartin.chat.api.ConversationMetaData;
import me.haroldmartin.chat.api.Inbox;
import me.haroldmartin.chat.firebase.DatabaseRouter;

import me.haroldmartin.firebaseextensions.FBX;
import me.haroldmartin.firebaseextensions.db.Resource;
import me.haroldmartin.chat.firebase.ShoudBeDoneOnServer;
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

    public static void addConversation(String id) {
        DatabaseRouter.getConversationRef(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Conversation conversation = null;
                if (dataSnapshot.exists()) {
                    conversation = dataSnapshot.getValue(Conversation.class);
                    conversation.addParticipant(FBX.auth.getCurrentUserId());

                    dataSnapshot.getRef().child(META).child(PARTICIPANTS)
                            .child(FBX.auth.getCurrentUserId()).setValue(true);
                } else {
                    conversation = new Conversation(id);
                    conversation.addParticipant(FBX.auth.getCurrentUserId());
                    dataSnapshot.getRef().setValue(conversation);
                }

                // Now add this conversation to my inbox after it's been added to /conversations
                // TODO: put children like in addMessage
                ShoudBeDoneOnServer.addConversationToInbox(conversation);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public static void addMessage(ChatMessage chatMessage) {
        DatabaseReference chatMsgRef = DatabaseRouter.getConversationRef(chatMessage.getConversationId()).child(DATA).push();
        chatMessage.id = chatMsgRef.getKey();
//        chatMsgRef.setValue(chatMessage);
//        ShoudBeDoneOnServer.updateChatMessageInInbox(chatMessage);

        HashMap<String, Object> childUpdates = new HashMap<>();
        String msgPath = FBX.db.getPathFromRef(chatMsgRef);
        Timber.e(msgPath);
        childUpdates.put(msgPath, chatMessage);

        DatabaseReference inboxItemRef = DatabaseRouter.getInboxRef().child(DATA).child(chatMessage.getConversationId());
//        inboxItemRef.setValue(chatMessage);
        childUpdates.put(FBX.db.getPathFromRef(inboxItemRef.child("text")), chatMessage.getText());
        childUpdates.put(FBX.db.getPathFromRef(inboxItemRef.child("name")), chatMessage.getUserName());
        childUpdates.put(FBX.db.getPathFromRef(inboxItemRef.child("timestamp")), chatMessage.getTimestamp());

        FBX.db.getBaseRef().updateChildren(childUpdates);
    }
}
