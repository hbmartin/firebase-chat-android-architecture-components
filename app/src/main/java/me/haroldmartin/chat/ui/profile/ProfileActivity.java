package me.haroldmartin.chat.ui.profile;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import me.haroldmartin.chat.R;
import me.haroldmartin.chat.databinding.ProfileActivityBinding;
import me.haroldmartin.chat.ui.inbox.InboxActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.crash.FirebaseCrash;

import hugo.weaving.DebugLog;
import timber.log.Timber;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ProfileActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {
    private ViewGroup mSignInUi;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    protected ProfileActivityBinding binding;

    private static final int RC_SIGN_IN = 103;
    private CallbackManager mFacebookCallbackManager;
    private OnCompleteListener<AuthResult> mAuthCompleteListener;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.profile_activity);

        // Initialize authentication and set up callbacks
        setupFirebaseAuth();
        setupGoogleLogin();
        setupFacebookLogin();
        // TODO: check network connection UI
    }

    private void setupFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mAuthCompleteListener = task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
                launchInboxIntent();
                // TODO: copy user info into standalone profile
            } else {
                // If sign in fails, display a message to the user.
                Timber.e("signInWithCredential:failure");
                Timber.e(task.getException().toString());
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void setupFacebookLogin() {
        mFacebookCallbackManager = CallbackManager.Factory.create();

        binding.facebookButton.setReadPermissions("email","public_profile");

        // Callback registration
        binding.facebookButton.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(ProfileActivity.this, "Facebook Authentication cancelled.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(ProfileActivity.this, "Facebook Authentication failed.", Toast.LENGTH_SHORT).show();
                Timber.e(exception);
            }
        });
    }

    private void firebaseAuthWithFacebook(AccessToken token) {
        Timber.e("firebaseAuthWithFacebook:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, mAuthCompleteListener);
    }

    private void setupGoogleLogin() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .requestIdToken(getString(R.string.default_web_client_id))
                                .build())
                .build();
        mSignInUi = (ViewGroup) findViewById(R.id.sign_in_ui);

        binding.setGoogleCallback(() -> launchSignInIntent());
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
    @DebugLog
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        } else {
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
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
            String error = "Google Sign-In failed. status code : " +
                    result.getStatus().getStatusCode() + " . reason: " +
                    result.getStatus().getStatusMessage();
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            FirebaseCrash.log(error);
        }
    }

    @DebugLog
    void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
//        showProgressDialog(getString(R.string.profile_progress_message));
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, mAuthCompleteListener);
    }

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
        Toast.makeText(this, "Connection failed.", Toast.LENGTH_SHORT).show();
    }
}

