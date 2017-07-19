package me.haroldmartin.chat.api;


import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

public class ChatMessage implements IMessage {

    enum TYPES { TEXT }

    public String id;
    public String userId;
    public String conversationId;
    public String text;
    public String userName;
    public String type = TYPES.TEXT.name();
    public Long timestamp;

    String userAvatar;

    public ChatMessage() { }

    public ChatMessage(String text, String conversationId, FirebaseUser user) {
        this.userId = user.getUid();
        this.userName = user.getDisplayName();
        this.userAvatar = user.getPhotoUrl().toString();
        this.conversationId = conversationId;
        this.text = text;
        this.timestamp = System.currentTimeMillis(); // TODO: SNTP
    }

    @Override
    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    @Exclude
    @Override
    public IUser getUser() {
        return new User(userId, userName, userAvatar);
    }
    public void setUser(User user) {
        this.userId = user.getId();
        this.userAvatar= user.getAvatar();
        this.userName = user.getName();
    }

    @Exclude
    @Override
    public Date getCreatedAt() {
        return new Date(timestamp);
    }
    public void setCreatedAt(Date date) { this.timestamp = date.getTime(); }

    public String getConversationId() {
        return conversationId;
    }
    public String getUserName() {
        return userName;
    }
    public String getUserId() { return userId; }
    public String getUserAvatar() { return userAvatar; }
    public String getType() {
        return type;
    }
    public Long getTimestamp() { return timestamp; }
}
