package me.haroldmartin.chat.ui.conversation;

import me.haroldmartin.chat.api.ChatMessage;
import me.haroldmartin.chat.api.ConversationMetaData;
import me.haroldmartin.chat.repository.InboxRepository;
import me.haroldmartin.firebaseextensions.db.Resource;
import me.haroldmartin.chat.util.AbsentLiveData;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.VisibleForTesting;

import javax.inject.Inject;

public class ConversationViewModel extends ViewModel {
    @VisibleForTesting
    final MutableLiveData<String> conversationId;
    private final LiveData<ChatMessage> conversation;
    private final LiveData<Resource<ConversationMetaData>> metaData;

    @Inject
    public ConversationViewModel(InboxRepository repository) {
        this.conversationId = new MutableLiveData<>();

        conversation = Transformations.switchMap(conversationId, input -> {
            if (input.isEmpty()) {
                return AbsentLiveData.create();
            }
            return repository.conversation(input);
        });

        metaData = Transformations.switchMap(conversationId, input -> {
            if (input.isEmpty()) {
                return AbsentLiveData.create();
            }
            return repository.conversationMetaData(input);
        });
    }

    public LiveData<ChatMessage> getConversation() {
        return conversation;
    }
    public LiveData<Resource<ConversationMetaData>> getMetaData() {
        return metaData;
    }

    public void retry() {
        String current = conversationId.getValue();
        if (current != null && !current.isEmpty()) {
            conversationId.setValue(current);
        }
    }

    public void setId(String id) {
        if (id != null && !id.equals(conversationId.getValue())) {
            conversationId.setValue(id);
        }
    }

    public MutableLiveData<String> getId() {
        return conversationId;
    }
}
