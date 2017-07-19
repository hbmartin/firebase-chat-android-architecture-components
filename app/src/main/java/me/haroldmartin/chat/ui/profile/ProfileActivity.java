package me.haroldmartin.chat.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import me.haroldmartin.chat.R;
import me.haroldmartin.chat.ui.inbox.InboxActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import hugo.weaving.DebugLog;
import timber.log.Timber;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ProfileActivity extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {
    private ViewGroup mSignInUi;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;

    private static final int RC_SIGN_IN = 103;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        // Initialize authentication and set up callbacks
        mAuth = FirebaseAuth.getInstance();

        // GoogleApiClient with Sign In
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .requestIdToken(getString(R.string.default_web_client_id))
                                .build())
                .build();

        mSignInUi = (ViewGroup) findViewById(R.id.sign_in_ui);

        findViewById(R.id.launch_sign_in).setOnClickListener(this);

        // TODO: check network connection UI
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
            case R.id.launch_sign_in:
                launchSignInIntent();
                break;
            case R.id.sign_out_button:
                mAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                showSignedOutUI();
                break;
        }
    }

    @DebugLog
    void launchSignInIntent() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @DebugLog
    void launchInboxIntent() {
        Intent intent = new Intent(this, InboxActivity.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    @DebugLog
    void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Successful Google sign in, authenticate with Firebase.
            GoogleSignInAccount acct = result.getSignInAccount();
            firebaseAuthWithGoogle(acct);
        } else {
            // Unsuccessful Google Sign In, show signed-out UI
            Timber.e("Google Sign-In failed.");
            Timber.e("status code : " + result.getStatus().getStatusCode());
            Timber.e(result.getStatus().getStatusMessage());
            // TODO: error UI
        }
    }

    @DebugLog
    void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
//        showProgressDialog(getString(R.string.profile_progress_message));
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult result) {
                        launchInboxIntent();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        FirebaseCrash.logcat(Log.ERROR, TAG, "auth:onFailure:" + e.getMessage());
                        Toast.makeText(ProfileActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        showSignedOutUI();
                    }
                });
    }

//    private void showSignedInUI(FirebaseUser firebaseUser) {
//        Log.d(TAG, "Showing signed in UI");
//        mSignInUi.setVisibility(View.GONE);
//        mProfileUi.setVisibility(View.VISIBLE);
//        mProfileUsername.setVisibility(View.VISIBLE);
//        mProfilePhoto.setVisibility(View.VISIBLE);
//        if (firebaseUser.getDisplayName() != null) {
//            mProfileUsername.setText(firebaseUser.getDisplayName());
//        }
//
//        if (firebaseUser.getPhotoUrl() != null) {
//            GlideUtil.loadProfileIcon(firebaseUser.getPhotoUrl().toString(), mProfilePhoto);
//        }
//        Map<String, Object> updateValues = new HashMap<>();
//        updateValues.put("displayName", firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Anonymous");
//        updateValues.put("type", firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null);
//
//        FirebaseUtil.getCurrentUserRef().updateChildren(
//                updateValues,
//                new DatabaseReference.CompletionListener() {
//                    @Override
//                    public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
//                        if (firebaseError != null) {
//                            Toast.makeText(ProfileActivity.this,
//                                    "Couldn't save user data: " + firebaseError.getMessage(),
//                                    Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });
//    }

    private void showSignedOutUI() {
        mSignInUi.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && !currentUser.isAnonymous()) {
//            dismissProgressDialog();
            launchInboxIntent();
        } else {
            showSignedOutUI();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.w("onConnectionFailed:" + connectionResult);
    }
}

