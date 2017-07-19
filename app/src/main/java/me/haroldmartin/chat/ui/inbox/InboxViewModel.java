package me.haroldmartin.chat.ui.inbox;

import com.afollestad.materialdialogs.MaterialDialog;
import me.haroldmartin.chat.R;
import me.haroldmartin.chat.api.Inbox;
import me.haroldmartin.chat.repository.InboxRepository;
import me.haroldmartin.chat.util.Objects;
import me.haroldmartin.firebaseextensions.db.Resource;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.InputType;

import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;

public class InboxViewModel extends ViewModel {

    private final MutableLiveData<String> query = new MutableLiveData<>();

    private final LiveData<Resource<Inbox>> results;

    private final NextPageHandler nextPageHandler;

    @Inject
    InboxViewModel(InboxRepository inboxRepository) {
        nextPageHandler = new NextPageHandler(inboxRepository);
        results = inboxRepository.inbox();
    }

    LiveData<Resource<Inbox>> getResults() {
        return results;
    }

    public void setQuery(@NonNull String originalInput) {
        String input = originalInput.toLowerCase(Locale.getDefault()).trim();
        if (Objects.equals(input, query.getValue())) {
            return;
        }
        nextPageHandler.reset();
        query.setValue(input);
    }

    LiveData<LoadMoreState> getLoadMoreStatus() {
        return nextPageHandler.getLoadMoreState();
    }

    void loadNextPage() {
        String value = query.getValue();
        if (value == null || value.trim().length() == 0) {
            return;
        }
        nextPageHandler.queryNextPage(value);
    }

    void refresh() {
        if (query.getValue() != null) {
            query.setValue(query.getValue());
        }
    }

    // TODO replace with FAB in Fragment
    public void onNewConversationRequested(Context activity) {
        new MaterialDialog.Builder(activity)
                .title(R.string.new_conversation)
                .content("")
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
                .input(R.string.new_chat_input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        String conversation = input.toString();
                        Timber.e(conversation);
                        InboxRepository.addConversation(conversation);
                    }
                }).show();
    }

    static class LoadMoreState {
        private final boolean running;
        private final String errorMessage;
        private boolean handledError = false;

        LoadMoreState(boolean running, String errorMessage) {
            this.running = running;
            this.errorMessage = errorMessage;
        }

        boolean isRunning() {
            return running;
        }

        String getErrorMessage() {
            return errorMessage;
        }

        String getErrorMessageIfNotHandled() {
            if (handledError) {
                return null;
            }
            handledError = true;
            return errorMessage;
        }
    }

    @VisibleForTesting
    static class NextPageHandler implements Observer<Resource<Boolean>> {
        @Nullable
        private LiveData<Resource<Boolean>> nextPageLiveData;
        private final MutableLiveData<LoadMoreState> loadMoreState = new MutableLiveData<>();
        private String query;
        private final InboxRepository repository;
        @VisibleForTesting
        boolean hasMore;

        @VisibleForTesting
        NextPageHandler(InboxRepository repository) {
            this.repository = repository;
            reset();
        }

        void queryNextPage(String query) {
            if (Objects.equals(this.query, query)) {
                return;
            }
            unregister();
            this.query = query;
//            nextPageLiveData = repository.searchNextPage(query);
            loadMoreState.setValue(new LoadMoreState(true, null));
            //noinspection ConstantConditions
            nextPageLiveData.observeForever(this);
        }

        @Override
        public void onChanged(@Nullable Resource<Boolean> result) {
            if (result == null) {
                reset();
            } else {
                switch (result.status) {
                    case SUCCESS:
                        hasMore = Boolean.TRUE.equals(result.data);
                        unregister();
                        loadMoreState.setValue(new LoadMoreState(false, null));
                        break;
                    case ERROR:
                        hasMore = true;
                        unregister();
                        loadMoreState.setValue(new LoadMoreState(false,
                                result.message));
                        break;
                }
            }
        }

        private void unregister() {
            if (nextPageLiveData != null) {
                nextPageLiveData.removeObserver(this);
                nextPageLiveData = null;
                if (hasMore) {
                    query = null;
                }
            }
        }

        private void reset() {
            unregister();
            hasMore = true;
            loadMoreState.setValue(new LoadMoreState(false, null));
        }

        MutableLiveData<LoadMoreState> getLoadMoreState() {
            return loadMoreState;
        }
    }
}
