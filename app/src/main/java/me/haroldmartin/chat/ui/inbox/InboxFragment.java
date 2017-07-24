package me.haroldmartin.chat.ui.inbox;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import hugo.weaving.DebugLog;
import me.haroldmartin.chat.R;
import me.haroldmartin.chat.api.InboxItem;
import me.haroldmartin.chat.databinding.InboxFragmentBinding;
import me.haroldmartin.chat.di.Injectable;
import me.haroldmartin.chat.repository.InboxRepository;
import me.haroldmartin.chat.ui.common.BoundVmFragment;
import me.haroldmartin.chat.ui.common.GlideImageManager;
import me.haroldmartin.chat.util.AutoClearedValue;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.ArrayList;

import timber.log.Timber;

public class InboxFragment extends BoundVmFragment<InboxViewModel, InboxFragmentBinding>
        implements Injectable, DialogsListAdapter.OnDialogClickListener<InboxItem> {

    AutoClearedValue<DialogsListAdapter> adapter;

    @Override
    protected void setupUi() {
        DialogsListAdapter<InboxItem> dialogsAdapter = new DialogsListAdapter<>(new GlideImageManager());
        dialogsAdapter.setOnDialogClickListener(this);
        adapter = new AutoClearedValue<>(this, dialogsAdapter);
        binding.get().conversationList.setAdapter(dialogsAdapter);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.app_name));
        binding.get().setCallback(() -> viewModel.refresh());
        binding.get().setNewChatCallback(() -> showAutocompleteDialog() ); // TODO: FAB
    }

    void showAutocompleteDialog() {
            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.new_conversation)
                    .customView(R.layout.autocomplete_dialog, false)
                    .show();
            setupAutocomplete(dialog);
    }

    void setupAutocomplete(MaterialDialog dialog) {
        View view = dialog.getCustomView();
        String autocompleteValues[] = { "random", "test", "whatever" };
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.auto_complete_text);
        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, autocompleteValues);
        autoCompleteTextView.setAdapter(autoCompleteAdapter);

        // TODO: query Firebase as user types

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @DebugLog
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InboxRepository.addConversation(autocompleteValues[position], new InboxConversationAddedListener());
                dialog.dismiss();
            }
        });

        Button submit = (Button) view.findViewById(R.id.auto_complete_submit);
        submit.setOnClickListener((l) -> {
            String id = autoCompleteTextView.getText().toString();
            dialog.dismiss();
            InboxRepository.addConversation(id, new InboxConversationAddedListener());
        });
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
        navigationController.navigateToConversation(dialog.getId());
    }

    class InboxConversationAddedListener implements InboxRepository.ConversationAddedListener {
        @Override
        public void onSuccess(String conversationId) {
            navigationController.navigateToConversation(conversationId);
        }
    }
}
