package me.haroldmartin.chat.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import me.haroldmartin.chat.ui.conversation.ConversationViewModel;
import me.haroldmartin.chat.ui.inbox.InboxViewModel;
import me.haroldmartin.chat.viewmodel.ArchCompViewModelFactory;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(InboxViewModel.class)
    abstract ViewModel bindInboxViewModel(InboxViewModel inboxViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ConversationViewModel.class)
    abstract ViewModel bindChatViewModel(ConversationViewModel conversationViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ArchCompViewModelFactory factory);
}
