package me.haroldmartin.chat.ui.conversation;

import me.haroldmartin.chat.R;
import me.haroldmartin.chat.api.ChatMessage;
import me.haroldmartin.chat.databinding.ChatMessageItemBinding;
import me.haroldmartin.chat.ui.common.DataBoundListAdapter;
import me.haroldmartin.chat.util.Objects;

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;


public class ChatMessageAdapter extends DataBoundListAdapter<ChatMessage, ChatMessageItemBinding> {
    private final DataBindingComponent dataBindingComponent;
    private final ChatMessageClickCallback clickCallback;

    public ChatMessageAdapter(DataBindingComponent dataBindingComponent, ChatMessageClickCallback clickCallback) {
        this.dataBindingComponent = dataBindingComponent;
        this.clickCallback = clickCallback;
    }

    @Override
    protected ChatMessageItemBinding createBinding(ViewGroup parent) {
        ChatMessageItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.chat_message_item,
                        parent, false, dataBindingComponent);
        binding.getRoot().setOnClickListener(v -> {
            ChatMessage chatMessage = binding.getChatMessage();
            if (chatMessage != null && clickCallback != null) {
                clickCallback.onClick(chatMessage);
            }
        });
        return binding;
    }

    @Override
    protected void bind(ChatMessageItemBinding binding, me.haroldmartin.chat.api.ChatMessage item) {
        binding.setChatMessage(item);
    }

    @Override
    protected boolean areItemsTheSame(ChatMessage oldItem, ChatMessage newItem) {
        return Objects.equals(oldItem.getText(), newItem.getText()) &&
                Objects.equals(oldItem.getUserName(), newItem.getUserName());
    }

    @Override
    protected boolean areContentsTheSame(ChatMessage oldItem, ChatMessage newItem) {
        return Objects.equals(oldItem.getText(), newItem.getText());
    }

    public interface ChatMessageClickCallback {
        void onClick(ChatMessage chatMessage);
    }
}
