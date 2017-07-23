package me.haroldmartin.chat.firebase;

import android.support.annotation.WorkerThread;

import me.haroldmartin.chat.api.ChatMessage;
import me.haroldmartin.chat.api.Conversation;
import me.haroldmartin.chat.api.Inbox;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import me.haroldmartin.firebaseextensions.FBX;
import timber.log.Timber;


import static me.haroldmartin.chat.firebase.MetaData.DATA;
import static me.haroldmartin.chat.firebase.MetaData.META;

public class ShoudBeDoneOnServer {
    // TODO save user to /people/ when they signin
}
