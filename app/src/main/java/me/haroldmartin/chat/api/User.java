package me.haroldmartin.chat.api;

import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;
import com.stfalcon.chatkit.commons.models.IUser;

public class User implements IUser {
    String id;
    String name;
    String avatar;

    public User() { }

    public User(String userId, String userName, String userAvatar) {
        this.id = userId;
        this.name = userName;
        if (!TextUtils.isEmpty(userAvatar)) {
            this.avatar = userAvatar;
        }
    }

    public User(FirebaseUser firebaseUser) {
        new User(firebaseUser.getUid(),
                firebaseUser.getDisplayName(),
                firebaseUser.getPhotoUrl().toString()
        );
    }
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }
}
