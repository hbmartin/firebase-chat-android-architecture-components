package me.haroldmartin.chat.api;

import android.text.TextUtils;

import com.google.firebase.database.Exclude;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.ArrayList;
import java.util.List;

import me.haroldmartin.firebaseextensions.Fire;

public class InboxItem extends ChatMessage implements IDialog {
    @Exclude
    public ArrayList<User> users;
    public Long lastRead;
    public String dialogName;
    public int unreadCount;

    public InboxItem() {
    }


    public InboxItem(String conversationId, String dialogName) {
        super("new bonfire", conversationId, Fire.auth.getCurrentUser());
        this.lastRead = getTimestamp();
        this.dialogName = dialogName;
        this.users = new ArrayList<User>();
        this.users.add(new User(Fire.auth.getCurrentUser()));
    }

    public Long getLastRead() {
        return lastRead;
    }

    @Override
    public String getId() {
        return getConversationId();
    }

    @Override
    public String getDialogPhoto() {
        return getUserAvatar();
    }

    @Override
    public String getDialogName() {
        return !TextUtils.isEmpty(dialogName) ? dialogName : getConversationId();
    }

    @Override
    public List<User> getUsers() {
        // TODO
        return users != null ? users : new ArrayList<User>();
    }

    @Override
    public IMessage getLastMessage() {
        return this;
    }

    @Override
    public void setLastMessage(IMessage message) {
        // TODO
    }

    @Override
    public int getUnreadCount() {
        // TODO
        return unreadCount;
    }
}
