package me.haroldmartin.chat.ui.conversation;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import me.haroldmartin.chat.R;
import me.haroldmartin.chat.api.ChatMessage;
import me.haroldmartin.chat.api.ConversationMetaData;
import me.haroldmartin.chat.databinding.ConversationFragmentBinding;
import me.haroldmartin.chat.di.Injectable;

import me.haroldmartin.firebaseextensions.FBX;
import me.haroldmartin.firebaseextensions.android.lifecycle.AutoClearedValue;
import me.haroldmartin.firebaseextensions.db.Resource;
import me.haroldmartin.chat.repository.InboxRepository;
import me.haroldmartin.chat.ui.common.BoundVmFragment;
import me.haroldmartin.chat.ui.common.GlideImageManager;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import timber.log.Timber;

public class ConversationFragment extends BoundVmFragment<ConversationViewModel, ConversationFragmentBinding>
        implements Injectable, MessageInput.InputListener {

    private static final String CONVERSATION_ID_KEY = "conversation_id";

    AutoClearedValue<MessagesListAdapter> adapter;
    private String conversationId;
    private String userId;

    @Override
    protected void setupFragment(Bundle args) {
        if (args != null && args.containsKey(CONVERSATION_ID_KEY)) {
            conversationId = args.getString(CONVERSATION_ID_KEY);
            viewModel.setId(conversationId);
        } else {
            viewModel.setId(null);
        }
        userId = FBX.auth.getCurrentUserId();
    }

    protected void setupUi() {
        binding.get().setCallback(() -> viewModel.retry());
        MessagesListAdapter<ChatMessage> messageAdapter =
                new MessagesListAdapter<ChatMessage>(userId, new GlideImageManager());
        binding.get().input.setInputListener(this);
        binding.get().messagesList.setAdapter(messageAdapter);
        this.adapter = new AutoClearedValue<>(this, messageAdapter);
    }

    protected void setupObservers() {
        LiveData<Resource<ConversationMetaData>> meta = viewModel.getMetaData();
        meta.observe(this, resource -> {
            binding.get().setConversationMetaData(resource == null || resource.data == null ? null : resource.data);
            binding.get().setConversationResource(resource);
            if (resource != null & resource.data != null) {
                String name = resource.data.getName();
                if (!TextUtils.isEmpty(name)) {
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(name);
                } else {
                    // TODO: set usernames or something
                }

            }
            binding.get().executePendingBindings();
        });

        LiveData<ChatMessage> convo = viewModel.getConversation();
        convo.observe(this, data -> {
            if (data != null) {
                adapter.get().upsert(data);

            }
            binding.get().executePendingBindings();
        });
    }

    @Override
    protected int getLayout() { return R.layout.conversation_fragment; }

    @Override
    protected Class getVmClass() { return ConversationViewModel.class; }

    public static ConversationFragment create(String id) {
        ConversationFragment repoFragment = new ConversationFragment();
        Bundle args = new Bundle();
        args.putString(CONVERSATION_ID_KEY, id);
        repoFragment.setArguments(args);
        return repoFragment;
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        ChatMessage chat = new ChatMessage(input.toString(), conversationId, FBX.auth.getCurrentUser());
        adapter.get().addToStart(chat, true); // TODO: should be added in grayed out state until Firebase child loaded
        InboxRepository.addMessage(chat);
        return true;
    }
}
