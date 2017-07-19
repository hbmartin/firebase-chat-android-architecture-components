package me.haroldmartin.chat.ui.common;

import android.support.v4.app.FragmentManager;

import me.haroldmartin.chat.R;
import me.haroldmartin.chat.ui.conversation.ConversationFragment;
import me.haroldmartin.chat.ui.inbox.InboxActivity;
import me.haroldmartin.chat.ui.inbox.InboxFragment;

import javax.inject.Inject;


public class NavigationController {
    private final int containerId;
    private final FragmentManager fragmentManager;
    @Inject
    public NavigationController(InboxActivity inboxActivity) {
        this.containerId = R.id.container;
        this.fragmentManager = inboxActivity.getSupportFragmentManager();
    }

    public void navigateToInbox() {
        InboxFragment searchFragment = new InboxFragment();
        fragmentManager.beginTransaction()
                .replace(containerId, searchFragment)
                .commitAllowingStateLoss();
    }

    public void navigateToConversation(String id) {
        ConversationFragment fragment = ConversationFragment.create(id);
        String tag = "conversation" + "/" + id;
        fragmentManager.beginTransaction()
                .replace(containerId, fragment, tag)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }
}
