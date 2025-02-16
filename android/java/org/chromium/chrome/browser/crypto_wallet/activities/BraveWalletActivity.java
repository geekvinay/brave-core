/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.chromium.chrome.browser.crypto_wallet.activities;

import static org.chromium.chrome.browser.crypto_wallet.util.Utils.ONBOARDING_ACTION;
import static org.chromium.chrome.browser.crypto_wallet.util.Utils.ONBOARDING_FIRST_PAGE_ACTION;
import static org.chromium.chrome.browser.crypto_wallet.util.Utils.RESTORE_WALLET_ACTION;
import static org.chromium.chrome.browser.crypto_wallet.util.Utils.UNLOCK_WALLET_ACTION;

import android.app.SearchManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.chromium.base.Log;
import org.chromium.brave_wallet.mojom.AssetRatioController;
import org.chromium.brave_wallet.mojom.BraveWalletService;
import org.chromium.brave_wallet.mojom.ErcTokenRegistry;
import org.chromium.brave_wallet.mojom.EthJsonRpcController;
import org.chromium.brave_wallet.mojom.EthTxController;
import org.chromium.brave_wallet.mojom.KeyringController;
import org.chromium.brave_wallet.mojom.TransactionInfo;
import org.chromium.brave_wallet.mojom.TransactionStatus;
import org.chromium.chrome.R;
import org.chromium.chrome.browser.crypto_wallet.AssetRatioControllerFactory;
import org.chromium.chrome.browser.crypto_wallet.BraveWalletServiceFactory;
import org.chromium.chrome.browser.crypto_wallet.ERCTokenRegistryFactory;
import org.chromium.chrome.browser.crypto_wallet.EthJsonRpcControllerFactory;
import org.chromium.chrome.browser.crypto_wallet.EthTxControllerFactory;
import org.chromium.chrome.browser.crypto_wallet.KeyringControllerFactory;
import org.chromium.chrome.browser.crypto_wallet.adapters.CryptoFragmentPageAdapter;
import org.chromium.chrome.browser.crypto_wallet.adapters.CryptoWalletOnboardingPagerAdapter;
import org.chromium.chrome.browser.crypto_wallet.fragments.SwapBottomSheetDialogFragment;
import org.chromium.chrome.browser.crypto_wallet.fragments.onboarding_fragments.BackupWalletFragment;
import org.chromium.chrome.browser.crypto_wallet.fragments.onboarding_fragments.RecoveryPhraseFragment;
import org.chromium.chrome.browser.crypto_wallet.fragments.onboarding_fragments.RestoreWalletFragment;
import org.chromium.chrome.browser.crypto_wallet.fragments.onboarding_fragments.SecurePasswordFragment;
import org.chromium.chrome.browser.crypto_wallet.fragments.onboarding_fragments.SetupWalletFragment;
import org.chromium.chrome.browser.crypto_wallet.fragments.onboarding_fragments.UnlockWalletFragment;
import org.chromium.chrome.browser.crypto_wallet.fragments.onboarding_fragments.VerifyRecoveryPhraseFragment;
import org.chromium.chrome.browser.crypto_wallet.listeners.OnNextPage;
import org.chromium.chrome.browser.crypto_wallet.util.NavigationItem;
import org.chromium.chrome.browser.crypto_wallet.util.Utils;
import org.chromium.chrome.browser.init.AsyncInitializationActivity;
import org.chromium.chrome.browser.settings.BraveWalletPreferences;
import org.chromium.chrome.browser.settings.SettingsLauncherImpl;
import org.chromium.components.browser_ui.modaldialog.AppModalPresenter;
import org.chromium.components.browser_ui.settings.SettingsLauncher;
import org.chromium.mojo.bindings.ConnectionErrorHandler;
import org.chromium.mojo.system.MojoException;
import org.chromium.ui.base.ActivityWindowAndroid;
import org.chromium.ui.modaldialog.ModalDialogManager;

import java.util.ArrayList;
import java.util.List;

public class BraveWalletActivity
        extends AsyncInitializationActivity implements OnNextPage, ConnectionErrorHandler {
    private Toolbar mToolbar;

    private View cryptoLayout;
    private View cryptoOnboardingLayout;
    private ImageView swapButton;
    private ViewPager cryptoWalletOnboardingViewPager;
    private ModalDialogManager mModalDialogManager;
    private CryptoWalletOnboardingPagerAdapter cryptoWalletOnboardingPagerAdapter;
    private KeyringController mKeyringController;
    private ErcTokenRegistry mErcTokenRegistry;
    private EthJsonRpcController mEthJsonRpcController;
    private EthTxController mEthTxController;
    private AssetRatioController mAssetRatioController;
    private BraveWalletService mBraveWalletService;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wallet_search, menu);
        final MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        EditText searchEditText = searchView.findViewById(R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(android.R.color.black));
        searchEditText.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
        ImageView closeButtonImage = searchView.findViewById(R.id.search_close_btn);
        closeButtonImage.setImageResource(R.drawable.ic_baseline_close_24);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            SettingsLauncher settingsLauncher = new SettingsLauncherImpl();
            settingsLauncher.launchSettingsActivity(this, BraveWalletPreferences.class);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void triggerLayoutInflation() {
        setContentView(R.layout.activity_brave_wallet);
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        mToolbar.setTitle("");
        mToolbar.setOverflowIcon(
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_more_vert_24));
        setSupportActionBar(mToolbar);

        swapButton = findViewById(R.id.swap_button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // For Android 7 and above use vector images for send/swap button.
            // For Android 5 and 6 it is a bitmap specified in activity_brave_wallet.xml.
            swapButton.setImageResource(R.drawable.ic_swap_icon);
            swapButton.setBackgroundResource(R.drawable.ic_swap_bg);
        }

        swapButton.setOnClickListener(v -> {
            SwapBottomSheetDialogFragment swapBottomSheetDialogFragment =
                    SwapBottomSheetDialogFragment.newInstance();
            swapBottomSheetDialogFragment.show(
                    getSupportFragmentManager(), SwapBottomSheetDialogFragment.TAG_FRAGMENT);
        });

        cryptoLayout = findViewById(R.id.crypto_layout);
        cryptoWalletOnboardingViewPager = findViewById(R.id.crypto_wallet_onboarding_viewpager);
        cryptoWalletOnboardingPagerAdapter =
                new CryptoWalletOnboardingPagerAdapter(getSupportFragmentManager());
        cryptoWalletOnboardingViewPager.setAdapter(cryptoWalletOnboardingPagerAdapter);
        cryptoWalletOnboardingViewPager.setOffscreenPageLimit(
                cryptoWalletOnboardingPagerAdapter.getCount() - 1);

        ImageView onboardingBackButton = findViewById(R.id.onboarding_back_button);
        onboardingBackButton.setVisibility(View.GONE);
        onboardingBackButton.setOnClickListener(v -> {
            if (cryptoWalletOnboardingViewPager != null) {
                cryptoWalletOnboardingViewPager.setCurrentItem(
                        cryptoWalletOnboardingViewPager.getCurrentItem() - 1);
            }
        });

        cryptoWalletOnboardingViewPager.addOnPageChangeListener(
                new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(
                            int position, float positionOffset, int positionOffsetPixels) {}

                    @Override
                    public void onPageSelected(int position) {
                        if (position == 0 || position == 2) {
                            onboardingBackButton.setVisibility(View.GONE);
                        } else {
                            onboardingBackButton.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {}
                });

        mModalDialogManager = new ModalDialogManager(
                new AppModalPresenter(this), ModalDialogManager.ModalDialogType.APP);

        onInitialLayoutInflationComplete();
    }

    @Override
    public void onUserInteraction() {
        if (mKeyringController == null) {
            return;
        }
        mKeyringController.notifyUserInteraction();
    }

    @Override
    public void onConnectionError(MojoException e) {
        mKeyringController.close();
        mAssetRatioController.close();
        mErcTokenRegistry.close();
        mEthJsonRpcController.close();
        mEthTxController.close();
        mBraveWalletService.close();

        mKeyringController = null;
        mErcTokenRegistry = null;
        mEthJsonRpcController = null;
        mEthTxController = null;
        mAssetRatioController = null;
        mBraveWalletService = null;
        InitKeyringController();
        InitErcTokenRegistry();
        InitEthJsonRpcController();
        InitEthTxController();
        InitAssetRatioController();
        InitBraveWalletService();
    }

    private void InitEthTxController() {
        if (mEthTxController != null) {
            return;
        }

        mEthTxController = EthTxControllerFactory.getInstance().getEthTxController(this);
    }

    private void InitKeyringController() {
        if (mKeyringController != null) {
            return;
        }

        mKeyringController = KeyringControllerFactory.getInstance().getKeyringController(this);
    }

    private void InitErcTokenRegistry() {
        if (mErcTokenRegistry != null) {
            return;
        }

        mErcTokenRegistry = ERCTokenRegistryFactory.getInstance().getERCTokenRegistry(this);
    }

    private void InitEthJsonRpcController() {
        if (mEthJsonRpcController != null) {
            return;
        }

        mEthJsonRpcController =
                EthJsonRpcControllerFactory.getInstance().getEthJsonRpcController(this);
    }

    private void InitAssetRatioController() {
        if (mAssetRatioController != null) {
            return;
        }

        mAssetRatioController =
                AssetRatioControllerFactory.getInstance().getAssetRatioController(this);
    }

    private void InitBraveWalletService() {
        if (mBraveWalletService != null) {
            return;
        }

        mBraveWalletService = BraveWalletServiceFactory.getInstance().getBraveWalletService(this);
    }

    public KeyringController getKeyringController() {
        return mKeyringController;
    }

    public ErcTokenRegistry getErcTokenRegistry() {
        return mErcTokenRegistry;
    }

    public EthJsonRpcController getEthJsonRpcController() {
        return mEthJsonRpcController;
    }

    public EthTxController getEthTxController() {
        return mEthTxController;
    }

    public AssetRatioController getAssetRatioController() {
        return mAssetRatioController;
    }

    public BraveWalletService getBraveWalletService() {
        return mBraveWalletService;
    }

    @Override
    public void finishNativeInitialization() {
        super.finishNativeInitialization();
        InitKeyringController();
        InitErcTokenRegistry();
        InitEthJsonRpcController();
        InitEthTxController();
        InitAssetRatioController();
        InitBraveWalletService();
        if (Utils.shouldShowCryptoOnboarding()) {
            setNavigationFragments(ONBOARDING_FIRST_PAGE_ACTION);
        } else if (mKeyringController != null) {
            mKeyringController.isLocked(isLocked -> {
                if (isLocked) {
                    setNavigationFragments(UNLOCK_WALLET_ACTION);
                } else {
                    setCryptoLayout();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        mKeyringController.close();
        mAssetRatioController.close();
        mErcTokenRegistry.close();
        mEthJsonRpcController.close();
        mEthTxController.close();
        mBraveWalletService.close();
        mModalDialogManager.destroy();
        super.onDestroy();
    }

    @Override
    public boolean shouldStartGpuProcess() {
        return true;
    }

    @Override
    protected ActivityWindowAndroid createWindowAndroid() {
        return new ActivityWindowAndroid(this, true, getIntentRequestTracker()) {
            @Override
            public ModalDialogManager getModalDialogManager() {
                return mModalDialogManager;
            }
        };
    }

    private void setNavigationFragments(int type) {
        List<NavigationItem> navigationItems = new ArrayList<>();
        cryptoLayout.setVisibility(View.GONE);
        if (type == ONBOARDING_FIRST_PAGE_ACTION) {
            SetupWalletFragment setupWalletFragment = new SetupWalletFragment();
            setupWalletFragment.setOnNextPageListener(this);
            navigationItems.add(new NavigationItem(
                    getResources().getString(R.string.setup_crypto), setupWalletFragment));
        } else if (type == UNLOCK_WALLET_ACTION) {
            UnlockWalletFragment unlockWalletFragment = new UnlockWalletFragment();
            unlockWalletFragment.setOnNextPageListener(this);
            navigationItems.add(new NavigationItem(
                    getResources().getString(R.string.unlock_wallet_title), unlockWalletFragment));
        } else if (type == RESTORE_WALLET_ACTION) {
            RestoreWalletFragment restoreWalletFragment = new RestoreWalletFragment();
            restoreWalletFragment.setOnNextPageListener(this);
            navigationItems.add(
                    new NavigationItem(getResources().getString(R.string.restore_crypto_account),
                            restoreWalletFragment));
        }

        if (cryptoWalletOnboardingPagerAdapter != null) {
            cryptoWalletOnboardingPagerAdapter.setNavigationItems(navigationItems);
            cryptoWalletOnboardingPagerAdapter.notifyDataSetChanged();
        }
    }

    private void replaceNavigationFragments(int type, boolean doNavigate) {
        if (cryptoWalletOnboardingViewPager != null && cryptoWalletOnboardingPagerAdapter != null) {
            if (type == RESTORE_WALLET_ACTION) {
                RestoreWalletFragment restoreWalletFragment = new RestoreWalletFragment();
                restoreWalletFragment.setOnNextPageListener(this);
                cryptoWalletOnboardingPagerAdapter.replaceWithNavigationItem(
                        new NavigationItem(
                                getResources().getString(R.string.restore_crypto_account),
                                restoreWalletFragment),
                        cryptoWalletOnboardingViewPager.getCurrentItem() + 1);
            } else if (type == ONBOARDING_ACTION) {
                List<NavigationItem> navigationItems = new ArrayList<>();
                SecurePasswordFragment securePasswordFragment = new SecurePasswordFragment();
                securePasswordFragment.setOnNextPageListener(this);
                navigationItems.add(
                        new NavigationItem(getResources().getString(R.string.secure_your_crypto),
                                securePasswordFragment));
                addBackupWalletSequence(navigationItems);
                cryptoWalletOnboardingPagerAdapter.replaceWithNavigationItems(
                        navigationItems, cryptoWalletOnboardingViewPager.getCurrentItem() + 1);
            }

            cryptoWalletOnboardingPagerAdapter.notifyDataSetChanged();

            if (doNavigate) {
                cryptoWalletOnboardingViewPager.setCurrentItem(
                        cryptoWalletOnboardingViewPager.getCurrentItem() + 1);
            }
        }
    }

    private void setCryptoLayout() {
        cryptoOnboardingLayout = findViewById(R.id.crypto_onboarding_layout);
        cryptoOnboardingLayout.setVisibility(View.GONE);
        cryptoLayout.setVisibility(View.VISIBLE);

        ViewPager viewPager = findViewById(R.id.navigation_view_pager);
        CryptoFragmentPageAdapter adapter =
                new CryptoFragmentPageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(adapter.getCount() - 1);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        if (swapButton != null) swapButton.setVisibility(View.VISIBLE);

        if (mKeyringController != null)
            mKeyringController.isWalletBackedUp(backed_up -> {
                if (!backed_up) {
                    showWalletBackupBanner();
                } else {
                    findViewById(R.id.wallet_backup_banner).setVisibility(View.GONE);
                }
            });
    }

    private void addBackupWalletSequence(List<NavigationItem> navigationItems) {
        BackupWalletFragment backupWalletFragment = new BackupWalletFragment();
        backupWalletFragment.setOnNextPageListener(this);
        navigationItems.add(new NavigationItem(
                getResources().getString(R.string.backup_your_wallet), backupWalletFragment));
        RecoveryPhraseFragment recoveryPhraseFragment = new RecoveryPhraseFragment();
        recoveryPhraseFragment.setOnNextPageListener(this);
        navigationItems.add(new NavigationItem(
                getResources().getString(R.string.your_recovery_phrase), recoveryPhraseFragment));
        VerifyRecoveryPhraseFragment verifyRecoveryPhraseFragment =
                new VerifyRecoveryPhraseFragment();
        verifyRecoveryPhraseFragment.setOnNextPageListener(this);
        navigationItems.add(
                new NavigationItem(getResources().getString(R.string.verify_recovery_phrase),
                        verifyRecoveryPhraseFragment));
    }

    public void backupBannerOnClick() {
        cryptoOnboardingLayout.setVisibility(View.VISIBLE);
        cryptoLayout.setVisibility(View.GONE);

        List<NavigationItem> navigationItems = new ArrayList<>();
        addBackupWalletSequence(navigationItems);

        if (cryptoWalletOnboardingPagerAdapter != null) {
            cryptoWalletOnboardingPagerAdapter.setNavigationItems(navigationItems);
            cryptoWalletOnboardingPagerAdapter.notifyDataSetChanged();
        }
    }

    private void showWalletBackupBanner() {
        final ViewGroup backupTopBannerLayout = (ViewGroup) findViewById(R.id.wallet_backup_banner);
        backupTopBannerLayout.setVisibility(View.VISIBLE);
        backupTopBannerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backupBannerOnClick();
            }
        });
        ImageView bannerClose = backupTopBannerLayout.findViewById(R.id.backup_banner_close);
        bannerClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backupTopBannerLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void gotoNextPage(boolean finishOnboarding) {
        if (finishOnboarding) {
            setCryptoLayout();
        } else {
            if (cryptoWalletOnboardingViewPager != null) {
                cryptoWalletOnboardingViewPager.setCurrentItem(
                        cryptoWalletOnboardingViewPager.getCurrentItem() + 1);
            }
        }
    }

    @Override
    public void gotoOnboardingPage() {
        replaceNavigationFragments(ONBOARDING_ACTION, true);
    }

    @Override
    public void gotoRestorePage() {
        replaceNavigationFragments(RESTORE_WALLET_ACTION, true);
    }
}
