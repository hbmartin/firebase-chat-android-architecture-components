package me.haroldmartin.chat.ui.inbox;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import me.haroldmartin.chat.R;
import me.haroldmartin.chat.api.InboxItem;
import me.haroldmartin.chat.databinding.InboxFragmentBinding;
import me.haroldmartin.chat.di.Injectable;
import me.haroldmartin.chat.ui.common.BoundVmFragment;
import me.haroldmartin.chat.ui.common.GlideUtil;
import me.haroldmartin.chat.util.AutoClearedValue;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.ArrayList;

import timber.log.Timber;

public class InboxFragment extends BoundVmFragment<InboxViewModel, InboxFragmentBinding>
        implements Injectable, DialogsListAdapter.OnDialogClickListener<InboxItem> {

    AutoClearedValue<DialogsListAdapter> adapter;

    @Override
    protected void setupUi() {
        DialogsListAdapter<InboxItem> dialogsAdapter = new DialogsListAdapter<>(new GlideUtil());
        dialogsAdapter.setOnDialogClickListener(this);
        adapter = new AutoClearedValue<>(this, dialogsAdapter);
        binding.get().conversationList.setAdapter(dialogsAdapter);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.app_name));
        binding.get().setCallback(() -> viewModel.refresh());
        binding.get().setNewChatCallback(() -> viewModel.onNewConversationRequested(getActivity()));
    }

    @Override
    protected void setupObservers() {
        binding.get().conversationList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager)
                        recyclerView.getLayoutManager();
                int lastPosition = layoutManager
                        .findLastVisibleItemPosition();
                if (lastPosition == adapter.get().getItemCount() - 1) {
                    viewModel.loadNextPage();
                }
            }
        });

        viewModel.getResults().observe(this, result -> {
            binding.get().setSearchResource(result);
            binding.get().setResultCount((result == null || result.data == null || result.data.getData() == null)
                    ? 0 : result.data.getData().size());
            adapter.get().setItems(result == null || result.data == null ? new ArrayList<InboxItem>() :
                    result.data.getDataAsList()
            );
            binding.get().executePendingBindings();
        });

        viewModel.getLoadMoreStatus().observe(this, loadingMore -> {
            if (loadingMore == null) {
                binding.get().setLoadingMore(false);
            } else {
                binding.get().setLoadingMore(loadingMore.isRunning());
                String error = loadingMore.getErrorMessageIfNotHandled();
                if (error != null) {
                    Snackbar.make(binding.get().loadMoreBar, error, Snackbar.LENGTH_LONG).show();
                }
            }
            binding.get().executePendingBindings();
        });
    }

    @Override
    protected Class getVmClass() { return InboxViewModel.class; }

    @Override
    protected int getLayout() { return R.layout.inbox_fragment; }

    @Override
    public void onDialogClick(InboxItem dialog) {
        Timber.e("onDialogClick");
        Timber.e(dialog.getDialogName());
        navigationController.navigateToConversation(dialog.getId());
    }
}
