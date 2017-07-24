package me.haroldmartin.chat.ui.common;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.haroldmartin.chat.binding.FragmentDataBindingComponent;
import me.haroldmartin.firebaseextensions.android.lifecycle.AutoClearedValue;

import javax.inject.Inject;

// subclass must implement injection
public abstract class BoundVmFragment<VM extends ViewModel, VDB extends ViewDataBinding> extends LifecycleFragment {

    @Inject protected ViewModelProvider.Factory viewModelFactory;
    @Inject protected NavigationController navigationController;

    protected FragmentDataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    protected VM viewModel;
    protected AutoClearedValue<VDB> binding;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(getVmClass());
        setupFragment(getArguments());
        setupUi();
        setupObservers();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        VDB dataBinding = DataBindingUtil
                .inflate(inflater, getLayout(), container, false,
                        dataBindingComponent);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }

    //    ____ _  _ ____ ____ ____ _ ___  ____
    //    |  | |  | |___ |__/ |__/ | |  \ |___
    //    |__|  \/  |___ |  \ |  \ | |__/ |___

    protected abstract int getLayout();
    protected abstract Class<VM> getVmClass();
    protected void setupFragment(Bundle args) { }
    protected abstract void setupUi();
    protected abstract void setupObservers();
}
