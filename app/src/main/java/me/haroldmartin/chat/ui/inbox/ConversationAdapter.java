package me.haroldmartin.chat.ui.inbox;

import me.haroldmartin.chat.R;
import me.haroldmartin.chat.api.InboxItem;
import me.haroldmartin.chat.databinding.ConversationItemBinding;
import me.haroldmartin.chat.ui.common.DataBoundListAdapter;
import me.haroldmartin.chat.util.Objects;

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;


public class ConversationAdapter extends DataBoundListAdapter<InboxItem, ConversationItemBinding> {
    private final DataBindingComponent dataBindingComponent;
    private final ConversationClickCallback clickCallback;

    public ConversationAdapter(DataBindingComponent dataBindingComponent, ConversationClickCallback clickCallback) {
        this.dataBindingComponent = dataBindingComponent;
        this.clickCallback = clickCallback;
    }

    @Override
    protected ConversationItemBinding createBinding(ViewGroup parent) {
        ConversationItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.conversation_item,
                        parent, false, dataBindingComponent);
        binding.getRoot().setOnClickListener(v -> {
            InboxItem conversation = binding.getConversation();
            if (conversation != null && clickCallback != null) {
                clickCallback.onClick(conversation);
            }
        });
        return binding;
    }

    @Override
    protected void bind(ConversationItemBinding binding, InboxItem item) {
        binding.setConversation(item);
    }

    @Override
    protected boolean areItemsTheSame(InboxItem oldItem, InboxItem newItem) {
        return Objects.equals(oldItem.getConversationId(), newItem.getConversationId());
    }

    @Override
    protected boolean areContentsTheSame(InboxItem oldItem, InboxItem newItem) {
        return Objects.equals(oldItem.getConversationId(), newItem.getConversationId());
    }

    public interface ConversationClickCallback {
        void onClick(InboxItem conversation);
    }
}
