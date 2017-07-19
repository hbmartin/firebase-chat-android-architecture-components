/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.haroldmartin.chat.ui.conversation;

import me.haroldmartin.chat.repository.InboxRepository;
import me.haroldmartin.chat.vo.ChatMessage;
import me.haroldmartin.chat.vo.Contributor;
import me.haroldmartin.firebaseextensions.db.Resource;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(JUnit4.class)
public class ConversationViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private InboxRepository repository;
    private ConversationViewModel conversationViewModel;

    @Before
    public void setup() {
        repository = mock(InboxRepository.class);
        conversationViewModel = new ConversationViewModel(repository);
    }


    @Test
    public void testNull() {
        assertThat(conversationViewModel.getConversation(), notNullValue());
        assertThat(conversationViewModel.getContributors(), notNullValue());
        verify(repository, never()).conversation(anyString(), anyString());
    }

    @Test
    public void dontFetchWithoutObservers() {
        conversationViewModel.setId("a", "b");
        verify(repository, never()).conversation(anyString(), anyString());
    }

    @Test
    public void fetchWhenObserved() {
        ArgumentCaptor<String> owner = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> name = ArgumentCaptor.forClass(String.class);

        conversationViewModel.setId("a", "b");
        conversationViewModel.getConversation().observeForever(mock(Observer.class));
        verify(repository, times(1)).conversation(owner.capture(),
                name.capture());
        assertThat(owner.getValue(), is("a"));
        assertThat(name.getValue(), is("b"));
    }

    @Test
    public void changeWhileObserved() {
        ArgumentCaptor<String> owner = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> name = ArgumentCaptor.forClass(String.class);
        conversationViewModel.getConversation().observeForever(mock(Observer.class));

        conversationViewModel.setId("a", "b");
        conversationViewModel.setId("c", "d");

        verify(repository, times(2)).conversation(owner.capture(),
                name.capture());
        assertThat(owner.getAllValues(), is(Arrays.asList("a", "c")));
        assertThat(name.getAllValues(), is(Arrays.asList("b", "d")));
    }

    @Test
    public void contributors() {
        Observer<Resource<List<Contributor>>> observer = mock(Observer.class);
        conversationViewModel.getContributors().observeForever(observer);
        verifyNoMoreInteractions(observer);
        verifyNoMoreInteractions(repository);
        conversationViewModel.setId("foo", "bar");
        verify(repository).loadContributors("foo", "bar");
    }

    @Test
    public void resetId() {
        Observer<ConversationViewModel.RepoId> observer = mock(Observer.class);
        conversationViewModel.conversationId.observeForever(observer);
        verifyNoMoreInteractions(observer);
        conversationViewModel.setId("foo", "bar");
        verify(observer).onChanged(new ConversationViewModel.RepoId("foo", "bar"));
        reset(observer);
        conversationViewModel.setId("foo", "bar");
        verifyNoMoreInteractions(observer);
        conversationViewModel.setId("a", "b");
        verify(observer).onChanged(new ConversationViewModel.RepoId("a", "b"));
    }

    @Test
    public void retry() {
        conversationViewModel.retry();
        verifyNoMoreInteractions(repository);
        conversationViewModel.setId("foo", "bar");
        verifyNoMoreInteractions(repository);
        Observer<Resource<ChatMessage>> observer = mock(Observer.class);
        conversationViewModel.getConversation().observeForever(observer);
        verify(repository).conversation("foo", "bar");
        reset(repository);
        conversationViewModel.retry();
        verify(repository).conversation("foo", "bar");
    }

    @Test
    public void nullRepoId() {
        conversationViewModel.setId(null, null);
        Observer<Resource<ChatMessage>> observer1 = mock(Observer.class);
        Observer<Resource<List<Contributor>>> observer2 = mock(Observer.class);
        conversationViewModel.getConversation().observeForever(observer1);
        conversationViewModel.getContributors().observeForever(observer2);
        verify(observer1).onChanged(null);
        verify(observer2).onChanged(null);
    }
}
