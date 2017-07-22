package me.haroldmartin.chat.ui.inbox;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import me.haroldmartin.chat.R;
import me.haroldmartin.chat.databinding.MainActivityBinding;
import me.haroldmartin.chat.ui.common.NavigationController;
import me.haroldmartin.chat.ui.profile.ProfileActivity;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class InboxActivity extends AppCompatActivity implements LifecycleRegistryOwner,
        HasSupportFragmentInjector {
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    protected MainActivityBinding binding;

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;
    @Inject
    NavigationController navigationController;

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity);
        if (savedInstanceState == null) {
            navigationController.navigateToInbox();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(true);
        drawer.addDrawerListener(toggle);

        binding.setSignOutCallback(() -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(ProfileActivity.class, true);
            // TODO: signout facebook/google ?
        });
    }

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

    public void startActivity(Class clazz, boolean clear) {
        if (!(clazz.equals(AppCompatActivity.class))) {
            throw new RuntimeException("class must be instance of AppCompatActivity");
        }
        Intent intent = new Intent(this, clazz);
        if (clear) {
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
        }
        startActivity(intent);
    }
}
