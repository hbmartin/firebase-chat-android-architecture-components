package me.haroldmartin.chat.di;

import me.haroldmartin.chat.ui.inbox.InboxFragment;
import me.haroldmartin.chat.ui.conversation.ConversationFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract ConversationFragment contributeConversationFragment();

    @ContributesAndroidInjector
    abstract InboxFragment contributeInboxFragment();
}
